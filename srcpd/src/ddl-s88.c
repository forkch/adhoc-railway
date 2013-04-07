/***************************************************************************
                          ddl-s88.c  -  description
                             -------------------
    begin                : Wed Aug 1 2001
    copyright            : (C) 2001 by Dipl.-Ing. Frank Schmischke
    email                : frank.schmischke@t-online.de

    This source based on errdcd-source code by Torsten Vogt.
    full header statement below!
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include <errno.h>
#include <string.h>
#include <unistd.h>

#ifdef linux
#include <sys/io.h>
#else
#ifdef __CYGWIN__
#include <sys/io.h>
#else
#if __FreeBSD__
#include <sys/stat.h>
#include <fcntl.h>
#include <dev/ppbus/ppi.h>
#include <dev/ppbus/ppbconf.h>
/* #else */
/* #error This driver is not usable on your operation system. Sorry. */
#endif
#endif
#endif

#if defined(linux) || defined(__CYGWIN__) || defined(__FreeBSD__)

#include "config-srcpd.h"
#include "srcp-fb.h"
#include "ddl-s88.h"
#include "io.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "syslogmessage.h"

/***************************************************************/
/* erddcd - Electric Railroad Direct Digital Command Daemon    */
/*    generates without any other hardware digital commands    */
/*    to control electric model railroads                      */
/*                                                             */
/* file: maerklin_s88.c                                        */
/* job : some routines to read s88 data from the printer-port  */
/*                                                             */
/* Torsten Vogt, Dieter Schaefer, October 1999                 */
/* Martin Wolf, November 2000                                  */
/*                                                             */
/* last changes: Torsten Vogt, march 2000                      */
/*               Martin Wolf, November 2000                    */
/* modified for srcpd: Matthias Trute, may 2002 */
/* modified for FreeBSD: Michael Meiszl, January 2003              */
/*                                                             */
/***************************************************************/

/* signals on the S88-Bus */
#define S88_QUIET 0x00          /* all signals low */
#define S88_RESET 0x04          /* reset signal high */
#define S88_LOAD  0x02          /* load signal high */
#define S88_CLOCK 0x01          /* clock signal high */
#define S88_DATA1 0x40          /* mask for data form S88 bus 1 (ACK) */
#define S88_DATA2 0x80          /* mask for data from S88 bus 2 (BUSY) !inverted */
#define S88_DATA3 0x20          /* mask for data from S88 bus 3 (PEND) */
#define S88_DATA4 0x10          /* mask for data from S88 bus 4 (SEL) */

/* Output a Signal to the Bus */
#define S88_WRITE(x) for (i = 0; i < S88CLOCK_SCALE; i++) outb(x, S88PORT)

/* possible io-addresses for the parallel port */
static const unsigned long int LPT_BASE[] = { 0x378, 0x278, 0x3BC };

/* number of possible parallel ports */
static const unsigned int LPT_NUM = 3;
/* values of the bits in a byte */
static const unsigned char BIT_VALUES[] =
    { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };

#define __ddl_s88 ((DDL_S88_DATA *) buses[busnumber].driverdata)
#define __ddl_s88t ((DDL_S88_DATA *) buses[btd->bus].driverdata)

int readconfig_DDL_S88(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    int i;

    buses[busnumber].driverdata = malloc(sizeof(struct _DDL_S88_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_S88;
    buses[busnumber].init_func = &init_bus_S88;
    buses[busnumber].thr_func = &thr_sendrec_S88;

    __ddl_s88->port = 0x0378;
#ifdef __FreeBSD__
    /* da wir ueber eine viel langsamere Schnittstelle gehen, ist es
       unnoetig, soviel Zeit zu verblasen. Wahrscheinlich reicht sogar
       ein einziger Schreibversuch. MAM */
    __ddl_s88->clockscale = 2;
#else
    __ddl_s88->clockscale = 35;
#endif

    __ddl_s88->refresh = 100;

#ifdef __FreeBSD__
    __ddl_s88->Fd = -1;         /* signal closed Port */
#endif

    strcpy(buses[busnumber].description, "FB POWER");
    __ddl_s88->number_fb[0] = 1;
    __ddl_s88->number_fb[1] = 1;
    __ddl_s88->number_fb[2] = 1;
    __ddl_s88->number_fb[3] = 1;

    for (i = 1; i < 4; i++) {
        strcpy(buses[busnumber + i].description, "FB");
        buses[busnumber + i].type = SERVER_S88;
        buses[busnumber + i].debuglevel = buses[busnumber].debuglevel;
        buses[busnumber + i].init_func = NULL;
        buses[busnumber + i].thr_func = &thr_sendrec_dummy;
        buses[busnumber + i].driverdata = NULL;
    }

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "ioport") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                /* better than atoi(3) */
                __ddl_s88->port = strtol((char *) txt, (char **) NULL, 0);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "clockscale") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->clockscale = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "refresh") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->refresh = atoi((char *) txt);
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

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_1") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[0] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_2") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[1] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_3") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[2] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_4") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[3] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }

    for (i = 0; i < 4; i++) {
        if (init_FB(busnumber + i, __ddl_s88->number_fb[i] * 16)) {
            __ddl_s88->number_fb[i] = 0;
            syslog_bus(busnumber + i, DBG_ERROR,
                       "Can't create array for s88-feedback "
                       "channel %d", i + 1);
        }
        else
            syslog_bus(busnumber + i, DBG_INFO,
                       "%d feeback contacts for channel %d successfully "
                       "initialized.", __ddl_s88->number_fb[i] * 16,
                       i + 1);
    }

    return (4);
}

