/***************************************************************************
                      ib.c  -  description
                       -------------------
begin                : Don Apr 19 17:35:13 MEST 2001
copyright            : (C) 2001 by Dipl.-Ing. Frank Schmischke
email                : frank.schmischke@t-online.de
***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *  Changes:                                                               *
 *                                                                         *
 *  17.01.2002 Frank Schmischke                                            *
 *   - using of kernelmodul/-device "ibox"                                 *
 *                                                                         *
 ***************************************************************************/

#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>

#include "config-srcpd.h"
#include "ib.h"
#include "io.h"
#include "toolbox.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-sm.h"
#include "srcp-time.h"
#include "srcp-fb.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "srcp-error.h"
#include "srcp-session.h"
#include "syslogmessage.h"
#include "ttycygwin.h"

#define __ib ((IB_DATA*)buses[busnumber].driverdata)
#define __ibt ((IB_DATA*)buses[btd->bus].driverdata)

/* forward declaration of some IB helper functions */
static int init_lineIB(bus_t busnumber);
static int sendBreak(const int fd, bus_t busnumber);
static int readAnswer_IB(const bus_t busnumber, const int generatePrintf);
static int readByte_IB(bus_t busnumber, int wait, unsigned char *the_byte);
static bool readList_IB(bus_t busnumber, unsigned char *bytes, size_t n);
static speed_t checkBaudrate(const int fd, const bus_t busnumber);
static void check_status_IB(bus_t busnumber);
static void check_status_fb_IB(bus_t busnumber);
static void check_status_ga_IB(bus_t busnumber);
static void check_status_gl_IB(bus_t busnumber);
static void check_status_pt_IB(bus_t busnumber);
static void send_command_ga_IB(bus_t busnumber);
static void send_command_gl_IB(bus_t busnumber);
static void send_command_sm_IB(bus_t busnumber);
static unsigned char send_command_power_IB(bus_t busnumber);

int readConfig_IB(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    syslog_bus(busnumber, DBG_INFO, "Reading configuration for bus '%s'",
               node->name);

    buses[busnumber].driverdata = malloc(sizeof(struct _IB_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_IB;
    buses[busnumber].init_func = &init_bus_IB;
    buses[busnumber].thr_func = &thr_sendrec_IB;
    buses[busnumber].init_gl_func = &init_gl_IB;
    buses[busnumber].init_ga_func = &init_ga_IB;
    buses[busnumber].flags |= FB_16_PORTS;
    buses[busnumber].flags |= USE_AUTODETECTION;
    buses[busnumber].device.file.baudrate = B38400;

    strcpy(buses[busnumber].description,
           "GA GL FB SM POWER LOCK DESCRIPTION");

    /* max. 31 modules for S88; Loconet not yet implemented */
    __ib->number_fb = 0;
    __ib->number_ga = 256;
    __ib->number_gl = 80;
    __ib->pause_between_cmd = 250;

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ib->number_fb = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ib->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ib->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "fb_delay_time_0") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                set_min_time(busnumber, atoi((char *) txt));
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "pause_between_commands")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ib->pause_between_cmd = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);

        child = child->next;
    }

    return (1);
}

/**
 * cacheInitGL: modifies the gl data used to initialize the device
 * this is called whenever a new loco comes in town...
 */
int init_gl_IB(gl_state_t * gl)
{
    gl->n_fs = SPEED_STEPS;
    if (gl->n_func > 29) {
        return SRCP_WRONGVALUE;
    }
    gl->protocol = 'P';
    return SRCP_OK;
}

/**
 * initGA: modifies the ga data used to initialize the device
 */
int init_ga_IB(ga_state_t * ga)
{
    ga->protocol = 'M';
    return SRCP_OK;
}

int init_bus_IB(bus_t busnumber)
{
  int status;
    static char *protocols = "MNP";

    buses[busnumber].protocols = protocols;

    if (init_GA(busnumber, __ib->number_ga)) {
        __ib->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessories");
    }

    if (init_GL(busnumber, __ib->number_gl)) {
        __ib->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for locomotives");
    }
    if (init_FB(busnumber, __ib->number_fb * 16)) {
        __ib->number_fb = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }

    status = 0;
    syslog_bus(busnumber, DBG_INFO, "Bus %d with debuglevel %d\n",
               busnumber, buses[busnumber].debuglevel);
    if (buses[busnumber].type != SERVER_IB) {
        status = -2;
    }
    else {
        if (buses[busnumber].device.file.fd > 0)
            status = -3;        /* bus is already in use */
    }

    if (status == 0) {
        __ib->working_IB = 0;
    }

    if (buses[busnumber].debuglevel < 7) {
        if (status == 0)
            status = init_lineIB(busnumber);
    }
    else
        buses[busnumber].device.file.fd = -1;
    if (status == 0)
        __ib->working_IB = 1;

    syslog_bus(busnumber, DBG_INFO, "INIT_BUS_IB exited with code: %d\n",
               status);

    __ib->last_type = -1;
    __ib->emergency_on_ib = 2;

    return status;
}

/**
 * Sends the command to switch P50 commands on or off, see interface
 * description of Intellibox
 *
 * The answer of the Intellibox is written to syslog
 *
 * @param  busnumber inside srcpd
 * @param  off, enable or disable
 **/
