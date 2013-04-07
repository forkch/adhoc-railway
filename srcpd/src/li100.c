/***************************************************************************
                      li100.c  -  description
                        -------------------
begin                : Tue Jan 22 11:35:13 CEST 2002
copyright            : (C) 2002-2007 by Dipl.-Ing. Frank Schmischke
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
#include <sys/ioctl.h>
#include <unistd.h>

#ifdef __CYGWIN__
#include <sys/socket.h>         /*for FIONREAD */
#endif
#ifdef __sun__
#include <sys/filio.h>
#endif
#include "syslogmessage.h"


#ifdef LI100_USB
static int readAnswer_LI100_USB(bus_t busnumber);
static int initLine_LI100_USB(bus_t busnumber);
#else
static int readAnswer_LI100_SERIAL(bus_t busnumber);
static int initLine_LI100_SERIAL(bus_t busnumber);
#endif

#ifdef LI100_USB
int readConfig_LI100_USB(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
#else
int readConfig_LI100_SERIAL(xmlDocPtr doc, xmlNodePtr node,
                            bus_t busnumber)
#endif
{
#ifdef LI100_USB
    syslog_bus(busnumber, DBG_INFO,
               "reading configuration for LI100 (usb) at bus #%ld",
               busnumber);
#else
    syslog_bus(busnumber, DBG_INFO,
               "reading configuration for LI100 (serial) at bus #%ld",
               busnumber);
#endif
    buses[busnumber].driverdata = malloc(sizeof(struct _LI100_DATA));
    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

#ifdef LI100_USB
    buses[busnumber].type = SERVER_LI100_USB;
    buses[busnumber].init_func = &init_bus_LI100_USB;
    buses[busnumber].thr_func = &thr_sendrec_LI100_USB;
    buses[busnumber].device.file.baudrate = B57600;
#else
    buses[busnumber].type = SERVER_LI100_SERIAL;
    buses[busnumber].init_func = &init_bus_LI100_SERIAL;
    buses[busnumber].thr_func = &thr_sendrec_LI100_SERIAL;
    buses[busnumber].device.file.baudrate = B9600;
#endif
    buses[busnumber].init_gl_func = &init_gl_LI100;
    buses[busnumber].init_ga_func = &init_ga_LI100;
    buses[busnumber].flags |= FB_4_PORTS;
    buses[busnumber].flags |= FB_ORDER_0;
    strcpy(buses[busnumber].description,
           "GA GL FB SM POWER LOCK DESCRIPTION");

#ifdef LI100_USB
    __li100->number_fb = 512;
    __li100->number_ga = 1024;
    __li100->number_gl = 9999;
#else
    __li100->number_fb = 256;
    __li100->number_ga = 256;
    __li100->number_gl = 99;
#endif

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
                __li100->number_fb = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __li100->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __li100->number_ga = atoi((char *) txt);
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
        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);
        child = child->next;
    }

    return (1);
}

#ifdef LI100_USB
int init_bus_LI100_USB(bus_t busnumber)
#else
int init_bus_LI100_SERIAL(bus_t busnumber)
#endif
{
    int status;
    int i;
    unsigned char byte2send[20];
    static char *protocols = "N";
    buses[busnumber].protocols = protocols;

    if (init_GA(busnumber, __li100->number_ga)) {
        __li100->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessories");
    }

    if (init_GL(busnumber, __li100->number_gl)) {
        __li100->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for locomotives");
    }

    syslog_bus(busnumber, DBG_WARN, "debug array for locomotives");

    if (init_FB(busnumber, __li100->number_fb * 8)) {
        __li100->number_fb = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }

    status = 0;
    syslog_bus(busnumber, DBG_DEBUG,
               "Lenz interface with debug level %d\n",
               buses[busnumber].debuglevel);

#ifdef LI100_USB
    if (buses[busnumber].type != SERVER_LI100_USB)
#else
    if (buses[busnumber].type != SERVER_LI100_SERIAL)
#endif
    {
        status = -2;
    }
    else {
        if (buses[busnumber].device.file.fd > 0)
            status = -3;        /* bus is already in use */
    }

    if (status == 0) {
        __li100->working_LI100 = 0;
    }

    if (buses[busnumber].debuglevel < 7) {
        if (status == 0)
#ifdef LI100_USB
            status = initLine_LI100_USB(busnumber);
#else
            status = initLine_LI100_SERIAL(busnumber);
#endif
    }
    else
        buses[busnumber].device.file.fd = 9999;

    if (status == 0) {
        __li100->working_LI100 = 1;
        syslog_bus(busnumber, DBG_INFO, "Version Lenz interface: %d.%d\n",
                   __li100->version_interface / 256,
                   __li100->version_interface % 256);
        syslog_bus(busnumber, DBG_INFO, "Code Lenz interface: %d%d\n",
                   __li100->code_interface / 16,
                   __li100->code_interface % 16);
        syslog_bus(busnumber, DBG_INFO,
                   "Version Lenz central unit: %d.%d\n",
                   __li100->version_zentrale / 256,
                   __li100->version_zentrale % 256);
        /* printf("Code LENZ-Central unit: %d",__li100->code_zentrale); */
        __li100->get_addr = 0;

        /* if version of central unit is greater than 3.0, cleanup stack */
        if (__li100->version_zentrale >= 0x0300) {
            for (;;) {
                byte2send[0] = 0xe3;
                byte2send[1] = 0x05;
                byte2send[2] = 0;
                byte2send[3] = 0;

#ifdef LI100_USB
                send_command_LI100_USB(busnumber, byte2send);
#else
                send_command_LI100_SERIAL(busnumber, byte2send);
#endif

                if (__li100->get_addr == 0)
                    break;

                syslog_bus(busnumber, DBG_INFO,
                           "Remove engine with address %d from stack\n",
                           __li100->get_addr & 0x3fff);
                byte2send[0] = 0xe3;
                byte2send[1] = 0x44;
                byte2send[2] = (unsigned char) (__li100->get_addr >> 8);
                byte2send[3] =
                    (unsigned char) (__li100->get_addr & 0x00FF);

#ifdef LI100_USB
                send_command_LI100_USB(busnumber, byte2send);
#else
                send_command_LI100_SERIAL(busnumber, byte2send);
#endif
            }
        }

        /* read all feedbacks for first time */
        for (i = 0; i < __li100->number_fb; i++) {
            /* read bit 0..3 */
            byte2send[0] = 0x42;
            byte2send[1] = i;
            byte2send[2] = 0x80;
#ifdef LI100_USB
            send_command_LI100_USB(busnumber, byte2send);
#else
            send_command_LI100_SERIAL(busnumber, byte2send);
#endif
            /* read bit 4..7 */
            byte2send[0] = 0x42;
            byte2send[1] = i;
            byte2send[2] = 0x81;
#ifdef LI100_USB
            send_command_LI100_USB(busnumber, byte2send);
#else
            send_command_LI100_SERIAL(busnumber, byte2send);
#endif
        }
    }

#ifdef LI100_USB
    syslog_bus(busnumber, DBG_DEBUG, "INIT_BUS_LI100 (usb) finished with "
               "code: %d\n", status);
#else
    syslog_bus(busnumber, DBG_DEBUG,
               "INIT_BUS_LI100 (serial) finished with code: %d\n", status);
#endif
    __li100->last_type = -1;
    __li100->last_value = -1;
    __li100->emergency_on_LI100 = 0;
    __li100->extern_engine_ctr = 0;
    for (i = 0; i < 100; i++)
        __li100->extern_engine[i] = -1;
    __li100->pgm_mode = 0;

    return status;
}