/****************************************************************
* function s88init                                              *
*                                                               *
* purpose: test the parallel port for the s88bus and initializes*
*          the bus. The port address must be one of LPT_BASE, the *
*          port must be accessible through ioperm and there must*
*          be an real device at the address.                     *
*                                                               *
* in:      ---                                                  *
* out:     return value: 1 if testing and initializing was      *
*                        successful, otherwise 0               *
*                                                               *
* remarks: tested MW, 20.11.2000                                *
* bit ordering is changed from erddcd code! (MT)     *
*                                                               *
****************************************************************/
int init_bus_S88(bus_t busnumber)
{
    int result;
    unsigned int i;             /* loop counter */
    int isin = 0;               /* reminder for checking */
    int S88PORT = __ddl_s88->port;
    int S88CLOCK_SCALE = __ddl_s88->clockscale;

#ifdef linux
    syslog_bus(busnumber, DBG_INFO, "init_bus DDL(Linux) S88");
#else
#ifdef __FreeBSD__
    syslog_bus(busnumber, DBG_INFO, "init_bus DDL(FreeBSD) S88");
#endif
#endif
    /* is the port disabled from user, everything is fine */
    if (!S88PORT) {
        syslog_bus(busnumber, DBG_INFO, "s88 port is disabled.");
        return 1;
    }
    /* test, whether S88DEV is a valid io-address for a parallel device */
    for (i = 0; i < LPT_NUM; i++)
        isin = isin || (S88PORT == LPT_BASE[i]);
    if (isin) {
        /* test if port is accessible */
        result = ioperm(S88PORT, 3, 1);
        if (result == -1) {
            syslog_bus(busnumber, DBG_FATAL,
                       "ioperm() failed: %s (errno = %d).",
                       strerror(result), result);
            return 1;
        }
        else {
            /* test, whether there is a real device on the S88DEV-port
               by writing and reading data to the port. If the written
               data is returned, a real port is there
             */
            outb(0x00, S88PORT);
            isin = (inb(S88PORT) == 0);
            outb(0xFF, S88PORT);
            isin = (inb(S88PORT) == 0xFF) && isin;
            if (isin) {
                /* initialize the S88 by doing a reset */
                /* for ELEKTOR-Module the reset must be on the load line */
                S88_WRITE(S88_QUIET);
                S88_WRITE(S88_RESET & S88_LOAD);
                S88_WRITE(S88_QUIET);
            }
            else {
                syslog_bus(busnumber, DBG_WARN,
                           "Warning: There is no port for s88 at 0x%X.",
                           S88PORT);
                /* stop access to port address */
                ioperm(S88PORT, 3, 0);
                return 1;
            }
        }
    }
    else {
        syslog_bus(busnumber, DBG_WARN,
                   "Warning: 0x%X is not valid port address for s88 device.",
                   S88PORT);
        return 1;
    }
    syslog_bus(busnumber, DBG_INFO,
               "s88 port successfully initialized at 0x%X.", S88PORT);
    return 0;
}