static void enableP50Commands(const bus_t busnumber, bool on)
{
    int status;

    if (on) {
        syslog_bus(busnumber, DBG_INFO, "Switching P50-commands on ...\n");
        writeString(busnumber, P50_ENABLE, 0);
    }
    else {
        syslog_bus(busnumber, DBG_INFO,
                   "Switching P50-commands off ...\n");
        writeString(busnumber, P50_DISABLE, 0);
    }

    writeByte(busnumber, '\r', 0);

    status = readAnswer_IB(busnumber, 0);
    if (status == 0)
        syslog_bus(busnumber, DBG_INFO, "Success.\n");
    else
        syslog_bus(busnumber, DBG_INFO, "Failure.\n");
}


/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    enableP50Commands(btd->bus, true);
    syslog_bus(btd->bus, DBG_INFO, "Intellibox bus terminated.");
    __ibt->working_IB = 0;
    close_comport(btd->bus);

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

static void handle_power_command(bus_t bus)
{
    buses[bus].power_changed = 0;
    char msg[110];

    infoPower(bus, msg);
    enqueueInfoMessage(msg);
    buses[bus].watchdog++;
}

void *thr_sendrec_IB(void *v)
{
    int status;
    unsigned char rr;
    int zaehler1;
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));

    if (btd == NULL)
        pthread_exit((void *) 1);
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);

    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    syslog_bus(btd->bus, DBG_INFO, "Intellibox bus started (device = %s).",
               buses[btd->bus].device.file.path);

    /* initialize tga-structure */
    for (zaehler1 = 0; zaehler1 < 50; zaehler1++)
        __ibt->tga[zaehler1].id = 0;

    /* request current sensor status */
    writeByte(btd->bus, XSensOff, 0);
    status = readByte_IB(btd->bus, 1, &rr);

    while (true) {
        buses[btd->bus].watchdog = 1;
        pthread_testcancel();
        if (buses[btd->bus].power_changed == 1) {
            if (__ibt->emergency_on_ib == 1) {
                syslog_bus(btd->bus, DBG_INFO, "got power off from IB");
                __ibt->emergency_on_ib = 2;
                setPower(btd->bus, POWER_OFF, "Emergency Stop");
            }
            else {
                if ((__ibt->emergency_on_ib == 2)
                    && (buses[btd->bus].power_state == POWER_OFF)) {
                    check_status_IB(btd->bus);
                    if (usleep(50000) == -1) {
                        syslog_bus(btd->bus, DBG_ERROR,
                                   "usleep() failed: %s (errno = %d)\n",
                                   strerror(errno), errno);
                    }
                    continue;
                }

                if (send_command_power_IB(btd->bus) == XPWOFF) {
                    syslog_bus(btd->bus, DBG_INFO,
                               "power on not possible - overheating");
                    setPower(btd->bus, POWER_OFF,
                             "power on not possible - overheating");
                }
                else if (buses[btd->bus].power_state == POWER_ON)
                    __ibt->emergency_on_ib = 0;
            }
            handle_power_command(btd->bus);
        }

        if (buses[btd->bus].power_state == POWER_OFF) {
            check_status_IB(btd->bus);
            if (usleep(50000) == -1) {
                syslog_bus(btd->bus, DBG_ERROR,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            continue;
        }
        send_command_gl_IB(btd->bus);
        send_command_ga_IB(btd->bus);
        check_status_IB(btd->bus);
        send_command_sm_IB(btd->bus);
        check_reset_fb(btd->bus);
        if (usleep(50000) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }                           /* End WHILE(1) */

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}

static void send_command_ga_IB(bus_t busnumber)
{
    int i, i1;
    int addr;
    unsigned char byte2send;
    unsigned char status;
    unsigned char rr;
    ga_state_t gatmp;
    struct timeval akt_time, cmp_time;

    gettimeofday(&akt_time, NULL);

    /* first switch of decoders */
    for (i = 0; i < 50; i++) {
        if (__ib->tga[i].id) {
            syslog_bus(busnumber, DBG_DEBUG, "Time %i,%i",
                       (int) akt_time.tv_sec, (int) akt_time.tv_usec);
            cmp_time = __ib->tga[i].t;

            /* switch off time reached? */
            if (cmpTime(&cmp_time, &akt_time)) {
                gatmp = __ib->tga[i];
                addr = gatmp.id;
                writeByte(busnumber, XTrnt, 0);

                writeByte(busnumber, gatmp.id & 0xFF, 0);

                byte2send = gatmp.id >> 8;
                if (gatmp.port) {
                    byte2send |= 0x80;
                }
                writeByte(busnumber, byte2send, 0);

                readByte_IB(busnumber, 1, &rr);
                gatmp.action = 0;
                setGA(busnumber, addr, gatmp);
                __ib->tga[i].id = 0;
            }
        }
    }

    /* switch on decoder */
    if (!queue_GA_isempty(busnumber)) {
        dequeueNextGA(busnumber, &gatmp);
        addr = gatmp.id;
        writeByte(busnumber, XTrnt, 0);

        writeByte(busnumber, gatmp.id & 0xFF, 0);

        byte2send = gatmp.id >> 8;
        if (gatmp.action) {
            byte2send |= 0x40;
        }
        if (gatmp.port) {
            byte2send |= 0x80;
        }
        writeByte(busnumber, byte2send, 0);

        status = 0;

        /* reschedule event: turn off --to be done-- */
        if (gatmp.action && (gatmp.activetime > 0)) {
            status = 1;
            for (i1 = 0; i1 < 50; i1++) {
                if (__ib->tga[i1].id == 0) {
                    gatmp.t = akt_time;
                    gatmp.t.tv_sec += gatmp.activetime / 1000;
                    gatmp.t.tv_usec += (gatmp.activetime % 1000) * 1000;
                    if (gatmp.t.tv_usec > 1000000) {
                        gatmp.t.tv_sec++;
                        gatmp.t.tv_usec -= 1000000;
                    }
                    __ib->tga[i1] = gatmp;
                    syslog_bus(busnumber, DBG_DEBUG,
                               "GA %i for switch off at %i,%i on %i",
                               __ib->tga[i1].id,
                               (int) __ib->tga[i1].t.tv_sec,
                               (int) __ib->tga[i1].t.tv_usec, i1);
                    break;
                }
            }
        }
        readByte_IB(busnumber, 1, &rr);
        if (status) {
            setGA(busnumber, addr, gatmp);
        }
    }
}

static void send_command_gl_IB(bus_t busnumber)
{
    int addr = 0;
    unsigned char byte2send;
    unsigned char status;
    gl_state_t gltmp, glakt;

    /* locomotive decoder */
    /* fprintf(stderr, "LOK's... "); */
    /* send only if new data is available */
    if (!queue_GL_isempty(busnumber)) {
        dequeueNextGL(busnumber, &gltmp);
        addr = gltmp.id;
	cacheGetGL(busnumber, addr, &glakt);

        /* speed, direction or function changed? */
        if ((gltmp.direction != glakt.direction) ||
            (gltmp.speed != glakt.speed) || (gltmp.funcs != glakt.funcs)) {
            /* send loco command */
            writeByte(busnumber, XLok, 0);

            /* send low byte of address */
            writeByte(busnumber, gltmp.id & 0xFF, 0);

            /* send high byte of address */
            writeByte(busnumber, gltmp.id >> 8, 0);

            /* if emergency stop is activated set emergency stop */
            if (gltmp.direction == 2) {
                byte2send = 1;
            }
            else {
                /* IB scales speeds INTERNALLY down! */
                /* but gltmp.speed can already contain down-scaled speed */
                /* IB has general range of 0..127, independent of decoder type */
                byte2send =
                    (unsigned char) ((gltmp.speed * SPEED_STEPS) / glakt.n_fs);

                if (byte2send > 0) {
                    byte2send++;
                }
            }
            writeByte(busnumber, byte2send, 0);

            /* set direction, light and function */
            byte2send =
                (gltmp.funcs >> 1) + (gltmp.funcs & 0x01 ? 0x10 : 0);
            byte2send |= 0xc0;
            if (gltmp.direction) {
                byte2send |= 0x20;
            }
            writeByte(busnumber, byte2send, 0);
            readByte_IB(busnumber, 1, &status);
            if ((status == 0) || (status == XLKHALT)
                || (status == XLKPOFF)) {
                if (gltmp.n_func > 5) {
                    writeByte(busnumber, XFunc, 0);

                    /* send low byte of address */
                    writeByte(busnumber, gltmp.id & 0xFF, 0);

                    /* send high byte of address */
                    writeByte(busnumber, gltmp.id >> 8, 0);

                    /* send F1 ... F8 */
                    writeByte(busnumber, (gltmp.funcs >> 1) & 0xFF, 0);

                    readByte_IB(busnumber, 1, &status);
                }
                if (gltmp.n_func > 9) {
                    writeByte(busnumber, XFuncX, 0);

                    /* send low byte of address */
                    writeByte(busnumber, gltmp.id & 0xFF, 0);

                    /* send high byte of address */
                    writeByte(busnumber, gltmp.id >> 8, 0);

                    /* send F9 ... F16 */
                    writeByte(busnumber, (gltmp.funcs >> 9) & 0xFF, 0);

                    readByte_IB(busnumber, 1, &status);
                }
                if (gltmp.n_func > 17) {
                    writeByte(busnumber, XFunc34, 0);

                    /* send low byte of address */
                    writeByte(busnumber, gltmp.id & 0xFF, 0);

                    /* send high byte of address */
                    writeByte(busnumber, gltmp.id >> 8, 0);

                    /* send F17 ... F24 */
                    writeByte(busnumber, (gltmp.funcs >> 17) & 0xFF, 0);

                    /* send F25 ... F28 */
                    writeByte(busnumber, (gltmp.funcs >> 25) & 0x0F, 0);

                    readByte_IB(busnumber, 1, &status);
                }
                cacheSetGL(busnumber, addr, gltmp);
            }
        }
    }
}

static int read_register_IB(bus_t busnumber, int reg)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCRR, 0);
    writeByte(busnumber, reg, 0);
    writeByte(busnumber, 0, 2);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int write_register_IB(bus_t busnumber, int reg, int value)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCWR, 0);
    writeByte(busnumber, reg, 0);
    writeByte(busnumber, 0, 0);
    writeByte(busnumber, value, 2);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int read_page_IB(bus_t busnumber, int cv)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCRP, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 0);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int write_page_IB(bus_t busnumber, int cv, int value)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCWP, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 0);
    writeByte(busnumber, value, 0);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int read_cv_IB(bus_t busnumber, int cv)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCRD, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 2);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int write_cv_IB(bus_t busnumber, int cv, int value)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCWD, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 0);
    writeByte(busnumber, value, 2);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int read_cvbit_IB(bus_t busnumber, int cv, int bit)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCRB, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 2);

    readByte_IB(busnumber, 1, &status);

    return status;
}