/*thread cleanup routine for this bus*/
#ifdef LI100_USB
static void end_bus_usb_thread(bus_thread_t * btd)
#else
static void end_bus_rs232_thread(bus_thread_t * btd)
#endif
{
    int result;

#ifdef LI100_USB
    syslog_bus(btd->bus, DBG_INFO, "LI100 bus (usb) terminated.");
#else
    syslog_bus(btd->bus, DBG_INFO, "LI100 bus (serial) terminated.");
#endif
    __li100t->working_LI100 = 0;
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

#ifdef LI100_USB
void *thr_sendrec_LI100_USB(void *v)
#else
void *thr_sendrec_LI100_SERIAL(void *v)
#endif
{
    unsigned char byte2send[20];
    int status;
    int zaehler1, fb_zaehler1, fb_zaehler2;
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));
    if (btd == NULL)
        pthread_exit((void *) 1);
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);


#ifdef LI100_USB
    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_usb_thread, (void *) btd);
    syslog_bus(btd->bus, DBG_INFO, "LI100 bus (usb) started.");
#else
    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_rs232_thread, (void *) btd);
    syslog_bus(btd->bus, DBG_INFO, "LI100 bus (serial) started.");
#endif

    /* initialize tga-structure */
    for (zaehler1 = 0; zaehler1 < 50; zaehler1++)
        __li100t->tga[zaehler1].id = 0;
    fb_zaehler1 = 0;
    fb_zaehler2 = 1;

    while (1) {
        pthread_testcancel();
        /* syslog(LOG_INFO, "thr_sendrec_LI100 Start in loop"); */
        /* Start/Stop */
        /* fprintf(stderr, "START/STOP... "); */

        if (buses[btd->bus].power_changed == 1) {
            byte2send[0] = 0x21;
            byte2send[1] = buses[btd->bus].power_state ? 0x81 : 0x80;
#ifdef LI100_USB
            status = send_command_LI100_USB(btd->bus, byte2send);
#else
            status = send_command_LI100_SERIAL(btd->bus, byte2send);
#endif

            if (status == 0)    /* all was OK ? */
                buses[btd->bus].power_changed = 0;
        }

#ifdef LI100_USB
        send_command_gl_LI100_USB(btd->bus);
        send_command_ga_LI100_USB(btd->bus);
        check_status_LI100_USB(btd->bus);
        check_extern_engines_USB(btd->bus);
        send_command_sm_LI100_USB(btd->bus);
#else
        send_command_gl_LI100_SERIAL(btd->bus);
        send_command_ga_LI100_SERIAL(btd->bus);
        check_status_LI100_SERIAL(btd->bus);
        check_extern_engines_SERIAL(btd->bus);
        send_command_sm_LI100_SERIAL(btd->bus);
#endif
        check_reset_fb(btd->bus);
        buses[btd->bus].watchdog = 1;
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

#ifdef LI100_USB
void send_command_ga_LI100_USB(bus_t busnumber)
#else
void send_command_ga_LI100_SERIAL(bus_t busnumber)
#endif
{
    int i, i1;
    unsigned int temp;
    unsigned char byte2send[20];
    unsigned char status;

    struct timeval akt_time, cmp_time;

    gettimeofday(&akt_time, NULL);

    /* first eventually Decoder switch off */
    for (i = 0; i < 50; i++) {
        if (__li100->tga[i].id > 0) {
            syslog_bus(busnumber, DBG_DEBUG, "time %i,%i",
                       (int) akt_time.tv_sec, (int) akt_time.tv_usec);
            cmp_time = __li100->tga[i].t;

            /* switch off time reached? */
            if (cmpTime(&cmp_time, &akt_time)) {
                ga_state_t delayedga = __li100->tga[i];

                /*align SRCP address (1..2048) to li100 address (0..2047)*/
                temp = delayedga.id - 1;
                byte2send[0] = 0x52;
                byte2send[1] = temp >> 2;
                byte2send[2] = 0x80;
                temp &= 0x03;
                temp <<= 1;
                byte2send[2] |= temp;

                if (delayedga.port > 0) {
                    byte2send[2] |= 0x01;
                }

#ifdef LI100_USB
                send_command_LI100_USB(busnumber, byte2send);
#else
                send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                delayedga.action = 0;
                setGA(busnumber, delayedga.id, delayedga);
                __li100->tga[i].id = 0;
            }
        }
    }

    /* Decoder switch on */
    if (!queue_GA_isempty(busnumber)) {

        ga_state_t gatmp;
        dequeueNextGA(busnumber, &gatmp);

        /*align SRCP address (1..2048) to li100 address (0..2047)*/
        temp = gatmp.id - 1;
        byte2send[0] = 0x52;
        byte2send[1] = temp >> 2;
        byte2send[2] = 0x80;
        temp &= 0x03;
        temp <<= 1;
        byte2send[2] |= temp;

        if (gatmp.action > 0) {
            byte2send[2] |= 0x08;
        }
        if (gatmp.port > 0) {
            byte2send[2] |= 0x01;
        }

#ifdef LI100_USB
        send_command_LI100_USB(busnumber, byte2send);
#else
        send_command_LI100_SERIAL(busnumber, byte2send);
#endif

        status = 1;

        /*add GA to empty place of delayed switch back queue*/
        if ((gatmp.action > 0) && (gatmp.activetime > 0)) {
            for (i1 = 0; i1 < 50; i1++) {
                if (__li100->tga[i1].id == 0) {
                    gatmp.t = akt_time;
                    gatmp.t.tv_sec += gatmp.activetime / 1000;
                    gatmp.t.tv_usec += (gatmp.activetime % 1000) * 1000;

                    if (gatmp.t.tv_usec > 1000000) {
                        gatmp.t.tv_sec++;
                        gatmp.t.tv_usec -= 1000000;
                    }

                    __li100->tga[i1] = gatmp;

                    status = 0;
                    syslog_bus(busnumber, DBG_DEBUG,
                               "GA %i for switch off at %i,%i on %i",
                               __li100->tga[i1].id,
                               (int) __li100->tga[i1].t.tv_sec,
                               (int) __li100->tga[i1].t.tv_usec, i1);
                    break;
                }
            }
        }

        if (status) {
            setGA(busnumber, gatmp.id, gatmp);
        }
    }
}

#ifdef LI100_USB
void send_command_gl_LI100_USB(bus_t busnumber)
#else
void send_command_gl_LI100_SERIAL(bus_t busnumber)
#endif
{
    int temp;
    int addr;
    unsigned char byte2send[20];
    int status = -1;

    gl_state_t gltmp, glakt;

    /* Locomotive decoder */
    /* fprintf(stderr, "LOK's... "); */
    /* nur senden, wenn wirklich etwas vorliegt */

    if (!queue_GL_isempty(busnumber)) {
        dequeueNextGL(busnumber, &gltmp);
        addr = gltmp.id;
        cacheGetGL(busnumber, addr, &glakt);

        /* speed, direction or function changed? */

        if ((gltmp.direction != glakt.direction) ||
            (gltmp.speed != glakt.speed) || (gltmp.funcs != glakt.funcs)) {
            /* Lokkommando soll gesendet werden */
            /* emergency stop for one locomotive */

            if (gltmp.direction == 2) {
                if (__li100->version_zentrale >= 0x0300) {
                    byte2send[0] = 0x92;
                    byte2send[1] = addr >> 8;

                    if (addr > 99)
                        byte2send[1] |= 0xc0;

                    byte2send[2] = addr & 0xff;
                }
                else {
                    byte2send[0] = 0x91;
                    byte2send[1] = addr;
                }

#ifdef LI100_USB
                status = send_command_LI100_USB(busnumber, byte2send);
#else
                status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif
            }
            else {
#ifndef LI100_USB
                /* version <= 1.50 */
                if (__li100->version_zentrale <= 0x0150) {
                    byte2send[0] = 0xb3;
                    /* address */
                    byte2send[1] = gltmp.id;
                    /* setting direction and speed */
                    byte2send[2] = gltmp.speed;

                    if (gltmp.speed > 0)
                        byte2send[2]++;
                    if (gltmp.direction) {
                        byte2send[2] |= 0x40;
                    }
                    if (gltmp.funcs & 1) {
                        byte2send[2] |= 0x20;
                    }

                    /* functions f1..f4 */
                    byte2send[3] = (gltmp.funcs >> 1) & 0x000F;
                    status =
                        send_command_LI100_SERIAL(busnumber, byte2send);
                }

                /* version > 1.50 + < 3.00 */
                else if (__li100->version_zentrale < 0x0300) {
                    byte2send[0] = 0xb4;
                    /* address */
                    byte2send[1] = gltmp.id;
                    /* mode */

                    switch (gltmp.n_fs) {
                        case 14:
                            byte2send[4] = 0x00;
                            /* setting direction and speed */
                            byte2send[2] = gltmp.speed;
                            if (gltmp.speed > 0)
                                byte2send[2]++;
                            if (gltmp.direction) {
                                byte2send[2] |= 0x40;
                            }
                            if (gltmp.funcs & 1) {
                                byte2send[2] |= 0x20;
                            }
                            break;
                        case 27:
                            byte2send[4] = 0x01;
                            /* setting direction and speed */
                            byte2send[2] = gltmp.speed;
                            if (gltmp.speed > 0)
                                byte2send[2] += 3;
                            if (byte2send[2] & 0x01)
                                byte2send[2] |= 0x20;
                            byte2send[2] >>= 1;
                            if (gltmp.direction) {
                                byte2send[2] |= 0x40;
                            }
                            if (gltmp.funcs & 1) {
                                byte2send[2] |= 0x20;
                            }
                            break;
                        case 28:
                            byte2send[4] = 0x02;
                            /* setting direction and speed */
                            byte2send[2] = gltmp.speed;
                            if (gltmp.speed > 0)
                                byte2send[2] += 3;
                            if (byte2send[2] & 0x01)
                                byte2send[2] |= 0x20;
                            byte2send[2] >>= 1;
                            if (gltmp.direction) {
                                byte2send[2] |= 0x40;
                            }
                            if (gltmp.funcs & 1) {
                                byte2send[2] |= 0x20;
                            }
                            break;
                        default:
                            byte2send[4] = 0x00;
                            /* setting direction and speed */
                            byte2send[2] = gltmp.speed;
                            if (gltmp.speed > 0)
                                byte2send[2]++;
                            if (gltmp.direction) {
                                byte2send[2] |= 0x40;
                            }
                            if (gltmp.funcs & 1) {
                                byte2send[2] |= 0x20;
                            }
                    }

                    /* functions f1..f4 */
                    byte2send[3] = (gltmp.funcs >> 1) & 0x000F;
                    status =
                        send_command_LI100_SERIAL(busnumber, byte2send);
                }

                /* version > 3.00 */
                /*else if (__li100->version_zentrale >= 0x0300) {*/
                else {
#endif
                    byte2send[0] = 0xe4;
                    /* mode */

                    switch (gltmp.n_fs) {
                        case 14:
                            byte2send[1] = 0x10;
                            break;
                        case 27:
                            byte2send[1] = 0x11;
                            break;
                        case 28:
                            byte2send[1] = 0x12;
                            break;
                        case 126:
                            byte2send[1] = 0x13;
                            break;
                        default:
                            byte2send[1] = 0x12;
                    }

                    /* high byte of address */
                    temp = gltmp.id;
                    temp >>= 8;
                    byte2send[2] = temp;
                    if (addr > 99)
                        byte2send[2] |= 0xc0;

                    /* low byte of address */
                    temp = gltmp.id;
                    temp &= 0x00FF;
                    byte2send[3] = temp;

                    /* setting direction and speed */
                    byte2send[4] = gltmp.speed;
                    if ((gltmp.n_fs == 27) || (gltmp.n_fs = 28)) {
                        if (gltmp.speed > 0)
                            byte2send[4] += 3;
                        if (byte2send[4] & 0x01)
                            byte2send[4] |= 0x20;
                        byte2send[4] >>= 1;
                    }
                    else {
                        if (gltmp.speed > 0)
                            byte2send[4]++;
                    }

                    if (gltmp.direction) {
                        byte2send[4] |= 0x80;
                    }

#ifdef LI100_USB
                    status = send_command_LI100_USB(busnumber, byte2send);
#else
                    status =
                        send_command_LI100_SERIAL(busnumber, byte2send);
#endif

                    /* send functions */
                    byte2send[0] = 0xe4;

                    /* function group 1: f0..f4 */
                    byte2send[1] = 0x20;

                    /* high byte of address */
                    temp = gltmp.id;
                    temp >>= 8;
                    byte2send[2] = temp;
                    if (addr > 99)
                        byte2send[2] |= 0xc0;

                    /* low byte of address */
                    temp = gltmp.id;
                    temp &= 0x00FF;
                    byte2send[3] = temp;

                    /* setting F0-F4, map: 0 0 0 F0 F4 F3 F2 F1 */
                    byte2send[4] = (gltmp.funcs >> 1) & 0x00FF;
                    if (gltmp.funcs & 1) {
                        byte2send[4] |= 0x10;
                    }

#ifdef LI100_USB
                    status = send_command_LI100_USB(busnumber, byte2send);
#else
                    status =
                        send_command_LI100_SERIAL(busnumber, byte2send);
#endif

                    /*function group 2: f5..f8 */
                    if (gltmp.n_func > 5) {
                        byte2send[1] = 0x21;
                        /* setting F5-F8, map: 0 0 0 0 F8 F7 F6 F5 */
                        byte2send[4] = (gltmp.funcs >> 5) & 0x00FF;
#ifdef LI100_USB
                        status = send_command_LI100_USB(busnumber, byte2send);
#else
                        status =
                            send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                    }

                    /* function group 3: f9..f12 */
                    if (gltmp.n_func > 9) {
                        byte2send[1] = 0x22;
                        /* setting F9-F12, map: 0 0 0 0 F12 F11 F10 F9 */
                        byte2send[4] = (gltmp.funcs >> 9) & 0x00FF;
#ifdef LI100_USB
                        status = send_command_LI100_USB(busnumber, byte2send);
#else
                        status =
                            send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                    }

                    /*support for functions > F12 since version 3.6*/
                    if (__li100->version_zentrale >= 0x0360) {

                        /* function group 4: f13..f20 */
                        /* map: F20 F19 F18 F17 F16 F15 F14 F13*/
                        if (gltmp.n_func > 13) {
                            byte2send[1] = 0x23;
                            byte2send[4] = (gltmp.funcs >> 13) & 0xffff;
#ifdef LI100_USB
                            status =
                                send_command_LI100_USB(busnumber, byte2send);
#else
                            status =
                                send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                        }

                        /* function group 5: f21..f28 */
                        /* map: F28 F27 F26 F25 F24 F23 F22 F21*/
                        if (gltmp.n_func > 21) {
                            byte2send[1] = 0x28;
                            byte2send[4] = (gltmp.funcs >> 21) & 0xffff;
#ifdef LI100_USB
                            status =
                                send_command_LI100_USB(busnumber, byte2send);
#else
                            status =
                                send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                        }
                    }
#ifndef LI100_USB
                } /* version > 3.00 */
#endif
            } /* gltmp.direction != 2*/

            if (status == 0) {
                cacheSetGL(busnumber, addr, gltmp);
            }
        }
    }
}

#ifdef LI100_USB
void check_extern_engines_USB(bus_t busnumber)
#else
void check_extern_engines_SERIAL(bus_t busnumber)
#endif
{
    int i;
    int tmp_addr;
    unsigned char byte2send[20];

    gl_state_t gltmp;

    if (__li100->extern_engine_ctr > 0) {
        for (i = 0; i < 100; i++) {
            tmp_addr = __li100->extern_engine[i];

            if (tmp_addr != -1) {
                if (__li100->version_zentrale <= 0x0150) {
                    __li100->last_value = tmp_addr;
                    byte2send[0] = 0xa1;
                    /* address */
                    byte2send[1] = tmp_addr;
#ifdef LI100_USB
                    send_command_LI100_USB(busnumber, byte2send);
#else
                    send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                }

                if ((__li100->version_zentrale > 0x0150) &&
                    (__li100->version_zentrale < 0x0300)) {
                    __li100->last_value = tmp_addr;
                    cacheGetGL(busnumber, tmp_addr, &gltmp);
                    byte2send[0] = 0xa2;
                    /* address */
                    byte2send[1] = tmp_addr;
                    /* mode */

                    switch (gltmp.n_fs) {
                        case 14:
                            byte2send[2] = 0x00;
                            break;
                        case 27:
                            byte2send[2] = 0x01;
                            break;
                        case 28:
                            byte2send[2] = 0x02;
                            break;
                        default:
                            byte2send[2] = 0x02;
                    }

#ifdef LI100_USB
                    send_command_LI100_USB(busnumber, byte2send);
#else
                    send_command_LI100_SERIAL(busnumber, byte2send);
#endif
                }

                if (__li100->version_zentrale >= 0x0300) {
                    __li100->last_value = tmp_addr;
                    byte2send[0] = 0xe3;
                    byte2send[1] = 0x00;
                    byte2send[2] = (tmp_addr & 0xff00) >> 8;
                    byte2send[3] = tmp_addr & 0x00ff;
#ifdef LI100_USB
                    send_command_LI100_USB(busnumber, byte2send);
                    check_status_LI100_USB(busnumber);
#else
                    send_command_LI100_SERIAL(busnumber, byte2send);
                    check_status_LI100_SERIAL(busnumber);
#endif
                }
            }
        }
    }
    __li100->last_value = -1;
}

#ifdef LI100_USB
int read_register_LI100_USB(bus_t busnumber, int reg)
#else
int read_register_LI100_SERIAL(bus_t busnumber, int reg)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x22;
    byte2send[1] = 0x11;
    byte2send[2] = reg;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
    get_status_sm_LI100_USB(busnumber);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
    get_status_sm_LI100_SERIAL(busnumber);
#endif

    return status;
}

