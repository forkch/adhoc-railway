/* $Id: loopback.c 1456 2010-02-28 20:01:39Z gscholz $ */

/**
 * adhocmm: srcp interface for the AdHoc Brain
 **/

#include <errno.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <unistd.h>
#include <math.h>
#include <stdlib.h>

#include "config-srcpd.h"
#include "adhocmm.h"
#include "toolbox.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-sm.h"
#include "srcp-power.h"
#include "srcp-server.h"
#include "srcp-info.h"
#include "srcp-error.h"
#include "syslogmessage.h"
#include "io.h"

#define __adhocmm ((ADHOCMM_DATA*)buses[busnumber].driverdata)
#define __adhocmmt ((ADHOCMM_DATA*)buses[btd->bus].driverdata)

#define MAX_CV_NUMBER 255

#define UART_DELAY 0

#define LOCO_DEAD 0
#define LOCO_ALIVE 1

#define POWER_OFF 0
#define POWER_ON  1

int locoState[1024];
unsigned char booster_state[8];

static const int END_CMD = 0x0D;
static const char *TURNOUT_CMD = "XT ";

void initLocomotiveOnBrain(bus_t bus, const gl_state_t *gl);

void terminateLocomotiveOnBrain(bus_t busnumber, gl_state_t *gltmp);

void sendNewLocomotiveValuesToBrain(bus_t busnumber, gl_state_t *gltmp);

