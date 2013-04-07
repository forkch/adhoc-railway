/***************************************************************************
                         hsi-88.c  -  description
                            -------------------
   begin                : Mon Oct 29 2001
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

#include <errno.h>
#include <fcntl.h>
#include <string.h>
#include <unistd.h>

#include "config-srcpd.h"
#include "hsi-88.h"
#include "io.h"
#include "srcp-fb.h"
#include "syslogmessage.h"

#define __hsi ((HSI_88_DATA*)buses[busnumber].driverdata)

static int working_HSI88;

int readConfig_HSI_88(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    int number;

    buses[busnumber].driverdata = malloc(sizeof(struct _HSI_88_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_HSI_88;
    buses[busnumber].init_func = &init_bus_HSI_88;
    buses[busnumber].thr_func = &thr_sendrec_HSI_88;
    buses[busnumber].flags |= FB_ORDER_0;
    buses[busnumber].flags |= FB_16_PORTS;
    strcpy(buses[busnumber].description, "FB POWER");
    __hsi->refresh = 10000;
    __hsi->number_fb[0] = 0;
    __hsi->number_fb[1] = 0;
    __hsi->number_fb[2] = 0;

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "refresh") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __hsi->refresh = atoi((char *) txt);
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

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_left") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __hsi->number_fb[0] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_center") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __hsi->number_fb[1] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_right") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __hsi->number_fb[2] = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);
        ;

        child = child->next;
    }

    /*  create an array for feedbacks; */
    number = __hsi->number_fb[0];
    number += __hsi->number_fb[1];
    number += __hsi->number_fb[2];

    if (init_FB(busnumber, number * 16)) {
        __hsi->number_fb[0] = 0;
        __hsi->number_fb[1] = 0;
        __hsi->number_fb[2] = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }

    return (1);
}

static int open_lineHSI88(bus_t bus, char *name)
{
    int fd;
    struct termios interface;

    syslog_bus(bus, DBG_DEBUG,
               "try opening serial line %s for 9600 baud\n", name);
    fd = open(name, O_RDWR);
    if (fd == -1) {
        syslog_bus(bus, DBG_ERROR,
                   "Open serial line failed: %s (errno = %d).\n",
                   strerror(errno), errno);
    }
    else {
        tcgetattr(fd, &interface);
        interface.c_oflag = ONOCR;
        interface.c_cflag =
            CS8 | CRTSCTS | CSTOPB | CLOCAL | CREAD | HUPCL;
        interface.c_iflag = IGNBRK;
        interface.c_lflag = IEXTEN;
        cfsetispeed(&interface, B9600);
        cfsetospeed(&interface, B9600);
        interface.c_cc[VMIN] = 0;
        interface.c_cc[VTIME] = 1;
        tcsetattr(fd, TCSANOW, &interface);
    }
    return fd;
}