#ifdef LI100_USB
int write_register_LI100_USB(bus_t busnumber, int reg, int value)
#else
int write_register_LI100_SERIAL(bus_t busnumber, int reg, int value)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x23;
    byte2send[1] = 0x12;
    byte2send[2] = reg;
    byte2send[3] = value;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    return status;
}

#ifdef LI100_USB
int read_page_LI100_USB(bus_t busnumber, int cv)
#else
int read_page_LI100_SERIAL(bus_t busnumber, int cv)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x22;
    byte2send[1] = 0x14;
    byte2send[2] = cv;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
    get_status_sm_LI100_USB(busnumber);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
    get_status_sm_LI100_SERIAL(busnumber);
#endif

    return status;
}

#ifdef LI100_USB
int write_page_LI100_USB(bus_t busnumber, int cv, int value)
#else
int write_page_LI100_SERIAL(bus_t busnumber, int cv, int value)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x23;
    byte2send[1] = 0x17;
    byte2send[2] = cv;
    byte2send[3] = value;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    return status;
}

#ifdef LI100_USB
int read_cv_LI100_USB(bus_t busnumber, int cv)
#else
int read_cv_LI100_SERIAL(bus_t busnumber, int cv)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x22;
    byte2send[1] = 0x15;
    byte2send[2] = cv;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
    get_status_sm_LI100_USB(busnumber);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
    get_status_sm_LI100_SERIAL(busnumber);