static int write_cvbit_IB(bus_t busnumber, int cv, int bit, int value)
{
    unsigned char status;

    writeByte(busnumber, XPT_DCCWB, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xFF, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 0);
    writeByte(busnumber, bit, 0);
    writeByte(busnumber, value, 0);

    readByte_IB(busnumber, 1, &status);

    return status;
}

/* program decoder on the main */
static int send_pom_IB(bus_t busnumber, int addr, int cv, int value)
{
    unsigned char status;

    /* send pom-command */
    writeByte(busnumber, XDCC_PD, 0);
    /* low-byte of decoder-address */
    writeByte(busnumber, addr & 0xFF, 0);
    /* high-byte of decoder-address */
    writeByte(busnumber, addr >> 8, 0);
    /* low-byte of cv */
    writeByte(busnumber, cv & 0xff, 0);
    /* high-byte of cv */
    writeByte(busnumber, cv >> 8, 0);
    writeByte(busnumber, value, 0);

    readByte_IB(busnumber, 1, &status);

    return (status == 0) ? 0 : -1;
}

static int term_pgm_IB(bus_t busnumber)
{
    unsigned char status;

    /* send command turn off PT */
    writeByte(busnumber, XPT_Off, 0);

    readByte_IB(busnumber, 1, &status);

    return (status == 0) ? 0 : -1;
}

