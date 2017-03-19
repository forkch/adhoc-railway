/*
 * This software is published under the restrictions of the
 * GNU License Version2
 *
 */

#include <stdlib.h>
#include <string.h>
#include <stdio.h>

#include "config-srcpd.h"
#include "srcp-session.h"
#include "srcp-ga.h"
#include "srcp-error.h"
#include "srcp-info.h"
#include "syslogmessage.h"

#define QUEUELEN 50

/* current state state */
static volatile struct _GA ga[MAX_BUSES];

/* command queues for each bus */
static ga_state_t queue[MAX_BUSES][QUEUELEN];
static pthread_mutex_t queue_mutex[MAX_BUSES];
static int out[MAX_BUSES], in[MAX_BUSES];

/* forward declaration of internal functions */
static int queue_len(bus_t busnumber);
static int queue_isfull(bus_t busnumber);


int get_number_ga(bus_t busnumber)
{
    return ga[busnumber].numberOfGa;
}

/* Uebernehme die neuen Angaben fuer die Weiche, einige wenige Pruefungen */
int enqueueGA(bus_t busnumber, int addr, int port, int action,
              long int activetime)
{
    int result;
    struct timeval akt_time;
    int number_ga = get_number_ga(busnumber);

    if ((addr > 0) && (addr <= number_ga)) {
        if (queue_isfull(busnumber)) {
            syslog_bus(busnumber, DBG_WARN, "GA Command Queue full");
            return SRCP_TEMPORARILYPROHIBITED;
        }

        result = pthread_mutex_lock(&queue_mutex[busnumber]);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "pthread_mutex_lock() failed: %s (errno = %d).",
                       strerror(result), result);
        }

        queue[busnumber][in[busnumber]].protocol =
            ga[busnumber].gastate[addr].protocol;
        queue[busnumber][in[busnumber]].action = action;
        queue[busnumber][in[busnumber]].port = port;
        queue[busnumber][in[busnumber]].activetime = activetime;
        gettimeofday(&akt_time, NULL);
        queue[busnumber][in[busnumber]].tv[port] = akt_time;
        queue[busnumber][in[busnumber]].id = addr;
        in[busnumber]++;
        if (in[busnumber] == QUEUELEN)
            in[busnumber] = 0;

        result = pthread_mutex_unlock(&queue_mutex[busnumber]);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "pthread_mutex_unlock() failed: %s (errno = %d).",
                       strerror(result), result);
        }
        /* Restart thread to send GL command */
        resume_bus_thread(busnumber);
    }
    else {
        return SRCP_WRONGVALUE;
    }
    return SRCP_OK;
}

int queue_GA_isempty(bus_t busnumber)
{
    return (in[busnumber] == out[busnumber]);
}

static int queue_len(bus_t busnumber)
{
    if (in[busnumber] >= out[busnumber])
        return in[busnumber] - out[busnumber];
    else
        return QUEUELEN + in[busnumber] - out[busnumber];
}

/* maybe, 1 element in the queue cannot be used.. */
static int queue_isfull(bus_t busnumber)
{
    return queue_len(busnumber) >= QUEUELEN - 1;
}

/** liefert naechsten Eintrag oder -1, setzt fifo pointer neu! */
int dequeueNextGA(bus_t busnumber, ga_state_t * a)
{
    if (in[busnumber] == out[busnumber])
        return -1;

    *a = queue[busnumber][out[busnumber]];
    out[busnumber]++;
    if (out[busnumber] == QUEUELEN)
        out[busnumber] = 0;
    return out[busnumber];
}

int getGA(bus_t busnumber, int addr, ga_state_t * a)
{
    int number_ga = get_number_ga(busnumber);

    if ((addr > 0) && (addr <= number_ga)) {
        *a = ga[busnumber].gastate[addr];
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }
}

bool isInitializedGA(bus_t busnumber, int addr)
{
    return ga[busnumber].gastate[addr].protocol != 0x00;
}

/* ********************
 *   SRCP commands
 */