#endif

    return status;
}

#ifdef LI100_USB
int write_cv_LI100_USB(bus_t busnumber, int cv, int value)
#else
int write_cv_LI100_SERIAL(bus_t busnumber, int cv, int value)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    byte2send[0] = 0x23;
    byte2send[1] = 0x16;
    byte2send[2] = cv;
    byte2send[3] = value;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    return status;
}

/* program decoder on the main */
#ifdef LI100_USB
int send_pom_cv_LI100_USB(bus_t busnumber, int addr, int cv, int value)
#else
int send_pom_cv_LI100_SERIAL(bus_t busnumber, int addr, int cv, int value)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;
    int ret_val;
    int tmp;

    cv--;

    /* send pom-command */
    byte2send[0] = 0xE6;
    byte2send[1] = 0x30;
    /* high-byte of decoder-address */
    tmp = addr >> 8;
    byte2send[2] = tmp;
    /* low-byte of decoder-address */
    tmp = addr & 0xFF;
    byte2send[3] = tmp;
    tmp = 0x7C | ((cv >> 8) & 0x03);
    byte2send[4] = tmp;
    byte2send[5] = cv & 0xff;
    byte2send[6] = value;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    ret_val = 0;

    if (status != 0)
        ret_val = -1;

    return ret_val;
}

/* program decoder on the main */
#ifdef LI100_USB
int send_pom_cvbit_LI100_USB(bus_t busnumber, int addr, int cv, int cvbit,
                             int value)
#else
int send_pom_cvbit_LI100_SERIAL(bus_t busnumber, int addr, int cv,
                                int cvbit, int value)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;
    int ret_val;
    int tmp;

    cv--;

    /* send pom-command */
    byte2send[0] = 0xE6;
    byte2send[1] = 0x30;
    /* high-byte of decoder-address */
    tmp = addr >> 8;
    byte2send[2] = tmp;
    /* low-byte of decoder-address */
    tmp = addr & 0xFF;
    byte2send[3] = tmp;
    tmp = 0x7C | ((cv >> 8) & 0x03);
    byte2send[4] = tmp;
    byte2send[5] = cv & 0xff;
    byte2send[6] = cvbit;

    if (value)
        byte2send[6] |= 0x08;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);

#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);

#endif

    ret_val = 0;

    if (status != 0)
        ret_val = -1;

    return ret_val;
}

#ifdef LI100_USB
int term_pgm_LI100_USB(bus_t busnumber)
#else
int term_pgm_LI100_SERIAL(bus_t busnumber)
#endif
{
    unsigned char byte2send[20];
    unsigned char status;

    /* send command "turn all on" */
    byte2send[0] = 0x21;
    byte2send[1] = 0x81;

#ifdef LI100_USB
    status = send_command_LI100_USB(busnumber, byte2send);
#else
    status = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    return status;
}