/****************************************************************
* function s88load                                              *
*                                                               *
* purpose: Loads the data from the bus in s88data if the valid- *
*          time space S88REFRESH is over. Then also the new     *
*          validity-timeout is set to s88valid.                 *
*          If port is disabled or data is valid does nothing.   *
*                                                               *
* in:      ---                                                  *
* out:     ---                                                  *
*                                                               *
* remarks: tested MW, 20.11.2000                                *
*                                                               *
****************************************************************/
void s88load(bus_t busnumber)
{
    int i, j, k, inbyte;
    struct timeval nowtime;
    unsigned int s88data[S88_MAXPORTSB * S88_MAXBUSSES];        /* valid bus-data */
    int S88PORT = __ddl_s88->port;
    int S88CLOCK_SCALE = __ddl_s88->clockscale;
    int S88REFRESH = 1000 * __ddl_s88->refresh;

    gettimeofday(&nowtime, NULL);
    if ((nowtime.tv_sec > __ddl_s88->s88valid.tv_sec) ||
        ((nowtime.tv_sec == __ddl_s88->s88valid.tv_sec) &&
         (nowtime.tv_usec > __ddl_s88->s88valid.tv_usec))) {
        /* data is out of date - get new data from the bus */

        /* initialize the s88data array */
        memset(s88data, 0, sizeof(s88data));

        if (S88PORT) {
            /* if port is disabled do nothing */
            /* load the bus */
            ioperm(S88PORT, 3, 1);
            /*TODO: check ioperm return value (should be 0) */
            S88_WRITE(S88_LOAD);
            S88_WRITE(S88_LOAD | S88_CLOCK);
            S88_WRITE(S88_QUIET);
            S88_WRITE(S88_RESET);
            S88_WRITE(S88_QUIET);
            /* reading the data */
            for (j = 0; j < S88_MAXPORTSB; j++) {
                for (k = 0; k < 8; k++) {
                    /* reading from port */
                    inbyte = inb(S88PORT + 1);
                    /* interpreting the four buses */
                    if (inbyte & S88_DATA1)
                        s88data[j] += BIT_VALUES[k];
                    if (!(inbyte & S88_DATA2))
                        s88data[j + S88_MAXPORTSB] += BIT_VALUES[k];
                    if (inbyte & S88_DATA3)
                        s88data[j + 2 * S88_MAXPORTSB] += BIT_VALUES[k];
                    if (inbyte & S88_DATA4)
                        s88data[j + 3 * S88_MAXPORTSB] += BIT_VALUES[k];
                    /* getting the next data */
                    S88_WRITE(S88_CLOCK);
                    S88_WRITE(S88_QUIET);
                }
                if (j < __ddl_s88->number_fb[0] * 2)
                    setFBmodul(busnumber, j + 1, s88data[j]);
                if (j < __ddl_s88->number_fb[1] * 2)
                    setFBmodul(busnumber + 1, j + 1,
                               s88data[j + S88_MAXPORTSB]);
                if (j < __ddl_s88->number_fb[2] * 2)
                    setFBmodul(busnumber + 2, j + 1,
                               s88data[j + 2 * S88_MAXPORTSB]);
                if (j < __ddl_s88->number_fb[3] * 2)
                    setFBmodul(busnumber + 3, j + 1,
                               s88data[j + 3 * S88_MAXPORTSB]);
            }
            nowtime.tv_usec += S88REFRESH;
            __ddl_s88->s88valid.tv_usec = nowtime.tv_usec % 1000000;
            __ddl_s88->s88valid.tv_sec =
                nowtime.tv_sec + nowtime.tv_usec / 1000000;
        }
    }
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "DDL-S88 bus terminated.");

#ifdef __FreeBSD__
    if (((DDL_S88_DATA *) buses[btd->bus].driverdata)->Fd != -1)
        close(((DDL_S88_DATA *) buses[btd->bus].driverdata)->Fd);
#endif

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

