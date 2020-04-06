
#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "config-srcpd.h"
#include "srcp-gl.h"
#include "srcp-error.h"
#include "srcp-info.h"
#include "syslogmessage.h"

#define QUEUELEN 200 //SIG 29.10.16, 50 sind zuwenig!

/* current state */
static struct _GL gl[MAX_BUSES];

/* command queues for each bus */
typedef struct _tQueueGL {
    int out, in;
    gl_state_t queue[QUEUELEN];
} tQueueGL;
typedef struct _tQueue2GL {
    tQueueGL queue[2];  //Extra Queue für Lok Halt Kommandos, diese haben Priorität und können andere überholen. 0=normal, 1=priorität
} tQueue2GL;
static tQueue2GL queue[MAX_BUSES];
#define GET_QUEUE_PRIO(prio) ((prio) ? 1 : 0)

static pthread_mutex_t queue_mutex[MAX_BUSES];

/* write position for queue writers */
static int out[MAX_BUSES], in[MAX_BUSES];

/* forward declaration of internal functions */
static int queue_len(bus_t busnumber);
static int queue_isfull(tQueueGL *q);
static int queue_len_one(tQueueGL *q);

/**
 * isValidGL: checks if a given address could be a valid GL.
 * returns true or false. false, if not all requirements are met.
 */
int isValidGL(bus_t busnumber, int addr)
{
    /* in bus 0 GL are not allowed */
    /* only num_buses are configured */
    /* number of GL is set */
    /* address must be greater 0 */
    /* but not more than the maximum address on that bus */
    if (busnumber <= num_buses &&
        gl[busnumber].numberOfGl > 0 && addr > 0 &&
        addr <= gl[busnumber].numberOfGl) {
        return 1 == 1;
    }
    else {
        return 1 == 0;
    }
}

/**
 * getMaxAddrGL: returns the maximum Address for GL on the given bus
 * returns: <0: invalid bus number
            =0: no GL on that bus
      >0: maximum address
 */
int getMaxAddrGL(bus_t busnumber)
{
    if (busnumber <= num_buses) {
        return gl[busnumber].numberOfGl;
    }
    else {
        return -1;
    }
}

/* es gibt Decoder fr 14, 27, 28 und 128 FS */
static int calcspeed(int vs, int vmax, int n_fs)
{
    int rs;

    if (vmax == 0)
        return vs;
    if (vs < 0)
        vs = 0;
    if (vs > vmax)
        vs = vmax;
    /* rs = (vs * n_fs) / vmax; */
    /* for test: rs = ((vs * n_fs) / v_max) + 0.5 */
    /* ==> rs = ((2 * vs * n_fs) + v_max) / (2 * v_max) */
    rs = vs * n_fs;             /* vs * n_fs */
    rs <<= 1;                   /* 2 * vs * n_fs */
    rs += vmax;                 /* (2 * vs * n_fs) + v_max */
    rs /= vmax;                 /* ((2 * vs * n_fs) + v_max) / v_max */
    rs >>= 1;                   /* ((2 * vs * n_fs) + v_max) / (2 * v_max) */
    if ((rs == 0) && (vs != 0))
        rs = 1;

    return rs;
}

/* checks whether a GL is already initialized or not
 * returns false even, if it is an invalid address!
 */
bool isInitializedGL(bus_t busnumber, int addr)
{
    if (isValidGL(busnumber, addr)) {
        return (gl[busnumber].glstate[addr].state == 1);
    }
    else {
        return false;
    }
}

static void mutex_unlock(bus_t busnumber, pthread_mutex_t *mutex) {
    int result;
    result = pthread_mutex_unlock(mutex);
    if (result != 0) {
      syslog_bus(busnumber, DBG_ERROR,
                 "pthread_mutex_unlock() failed: %s (errno = %d).",
                 strerror(result), result);
    }
}

/* Uebernehme die neuen Angaben fr die Lok, einige wenige Prüfungen.
   Lock wird ignoriert! Lock wird in den SRCP Routinen beachtet, hier
   ist das nicht angebracht (Notstop)
*/