#ifdef LI100_USB
void send_command_sm_LI100_USB(bus_t busnumber)
#else
void send_command_sm_LI100_SERIAL(bus_t busnumber)
#endif
{
    /* unsigned char byte2send; */
    /* unsigned char status; */

    struct _SM smakt;

    /* Locomotive decoder */
    /* fprintf(stderr, "LOK's... "); */
    /* nur senden, wenn wirklich etwas vorliegt */

    if (!queue_SM_isempty(busnumber)) {
        dequeueNextSM(busnumber, &smakt);

        __li100->last_type = smakt.type;
        __li100->last_typeaddr = smakt.typeaddr;
        __li100->last_bit = smakt.bit;
        __li100->last_value = smakt.value;

        syslog_bus(busnumber, DBG_DEBUG,
                   "in send_command_sm: last_type[%d] = %d", busnumber,
                   __li100->last_type);

        switch (smakt.command) {
            case SET:
                if (smakt.addr == -1) {
                    switch (smakt.type) {
                        case REGISTER:
#ifdef LI100_USB
                            write_register_LI100_USB(busnumber,
                                                     smakt.typeaddr,
                                                     smakt.value);
#else
                            write_register_LI100_SERIAL(busnumber,
                                                        smakt.typeaddr,
                                                        smakt.value);
#endif
                            break;
                        case CV:
#ifdef LI100_USB
                            write_cv_LI100_USB(busnumber, smakt.typeaddr,
                                               smakt.value);
#else
                            write_cv_LI100_SERIAL(busnumber,
                                                  smakt.typeaddr,
                                                  smakt.value);
#endif
                            break;
                            /*        case CV_BIT:
                               write_cvbit( busnumber, smakt.typeaddr, smakt.bit, smakt.value );
                               break; */
                        case PAGE:
#ifdef LI100_USB
                            write_page_LI100_USB(busnumber, smakt.typeaddr,
                                                 smakt.value);
#else
                            write_page_LI100_SERIAL(busnumber,
                                                    smakt.typeaddr,
                                                    smakt.value);
#endif
                    }
                }
                else {
                    switch (smakt.type) {
                        case CV:
#ifdef LI100_USB
                            send_pom_cv_LI100_USB(busnumber, smakt.addr,
                                                  smakt.typeaddr,
                                                  smakt.value);
#else
                            send_pom_cv_LI100_SERIAL(busnumber, smakt.addr,
                                                     smakt.typeaddr,
                                                     smakt.value);
#endif
                            break;
                        case CV_BIT:
#ifdef LI100_USB
                            send_pom_cvbit_LI100_USB(busnumber, smakt.addr,
                                                     smakt.typeaddr,
                                                     smakt.bit,
                                                     smakt.value);
#else
                            send_pom_cvbit_LI100_SERIAL(busnumber,
                                                        smakt.addr,
                                                        smakt.typeaddr,
                                                        smakt.bit,
                                                        smakt.value);
#endif
                            break;
                    }
                }
                break;
            case GET:
                switch (smakt.type) {
                    case REGISTER:
#ifdef LI100_USB
                        read_register_LI100_USB(busnumber, smakt.typeaddr);
#else
                        read_register_LI100_SERIAL(busnumber,
                                                   smakt.typeaddr);
#endif
                        break;
                    case CV:
#ifdef LI100_USB
                        read_cv_LI100_USB(busnumber, smakt.typeaddr);
#else
                        read_cv_LI100_SERIAL(busnumber, smakt.typeaddr);
#endif
                        break;
                        /*      case CV_BIT:
                           read_cvbit( busnumber, smakt.typeaddr, smakt.bit );
                           break; */
                    case PAGE:
#ifdef LI100_USB
                        read_page_LI100_USB(busnumber, smakt.typeaddr);
#else
                        read_page_LI100_SERIAL(busnumber, smakt.typeaddr);
#endif
                }
                break;
            case VERIFY:
                break;
            case INIT:
                break;
            case TERM:
                syslog_bus(busnumber, DBG_DEBUG,
                           "on bus %i pgm_mode is %i", busnumber,
                           __li100->pgm_mode);
                if (__li100->pgm_mode == 1) {
#ifdef LI100_USB
                    term_pgm_LI100_USB(busnumber);
#else
                    term_pgm_LI100_SERIAL(busnumber);
#endif
                }
                break;
        }
    }
}

#ifdef LI100_USB
void get_status_sm_LI100_USB(bus_t busnumber)
#else
void get_status_sm_LI100_SERIAL(bus_t busnumber)
#endif
{
    unsigned char byte2send[20];

    byte2send[0] = 0x21;
    byte2send[1] = 0x10;

#ifdef LI100_USB
    send_command_LI100_USB(busnumber, byte2send);
#else
    send_command_LI100_SERIAL(busnumber, byte2send);
#endif
}

#ifdef LI100_USB
void check_status_LI100_USB(bus_t busnumber)
#else
void check_status_LI100_SERIAL(bus_t busnumber)
#endif
{
    int i;
    int status;

    /* with debug level beyond DBG_DEBUG, we will not really work on hardware */
    if (buses[busnumber].debuglevel <= DBG_DEBUG) {
        i = 1;

        while (i > 0) {
            status = ioctl(buses[busnumber].device.file.fd, FIONREAD, &i);

            if (status < 0) {
                char msg[200];
                strcpy(msg, strerror(errno));
                syslog_bus(busnumber, DBG_ERROR,
                           "readbyte(): IOCTL   status: %d with errno = %d: %s",
                           status, errno, msg);
            }

            syslog_bus(busnumber, DBG_DEBUG,
                       "readbyte(): (fd = %d), there are %d bytes to read.",
                       buses[busnumber].device.file.fd, i);
            /* read only, if there is really an input */
            if (i > 0)
#ifdef LI100_USB
                status = readAnswer_LI100_USB(busnumber);
#else
                status = readAnswer_LI100_SERIAL(busnumber);
#endif
        }
    }
}

#ifdef LI100_USB
int send_command_LI100_USB(bus_t busnumber, const unsigned char *str)
#else
int send_command_LI100_SERIAL(bus_t busnumber, const unsigned char *str)
#endif
{
    int i, ctr;
    int status;
    unsigned char xor = 0x00; /* control-byte for xor */

#ifdef LI100_USB
    /* header for LI100_USB */
    writeByte(busnumber, 0xff, 0);
    writeByte(busnumber, 0xfe, 0);
#endif

    ctr = str[0] & 0x0f;        /* generate length of command */
    ctr++;

    for (i = 0; i < ctr; i++) { /* send command */
        xor ^= str[i];
        writeByte(busnumber, str[i], 0);
    }

    writeByte(busnumber, xor, 0);   /* send X-Or-Byte */

#ifdef LI100_USB
    status = readAnswer_LI100_USB(busnumber);
#else
    status = readAnswer_LI100_SERIAL(busnumber);
#endif

    return status;
}