int setGA(bus_t busnumber, int addr, ga_state_t a)
{
    int number_ga = get_number_ga(busnumber);

    if ((addr > 0) && (addr <= number_ga)) {
        char msg[1000];
        if (!isInitializedGA(busnumber, addr))
            initGA(busnumber, addr, 'P');
        ga[busnumber].gastate[addr].id = a.id;
        ga[busnumber].gastate[addr].action = a.action;
        ga[busnumber].gastate[addr].port = a.port;
        gettimeofday(&ga[busnumber].gastate[addr].
                     tv[ga[busnumber].gastate[addr].port], NULL);

        infoGA(busnumber, addr, a.port, msg);
        enqueueInfoMessage(msg);
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }
}

int termGA(bus_t busnumber, int addr)
{
    if (isInitializedGA(busnumber, addr)) {
        ga[busnumber].gastate[addr].state = 2;
        enqueueGA(busnumber, addr, 0, 0, 0);
        return SRCP_OK;
    }
    else {
        return SRCP_NODATA;
    }
}

int describeGA(bus_t busnumber, int addr, char *msg)
{
    int number_ga = get_number_ga(busnumber);

    if (number_ga <= 0)
        return SRCP_UNSUPPORTEDDEVICEGROUP;

    if ((addr > 0) && (addr <= number_ga)
        && (ga[busnumber].gastate[addr].protocol)) {
        sprintf(msg, "%lu.%.3lu 101 INFO %ld GA %d %c\n",
                ga[busnumber].gastate[addr].inittime.tv_sec,
                ga[busnumber].gastate[addr].inittime.tv_usec / 1000,
                busnumber, addr, ga[busnumber].gastate[addr].protocol);
    }
    else {
        strcpy(msg, "");
        return SRCP_NODATA;
    }
    return SRCP_INFO;
}

int infoGA(bus_t busnumber, int addr, int port, char *msg)
{
    int number_ga = get_number_ga(busnumber);

    if ((addr > 0) && (addr <= number_ga) && (port >= 0)
        && (port < MAXGAPORT)
        && (ga[busnumber].gastate[addr].tv[port].tv_sec > 0)) {
        sprintf(msg, "%lu.%.3lu 100 INFO %ld GA %d %d %d\n",
                ga[busnumber].gastate[addr].tv[port].tv_sec,
                ga[busnumber].gastate[addr].tv[port].tv_usec / 1000,
                busnumber, addr, port, ga[busnumber].gastate[addr].action);
    }
    else {
        struct timeval t;
        gettimeofday(&t, NULL);
        srcp_fmt_msg(SRCP_NODATA, msg, t);
        return SRCP_NODATA;
    }
    return SRCP_INFO;
}

int initGA(bus_t busnumber, int addr, const char protocol)
{
    int i;
    int rc = SRCP_OK;
    int number_ga = get_number_ga(busnumber);
    syslog_bus(busnumber, DBG_INFO, "init GA: %d %c", addr, protocol);
    if ((addr > 0) && (addr <= number_ga)) {
        char msg[100];
        rc = bus_supports_protocol(busnumber, protocol);
        if (rc != SRCP_OK) {
            return rc;
        }
        ga[busnumber].gastate[addr].protocol = protocol;
        gettimeofday(&ga[busnumber].gastate[addr].inittime, NULL);
        ga[busnumber].gastate[addr].activetime = 0;
        ga[busnumber].gastate[addr].action = 0;
        for (i = 0; i < MAXGAPORT; i++) {
            ga[busnumber].gastate[addr].tv[i].tv_sec = 0;
            ga[busnumber].gastate[addr].tv[i].tv_usec = 0;
        }
        if (buses[busnumber].init_ga_func != NULL)
            rc = (*buses[busnumber].init_ga_func) (&ga[busnumber].
                                                   gastate[addr]);
        if (rc == SRCP_OK) {
            ga[busnumber].gastate[addr].state = 1;
            describeGA(busnumber, addr, msg);
            enqueueInfoMessage(msg);
        }
        return rc;
    }
    else {
        return SRCP_UNSUPPORTEDDEVICE;
    }
}