int enqueueGL(bus_t busnumber, int addr, int dir, int speed, int maxspeed,
              const int f)
{
    int result;
    struct timeval akt_time;

    if (isValidGL(busnumber, addr)) {
        if (!isInitializedGL(busnumber, addr)) {
            cacheInitGL(busnumber, addr, 'P', 1, 14, 1, NULL);
            syslog_bus(busnumber, DBG_WARN, "GL default init for %d-%d",
                       busnumber, addr);
        }

        result = pthread_mutex_lock(&queue_mutex[busnumber]);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "pthread_mutex_lock() failed: %s (errno = %d).",
                       strerror(result), result);
        }

        //Priorität ist dann vorhanden, wenn die Lok noch fährt und nun angehalten werden soll.
        bool prio = (gl[busnumber].glstate[addr].speed > 0) && (speed == 0);
        tQueueGL *q = &(queue[busnumber].queue[GET_QUEUE_PRIO(prio)]);
        if (queue_isfull(q)) {
            //Wenn es die Prio Queue war können wir es noch mit der normalem probieren, besser als Kommando ganz verwerfen.
            if (prio) {
              q = &(queue[busnumber].queue[GET_QUEUE_PRIO(false)]);
              if (queue_isfull(q)) {
                syslog_bus(busnumber, DBG_WARN, "GL Command Queue double full");
                mutex_unlock(busnumber, &queue_mutex[busnumber]);
                return SRCP_TEMPORARILYPROHIBITED;
              }
            }
            else {
              syslog_bus(busnumber, DBG_WARN, "GL Command Queue full");
              mutex_unlock(busnumber, &queue_mutex[busnumber]);
              return SRCP_TEMPORARILYPROHIBITED;
            }
        }
        else if (prio) {
          //Kommando wird der Prio Queue hinzugefügt
          //Wenn in der normalen Queue nun ein Kommandof für die selbe Lok ist wurde dieses überholt und ist nicht mehr gültig
          tQueueGL *qNormal = &(queue[busnumber].queue[GET_QUEUE_PRIO(false)]);
          int i = qNormal -> out;
          while (i != qNormal -> in) {
            if (qNormal -> queue[i].id == addr) {
              //Nun nicht mehr gültiger, überholter, Eintrag gefunden
              qNormal -> queue[i].id = -1;
            }
            i++;
            if (i == QUEUELEN) {
              i = 0;
            }
          }
          //Wenn die Queue zu 75% voll ist und es schon ein Prio Halt Kommando für die selbe Lok hat muss dieses hier nicht auch noch dazu -> verwerfen
          //75% Regel: wenn keine Überlast dürfen mehrere Halt Komnados da sein (z.B. mit Schaltung verschiedener Funktionen).
          if (queue_len_one(q) > (QUEUELEN * 3 / 4)) {
            i = q -> out;
            while (i != q -> in) {
	      if (q -> queue[i].id == addr) {
	        //Schon ein Prio Stop Kommando vorhanden -> neues verwerfen
                mutex_unlock(busnumber, &queue_mutex[busnumber]);
                return SRCP_OK;
	      }
	      i++;
              if (i == QUEUELEN) {
                i = 0;
              }
	    }
	  }
        }
        
        /* Protokollbezeichner und sonstige INIT Werte in die Queue kopieren! */
        (q -> queue[q -> in]).prio = prio;
        (q -> queue[q -> in]).protocol =
            gl[busnumber].glstate[addr].protocol;
        (q -> queue[q -> in]).protocolversion =
            gl[busnumber].glstate[addr].protocolversion;

        (q -> queue[q -> in]).speed =
            calcspeed(speed, maxspeed, gl[busnumber].glstate[addr].n_fs);

        (q -> queue[q -> in]).n_fs =
            gl[busnumber].glstate[addr].n_fs;

        (q -> queue[q -> in]).n_func =
            gl[busnumber].glstate[addr].n_func;

        (q -> queue[q -> in]).direction = dir;
        (q -> queue[q -> in]).funcs = f;
        gettimeofday(&akt_time, NULL);
        (q -> queue[q -> in]).tv = akt_time;
        (q -> queue[q -> in]).id = addr;
        q -> in++;
        if (q -> in == QUEUELEN) {
            q -> in = 0;
        }

        mutex_unlock(busnumber, &queue_mutex[busnumber]);

        /* Restart thread to send GL command */
        resume_bus_thread(busnumber);
        return SRCP_OK;
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

int queue_GL_isempty(bus_t busnumber)
{
    return (queue[busnumber].queue[0].in == queue[busnumber].queue[0].out) &&
           (queue[busnumber].queue[1].in == queue[busnumber].queue[1].out);
}

static int queue_len_one(tQueueGL *q)
{
    if (q ->in >= q->out)
        return q->in - q->out;
    else
        return QUEUELEN + q->in - q->out;
}

static int queue_len(bus_t busnumber)
{
    return queue_len_one(&queue[busnumber].queue[0]) + queue_len_one(&queue[busnumber].queue[1]);
}

/* maybe, 1 element in the queue cannot be used.. */
static int queue_isfull(tQueueGL *q)
{
    return queue_len_one(q) >= QUEUELEN - 1;
}