#ifdef LI100_USB
int readAnswer_LI100_USB(bus_t busnumber)
#else
int readAnswer_LI100_SERIAL(bus_t busnumber)
#endif
{
    int status;
    int i, ctr;
    int tmp_addr;
    int message_processed;
    unsigned char cXor;
    unsigned char buffer[20];

    gl_state_t gltmp, glakt;

    gltmp.speed = 0;
    gltmp.funcs = 0;

    message_processed = 0;
    status = -1;                /* wait for answer */

    ctr = 50;

    while (status == -1) {
        ctr--;

        if (ctr < 0)
            return status;

        if (usleep(2000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        status = readByte(busnumber, 1, &buffer[0]);
    }

#ifdef LI100_USB
    /* skip LI100_USB-header */
    readByte(busnumber, 1, &buffer[0]);
    readByte(busnumber, 1, &buffer[0]);
#endif

    ctr = buffer[0] & 0x0f;        /* generate length of answer */
    ctr += 2;

    /* read answer */
    for (i = 1; i < ctr; i++) {
        readByte(busnumber, 1, &buffer[i]);
    }

    cXor = 0;
    for (i = 0; i < ctr; i++) {
        cXor ^= buffer[i];
    }

    if (cXor != 0x00)           /* must be 0x00 */
        status = -1;            /* error */


    /* li100 reply message */
    if (buffer[0] == 0x01) {

        switch (buffer[1]) {
            case 0x01:
                syslog_bus(busnumber, DBG_ERROR,
                        "Interface/PC communication error\n");
                break;

            case 0x02:
                syslog_bus(busnumber, DBG_ERROR,
                        "Interface/central unit communication error\n");
                break;

            case 0x03:
                syslog_bus(busnumber, DBG_ERROR,
                        "Unknown error\n");
                break;

            case 0x04:
            /* command send to central unit (normal operation)*/
                break;

            case 0x05:
                syslog_bus(busnumber, DBG_ERROR,
                        "Central unit can not address LI101F\n");
                break;

            case 0x06:
                syslog_bus(busnumber, DBG_ERROR,
                        "LI101F buffer overflow\n");
                break;

            default:
                syslog_bus(busnumber, DBG_ERROR,
                        "Unknown command key received: 0x%02x\n", buffer[1]);
                break;
        }
        message_processed = 1;
    }

    /* version-number of interface */
    else if (buffer[0] == 0x02) {
        __li100->version_interface =
            ((buffer[1] & 0xf0) << 4) + (buffer[1] & 0x0f);
        __li100->code_interface = (int) buffer[2];
        message_processed = 1;
    }

    /* power on/off, service mode changes */
    else if (buffer[0] == 0x61) {

        /* power off */
        if (buffer[1] == 0x00) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "on bus %i no power detected; old-state is %i",
                       busnumber, getPower(busnumber));
            if ((__li100->emergency_on_LI100 == 0)
                && (getPower(busnumber))) {
                setPower(busnumber, 0, "Emergency Stop");
                __li100->emergency_on_LI100 = 1;
            }
            message_processed = 1;
        }

        /* power on */
        else if (buffer[1] == 0x01) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "on bus %i power detected; old-state is %i",
                       busnumber, getPower(busnumber));
            if ((__li100->emergency_on_LI100 == 1)
                || (!getPower(busnumber))) {
                setPower(busnumber, 1, "No Emergency Stop");
                __li100->emergency_on_LI100 = 0;
            }
            else if (__li100->pgm_mode == 1) {
                session_endwait(busnumber, -1);
                setPower(busnumber, 1, "Service mode end");
                __li100->pgm_mode = 0;
            }
            message_processed = 1;
        }

        /* service mode on */
        else if (buffer[1] == 0x02) {
            if (__li100->pgm_mode == 0) {
                __li100->pgm_mode = 1;
                syslog_bus(busnumber, DBG_DEBUG,
                           "Service mode activated on bus %i",
                           busnumber);
                setPower(busnumber, -1, "Service mode start");
            }
            message_processed = 1;
        }

        /*short circuit*/
        else if (buffer[1] == 0x12) {
            syslog_bus(busnumber, DBG_WARN,
                    "Short circuit detected on bus %i", busnumber);
            message_processed = 1;
        }

        /*data byte not found*/
        else if (buffer[1] == 0x13) {
            if (__li100->last_type != -1) {
                session_endwait(busnumber, -1);
                setSM(busnumber, __li100->last_type, -1,
                        __li100->last_typeaddr, __li100->last_bit,
                        __li100->last_value, -1);
                __li100->last_type = -1;
            }
            message_processed = 1;
        }

        /*Command station ready*/
        else if (buffer[1] == 0x11) {
            syslog_bus(busnumber, DBG_WARN,
                    "Command station ready on bus %i", busnumber);
            message_processed = 1;
        }

        /*Command station busy*/
        else if (buffer[1] == 0x1f) {
            syslog_bus(busnumber, DBG_WARN,
                    "Command station busy on bus %i", busnumber);
            message_processed = 1;
        }

        /*Transfer error*/
        else if (buffer[1] == 0x80) {
            syslog_bus(busnumber, DBG_WARN,
                    "Transfer error on bus %i", busnumber);
            message_processed = 1;
        }

        /*Command station busy*/
        else if (buffer[1] == 0x81) {
            syslog_bus(busnumber, DBG_WARN,
                    "Command station busy on bus %i", busnumber);
            message_processed = 1;
        }

        /*Instruction not supported*/
        else if (buffer[1] == 0x82) {
            syslog_bus(busnumber, DBG_WARN,
                    "Instruction not supported on bus %i", busnumber);
            message_processed = 1;
        }

    }

    /*FIXME: 0x62 and 0x63 are mixed, messing up the "if/else" sequence*/
    /* 0x62: X-Bus 1 + 2, 0x63: XpressNet */
    else if ((buffer[0] == 0x62) || (buffer[0] == 0x63)) {

        /* version-number of central unit */
        if (buffer[1] == 0x21) {
            __li100->version_zentrale =
                ((buffer[2] & 0xf0) << 4) + (buffer[2] & 0x0f);

            if (buffer[0] == 0x63)
                __li100->code_interface = (int) buffer[3];
            else
                __li100->code_interface = -1;

#ifndef LI100_USB
            /* check for address-range 1..99; 1..256 for version < V3.00 */
            if (__li100->version_zentrale < 0x0300) {
                if (__li100->number_gl > 99)
                    __li100->number_gl = 99;

                if (__li100->number_ga > 256)
                    __li100->number_ga = 256;
            }
#endif
            message_processed = 1;
        }

        /*central unit status*/
        else if (buffer[1] == 0x22) {
            int emergencystop = buffer[2] & 0x01;
            int emergencyoff = buffer[2] & 0x02;
            int startmode = buffer[2] & 0x04;
            int programmingmode = buffer[2] & 0x08;
            int coldstart = buffer[2] & 0x20;
            int ramcheckerror = buffer[2] & 0x40;

            syslog_bus(busnumber, DBG_INFO,
                    "Central unit status: "
                    "emergency stop: %d, "
                    "emergency off: %d, "
                    "start mode: %d, "
                    "programming mode: %d, "
                    "cold start: %d, "
                    "RAM check error: %d\n",
                    emergencystop, emergencyoff, startmode,
                    programmingmode, coldstart, ramcheckerror);

            message_processed = 1;
        }
    }

    /* answer of programming */
    if ((buffer[0] == 0x63) && ((buffer[1] & 0xf0) == 0x10)) {
        if (__li100->last_type != -1) {
            session_endwait(busnumber, (int) buffer[3]);
            setSM(busnumber, __li100->last_type, -1,
                  __li100->last_typeaddr, __li100->last_bit, (int) buffer[3],
                  0);
            __li100->last_type = -1;
        }
        message_processed = 1;
    }

    /*0x83: Locomotive information response */
    /*0x84: Locomotive information response (available for operation)
            not handled yet*/
    /*0xa3: Locomotive is being operated by another device*/
    /*0xa4: Locomotive is being operated by another device
            not handled yet*/
    else if ((buffer[0] == 0x83) || (buffer[0] == 0xa3)) {
        if (buffer[0] & 0x20)
            add_extern_engine(busnumber, buffer[1]);
        else
            remove_extern_engine(busnumber, buffer[1]);

        gltmp.id = buffer[1];
        gltmp.direction = (buffer[2] & 0x40) ? 1 : 0;
        gltmp.speed = buffer[2] & 0x0f;
        if (gltmp.speed == 1) {
            gltmp.speed = 0;
            gltmp.direction = 2;
        }
        else {
            if (gltmp.speed > 0)
                gltmp.speed--;
        }
        gltmp.funcs = ((buffer[3] & 0x0f) << 1);
        if (buffer[2] & 0x20)
            gltmp.funcs |= 0x01;        /* light is on */

        /*functions f5..f12, map: F12 F11 F10 F9 F8 F7 F6 F5 */
        unsigned int f5tof12 = buffer[4];
        f5tof12 <<= 5;
        gltmp.funcs |= f5tof12;

        /* get old data, to send only if something changed */
        int result = cacheGetGL(busnumber, gltmp.id, &glakt);

        /* If GL is unknown, show warning message*/
        if (SRCP_NODATA == result) {
            syslog_bus(busnumber, DBG_WARN,
                    "Command for uninitialized GL received (address = %d)",
                    gltmp.id);
        }
        gltmp.n_func = glakt.n_func;

        if ((glakt.speed != gltmp.speed) ||
            (glakt.direction != gltmp.direction) ||
            (glakt.funcs != gltmp.funcs))
            cacheSetGL(busnumber, gltmp.id, gltmp);

        message_processed = 1;
    }

    /* Double Header information response (X-Bus V1) */
    else if (buffer[0] == 0xc5) {
        syslog_bus(busnumber, DBG_WARN,
                "Double Header information response on bus %i "
                " (not supported)", busnumber);
        message_processed = 1;
    }

    /* XpressNet MU+DH error message response */
    else if (buffer[0] == 0xe1) {
        syslog_bus(busnumber, DBG_WARN,
                "XpressNet MU+DH error message response on bus %i "
                " (not supported)", busnumber);

        switch (buffer[1]) {

            case 0x81:
                syslog_bus(busnumber, DBG_WARN,
                "Locomotive has not been operated on bus %i", busnumber);
                break;

            case 0x82:
                syslog_bus(busnumber, DBG_WARN,
                        "Locomotive operated by another XpressNet "
                        "device on bus %i", busnumber);
                break;

            case 0x83:
                syslog_bus(busnumber, DBG_WARN,
                        "One of the locomotives already is in another "
                        "Multi-Unit or Double Header on bus %i", busnumber);
                break;

            case 0x84:
                syslog_bus(busnumber, DBG_WARN,
                        "The speed of one of the locomotives of the "
                        "Double Header/Multi-Unit is not zero on bus %i",
                        busnumber);
                break;

            case 0x85:
                syslog_bus(busnumber, DBG_WARN,
                        "The locomotive is not in a multi-unit on bus %i",
                        busnumber);
                break;

            case 0x86:
                syslog_bus(busnumber, DBG_WARN,
                        "The locomotive address is not a multi-unit "
                        "base address on bus %i", busnumber);
                break;

            case 0x87:
                syslog_bus(busnumber, DBG_WARN,
                        "It is not possible to delete the locomotive "
                        "on bus %i", busnumber);
                break;

            case 0x88:
                syslog_bus(busnumber, DBG_WARN,
                        "The command station stack is full on bus %i",
                        busnumber);
                break;

            default:
                syslog_bus(busnumber, DBG_WARN,
                "Unknown error code 0x%02x on bus %i", buffer[1], busnumber);
                break;
        }

        message_processed = 1;
    }

    /* Locomotive information for the Multi-unit address */
    else if (buffer[0] == 0xe2) {
        syslog_bus(busnumber, DBG_WARN,
                "Multi-unit locomotive address response on bus %i "
                " (not supported)", busnumber);
        message_processed = 1;
    }

    /*Locomotive status response*/
    else if (buffer[0] == 0xe3) {

        /* Locomotive information response for address retrieval requests
         * (XpressNet only)*/
        if ((buffer[1] & 0x30) == 0x30) {
            __li100->get_addr = 256 * (int) buffer[2];
            __li100->get_addr += (int) buffer[3];
            message_processed = 1;
        }

        /* Locomotive is being operated by another device response
         * (XpressNet only)*/
        else if (buffer[1] == 0x40) {
            tmp_addr = buffer[3];
            tmp_addr |= buffer[2] << 8;
            add_extern_engine(busnumber, tmp_addr);
            message_processed = 1;
        }

        /*not part of SRCP: function type report (switch/button) f0..f12*/
        else if (buffer[1] == 0x50) {
        }

        /*not part of SRCP: function type report (switch/button) f13..f28*/
        else if (buffer[1] == 0x51) {
        }

        /*function status report f13..f28*/
        else if (buffer[1] == 0x52) {
            cacheGetGL(busnumber, __li100->get_addr, &gltmp);
            unsigned int fncblock = buffer[3];
            fncblock <<= 8;
            fncblock |= buffer[2];
            fncblock <<= 13;
            unsigned int tmpfuncs = gltmp.funcs;
            tmpfuncs &= ~0x1fffe000;
            tmpfuncs |= fncblock;
            if (gltmp.funcs != tmpfuncs)
                cacheSetGL(busnumber, __li100->get_addr, gltmp);
        }
    }

    /* Locomotive information normal locomotive (single traction) */
    else if (buffer[0] == 0xe4) {
        gltmp.id = __li100->last_value & 0x3fff;
        /*CHECK: __li100->get_addr = gltmp.id;*/
        /* is engine always allocated by an external device? */
        if (!(buffer[1] & 0x08)) {
            remove_extern_engine(busnumber, __li100->last_value);
        }
        gltmp.direction = (buffer[2] & 0x80) ? 1 : 0;
        switch (buffer[1] & 7) {
            case 0: /*14 speed steps*/
            case 4: /*128 speed steps*/
                gltmp.speed = buffer[2] & 0x7f;
                if (gltmp.speed == 1) {
                    gltmp.speed = 0;
                    /* gltmp.direction = 2; */
                }
                else {
                    if (gltmp.speed > 0)
                        gltmp.speed--;
                }
                break;
            case 1: /*27 speed steps*/
            case 2: /*28 speed steps*/
                gltmp.speed = buffer[2] & 0x7f;
                gltmp.speed <<= 1;
                if (gltmp.speed & 0x20)
                    gltmp.speed |= 0x01;
                gltmp.speed &= 0xdf;
                if (gltmp.speed == 2) {
                    gltmp.speed = 0;
                    /* gltmp.direction = 2; */
                }
                else {
                    if (gltmp.speed > 0)
                        gltmp.speed -= 3;
                }
                break;
        }

        /*function map: F12 F11 F10 F9 F8 F7 F6 F5*/
        gltmp.funcs = buffer[4];
        gltmp.funcs <<= 5;

        /*function map: 0 0 0 F0 F4 F3 F2 F1*/
        unsigned int tmpfuncs = buffer[3] & 0x0F;
        tmpfuncs <<= 1;

        if (buffer[3] & 0x10)
            tmpfuncs |= 0x01;        /* light is on */

        gltmp.funcs |= tmpfuncs;

        /* get old data, to send only if something changed */
        int result = cacheGetGL(busnumber, gltmp.id, &glakt);

        /* If GL is unknown, show warning message*/
        if (SRCP_NODATA == result) {
            syslog_bus(busnumber, DBG_WARN,
                    "Command for uninitialized GL received (address = %d)",
                    gltmp.id);
        }
        gltmp.n_func = glakt.n_func;

        if ((glakt.speed != gltmp.speed) ||
            (glakt.direction != gltmp.direction) ||
            (glakt.funcs != gltmp.funcs))
            cacheSetGL(busnumber, gltmp.id, gltmp);

        message_processed = 1;
    }

    /* Locomotive information for a locomotive in a multi-unit */
    else if (buffer[0] == 0xe5) {
        syslog_bus(busnumber, DBG_WARN,
                "Multi-unit locomotive response on bus %i "
                " (not supported)", busnumber);
        message_processed = 1;
    }

    /*Locomotive information for a locomotive in a Double Header*/
    else if (buffer[0] == 0xe6) {
        syslog_bus(busnumber, DBG_WARN,
                "Double header locomotive response on bus %i "
                " (not supported)", busnumber);
        message_processed = 1;
    }

    /* information about feedback, bit pattern: AAAA AAAA ITTN ZZZZ*/
    /*0x42: Accessory Decoder information response*/
    if ((buffer[0] & 0xf0) == 0x40) {

        ga_state_t gatmp;
        ctr = buffer[0] & 0xf;
        
        for (i = 1; i < ctr; i += 2) {

            /*check for address type TT, mask: 0110 0000 */
            switch (buffer[i + 1] & 0x60) {
                case 0x00:     /* switch-decoder without feedback */
                case 0x20:     /* switch-decoder with feedback */
                    gatmp.id = buffer[i];
                    gatmp.id <<= 2;

                    /*align li100 address (0..2047) to SRCP address (1..2048)*/
                    gatmp.id++;

                    /*check for upper nibble, mask: 0001 0000 */
                    if (buffer[i + 1] & 0x10)
                        gatmp.id += 2;

                    /*first address, mask 0011 */
                    tmp_addr = buffer[i + 1] & 0x03;

                    /*position left, mask: 0001 */
                    if (tmp_addr == 0x01) {
                        gatmp.port = 0;
                        gatmp.action = 1;
                        setGA(busnumber, gatmp.id, gatmp);
                    }

                    /*position right, mask: 0010 */
                    if (tmp_addr == 0x02) {
                        gatmp.port = 1;
                        gatmp.action = 1;
                        setGA(busnumber, gatmp.id, gatmp);
                    }

                    /*second address, mask 1100 */
                    tmp_addr = buffer[i + 1] & 0x0C;
                    gatmp.id++;

                    /*position left, mask: 0100 */
                    if (tmp_addr == 0x04) {
                        gatmp.port = 0;
                        gatmp.action = 1;
                        setGA(busnumber, gatmp.id, gatmp);
                    }

                    /*position right, mask: 1000 */
                    if (tmp_addr == 0x08) {
                        gatmp.port = 1;
                        gatmp.action = 1;
                        setGA(busnumber, gatmp.id, gatmp);
                    }
                    break;

                case 0x40:     /* feedback-decoder */
                    setFBmodul(busnumber,
                               (buffer[i] * 2) +
                               ((buffer[i + 1] & 0x10) ? 2 : 1),
                               buffer[i + 1] & 0x0f);
                    break;
            }
        }
        message_processed = 1;
    }

    else if (buffer[0] == 0xf2) {

        /* XpresssNet address answer*/
        if (buffer[1] == 0x01) {
            syslog_bus(busnumber, DBG_WARN,
                    "XpressNet address changes not handled yet: "
                    "currrent = 0x%02x", buffer[2]);
            message_processed = 1;
        }

        /* baud rate settings answer*/
        else if (buffer[1] == 0x02) {
            syslog_bus(busnumber, DBG_WARN,
                    "Baud rate setting changes not handled yet: "
                    "currrent = 0x%02x", buffer[2]);
            message_processed = 1;
        }
    }

    /* at last catch all unknown command keys and show a warning message */
    if (message_processed == 0) {
        syslog_bus(busnumber, DBG_WARN,
                   "Unknown command key received: 0x%02x 0x%02x", buffer[0],
                   buffer[1]);
    }

    return status;
}