static void send_command_sm_IB(bus_t busnumber)
{
    struct _SM smakt;

    /* locomotive decoder */
    /* send only if data is available */
    if (!queue_SM_isempty(busnumber)) {
        dequeueNextSM(busnumber, &smakt);

        __ib->last_type = smakt.type;
        __ib->last_typeaddr = smakt.typeaddr;
        __ib->last_bit = smakt.bit;
        __ib->last_value = smakt.value;

        syslog_bus(busnumber, DBG_DEBUG,
                   "in send_command_sm: last_type[%d] = %d", busnumber,
                   __ib->last_type);
        switch (smakt.command) {
            case SET:
                if (smakt.addr == -1) {
                    switch (smakt.type) {
                        case REGISTER:
                            write_register_IB(busnumber, smakt.typeaddr,
                                              smakt.value);
                            break;
                        case CV:
                            write_cv_IB(busnumber, smakt.typeaddr,
                                        smakt.value);
                            break;
                        case CV_BIT:
                            write_cvbit_IB(busnumber, smakt.typeaddr,
                                           smakt.bit, smakt.value);
                            break;
                        case PAGE:
                            write_page_IB(busnumber, smakt.typeaddr,
                                          smakt.value);
                    }
                }
                else {
                    send_pom_IB(busnumber, smakt.addr, smakt.typeaddr,
                                smakt.value);
                    session_endwait(busnumber, 0);
                }
                break;
            case GET:
                switch (smakt.type) {
                    case REGISTER:
                        read_register_IB(busnumber, smakt.typeaddr);
                        break;
                    case CV:
                        read_cv_IB(busnumber, smakt.typeaddr);
                        break;
                    case CV_BIT:
                        read_cvbit_IB(busnumber, smakt.typeaddr,
                                      smakt.bit);
                        break;
                    case PAGE:
                        read_page_IB(busnumber, smakt.typeaddr);
                }
                break;
            case VERIFY:
                session_endwait(busnumber, 0);
                break;
            case TERM:
                session_endwait(busnumber, term_pgm_IB(busnumber));
                break;
            case INIT:
                session_endwait(busnumber, 0);
                break;
        }
    }
}

static void check_status_IB(bus_t busnumber)
{
    unsigned char rr, xevnt1, xevnt2, xevnt3;

    /* Request for state�changes:
       1. �derungen an S88-Modulen
       2. manuelle Lokbefehle
       3. manuelle Weichenbefehle */

    /* #warning add loconet */

    writeByte(busnumber, XEvent, 0);
    xevnt2 = 0x00;
    xevnt3 = 0x00;
    readByte_IB(busnumber, 1, &xevnt1);
    if (xevnt1 & 0x80) {
        readByte_IB(busnumber, 1, &xevnt2);
        if (xevnt2 & 0x80) {
            readByte_IB(busnumber, 1, &xevnt3);
        }
    }

    /* at least one loco was controlled by throttle */
    if (xevnt1 & 0x01) {
        check_status_gl_IB(busnumber);
    }

    /* some feedback state has changed */
    if (xevnt1 & 0x04) {
        check_status_fb_IB(busnumber);
    }

    /* some turnout was switched by hand */
    if (xevnt1 & 0x20) {
        check_status_ga_IB(busnumber);
    }

    /* overheat, short-circuit on track etc. */
    if (xevnt2 & 0x3f) {
        syslog_bus(busnumber, DBG_DEBUG,
                   "On bus %i short detected; old-state is %i", busnumber,
                   getPower(busnumber));
        if ((__ib->emergency_on_ib == 0)
            && (getPower(busnumber) == POWER_ON)) {
            if (xevnt2 & 0x20)
                setPower(busnumber, POWER_OFF,
                         "Overheating condition detected");

            if (xevnt2 & 0x10)
                setPower(busnumber, POWER_OFF,
                         "Non-allowed electrical connection between "
                         "programming track and rest of layout");

            if (xevnt2 & 0x08)
                setPower(busnumber, POWER_OFF,
                         "Overload on DCC-Booster or Loconet");

            if (xevnt2 & 0x04)
                setPower(busnumber, POWER_OFF,
                         "Short-circuit on internal booster");

            if (xevnt2 & 0x02)
                setPower(busnumber, POWER_OFF, "Overload on Lokmaus-bus");

            if (xevnt2 & 0x01)
                setPower(busnumber, POWER_OFF,
                         "Short-circuit on external booster");

            __ib->emergency_on_ib = 1;
        }
    }

    /* power off? */
    /* we should send an XStatus-command */
    if ((xevnt1 & 0x08) || (xevnt2 & 0x40) || (__ib->emergency_on_ib == 2)) {
        writeByte(busnumber, XStatus, 0);
        readByte_IB(busnumber, 1, &rr);
        if (!(rr & 0x08)) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "On bus %i no power detected; old-state is %i",
                       busnumber, getPower(busnumber));
            if ((__ib->emergency_on_ib == 0)
                && (getPower(busnumber) == POWER_ON)) {
                setPower(busnumber, POWER_OFF, "Emergency Stop");
                __ib->emergency_on_ib = 1;
            }
        }
        else {
            syslog_bus(busnumber, DBG_DEBUG,
                       "On bus %i power detected; old-state is %i",
                       busnumber, getPower(busnumber));
            if ((__ib->emergency_on_ib == 1)
                || (getPower(busnumber) == POWER_OFF)) {
                setPower(busnumber, POWER_ON, "No Emergency Stop");
                __ib->emergency_on_ib = 0;
            }
        }
        if (rr & 0x80)
            readByte_IB(busnumber, 1, &rr);
    }

    /* we should send an XPT_event-command */
    if (xevnt3 & 0x01)
        check_status_pt_IB(busnumber);
}