/** liefert nächsten Eintrag oder -1, setzt fifo pointer neu! */
int dequeueNextGL(bus_t busnumber, gl_state_t * l)
{
    if (queue_GL_isempty(busnumber)) {
        return -1;
    }

    //Zuerst Prio Queue
    tQueueGL *q = &queue[busnumber].queue[GET_QUEUE_PRIO(true)];
    if (queue_len_one(q) == 0) {
      //Keine Prio Kommandos -> normale Queue kommt drann
      q = &queue[busnumber].queue[GET_QUEUE_PRIO(false)];
    }
    *l = q -> queue[q -> out];
    q -> out++;
    if (q -> out == QUEUELEN) {
        q -> out = 0;
    }
    return q -> out;
}

int cacheGetGL(bus_t busnumber, int addr, gl_state_t * l)
{
    if (isInitializedGL(busnumber, addr)) {
        *l = gl[busnumber].glstate[addr];
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }
}

/**
 * cacheSetGL is called from the hardware drivers to keep the
 * the data and the info mode informed. It is called from
 * within the SRCP SET Command code.
 * It respects the TERM function.
*/
int cacheSetGL(bus_t busnumber, int addr, gl_state_t l)
{
    if (isValidGL(busnumber, addr)) {
        char msg[1000];
        gl[busnumber].glstate[addr].direction = l.direction;
        gl[busnumber].glstate[addr].speed = l.speed;
        gl[busnumber].glstate[addr].funcs = l.funcs;
        gl[busnumber].glstate[addr].n_fs = l.n_fs;
        gl[busnumber].glstate[addr].n_func = l.n_func;
        gettimeofday(&gl[busnumber].glstate[addr].tv, NULL);
        if (gl[busnumber].glstate[addr].state == 2) {
            snprintf(msg, sizeof(msg), "%lu.%.3lu 102 INFO %ld GL %d\n",
                     gl[busnumber].glstate[addr].tv.tv_sec,
                     gl[busnumber].glstate[addr].tv.tv_usec / 1000,
                     busnumber, addr);
            bzero(&gl[busnumber].glstate[addr], sizeof(gl_state_t));
        }
        else {
            cacheInfoGL(busnumber, addr, msg);
        }
        enqueueInfoMessage(msg);
        return SRCP_OK;
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

int cacheInitGL(bus_t busnumber, int addr, const char protocol,
                int protoversion, int n_fs, int n_func, char *optData)
{
    int rc = SRCP_WRONGVALUE;
    if (isValidGL(busnumber, addr)) {
        char msg[1000];
        gl_state_t tgl;
        memset(&tgl, 0, sizeof(tgl));
        rc = bus_supports_protocol(busnumber, protocol);
        if (rc != SRCP_OK) {
            return rc;
        }
        gettimeofday(&tgl.inittime, NULL);
        tgl.tv = tgl.inittime;
        tgl.n_fs = n_fs;
        tgl.n_func = n_func;
        tgl.protocolversion = protoversion;
        tgl.protocol = protocol;
        tgl.id = addr;
        if (buses[busnumber].init_gl_func) {
            rc = (*buses[busnumber].init_gl_func) (busnumber, &tgl, optData);
        }
        if (rc == SRCP_OK) {
            gl[busnumber].glstate[addr] = tgl;
            gl[busnumber].glstate[addr].state = 1;
            cacheDescribeGL(busnumber, addr, msg);
            enqueueInfoMessage(msg);
            enqueueGL(busnumber, addr, 0, 0, 1, 0);
        }
    }
    else {
    printf("7\n");
        rc = SRCP_WRONGVALUE;
    }
    return rc;
}

//SID, 15.9.08
//Alle GL sate aller Buse werden auf 0 gesetzt -> Lok nicht mehr vorhanden
int resetAllGL(bus_t bus)
{
  int glAddr;
  if (bus > 0 && bus <= num_buses) {
    for (glAddr=1; glAddr<=gl[bus].numberOfGl; glAddr++) {
      gl[bus].glstate[glAddr].state = 0;
    }
    int rc = SRCP_OK;
    if (buses[bus].init_func) {
      rc = (*buses[bus].init_func) (bus);
    }
    return rc;
  }
  return SRCP_WRONGVALUE;
}

int cacheTermGL(bus_t busnumber, int addr)
{
    if (isInitializedGL(busnumber, addr)) {
        gl[busnumber].glstate[addr].state = 2;
        //Bei MFX muss eine Abmeldung bis zum Lokdekoder
        if (gl[busnumber].glstate[addr].protocol == 'X') {
          enqueueGL(busnumber, addr, -1, 0, 1, 0); //Direction -1 -> Info Term
        }
        else {
          enqueueGL(busnumber, addr, 0, 0, 1, 0); //Direction -1 -> Info Term
        }
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }

}

/*
 * RESET a GL to its defaults
 */
int resetGL(bus_t busnumber, int addr)
{
    if (isInitializedGL(busnumber, addr)) {
        enqueueGL(busnumber, addr, 0, 0, 1, 0);
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }
}

int cacheDescribeGL(bus_t busnumber, int addr, char *msg)
{
    if (isInitializedGL(busnumber, addr)) {
        sprintf(msg, "%lu.%.3lu 101 INFO %ld GL %d %c %d %d %d",
                gl[busnumber].glstate[addr].inittime.tv_sec,
                gl[busnumber].glstate[addr].inittime.tv_usec / 1000,
                busnumber, addr, gl[busnumber].glstate[addr].protocol,
                gl[busnumber].glstate[addr].protocolversion,
                gl[busnumber].glstate[addr].n_fs,
                gl[busnumber].glstate[addr].n_func);
        //Protokoll / Busspezifische Daten ergänzen
        if (buses[busnumber].describe_gl_func) {
          (*buses[busnumber].describe_gl_func) (&(gl[busnumber].glstate[addr]), msg);
        }
        //Noch Newline anhängen
        sprintf(msg + strlen(msg), "\n");
     }
    else {
        strcpy(msg, "");
        return SRCP_NODATA;
    }
    return SRCP_INFO;
}

int cacheInfoGL(bus_t busnumber, int addr, char *msg)
{
    int i;
    char *tmp;

    if (isInitializedGL(busnumber, addr)) {
        sprintf(msg, "%lu.%.3lu 100 INFO %ld GL %d %d %d %d %d",
                gl[busnumber].glstate[addr].tv.tv_sec,
                gl[busnumber].glstate[addr].tv.tv_usec / 1000,
                busnumber, addr, gl[busnumber].glstate[addr].direction,
                gl[busnumber].glstate[addr].speed,
                gl[busnumber].glstate[addr].n_fs,
                (gl[busnumber].glstate[addr].funcs & 0x01) ? 1 : 0);

        for (i = 1; i < gl[busnumber].glstate[addr].n_func; i++) {
            tmp = malloc(strlen(msg) + 100);
            sprintf(tmp, "%s %d", msg,
                    ((gl[busnumber].glstate[addr].
                      funcs >> i) & 0x01) ? 1 : 0);
            strcpy(msg, tmp);
            free(tmp);
        }
        tmp = malloc(strlen(msg) + 2);
        sprintf(tmp, "%s\n", msg);
        strcpy(msg, tmp);
        free(tmp);

    }
    else {
        return SRCP_NODATA;
    }
    return SRCP_INFO;
}

/* has to use a semaphore, must be atomized! */
int cacheLockGL(bus_t busnumber, int addr, long int duration,
                sessionid_t sessionid)
{
    char msg[256];

    if (isInitializedGL(busnumber, addr)) {
        if (gl[busnumber].glstate[addr].locked_by == sessionid
            || gl[busnumber].glstate[addr].locked_by == 0) {
            gl[busnumber].glstate[addr].locked_by = sessionid;
            gl[busnumber].glstate[addr].lockduration = duration;
            gettimeofday(&gl[busnumber].glstate[addr].locktime, NULL);
            describeLOCKGL(busnumber, addr, msg);
            enqueueInfoMessage(msg);
            return SRCP_OK;
        }
        else {
            return SRCP_DEVICELOCKED;
        }
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

int cacheGetLockGL(bus_t busnumber, int addr, sessionid_t * session_id)
{
    if (isInitializedGL(busnumber, addr)) {

        *session_id = gl[busnumber].glstate[addr].locked_by;
        return SRCP_OK;
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

int describeLOCKGL(bus_t bus, int addr, char *reply)
{
    if (isInitializedGL(bus, addr)) {

        sprintf(reply, "%lu.%.3lu 100 INFO %ld LOCK GL %d %ld %ld\n",
                gl[bus].glstate[addr].locktime.tv_sec,
                gl[bus].glstate[addr].locktime.tv_usec / 1000,
                bus, addr, gl[bus].glstate[addr].lockduration,
                gl[bus].glstate[addr].locked_by);
        return SRCP_OK;
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

int cacheUnlockGL(bus_t busnumber, int addr, sessionid_t sessionid)
{
    if (isInitializedGL(busnumber, addr)) {

        if (gl[busnumber].glstate[addr].locked_by == sessionid
            || gl[busnumber].glstate[addr].locked_by == 0) {
            char msg[256];
            gl[busnumber].glstate[addr].locked_by = 0;
            gettimeofday(&gl[busnumber].glstate[addr].locktime, NULL);
            sprintf(msg, "%lu.%.3lu 102 INFO %ld LOCK GL %d %ld\n",
                    gl[busnumber].glstate[addr].locktime.tv_sec,
                    gl[busnumber].glstate[addr].locktime.tv_usec / 1000,
                    busnumber, addr, sessionid);
            enqueueInfoMessage(msg);
            return SRCP_OK;
        }
        else {
            return SRCP_DEVICELOCKED;
        }
    }
    else {
        return SRCP_WRONGVALUE;
    }
}

/**
 * called when a session is terminating
 */
void unlock_gl_bysessionid(sessionid_t sessionid)
{
    bus_t i;
    int j;
    int number;

    syslog_session(sessionid, DBG_DEBUG, "Unlocking GLs by session-id");
    for (i = 0; i <= num_buses; i++) {
        number = getMaxAddrGL(i);
        for (j = 1; j <= number; j++) {
            if (gl[i].glstate[j].locked_by == sessionid) {
                cacheUnlockGL(i, j, sessionid);
            }
        }
    }
}

/**
 * called once per second to unlock
 */
void unlock_gl_bytime(void)
{
    bus_t i;
    int j;
    int number;

    for (i = 0; i <= num_buses; i++) {
        number = getMaxAddrGL(i);
        for (j = 1; j <= number; j++) {
            if (gl[i].glstate[j].lockduration > 0
                && gl[i].glstate[j].lockduration-- == 1) {
                cacheUnlockGL(i, j, gl[i].glstate[j].locked_by);
            }
        }
    }
}

/**
 * First initialisation after program start up
 */
int startup_GL(void)
{
    int result;
    bus_t i;

    for (i = 0; i < MAX_BUSES; i++) {
        in[i] = 0;
        out[i] = 0;
        gl[i].numberOfGl = 0;
        gl[i].glstate = NULL;

        result = pthread_mutex_init(&queue_mutex[i], NULL);
        if (result != 0) {
            syslog_bus(0, DBG_ERROR,
                       "pthread_mutex_init() failed: %s (errno = %d).",
                       strerror(result), result);
        }

    }
    return 0;
}

/**
 * allocates memory to hold all the data
 * called from the configuration routines
 */
int init_GL(bus_t busnumber, int number)
{
    int i;
    syslog_bus(busnumber, DBG_INFO, "init GL: %d", number);
    if (busnumber >= MAX_BUSES)
        return 1;

    if (number > 0) {
        gl[busnumber].glstate = malloc((number + 1) * sizeof(gl_state_t));
        if (gl[busnumber].glstate == NULL)
            return 1;
        gl[busnumber].numberOfGl = number;
        for (i = 0; i <= number; i++) {
            bzero(&gl[busnumber].glstate[i], sizeof(gl_state_t));
        }
    }
    return 0;
}

void debugGL(bus_t busnumber, int start, int end)
{
    gl_state_t *gls;
    int i;

    syslog_bus(busnumber, DBG_WARN, "debug GLSTATE from %d to %d", start,
               end);
    for (i = start; i <= end; i++) {
        gls = &gl[busnumber].glstate[i];
        syslog_bus(busnumber, DBG_WARN, "GLSTATE for %d/%d", busnumber, i);
        syslog_bus(busnumber, DBG_WARN, "state %d", gls->state);
        syslog_bus(busnumber, DBG_WARN, "protocol %c", gls->protocol);
        syslog_bus(busnumber, DBG_WARN, "protocolversion %d",
                   gls->protocolversion);
        syslog_bus(busnumber, DBG_WARN, "n_func %d", gls->n_func);
        syslog_bus(busnumber, DBG_WARN, "n_fs %d", gls->n_fs);
        syslog_bus(busnumber, DBG_WARN, "id %d", gls->id);
        syslog_bus(busnumber, DBG_WARN, "speed %d", gls->speed);
        syslog_bus(busnumber, DBG_WARN, "direction %d", gls->direction);
        syslog_bus(busnumber, DBG_WARN, "funcs %d", gls->funcs);
        syslog_bus(busnumber, DBG_WARN, "lockduration %ld",
                   gls->lockduration);
        syslog_bus(busnumber, DBG_WARN, "locked_by %ld", gls->locked_by);
        /*  struct timeval tv;
           struct timeval inittime;
           struct timeval locktime; */
    }
}