#ifdef LI100_USB
int initLine_LI100_USB(bus_t busnumber)
#else
int initLine_LI100_SERIAL(bus_t busnumber)
#endif
{
    int status, status2;
    int result;
    int fd;
    unsigned char byte2send[20];

    struct termios interface;

    char *name = buses[busnumber].device.file.path;
    syslog_bus(busnumber, DBG_INFO, "Beginning to detect LI100 on serial "
               "line: %s\n", name);
    status = -4;

    switch (buses[busnumber].device.file.baudrate) {
        case B9600:
            strcpy((char *) byte2send, "9600");
            break;
        case B19200:
            strcpy((char *) byte2send, "19200");
            break;
        case B38400:
            strcpy((char *) byte2send, "38400");
            break;
        case B57600:
            strcpy((char *) byte2send, "57600");
            break;
        case B115200:
            strcpy((char *) byte2send, "57600");
            break;
        default:
            strcpy((char *) byte2send, "9600");
            break;
    }

    syslog_bus(busnumber, DBG_INFO,
               "Try to open serial line %s for %s baud\n", name,
               byte2send);
    fd = open(name, O_RDWR);
    if (fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Open serial device '%s' failed: %s " "(errno = %d).\n",
                   name, strerror(errno), errno);
        return status;
    }

    buses[busnumber].device.file.fd = fd;
    tcgetattr(fd, &interface);
    interface.c_oflag = ONOCR;