static void check_status_pt_IB(bus_t busnumber)
{
    int i;
    int result;
    unsigned char rr[7];

    syslog_bus(busnumber, DBG_DEBUG,
               "We've got an answer from programming decoder");
    /* first clear input-buffer */
    i = 0;
    while (i == 0) {
        i = readByte_IB(busnumber, 0, &rr[0]);
    }

    i = -1;
    while (i == -1) {
        writeByte(busnumber, XEvtPT, 0);
        i = readByte_IB(busnumber, 1, &rr[0]);
        if (i == 0) {
            /* wait for an answer of our programming */
            if (rr[0] == 0xF5) {
                /* sleep for one second, if answer is not available yet */
                i = -1;
                result = sleep(1);
                if (result != 0) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "sleep() interrupted, %d seconds left\n",
                               result);
                }
            }
        }
    }

    for (i = 0; i < (int) rr[0]; i++)
        readByte_IB(busnumber, 1, &rr[i + 1]);

    if ((int) rr[0] < 2)
        rr[2] = __ib->last_value;


    if (__ib->last_type != -1) {
        session_endwait(busnumber, (int) rr[2]);
        setSM(busnumber, __ib->last_type, -1, __ib->last_typeaddr,
              __ib->last_bit, (int) rr[2], (int) rr[1]);
        __ib->last_type = -1;
    }
}

static int open_comport(bus_t busnumber, speed_t baud)
{
    int fd;
    char *name = buses[busnumber].device.file.path;

#ifdef linux
    unsigned char rr;
    int status;
    int result;
#endif

    struct termios interface;

    syslog_bus(busnumber, DBG_INFO,
               "Try to open serial line %s for %i baud", name,
               (2400 * (1 << (baud - 11))));
    fd = open(name, O_RDWR);
    buses[busnumber].device.file.fd = fd;
    if (fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Open serial line failed: %s (errno = %d).\n",
                   strerror(errno), errno);
    }
    else {
#ifdef linux
        tcgetattr(fd, &interface);
        interface.c_oflag = ONOCR;
        interface.c_cflag =
            CS8 | CRTSCTS | CSTOPB | CLOCAL | CREAD | HUPCL;
        interface.c_iflag = IGNBRK;
        interface.c_lflag = IEXTEN;
        cfsetispeed(&interface, baud);
        cfsetospeed(&interface, baud);
        interface.c_cc[VMIN] = 0;
        interface.c_cc[VTIME] = 1;
        tcsetattr(fd, TCSANOW, &interface);

        result = sleep(1);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "sleep() interrupted, %d seconds left\n", result);
        }

        status = 0;
        while (status != -1)
            status = readByte_IB(busnumber, 1, &rr);
#else
        cfsetispeed(&interface, baud);
        cfsetospeed(&interface, baud);
        interface.c_cflag = CREAD | HUPCL | CS8 | CSTOPB | CRTSCTS;
        cfmakeraw(&interface);
        tcsetattr(fd, TCSAFLUSH | TCSANOW, &interface);
#endif

    }
    return fd;
}

/*check if P50 commands are enabled
 * return values:
 * 1: enabled
 * 0: disabled
 * -1: error, e.g. download mode found*/