void *thr_sendrec_S88(void *v)
{
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

    unsigned long int sleepusec = 100000;

    int S88REFRESH =
        ((DDL_S88_DATA *) buses[btd->bus].driverdata)->refresh;
    /* set refresh-cycle */
    if (sleepusec < S88REFRESH * 1000)
        sleepusec = S88REFRESH * 1000;

    syslog_bus(btd->bus, DBG_INFO, "DDL_S88 bus startet (device = %04x).",
               __ddl_s88t->port);

    while (1) {
        if (buses[btd->bus].power_changed == 1) {
            char msg[110];
            buses[btd->bus].power_changed = 0;
            infoPower(btd->bus, msg);
            enqueueInfoMessage(msg);
        }

        /*do nothing if power is off */
        if (buses[btd->bus].power_state == 0) {
            if (usleep(1000) == -1) {
                syslog_bus(btd->bus, DBG_ERROR,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            continue;
        }

        check_reset_fb(btd->bus);
        if (usleep(sleepusec) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        s88load(btd->bus);
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}

void *thr_sendrec_dummy(void *v)
{
    int result;

    while (true) {
        result = sleep(1);
        if (result != 0) {
            syslog_bus(0, DBG_ERROR,
                       "sleep() interrupted, %d seconds left\n", result);
        }
    }
}

/*---------------------------------------------------------------------------
 * End of Linux Code
 *---------------------------------------------------------------------------*/
#ifdef __FreeBSD__
/*---------------------------------------------------------------------------
 * Start of FreeBSD Emulation of ioperm, inb and outb
 * MAM 01/06/03
 *---------------------------------------------------------------------------*/

int FBSD_ioperm(int Port, int KeineAhnung, int DesiredAccess,
                bus_t busnumber)
{
    int i;
    int found = 0;
    char DevName[256];
    int Fd;


    /* Simple: should be closed  ? */
    if (DesiredAccess == 0) {
        if (__ddl_s88->Fd != -1) {
            close(__ddl_s88->Fd);
            syslog_bus(busnumber, DBG_DEBUG,
                       "FBSD DDL-S88 closing port %04X", Port);
        }
        else {
            syslog_bus(busnumber, DBG_WARN,
                       "FBSD DDL-S88 closing NOT OPEN port %04X", Port);
        }
        __ddl_s88->Fd = -1;
        return 0;
    }

    /* is already open??? */
    if (__ddl_s88->Fd != -1) {
        /* syslog_bus(busnumber, DBG_INFO,  "FBSD DDL-S88 trying to re-open port %04X (ignored)",Port); */

        return 0;               /* gracious ignoring */
    }

    /* Also oeffnen, das ist schon trickreicher :-) */
    /* Erst einmal rausbekommen, welches Device denn gemeint war
     * Dazu muessen wir einfach die Portposition aus dem Array
     * ermitteln
     */

    __ddl_s88->Fd = -1;         /* traue keinem :-) */

    for (i = 0; i < LPT_NUM; i++) {
        if (Port == LPT_BASE[i]) {
            found = 1;
            break;
        }
    }
    if (found == 0)
        return -1;

    snprintf(DevName, sizeof(DevName), "/dev/ppi%d", i);

    Fd = open(DevName, O_RDWR);

    if (Fd < 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "FBSD DDL-S88 open port %04X on %s "
                   "failed: %s (errno = %d).\n", Port, DevName,
                   strerror(errno), errno);
        return Fd;
    }
    syslog_bus(busnumber, DBG_INFO,
               "FBSD DDL-S88 success opening port %04X on %s", Port,
               DevName);

    __ddl_s88->Fd = Fd;
    return 0;
}


/* Look out! Manchmal wird das Datenport, manchmal die Steuer */
/* leitungen angesprochen! */
unsigned char FBSD_inb(int Woher, bus_t busnumber)
{
    int result;
    unsigned char i = 0;
    int WelchesPort;
    int WelcherIoctl;

    /* syslog_bus(busnumber, DBG_DEBUG, "FBSD DDL-S88 InB start on port %04X",Woher); */
    if (__ddl_s88->Fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "FBSD DDL-S88 Device not open for reading");
        exit(1);
    }

    WelchesPort = Woher - __ddl_s88->port;

    switch (WelchesPort) {
        case 0:
            WelcherIoctl = PPIGDATA;
            break;
        case 1:
            WelcherIoctl = PPIGSTATUS;
            break;
        case 2:
            WelcherIoctl = PPIGCTRL;
            break;
        default:
            syslog_bus(busnumber, DBG_FATAL, "FBSD DDL-S88 Read access to "
                       "port %04X requested, not applicable!", Woher);
            return 0;
    }
    result = ioctl(__ddl_s88->Fd, WelcherIoctl, &i);
    if (result == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }
    /* syslog_bus(busnumber, DBG_DEBUG, "FBSD DDL-S88 InB finished Data %02X",i); */
    return i;
}

/* Find ioctl() matching the parallel port address */
unsigned char FBSD_outb(unsigned char Data, int Wohin, bus_t busnumber)
{
    int result;
    int WelchesPort;
    int WelcherIoctl;

    /* syslog_bus(busnumber, DBG_DEBUG, "FBSD DDL-S88 OutB %d on Port %04X",Data,Wohin); */
    if (__ddl_s88->Fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "FBSD DDL-S88 Device not open for writing byte %d",
                   Data);
        exit(1);
        return -1;
    }

    WelchesPort = Wohin - __ddl_s88->port;

    switch (WelchesPort) {
        case 0:
            WelcherIoctl = PPISDATA;
            break;
        case 1:
            WelcherIoctl = PPISSTATUS;
            break;
        case 2:
            WelcherIoctl = PPISCTRL;
            break;
        default:
            syslog_bus(busnumber, DBG_FATAL,
                       "FBSD DDL-S88 Write access to "
                       "port %04X requested, not applicable!", Wohin);
            return 0;
    }
    result = ioctl(__ddl_s88->Fd, WelcherIoctl, &Data);
    if (result == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }
    /* syslog_bus(busnumber, DBG_DEBUG, "FBSD DDL-S88 OutB finished"); */
    return Data;
}

#endif
#endif