static int init_lineHSI88(bus_t busnumber, int modules_left,
                          int modules_center, int modules_right)
{
    int status;
    int i;
    int ctr;
    int result;
    unsigned char byte2send;
    unsigned char rr;

    result = sleep(1);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "sleep() interrupted, %d seconds left\n", result);
    }

    byte2send = 0x0d;
    for (i = 0; i < 10; i++)
        writeByte(busnumber, byte2send, 500);

    while (readByte(busnumber, 0, &rr) == 0) {
    }

    /*  HSI-88 initialize */
    /*  switch off terminal-mode */
    i = 1;
    while (i) {
        byte2send = 't';
        writeByte(busnumber, byte2send, 0);
        byte2send = 0x0d;
        writeByte(busnumber, byte2send, 200);
        rr = 0;
        ctr = 0;
        status = readByte(busnumber, 0, &rr);
        while (rr != 't') {
            if (usleep(100000) == -1) {
                syslog_bus(busnumber, DBG_ERROR,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            status = readByte(busnumber, 0, &rr);
            if (status == -1)
                ctr++;
            if (ctr > 20)
                return -1;
        }
        readByte(busnumber, 0, &rr);
        if (rr == '0')
            i = 0;
        readByte(busnumber, 0, &rr);
    }
    /*  looking for version of HSI */
    byte2send = 'v';
    writeByte(busnumber, byte2send, 0);
    byte2send = 0x0d;
    writeByte(busnumber, byte2send, 500);

    for (i = 0; i < 49; i++) {
        status = readByte(busnumber, 1, &rr);
        if (status == -1)
            break;
        __hsi->v_text[i] = (char) rr;
    }
    __hsi->v_text[i] = 0x00;
    syslog_bus(busnumber, DBG_DEBUG, "HSI version: %s\n", __hsi->v_text);

    status = 1;
    while (status) {
        /*  initialise module-numbers */
        /*  up to "GO", non feedback-module */
        byte2send = 's';
        writeByte(busnumber, byte2send, 0);
        byte2send = 0;
        writeByte(busnumber, byte2send, 0);
        writeByte(busnumber, byte2send, 0);
        writeByte(busnumber, byte2send, 0);
        byte2send = 0x0d;
        writeByte(busnumber, byte2send, 0);
        byte2send = 0x0d;
        writeByte(busnumber, byte2send, 0);
        byte2send = 0x0d;
        writeByte(busnumber, byte2send, 0);
        byte2send = 0x0d;
        writeByte(busnumber, byte2send, 5);

        rr = 0;
        readByte(busnumber, 1, &rr);    /*  read answer (three bytes) */
        while (rr != 's') {
            if (usleep(100000) == -1) {
                syslog_bus(busnumber, DBG_ERROR,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            readByte(busnumber, 0, &rr);
        }
        readByte(busnumber, 0, &rr);    /* number of given modules */
        status = 0;
    }
    return 0;
}

int init_bus_HSI_88(bus_t busnumber)
{
    int fd;
    int status;
    int anzahl;

    status = 0;
    syslog_bus(busnumber, DBG_DEBUG, "HSI-88 with debuglevel %d\n",
               buses[busnumber].debuglevel);
    if (buses[busnumber].type != SERVER_HSI_88) {
        status = -2;
    }
    else {
        if (buses[busnumber].device.file.fd > 0)
            status = -3;        /* bus is already in use */
    }

    if (status == 0) {
        working_HSI88 = 0;
        anzahl = __hsi->number_fb[0];
        anzahl += __hsi->number_fb[1];
        anzahl += __hsi->number_fb[2];

        if (anzahl > 31) {
            syslog_bus(busnumber, DBG_ERROR,
                       "Number of feedback-modules greater than 31!");
            status = -4;
        }
    }

    if (buses[busnumber].debuglevel < 7) {
        if (status == 0) {
            fd = open_lineHSI88(busnumber,
                                buses[busnumber].device.file.path);
            if (fd > 0) {
                buses[busnumber].device.file.fd = fd;
                status = init_lineHSI88(busnumber, __hsi->number_fb[0],
                                        __hsi->number_fb[1],
                                        __hsi->number_fb[2]);
            }
            else
                status = -5;
        }
    }
    else
        buses[busnumber].device.file.fd = 9999;
    if (status == 0)
        working_HSI88 = 1;

    syslog_bus(busnumber, DBG_DEBUG, "INIT_BUS_HSI with code: %d\n",
               status);
    return status;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "HSI-88 bus terminated.");
    working_HSI88 = 0;
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

void *thr_sendrec_HSI_88(void *v)
{
    int refresh_time;
    int anzahl, i, temp;
    unsigned char byte2send;
    unsigned char rr;
    int status;
    int result;
    int zaehler1, fb_zaehler1, fb_zaehler2;
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

    syslog_bus(btd->bus, DBG_INFO, "HSI-88 bus startet (device = %s).",
               buses[btd->bus].device.file.path);

    refresh_time = ((HSI_88_DATA *) buses[btd->bus].driverdata)->refresh;

    zaehler1 = 0;
    fb_zaehler1 = 0;
    fb_zaehler2 = 1;
    i = 0;
    temp = 1;
    if (buses[btd->bus].debuglevel <= DBG_DEBUG) {
        status = 1;
        while (status) {
            /* Modulbelegung initialisieren */
            byte2send = 's';
            writeByte(btd->bus, byte2send, 0);
            byte2send =
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[0];
            writeByte(btd->bus, byte2send, 0);
            byte2send =
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[1];
            writeByte(btd->bus, byte2send, 0);
            byte2send =
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[2];
            writeByte(btd->bus, byte2send, 0);
            byte2send = 0x0d;
            writeByte(btd->bus, byte2send, 0);
            byte2send = 0x0d;
            writeByte(btd->bus, byte2send, 0);
            byte2send = 0x0d;
            writeByte(btd->bus, byte2send, 0);
            byte2send = 0x0d;
            writeByte(btd->bus, byte2send, 200);

            rr = 0;
            readByte(btd->bus, 0, &rr); /*  read answer (three bytes) */
            while (rr != 's') {
                if (usleep(100000) == -1) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                readByte(btd->bus, 0, &rr);
            }
            readByte(btd->bus, 0, &rr); /*  number of given modules */
            anzahl = (int) rr;
            syslog_bus(btd->bus, DBG_INFO, "Number of modules: %i",
                       anzahl);
            anzahl -=
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[0];
            anzahl -=
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[1];
            anzahl -=
                ((HSI_88_DATA *) buses[btd->bus].driverdata)->number_fb[2];

            /* HSI initialisation correct? */
            if (anzahl == 0) {
                status = 0;
            }
            else {
                syslog_bus(btd->bus, DBG_ERROR,
                           "error while initialising");

                result = sleep(1);
                if (result != 0) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "sleep() interrupted, %d seconds left\n",
                               result);
                }

                while (readByte(btd->bus, 0, &rr) == 0) {
                }
            }
        }
    }

    while (true) {
        pthread_testcancel();
        if (buses[btd->bus].debuglevel <= DBG_DEBUG) {
            rr = 0;
            while (rr != 'i') {
                /* first check here for reset of feedbacks
                   (do this check at the end, we will not run, until
                   get new changes from HSI) */
                check_reset_fb(btd->bus);
                if (usleep(refresh_time) == -1) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                readByte(btd->bus, 0, &rr);
            }
            readByte(btd->bus, 1, &rr); /* number of given modules */
            anzahl = (int) rr;
            for (zaehler1 = 0; zaehler1 < anzahl; zaehler1++) {
                readByte(btd->bus, 1, &rr);
                i = rr;
                readByte(btd->bus, 1, &rr);
                temp = rr;
                temp <<= 8;
                readByte(btd->bus, 1, &rr);
                setFBmodul(btd->bus, i, temp | rr);
                syslog_bus(btd->bus, DBG_DEBUG, "feedback %i with 0x%04x",
                           i, temp | rr);
            }
            readByte(btd->bus, 1, &rr); /* <CR> */
        }
        else {                  /* only for testing */
            setFBmodul(btd->bus, 1, temp);
            i++;
            temp <<= 1;
            if (i > 16) {
                i = 0;
                temp = 1;
            }
            result = sleep(2);
            if (result != 0) {
                syslog_bus(btd->bus, DBG_ERROR,
                           "sleep() interrupted, %d seconds left\n",
                           result);
            }
        }
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