static int check_P50_command_state(bus_t busnumber)
{
    int i, len, result;
    int returnvalue = 0;
    unsigned char input[10];

    memset(input, '\0', sizeof(input));
    writeByte(busnumber, XNOP, 0);

    /* wait 200 ms, allow some time for eventual s88 data */
    if (usleep(200000) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "usleep() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    len = 0;
    for (i = 0; i < 2; i++) {
        result = readByte_IB(busnumber, 1, &input[i]);
        if (result == 0)
            len++;
    }

    switch (len) {
        case 1:
            if (input[0] == 'D') {
                syslog_bus(busnumber, DBG_FATAL,
                           "Intellibox in download mode.\n"
                           "Do not proceed!\n");
                returnvalue = -1;
            }
            syslog_bus(busnumber, DBG_INFO,
                       "P50-commands are disabled.\n");
            break;

        case 2:
            syslog_bus(busnumber, DBG_INFO, "P50-commands are enabled.\n");
            returnvalue = 1;
            break;

        default:
            syslog_bus(busnumber, DBG_ERROR,
                       "Unexpected read result: %d!\n", len);
            break;
    }
    return returnvalue;
}


/**
 * reset the baud rate inside ib depending on par 1
 * @param requested baudrate
 **/
static void resetBaudrate(const speed_t speed, const bus_t busnumber)
{
    int status;

    switch (check_P50_command_state(busnumber)) {
        case -1:
            /*Download mode, exit */
            return;
        case 0:
            /*P50 commands disabled, switch on */
            enableP50Commands(busnumber, true);
            break;
        case 1:
            /*P50 commands enabled, do nothing */
            break;
    }

    switch (speed) {
        case B2400:
            syslog_bus(busnumber, DBG_INFO,
                       "Changing baud rate to 2400 bps\n");
            writeString(busnumber, "B2400", 0);
            break;

        case B4800:
            syslog_bus(busnumber, DBG_INFO,
                       "Changing baud rate to 4800 bps\n");
            writeString(busnumber, "B4800", 0);
            break;

        case B9600:
            syslog_bus(busnumber, DBG_INFO,
                       "Changing baud rate to 9600 bps\n");
            writeString(busnumber, "B9600", 0);
            break;

        case B19200:
            syslog_bus(busnumber, DBG_INFO,
                       "Changing baud rate to 19200 bps\n");
            writeString(busnumber, "B19200", 0);
            break;

        case B38400:
            syslog_bus(busnumber, DBG_INFO,
                       "Changing baud rate to 38400 bps\n");
            writeString(busnumber, "B38400", 0);
            break;
    }

    writeByte(busnumber, '\r', 0);

    /* wait 200 ms */
    if (usleep(200000) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "usleep() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    status = readAnswer_IB(busnumber, 1);
    if (status == 0)
        syslog_bus(busnumber, DBG_INFO, "Success.\n");
    else
        syslog_bus(busnumber, DBG_INFO, "Failure.\n");
}

static int run_autodetection(bus_t busnumber)
{
    int fd;
    int status;
    int result;
    speed_t baud;
    struct termios interface;
    unsigned char rr;
    char *name = buses[busnumber].device.file.path;

    syslog_bus(busnumber, DBG_INFO,
               "Beginning to detect IB on serial line: %s\n", name);

    syslog_bus(busnumber, DBG_INFO,
               "Opening serial line %s for 2400 baud\n", name);
    fd = open(name, O_RDWR);
    if (fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Open serial line failed: %s (errno = %d)\n",
                   strerror(errno), errno);
        return 1;
    }
    buses[busnumber].device.file.fd = fd;
    tcgetattr(fd, &interface);
    interface.c_oflag = ONOCR;
    interface.c_cflag = CS8 | CRTSCTS | CSTOPB | CLOCAL | CREAD | HUPCL;
    interface.c_iflag = IGNBRK;
    interface.c_lflag = IEXTEN;
    cfsetispeed(&interface, B2400);
    cfsetospeed(&interface, B2400);
    interface.c_cc[VMIN] = 0;
    interface.c_cc[VTIME] = 1;
    tcsetattr(fd, TCSANOW, &interface);

    result = sleep(1);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "sleep() interrupted, %d seconds left\n", result);
    }
    syslog_bus(busnumber, DBG_INFO, "Clearing input-buffer\n");

    status = 0;
    while (status != -1)
        status = readByte_IB(busnumber, 1, &rr);

    syslog_bus(busnumber, DBG_INFO, "Sending BREAK... ");

    status = sendBreak(fd, busnumber);
    close(fd);

    if (status == 0)
        syslog_bus(busnumber, DBG_INFO, "Success.\n");
    else
        syslog_bus(busnumber, DBG_INFO, "Failure.\n");

    /* Open the comport with 2400 Baud, to get baud rate from IB */
    baud = B2400;
    fd = open_comport(busnumber, baud);
    buses[busnumber].device.file.fd = fd;
    if (fd < 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Open serial line failed: %s (errno = %d)\n",
                   strerror(errno), errno);
        return (-1);
    }

    baud = checkBaudrate(fd, busnumber);
    if ((baud == B0) || (baud > B38400)) {
        syslog_bus(busnumber, DBG_ERROR, "checkBaurate() failed\n");
        return -1;
    }

    buses[busnumber].device.file.baudrate = baud;
    resetBaudrate(buses[busnumber].device.file.baudrate, busnumber);
    close_comport(busnumber);

    result = sleep(1);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "sleep() interrupted, %d seconds left\n", result);
    }
    return 0;
}


static void show_firmware_version(bus_t bus)
{
    unsigned char buffer[20];

    memset(buffer, '\0', sizeof(buffer));
    writeByte(bus, XVer, 0);

    if (!readList_IB(bus, buffer, sizeof(buffer))) {
        syslog_bus(bus, DBG_INFO, "Could not read version information.\n");
        return;
    }

    /* FIXME: This solution definitively is not smart and needs some
     * fine tuning; the original IB delivers 6 list elements, other P50X
     * central units may do less. */
    syslog_bus(bus, DBG_INFO, "Firmware version: "
               "%hhi %hhi %hhi %hhi %hhi %hhi\n",
               buffer[0], buffer[1], buffer[2],
               buffer[3], buffer[4], buffer[5]);
}


static int init_lineIB(bus_t busnumber)
{
    int fd;

    if (buses[busnumber].flags & USE_AUTODETECTION) {
        if (0 != run_autodetection(busnumber))
            return (-1);
    }

    /* Open the serial line for the communication */
    fd = open_comport(busnumber, buses[busnumber].device.file.baudrate);
    buses[busnumber].device.file.fd = fd;
    if (fd < 0) {
        printf("open_comport() failed\n");
        return (-1);
    }

    switch (check_P50_command_state(busnumber)) {
        case -1:
            /*Download mode, exit */
            return 2;
        case 0:
            /*P50 commands disabled, do nothing */
            break;
        case 1:
            /*P50 commands enabled, switch off */
            enableP50Commands(busnumber, false);
            break;
    }

    /* read firmware version */
    show_firmware_version(busnumber);

    return 0;
}