int lockGA(bus_t busnumber, int addr, long int duration,
           sessionid_t sessionid)
{
    char msg[256];

    if (ga[busnumber].gastate[addr].locked_by == sessionid ||
        ga[busnumber].gastate[addr].locked_by == 0) {
        ga[busnumber].gastate[addr].locked_by = sessionid;
        ga[busnumber].gastate[addr].lockduration = duration;
        gettimeofday(&ga[busnumber].gastate[addr].locktime, NULL);
        describeLOCKGA(busnumber, addr, msg);
        enqueueInfoMessage(msg);
        return SRCP_OK;
    }
    else {
        return SRCP_DEVICELOCKED;
    }
    /* unreached */
    return SRCP_UNSUPPORTEDOPERATION;
}

int getlockGA(bus_t busnumber, int addr, sessionid_t * sessionid)
{
    *sessionid = ga[busnumber].gastate[addr].locked_by;
    return SRCP_OK;

}

int describeLOCKGA(bus_t bus, int addr, char *reply)
{
    sprintf(reply, "%lu.%.3lu 100 INFO %ld LOCK GA %d %ld %ld\n",
            ga[bus].gastate[addr].locktime.tv_sec,
            ga[bus].gastate[addr].locktime.tv_usec / 1000,
            bus, addr, ga[bus].gastate[addr].lockduration,
            ga[bus].gastate[addr].locked_by);
    return SRCP_OK;
}

int unlockGA(bus_t busnumber, int addr, sessionid_t sessionid)
{
    if (ga[busnumber].gastate[addr].locked_by == sessionid
        || ga[busnumber].gastate[addr].locked_by == 0) {
        char msg[256];
        ga[busnumber].gastate[addr].locked_by = 0;
        gettimeofday(&ga[busnumber].gastate[addr].locktime, NULL);
        sprintf(msg, "%lu.%.3lu 102 INFO %ld LOCK GA %d %ld\n",
                ga[busnumber].gastate[addr].locktime.tv_sec,
                ga[busnumber].gastate[addr].locktime.tv_usec / 1000,
                busnumber, addr, sessionid);
        enqueueInfoMessage(msg);
        return SRCP_OK;
    }
    else {
        return SRCP_DEVICELOCKED;
    }
}

void unlock_ga_bysessionid(sessionid_t sessionid)
{
    unsigned int i;
    int j, number;

    syslog_session(sessionid, DBG_DEBUG, "Unlocking GAs by session-id");
    for (i = 0; i < num_buses; i++) {
        number = get_number_ga(i);
        for (j = 1; j <= number; j++) {
            if (ga[i].gastate[j].locked_by == sessionid) {
                unlockGA(i, j, sessionid);
            }
        }
    }
}

/* must be called exactly once per second */
void unlock_ga_bytime(void)
{
    unsigned int i;
    int j, number;

    for (i = 0; i < num_buses; i++) {
        number = get_number_ga(i);
        for (j = 1; j <= number; j++) {
            if (ga[i].gastate[j].lockduration-- == 1) {
                unlockGA(i, j, ga[i].gastate[j].locked_by);
            }
        }
    }
}

int startup_GA(void)
{
    int result;
    unsigned int i;

    for (i = 0; i < MAX_BUSES; i++) {
        in[i] = 0;
        out[i] = 0;
        ga[i].numberOfGa = 0;
        ga[i].gastate = NULL;

        result = pthread_mutex_init(&queue_mutex[i], NULL);
        if (result != 0) {
            syslog_bus(0, DBG_ERROR,
                       "pthread_mutex_init() failed: %s (errno = %d).",
                       strerror(result), result);
        }
    }
    return 0;
}

int init_GA(bus_t busnumber, int number)
{
    int i;

    if (busnumber >= MAX_BUSES)
        return 1;

    if (number > 0) {
        /* one more, 'cause we do not use index 0, but start with 1 */
        ga[busnumber].gastate = malloc((number + 1) * sizeof(ga_state_t));
        if (ga[busnumber].gastate == NULL)
            return 1;
        ga[busnumber].numberOfGa = number;

        for (i = 0; i <= number; i++) {
            ga[busnumber].gastate[i].protocol = 0x00;
            ga[busnumber].gastate[i].locked_by = 0;
            ga[busnumber].gastate[i].action = 0;
            ga[busnumber].gastate[i].lockduration = 0;
        }
    }
    return 0;
}