#ifdef LI100_USB
    interface.c_cflag = CS8 | CLOCAL | CREAD | HUPCL;
#else
    interface.c_cflag = CS8 | CRTSCTS | CLOCAL | CREAD | HUPCL;
#endif
    interface.c_iflag = IGNBRK;
    interface.c_lflag = IEXTEN;
    cfsetispeed(&interface, buses[busnumber].device.file.baudrate);
    cfsetospeed(&interface, buses[busnumber].device.file.baudrate);
    interface.c_cc[VMIN] = 0;
    interface.c_cc[VTIME] = 1;
    tcsetattr(fd, TCSANOW, &interface);

    result = sleep(1);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "sleep() interrupted, %d seconds left\n", result);
    }

    status++;

    /* get version of LI100 */
    byte2send[0] = 0xF0;
#ifdef LI100_USB
    status2 = send_command_LI100_USB(busnumber, byte2send);
#else
    status2 = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    if (status2 == 0)
        status++;
    else
        return status;

    /* get version of central unit */
    byte2send[0] = 0x21;
    byte2send[1] = 0x21;

#ifdef LI100_USB
    status2 = send_command_LI100_USB(busnumber, byte2send);
#else
    status2 = send_command_LI100_SERIAL(busnumber, byte2send);
#endif
    if (status2 == 0)
        status++;
    else
        return status;

    /* get status of central unit */
    byte2send[0] = 0x21;
    byte2send[1] = 0x24;

#ifdef LI100_USB
    status2 = send_command_LI100_USB(busnumber, byte2send);
#else
    status2 = send_command_LI100_SERIAL(busnumber, byte2send);
#endif

    if (status2 == 0)
        status++;

    return status;
}