/* Send RS232 break signal
 * This is an inversion of the data lead for a specific period of
 * time, exceeding the period of at least one full character, including
 * the start/stop bits, and any parity. As such, the duration of the
 * BREAK signal is a function of the port speed of the UART sending the
 * data. */
static int sendBreak(const int fd, bus_t busnumber)
{
    int result;

    if (tcflush(fd, TCIOFLUSH) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "tcflush() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
        return -1;
    }

    if (tcflow(fd, TCOOFF) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "tcflow() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    /* wait 300 ms */
    if (usleep(300000) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "usleep() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    /* Why is this simple call not sufficient and what does the "100"
     * mean? More than undefined behavior? (gs) */
    if (tcsendbreak(fd, 100) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "tcsendbreak() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
        return -1;
    }

    /* wait 3 s */
    result = sleep(3);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "sleep() interrupted, %d seconds left\n", result);
    }

    /* wait 600 ms */
    if (usleep(600000) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "usleep() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    if (tcflow(fd, TCOON) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "tcflow() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    return 0;
}


/**
 * Autodetection of Intellibox baud rate setting;
 * see interface description of Intellibox.
 *
 * @param file descriptor of the port
 * @param  busnumber inside srcp
 * @return the correct baudrate or -1 if not recognized
 **/
speed_t checkBaudrate(const int fd, const bus_t busnumber)
{
    int found = 0;
    int baudrate = 2400;
    struct termios interface;
    unsigned char input[10];
    short len = 0;
    int i;
    speed_t internalBaudrate = B0;

    syslog_bus(busnumber, DBG_INFO,
               "Checking baud rate inside IB, see special option #1 "
               "in IB Programming Handbook\n");

    memset(input, '\0', sizeof(input));

    while ((found == 0) && (baudrate <= 38400)) {
        syslog_bus(busnumber, DBG_INFO, "baudrate = %i\n", baudrate);

        if (tcgetattr(fd, &interface) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "tcgetattr() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
            return B0;
        }

        switch (baudrate) {
            case 2400:
                internalBaudrate = B2400;
                break;
            case 4800:
                internalBaudrate = B4800;
                break;
            case 9600:
                internalBaudrate = B9600;
                break;
            case 19200:
                internalBaudrate = B19200;
                break;
            case 38400:
                internalBaudrate = B38400;
                break;
            default:
                internalBaudrate = B19200;
                break;
        }

        if (cfsetispeed(&interface, internalBaudrate) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "cfsetispeed() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
            /*CHECK: What to do now? */
        }

        if (tcflush(fd, TCOFLUSH) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "tcflush() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        if (tcsetattr(fd, TCSANOW, &interface) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "tcsetattr() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
            return B0;
        }

        writeByte(busnumber, XNOP, 0);

        /* wait 200 ms */
        if (usleep(200000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        for (i = 0; i < 2; i++) {
            int erg = readByte_IB(busnumber, 1, &input[i]);
            if (erg == 0)
                len++;
        }

        switch (len) {
            case 1:
                /* IBox has P50 commands disabled */
                found = 1;
                if (input[0] == 'D') {
                    syslog_bus(busnumber, DBG_FATAL,
                               "Intellibox in download mode.\n"
                               "DO NOT PROCEED!\n");
                    return (2);
                }
                syslog_bus(busnumber, DBG_INFO,
                           "IBox found; P50-commands are disabled.\n");
                break;

            case 2:
                /* IBox has P50 commands enabled */
                found = 1;
                /* don't know if this also works, when P50 is enabled... */
                /* check disabled for now... */
                syslog_bus(busnumber, DBG_INFO,
                           "IBox found; P50-commands are enabled.\n");
                break;

            default:
                found = 0;
                break;
        }

        if (found == 0) {
            baudrate <<= 1;
            internalBaudrate = B0;
        }

        /* wait 200 ms */
        if (usleep(200000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }
    syslog_bus(busnumber, DBG_INFO, "Baudrate checked: %d\n", baudrate);
    return internalBaudrate;
}

/**
 * reads an answer of the intellibox after a command in P50 mode.
 *
 * If required, the answer of the intellibox is shown via syslog.
 * Usually the method reads until '[' is received, which is defined
 * as the end of the string. This last char is not printed.
 *
 * @param  busnumber inside srcp
 * @param if > 0 the answer is printed
 * @return 0 if OK
 **/
static int readAnswer_IB(const bus_t busnumber, const int generatePrintf)
{
    unsigned char input[80];
    int counter = 0;
    bool found = false;

    memset(input, '\0', sizeof(input));

    while ((!found) && (counter < 80)) {
        if (readByte_IB(busnumber, 1, &input[counter]) == 0) {
            if (input[counter] == 0)
                input[counter] = 0x20;
            if (input[counter] == ']') {
                input[counter] = '\0';
                found = true;
            }
        }
        counter++;
    }

    if (!found)
        return -1;

    if (generatePrintf > 0) {
        syslog_bus(busnumber, DBG_INFO, "IBox returned: %s\n", input);
    }
    return 0;
}

/**
 * Provides an extra interface for reading one byte out of the intellibox.
 *
 * Tries to read one byte 10 times. If no byte is received -1 is returned.
 * Because the IB guarantees an answer during 50 ms all write bytes can
 * be generated with a waiting time of 0, and readByte_IB can be called
 * directly after write
 *
 * @param: busnumber
 * @param: wait time during read
 * @param: address of the byte to be received.
 **/
static int readByte_IB(bus_t busnumber, int wait, unsigned char *the_byte)
{
    int i, status;

    for (i = 0; i < 10; i++) {
        status = readByte(busnumber, wait, the_byte);
        if (status == 0)
            return 0;

        /* wait 10 ms */
        if (usleep(10000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }
    return -1;
}

/**
 * Read a configuration list from the intellibox. Every list entry starts
 * with one byte (length) followed by n bytes. After that another list entry
 * may follow. If a length byte is 0 then the end of the list is reached.
 *
 * All list entries are copied into the given array without any gaps.
 *
 * @param: busnumber
 * @param: bytes array to store the list content
 * @param: n array size
 *
 * @result true if read was successful
 **/
static bool readList_IB(bus_t busnumber, unsigned char *bytes, size_t n)
{
    bool result = false;
    size_t j = 0;
    int i = 0, status;
    unsigned char length;

    if (bytes != NULL) {
	while (true) {
	    if (j >= n) {
	        break;
	    }
	    status = readByte_IB(busnumber, 1, &length);
	    if (status != 0) {
	        break;
	    }
	    if (length == 0) {
	        result = true;
		break;
	    }
	    for (i = 0; i < length && j < n; i++) {
	        status = readByte_IB(busnumber, 1, &bytes[j++]);
		if (status != 0) {
		  break;
		}
	    }
	}
    }
    return result;
}

static unsigned char send_command_power_IB(bus_t busnumber)
{
    unsigned char result;
    int status;

    writeByte(busnumber,
              (buses[busnumber].power_state ==
               POWER_ON) ? XPwrOn : XPwrOff, __ib->pause_between_cmd);
    status = readByte_IB(busnumber, 1, &result);
    while (status == -1) {
        if (usleep(100000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        status = readByte_IB(busnumber, 1, &result);
    }
    return result;
}

static void check_status_fb_IB(bus_t busnumber)
{
    unsigned char rr;
    int temp, aktS88;

    writeByte(busnumber, XEvtSen, 0);
    readByte_IB(busnumber, 1, &rr);
    while (rr != 0x00) {
        aktS88 = rr;
        readByte_IB(busnumber, 1, &rr);
        temp = rr;
        temp <<= 8;
        readByte_IB(busnumber, 1, &rr);
        setFBmodul(busnumber, aktS88, temp | rr);
        readByte_IB(busnumber, 1, &rr);
    }
}

static void check_status_ga_IB(bus_t busnumber)
{
    unsigned char rr;
    int temp, i;
    ga_state_t gatmp;

    writeByte(busnumber, XEvtTrn, 0);
    readByte_IB(busnumber, 1, &rr);
    temp = rr;
    for (i = 0; i < temp; i++) {
        readByte_IB(busnumber, 1, &rr);
        gatmp.id = rr;
        readByte_IB(busnumber, 1, &rr);
        gatmp.id |= (rr & 0x07) << 8;
        gatmp.port = (rr & 0x80) ? 1 : 0;
        gatmp.action = (rr & 0x40) ? 1 : 0;
        setGA(busnumber, gatmp.id, gatmp);
    }
}

static void check_status_gl_IB(bus_t busnumber)
{
    unsigned char rr;
    gl_state_t gltmp, glakt;

    writeByte(busnumber, XEvtLok, 0);
    readByte_IB(busnumber, 1, &rr);

    while (rr != 0x80) {
        if (rr == 1) {
            /* Loco in emergency stop */
            gltmp.speed = 0;
            gltmp.direction = 2;
        }
        else {
            /* current loco speed */
            gltmp.speed = rr;
            gltmp.direction = 0;
            if (gltmp.speed > 0)
                gltmp.speed--;
        }

        /* 2. byte functions */
        readByte_IB(busnumber, 1, &rr);
        /* gltmp.funcs = rr & 0xf0; */
        gltmp.funcs = (rr << 1);

        /* 3. byte address (low-part A7..A0) */
        readByte_IB(busnumber, 1, &rr);
        gltmp.id = rr;

        /* 4. byte address (high-part A13..A8), direction, light */
        readByte_IB(busnumber, 1, &rr);
        if ((rr & 0x80) && (gltmp.direction == 0))
            gltmp.direction = 1;        /* direction is forward */
        if (rr & 0x40)
            gltmp.funcs |= 0x01;        /* light is on */
        rr &= 0x3F;
        gltmp.id |= rr << 8;

        /* 5. byte real speed (is ignored) */
        readByte_IB(busnumber, 1, &rr);
        /* gltmp.id, gltmp.speed, gltmp.direction); */

        /* initialize the GL if not done by user, */
        /* because IB can report uninitialized GLs... */
        if (!isInitializedGL(busnumber, gltmp.id)) {
            syslog_bus(busnumber, DBG_INFO,
                       "IB reported uninitialized GL. "
                       "Performing default init for %d", gltmp.id);
            cacheInitGL(busnumber, gltmp.id, 'P', 1, SPEED_STEPS, 5);
        }
        /* get old data, to know which FS the user wants to have... */
        cacheGetGL(busnumber, gltmp.id, &glakt);
        /* recalculate speed */
        gltmp.speed = (gltmp.speed * glakt.n_fs) / SPEED_STEPS;
        cacheSetGL(busnumber, gltmp.id, gltmp);
    }
}