int readconfig_ADHOCMM(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber) {
    buses[busnumber].driverdata = malloc(sizeof(struct _ADHOCMM_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_ADHOCMM;
    buses[busnumber].init_func = &init_bus_ADHOCMM;
    buses[busnumber].thr_func = &thr_sendrec_ADHOCMM;
    buses[busnumber].init_gl_func = &init_gl_ADHOCMM;
    buses[busnumber].init_ga_func = &init_ga_ADHOCMM;
    strcpy(buses[busnumber].description, "GA GL POWER LOCK DESCRIPTION");

    __adhocmm->number_ga = 256;
    __adhocmm->number_gl = 80;

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0)
        || (xmlStrncmp(child->name, BAD_CAST
        "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __adhocmm->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        } else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __adhocmm->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
        syslog_bus(busnumber, DBG_WARN,
                   "WARNING, unknown tag found: \"%s\"!\n", child->name);;

        child = child->next;
    }

    return (1);
}

static int init_lineADHOCMM(bus_t bus) {
    int fd;
    struct termios interface;

    if (buses[bus].debuglevel > 0) {
        syslog_bus(bus, DBG_INFO, "Opening serial connection to ATMega8: %s",
                   buses[bus].device.file.path);
    }

    fd = open(buses[bus].device.file.path, O_RDWR | O_NONBLOCK);
    if (fd == -1) {
        syslog_bus(bus, DBG_ERROR, "Open serial device '%s' failed: %s "
                           "(errno = %d).\n", buses[bus].device.file.path, strerror(errno),
                   errno);
        return -1;
    }
    tcgetattr(fd, &interface);
    cfsetispeed(&interface, B230400);
    cfsetospeed(&interface, B230400);
#ifdef linux
    interface.c_cflag &= ~PARENB;
    interface.c_cflag |= CSTOPB;
    interface.c_cflag &= ~CSIZE;
    interface.c_cflag |= CS8;
    interface.c_cflag |= CRTSCTS;

    interface.c_iflag &= ~(IXON | IXOFF | IXANY);
    interface.c_oflag &= ~OPOST;

    interface.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);

#endif

    tcsetattr(fd, TCSANOW, &interface);
    syslog_bus(bus, DBG_INFO,
               "Opening serial connection to ATMega8 succeeded (fd = %d).", fd);
    return fd;
}

/**
 * init_gl_ADHOCMM: modifies the gl data used to initialize the device
 **/
int init_gl_ADHOCMM(bus_t bus, gl_state_t *gl) {
    syslog_bus(bus, DBG_INFO, "new loco");

    switch (gl->protocol) {
        case 'X': //mfx
            if (gl->n_fs == 127) {
                syslog_bus(bus, DBG_DEBUG, "new dcc loco %dl", gl->uuid);
                break;
                //return SRCP_OK;
            } else {
                return SRCP_WRONGVALUE;
            }
            break;
        case 'N': //dcc
            if (gl->n_fs == 127) {
                syslog_bus(bus, DBG_DEBUG, "new dcc loco");
                break;
                //return SRCP_OK;
            } else {
                return SRCP_WRONGVALUE;
            }
            break;
        case 'M':
            switch (gl->protocolversion) {
                case 1:
                    if (gl->n_fs == 14 || (gl->n_fs == 127)) {
//                        return SRCP_OK;
                        syslog_bus(bus, DBG_DEBUG, "new m1 loco");
                        break;
                    } else {
                        return SRCP_WRONGVALUE;
                    }
                    break;
                case 2:
                    if ((gl->n_fs == 14) || (gl->n_fs == 27) || (gl->n_fs == 28) || (gl->n_fs == 127)) {
//                        return SRCP_OK;
                        syslog_bus(bus, DBG_DEBUG, "new m2 loco");
                        break;
                    } else {
                        return SRCP_WRONGVALUE;
                    }
            }
            break;
        case 'L':
        case 'S':
        case 'P':
        default:
            return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
    }

    return SRCP_OK;
}

void initLocomotiveOnBrain(bus_t bus, const gl_state_t *gl) {
    char addr_buf[4];
    char speed_buf[4];
    sprintf(addr_buf, "%d", gl->id);

    syslog_bus(bus, DBG_INFO, "configuring new loco: %c", gl->protocol);
    // send loco config command
    writeString(bus, "XLS ", UART_DELAY);

    //send loco address
    writeString(bus, addr_buf, UART_DELAY);
    writeByte(bus, ' ', UART_DELAY);

    char mfxUid_buf[16];
    //send protocol
    switch (gl->protocol) {
        case 'X': //mfx
            syslog_bus(bus, DBG_INFO, "initializing mfx locomotive");
            sprintf(mfxUid_buf, "%lu", gl->uuid);
            syslog_bus(bus, DBG_INFO, mfxUid_buf);
            writeString(bus, "MFX", UART_DELAY);
            writeByte(bus, ' ', UART_DELAY);
            writeString(bus, mfxUid_buf, UART_DELAY);
            break;
        case 'N': //dcc
            writeString(bus, "DCC", UART_DELAY);
            break;
        case 'M': // motorola
            switch (gl->protocolversion) {
                case 1:
                    writeString(bus, "MM", UART_DELAY);
                    break;
                case 2:
                    writeString(bus, "MM2", UART_DELAY);
                    break;
            }
            break;

    }

    writeByte(bus, END_CMD, UART_DELAY);

    locoState[gl->id] = LOCO_ALIVE;
}

/**
 * initGA: modifies the ga data used to initialize the device
 **/
int init_ga_ADHOCMM(ga_state_t *ga) {
    if ((ga->protocol == 'M') || (ga->protocol == 'P')) {
        char addr_buf[4];
        sprintf(addr_buf, "%d", ga->id);

        writeString(1, "XTS ", UART_DELAY);
        writeString(1, addr_buf, UART_DELAY);
        writeByte(1, ' ', UART_DELAY);
        if (ga->type == 1) {
            // turnoout
            writeString(1, "TURNOUT", UART_DELAY);

        } else if (ga->type == 2) {
            // cutter
            writeString(1, "CUTTER", UART_DELAY);
        } else if (ga->type == 3) {
            // turntable
            writeString(1, "TURNTABLE", UART_DELAY);
        } else {

        }
        writeByte(1, END_CMD, UART_DELAY);
        return SRCP_OK;
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

/* Initialisiere den Bus, signalisiere Fehler
 * Einmal aufgerufen mit busnummer als einzigem Parameter
 * return code wird ignoriert (vorerst)
 */
int init_bus_ADHOCMM(bus_t busnumber) {
    static char *protocols = "MNX";

    buses[busnumber].protocols = protocols;

    syslog_bus(busnumber, DBG_INFO,
               "AdHocMM start initialization (verbosity = %d).",
               buses[busnumber].debuglevel);

    if (init_GL(busnumber, __adhocmm->number_gl)) {
        __adhocmm->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR, "Can't create array for locomotives");
    }

    if (init_GA(busnumber, __adhocmm->number_ga)) {
        __adhocmm->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR, "Can't create array for accessoires");
    }

    int i = 0;
    for (i = 0; i < 1024; i++) {
        locoState[i] = LOCO_DEAD;
    }
    for (i = 0; i < 8; i++) {
        booster_state[i] = 'O';
    }

    syslog_bus(i, DBG_INFO, "AdHocMM open device %s.",
               buses[i].device.file.path);
    buses[busnumber].device.file.fd = init_lineADHOCMM(busnumber);

    if (buses[busnumber].device.file.fd > 0) {
        syslog_bus(i, DBG_INFO, "AdHocMM initialization done.");
        return 0;
    } else {
        syslog_bus(busnumber, DBG_ERROR, "INIT_BUS_ADHOCMM failed");
        return -1;
    }
}

static void check_status(bus_t busnumber) {

    char msg[110];
    int j;

    int i = 0;
    char c;
    int bytesToRead;

    int status = ioctl(buses[busnumber].device.file.fd, FIONREAD, &bytesToRead);
    if (bytesToRead == 0) {
        return;
    }
    while (readByte(busnumber, 0, &c) == 0) {
        if (c == END_CMD || c == 0x00 || c == '\r' || c == '\n') {
            msg[i] = 0;
            break;
        }
        msg[i] = c;
        i++;
    }
    if (i > 0) {
        syslog_bus(busnumber, DBG_INFO, "check_status() %s", msg);

        if (strncasecmp(msg, "XRS", 3) == 0) {
            syslog_bus(busnumber, DBG_INFO, "received reset from the brain");

            for (i = 0; i < 80; i++) {
                locoState[i] = LOCO_DEAD;
            }
            for (i = 0; i < 8; i++) {
                booster_state[i] = 'O';
            }

        } else if (strncasecmp(msg, "XBS", 3) == 0) {

            syslog_bus(busnumber, DBG_INFO, "received new booster state");
            unsigned char booster_state_new[8];
            sscanf(msg, "XBS %c %c %c %c %c %c %c %c", &booster_state_new[0],
                   &booster_state_new[1], &booster_state_new[2],
                   &booster_state_new[3], &booster_state_new[4],
                   &booster_state_new[5], &booster_state_new[6],
                   &booster_state_new[7]);

            int nr = 0;
            for (nr = 0; nr < 8; nr++) {

                booster_state[nr] = booster_state_new[nr];
                if (booster_state_new[nr] == 'S') {
                    char m[110];
                    sprintf(m, "%d S", nr);

                    syslog_bus(busnumber, DBG_INFO, "booster %d SHORTCUT", nr);

                    buses[busnumber].power_state = 0;
                    char msg1[110];
                    sprintf(msg1, "%lu.%.3lu 100 INFO %ld POWER %s %s\n",
                            buses[busnumber].power_change_time.tv_sec,
                            buses[busnumber].power_change_time.tv_usec / 1000,
                            busnumber, "OFF", m);
                    enqueueInfoMessage(msg1);

                } else if (booster_state_new[nr] == 'A') {
                    char m[110];
                    sprintf(m, "%d A", nr);

                    syslog_bus(busnumber, DBG_INFO, "booster %d ON", nr);

                    buses[busnumber].power_state = 1;
                    char msg1[110];

                    sprintf(msg1, "%lu.%.3lu 100 INFO %ld POWER %s %s\n",
                            buses[busnumber].power_change_time.tv_sec,
                            buses[busnumber].power_change_time.tv_usec / 1000,
                            busnumber, "ON", m);
                    enqueueInfoMessage(msg1);
                } else if (booster_state_new[nr] == 'O') {
                    char m[110];
                    sprintf(m, "%d O", nr);

                    syslog_bus(busnumber, DBG_INFO, "booster %d OFF", nr);

                    strcpy(buses[busnumber].power_msg, m);

                    buses[busnumber].power_state = 0;
                    char msg1[110];
                    sprintf(msg1, "%lu.%.3lu 100 INFO %ld POWER %s %s\n",
                            buses[busnumber].power_change_time.tv_sec,
                            buses[busnumber].power_change_time.tv_usec / 1000,
                            busnumber, "OFF", m);
                    enqueueInfoMessage(msg1);
                }
            }

            char power_msg[110];
            sprintf(power_msg,
                    "%d %c %d %c %d %c %d %c %d %c %d %c %d %c %d %c", 0,
                    booster_state[0], 1, booster_state[1], 2, booster_state[2],
                    3, booster_state[3], 4, booster_state[4], 5,
                    booster_state[5], 6, booster_state[6], 7, booster_state[7]);
            strcpy(buses[busnumber].power_msg, power_msg);
        }
    } else {

    }

}

static void handle_power_command(bus_t busnumber) {
    buses[busnumber].power_changed = 0;
    char msg[110];

    int boosterNumber = -1;
    sscanf(buses[busnumber].power_msg, "%d", &boosterNumber);
    if (buses[busnumber].power_state == POWER_OFF) {

        if (booster_state[boosterNumber] != 'S'
            || booster_state[boosterNumber] != 'O') {

            char booster_nr_buf[4];

            writeString(busnumber, "XSTOP ", UART_DELAY);

            if (boosterNumber != -1) {
                sprintf(booster_nr_buf, "%d", boosterNumber);
                writeString(busnumber, booster_nr_buf, UART_DELAY);
            }

            writeByte(busnumber, END_CMD, UART_DELAY);
        }

    } else {
        char booster_nr_buf[4];
        if (booster_state[boosterNumber] != 'A') {
            writeString(busnumber, "XGO ", UART_DELAY);

            if (boosterNumber != -1) {
                sprintf(booster_nr_buf, "%d", boosterNumber);
                writeString(busnumber, booster_nr_buf, UART_DELAY);
            }

            writeByte(busnumber, END_CMD, UART_DELAY);
        }
    }

    infoPower(busnumber, msg);
    enqueueInfoMessage(msg);
    buses[busnumber].watchdog++;
}

static void handle_gl_command(bus_t busnumber) {
    gl_state_t gltmp, glakt;
    int addr;

    if (!queue_GL_isempty(busnumber)) {
        dequeueNextGL(busnumber, &gltmp);
        addr = gltmp.id;
        int changeDirection = 0;
        cacheGetGL(busnumber, addr, &glakt);

        syslog_bus(busnumber, DBG_INFO, "new GL command loco state: %d", gltmp.state);
        if (locoState[gltmp.id] == LOCO_DEAD) {
            syslog_bus(busnumber, DBG_INFO, "initLocomotiveOnBrain");
            initLocomotiveOnBrain(busnumber, &gltmp);
            locoState[gltmp.id] = LOCO_ALIVE;
        }

        bool changeDetected = (gltmp.direction != glakt.direction) || (gltmp.speed != glakt.speed)
                              || (gltmp.funcs != glakt.funcs);
        if (gltmp.state == 1 && changeDetected) {
            syslog_bus(busnumber, DBG_INFO, "set new values");
            sendNewLocomotiveValuesToBrain(busnumber, &gltmp);

        } else if (gltmp.state == 2) {
            syslog_bus(busnumber, DBG_INFO, "terminating locomotive");
            terminateLocomotiveOnBrain(busnumber, &gltmp);

        }
        cacheSetGL(busnumber, addr, gltmp);
        buses[busnumber].watchdog++;

    }
}

void sendNewLocomotiveValuesToBrain(bus_t busnumber, gl_state_t *gltmp) {
    char addr_buf[4];
    char speed_buf[4];
    sprintf(addr_buf, "%d", (*gltmp).id);
    sprintf(speed_buf, "%d", (*gltmp).speed);

    // send loco command
    writeString(busnumber, "XL ", UART_DELAY);

    //send loco address
    writeString(busnumber, addr_buf, UART_DELAY);
    writeByte(busnumber, ' ', 0);

    //send speed
    writeString(busnumber, speed_buf, UART_DELAY);
    writeByte(busnumber, ' ', 0);

    //send direction
    if ((*gltmp).direction == 0) {
        writeByte(busnumber, '0', UART_DELAY);
    } else if ((*gltmp).direction == 1) {
        writeByte(busnumber, '1', UART_DELAY);
    } else if ((*gltmp).direction == 2) {
        writeByte(busnumber, '2', UART_DELAY);
    }

    writeByte(busnumber, ' ', UART_DELAY);
    int fn = 0;
    for (fn = 0; fn < (*gltmp).n_func; fn++) {
        //send function
        if (((*gltmp).funcs & (1 << fn)) == 0) {
            writeByte(busnumber, '0', UART_DELAY);
        } else {
            writeByte(busnumber, '1', UART_DELAY);
        }
        writeByte(busnumber, ' ', UART_DELAY);
    }

    writeByte(busnumber, END_CMD, UART_DELAY);
}

void terminateLocomotiveOnBrain(bus_t busnumber, gl_state_t *gltmp) {
    char addr_buf[4];
    char speed_buf[4];
    sprintf(addr_buf, "%d", (*gltmp).id);

    syslog_bus(busnumber, DBG_INFO, "removing loco");
    // send loco remove command
    writeString(busnumber, "XLOCREMOVE ", UART_DELAY);

    //send loco address
    writeString(busnumber, addr_buf, UART_DELAY);

    writeByte(busnumber, END_CMD, UART_DELAY);
    locoState[(*gltmp).id] = LOCO_DEAD;
}

static void handle_ga_command(bus_t busnumber) {
    ga_state_t gatmp;
    int addr, i;
    struct timeval akt_time;

    dequeueNextGA(busnumber, &gatmp);
    addr = gatmp.id;

    gettimeofday(&akt_time, NULL);
    gatmp.tv[gatmp.port] = akt_time;

    if (gatmp.action && (gatmp.activetime > 0)) {
        char buf[4];
        sprintf(buf, "%d", gatmp.id);

        writeStringLength(busnumber, TURNOUT_CMD, 3, UART_DELAY);
        writeStringLength(busnumber, buf, 3, UART_DELAY);
        writeByte(busnumber, ' ', UART_DELAY);

        if (gatmp.port == 0) {
            writeByte(busnumber, 'g', UART_DELAY);
        } else {
            writeByte(busnumber, 'r', UART_DELAY);
        }
        writeByte(busnumber, END_CMD, UART_DELAY);
    }
    //usleep(50000);
    setGA(busnumber, addr, gatmp);
    buses[busnumber].watchdog++;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t *btd) {
    int result;

    syslog_bus(btd->bus, DBG_INFO, "AdHocMM bus terminated.");

    result = pthread_mutex_destroy(&buses[btd->bus].transmit_mutex);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_WARN,
                   "pthread_mutex_destroy() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    result = pthread_cond_destroy(&buses[btd->bus].transmit_cond);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_WARN,
                   "pthread_mutex_init() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    free(buses[btd->bus].driverdata);
    free(btd);
}

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wmissing-noreturn"

/*main thread routine for this bus*/
void *thr_sendrec_ADHOCMM(void *v) {
    int addr, ctr;
    struct timeval akt_time, cmp_time;
    ga_state_t gatmp;
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));

    if (btd == NULL)
        pthread_exit((void *) 1);
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);
    /*register cleanup routine */

    pthread_cleanup_push((void *) end_bus_thread, (void *) btd) ;
            /* initialize tga-structure */
            for (ctr = 0; ctr < 50; ctr++)
                __adhocmmt->tga[ctr].id = 0;

            syslog_bus(btd->bus, DBG_INFO,
                       "AdHocMM bus started (device = %s).",
                       buses[btd->bus].device.file.path);

            /*enter endless loop to process work tasks */
            while (true) {

                buses[btd->bus].watchdog = 1;

                /*POWER action arrived */
                if (buses[btd->bus].power_changed == 1)
                    handle_power_command(btd->bus);

                /*GL action arrived */
                if (!queue_GL_isempty(btd->bus))
                    handle_gl_command(btd->bus);

                /* handle delayed switching of GAs (there is a better place) */
                gettimeofday(&akt_time, NULL);
                for (ctr = 0; ctr < 50; ctr++) {
                    if (__adhocmmt->tga[ctr].id) {
                        cmp_time = __adhocmmt->tga[ctr].t;

                        /* switch off time reached? */
                        if (cmpTime(&cmp_time, &akt_time)) {
                            gatmp = __adhocmmt->tga[ctr];
                            addr = gatmp.id;
                            gatmp.action = 0;
                            setGA(btd->bus, addr, gatmp);
                            __adhocmmt->tga[ctr].id = 0;
                        }
                    }
                }

                /*GA action arrived */
                if (!queue_GA_isempty(btd->bus))
                    handle_ga_command(btd->bus);

                /*FB action arrived */
                /* currently nothing to do here */
                buses[btd->bus].watchdog++;

                /* busy wait and continue loop */
                /* wait 5 ms */
                if (usleep(5000) == -1) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                check_status(btd->bus);
            }

            /*run the cleanup routine */
    pthread_cleanup_pop(1);
}

#pragma clang diagnostic pop
