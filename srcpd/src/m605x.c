/* cvs: $Id: m605x.c 1456 2010-02-28 20:01:39Z gscholz $             */

#include <errno.h>
#include <fcntl.h>
#include <sys/ioctl.h>
#include <unistd.h>

#ifdef __CYGWIN__
#include <sys/socket.h>         /*for FIONREAD */
#endif

#ifdef __sun__
#include <sys/filio.h>
#endif

#include "config-srcpd.h"
#include "io.h"
#include "m605x.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "srcp-server.h"
#include "srcp-error.h"
#include "syslogmessage.h"
#include "ttycygwin.h"

#define __m6051 ((M6051_DATA*)buses[busnumber].driverdata)

/**
 * readconfig_m605x: Liest den Teilbaum der xml Configuration und
 * parametriert den busspezifischen Datenteil, wird von register_bus()
 * aufgerufen.
 **/

int readconfig_m605x(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    buses[busnumber].driverdata = malloc(sizeof(struct _M6051_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_M605X;
    buses[busnumber].init_func = &init_bus_M6051;
    buses[busnumber].thr_func = &thr_sendrec_M6051;
    buses[busnumber].init_gl_func = &init_gl_M6051;
    buses[busnumber].init_ga_func = &init_ga_M6051;
    buses[busnumber].flags |= FB_16_PORTS;
    __m6051->number_fb = 0;     /* max 31 */
    __m6051->number_ga = 256;
    __m6051->number_gl = 80;
    __m6051->ga_min_active_time = 75;
    __m6051->pause_between_cmd = 200;
    __m6051->pause_between_bytes = 2;
    strcpy(buses[busnumber].description,
           "GA GL FB POWER LOCK DESCRIPTION");

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
                __m6051->number_fb = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __m6051->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __m6051->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "mode_m6020") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "yes") == 0)
                    __m6051->flags |= M6020_MODE;
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

        else if (xmlStrcmp(child->name, BAD_CAST "ga_min_activetime") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __m6051->ga_min_active_time = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "pause_between_commands")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __m6051->pause_between_cmd = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "pause_between_bytes") ==
                 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __m6051->pause_between_bytes = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }

    if (init_GA(busnumber, __m6051->number_ga)) {
        __m6051->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessories");
    }

    if (init_GL(busnumber, __m6051->number_gl)) {
        __m6051->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for locomotives");
    }

    if (init_FB(busnumber, __m6051->number_fb * 16)) {
        __m6051->number_fb = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }

    return 1;
}


/*******************************************************
 *     configure serial line
 *******************************************************/
static int init_lineM6051(bus_t bus)
{
    int fd;
    struct termios interface;

    if (buses[bus].debuglevel > 0) {
        syslog_bus(bus, DBG_INFO, "Opening 605x: %s",
                   buses[bus].device.file.path);
    }

    fd = open(buses[bus].device.file.path, O_RDWR | O_NONBLOCK);
    if (fd == -1) {
        syslog_bus(bus, DBG_ERROR, "Open serial device '%s' failed: %s "
                   "(errno = %d).\n", buses[bus].device.file.path,
                   strerror(errno), errno);
        return -1;
    }
    tcgetattr(fd, &interface);
#ifdef linux
    interface.c_cflag = CS8 | CRTSCTS | CREAD | CSTOPB;
    interface.c_oflag = ONOCR | ONLRET;
    interface.c_oflag &= ~(OLCUC | ONLCR | OCRNL);
    interface.c_iflag = IGNBRK | IGNPAR;
    interface.c_iflag &= ~(ISTRIP | IXON | IXOFF | IXANY);
    interface.c_lflag = NOFLSH | IEXTEN;
    interface.c_lflag &= ~(ISIG | ICANON | ECHO | ECHOE | TOSTOP | PENDIN);
#else
    cfmakeraw(&interface);

    interface.c_cflag = CREAD | HUPCL | CS8 | CSTOPB | CRTSCTS;
#endif
    cfsetospeed(&interface, B2400);
    cfsetispeed(&interface, B2400);

    tcsetattr(fd, TCSANOW, &interface);
    syslog_bus(bus, DBG_INFO, "Opening 605x succeeded (fd = %d).", fd);
    return fd;
}

int init_bus_M6051(bus_t bus)
{
    static char *protocols = "MP";
    buses[bus].protocols = protocols;
    syslog_bus(bus, DBG_INFO, "M605x  init: debug %d",
               buses[bus].debuglevel);
    if (buses[bus].debuglevel <= DBG_DEBUG) {
        buses[bus].device.file.fd = init_lineM6051(bus);
    }
    else {
        buses[bus].device.file.fd = -1;
    }
    syslog_bus(bus, DBG_INFO, "M605x init done, fd=%d",
               buses[bus].device.file.fd);
    syslog_bus(bus, DBG_INFO, "M605x: %s", buses[bus].description);
    syslog_bus(bus, DBG_INFO, "M605x flags: %d",
               buses[bus].flags & AUTO_POWER_ON);
    return 0;
}

/**
 * cacheInitGL: modifies the gl data used to initialize the device
 **/
int init_gl_M6051(bus_t bus, gl_state_t * gl)
{
    if (gl->protocol != 'M')
        return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
    switch (gl->protocolversion) {
        case 1:
            return (gl->n_fs == 14) ? SRCP_OK : SRCP_WRONGVALUE;
            break;
        case 2:
            return ((gl->n_fs == 14) ||
                    (gl->n_fs == 27) ||
                    (gl->n_fs == 28)) ? SRCP_OK : SRCP_WRONGVALUE;
            break;
    }
    return SRCP_WRONGVALUE;
}

/**
 * initGA: modifies the ga data used to initialize the device
 **/
int init_ga_M6051(ga_state_t * ga)
{
    if ((ga->protocol != 'M') || (ga->protocol != 'P'))
        return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
    return SRCP_OK;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "M605x bus terminated.");
    if (buses[btd->bus].device.file.fd != -1)
        close(buses[btd->bus].device.file.fd);

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

void *thr_sendrec_M6051(void *v)
{
    unsigned char SendByte;
    int akt_S88, addr, temp, number_fb;
    int result;
    char c;
    unsigned char rr;
    gl_state_t gltmp, glakt;
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
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    syslog_bus(btd->bus, DBG_INFO, "M605x bus started (device = %s).",
               buses[btd->bus].device.file.path);

    int ga_min_active_time =
        ((M6051_DATA *) buses[btd->bus].driverdata)->ga_min_active_time;
    int pause_between_cmd =
        ((M6051_DATA *) buses[btd->bus].driverdata)->pause_between_cmd;
    int pause_between_bytes =
        ((M6051_DATA *) buses[btd->bus].driverdata)->pause_between_bytes;
    number_fb = ((M6051_DATA *) buses[btd->bus].driverdata)->number_fb;

    akt_S88 = 1;
    buses[btd->bus].watchdog = 1;

    result = ioctl(buses[btd->bus].device.file.fd, FIONREAD, &temp);
    if (result == -1) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    if (((M6051_DATA *) buses[btd->bus].driverdata)->cmd32_pending) {
        SendByte = 32;
        writeByte(btd->bus, SendByte, pause_between_cmd);
        ((M6051_DATA *) buses[btd->bus].driverdata)->cmd32_pending = 0;
    }
    while (temp > 0) {
        readByte(btd->bus, 0, &rr);
        result = ioctl(buses[btd->bus].device.file.fd, FIONREAD, &temp);
        if (result == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        syslog_bus(btd->bus, DBG_INFO, "Ignoring unread byte: %d ", rr);
    }

    while (1) {
        pthread_testcancel();
        buses[btd->bus].watchdog = 2;

        /* Start/Stop */
        if (buses[btd->bus].power_changed == 1) {
            char msg[1000];
            SendByte = (buses[btd->bus].power_state) ? 96 : 97;
            /* zweimal, wir sind paranoid */
            writeByte(btd->bus, SendByte, pause_between_cmd);
            writeByte(btd->bus, SendByte, pause_between_cmd);
            buses[btd->bus].power_changed = 0;
            infoPower(btd->bus, msg);
            enqueueInfoMessage(msg);
        }

        /* do nothing, if power off */
        if (buses[btd->bus].power_state == 0) {
            if (usleep(1000) == -1) {
                syslog_bus(btd->bus, DBG_ERROR,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            continue;
        }
        buses[btd->bus].watchdog = 3;

        /* locomotive decoder */
        if (!((M6051_DATA *) buses[btd->bus].driverdata)->cmd32_pending) {
            if (!queue_GL_isempty(btd->bus)) {
                dequeueNextGL(btd->bus, &gltmp);
                addr = gltmp.id;
                cacheGetGL(btd->bus, addr, &glakt);

                if (gltmp.direction == 2) {
                    gltmp.speed = 0;
                    gltmp.direction = !glakt.direction;
                }
                /* forward/backward */
                if (gltmp.direction != glakt.direction) {
                    c = 15 + 16 * ((gltmp.funcs & 0x10) ? 1 : 0);
                    writeByte(btd->bus, c, pause_between_bytes);
                    SendByte = addr;
                    writeByte(btd->bus, SendByte, pause_between_cmd);
                }
                /* Geschwindigkeit und Licht setzen, erst recht nach
                   Richtungswechsel */
                c = gltmp.speed + 16 * ((gltmp.funcs & 0x01) ? 1 : 0);
                /* jetzt aufpassen: n_fs erzwingt ggf. mehrfache
                   Ansteuerungen des Dekoders, das Protokoll ist da
                   wirklich eigenwillig, vorerst ignoriert! */
                writeByte(btd->bus, c, pause_between_bytes);
                SendByte = addr;
                writeByte(btd->bus, SendByte, pause_between_cmd);
                /* Erweiterte Funktionen des 6021 senden, manchmal */
                if (!((((M6051_DATA *) buses[btd->bus].driverdata)->
                       flags & M6020_MODE) == M6020_MODE)
                    && (gltmp.funcs != glakt.funcs)) {
                    c = ((gltmp.funcs >> 1) & 0x0f) + 64;
                    writeByte(btd->bus, c, pause_between_bytes);
                    SendByte = addr;
                    writeByte(btd->bus, SendByte, pause_between_cmd);
                }
                cacheSetGL(btd->bus, addr, gltmp);
            }
            buses[btd->bus].watchdog = 4;
        }
        buses[btd->bus].watchdog = 5;

        /* Magnetantriebe, die muessen irgendwann sehr bald
           abgeschaltet werden */
        if (!queue_GA_isempty(btd->bus)) {
            dequeueNextGA(btd->bus, &gatmp);
            addr = gatmp.id;
            if (gatmp.action == 1) {
                gettimeofday(&gatmp.tv[gatmp.port], NULL);
                setGA(btd->bus, addr, gatmp);
                if (gatmp.activetime >= 0) {
                    gatmp.activetime =
                        (gatmp.activetime > ga_min_active_time) ?
                        ga_min_active_time : gatmp.activetime;
                    /* next action is auto switch off */
                    gatmp.action = 0;
                }
                else {
                    /* egal wieviel, mind. 75m ein */
                    gatmp.activetime = ga_min_active_time;
                }
                c = 33 + (gatmp.port ? 0 : 1);
                SendByte = gatmp.id;
                writeByte(btd->bus, c, pause_between_bytes);
                writeByte(btd->bus, SendByte, pause_between_bytes);
                if (usleep((unsigned long) gatmp.activetime * 1000) == -1) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                ((M6051_DATA *) buses[btd->bus].driverdata)->
                    cmd32_pending = 1;
            }
            if ((gatmp.action == 0)
                && ((M6051_DATA *) buses[btd->bus].driverdata)->
                cmd32_pending) {
                SendByte = 32;
                writeByte(btd->bus, SendByte, pause_between_cmd);
                ((M6051_DATA *) buses[btd->bus].driverdata)->
                    cmd32_pending = 0;
                setGA(btd->bus, addr, gatmp);
            }

            buses[btd->bus].watchdog = 6;
        }
        buses[btd->bus].watchdog = 7;

        /* read every single S88 state */
        if ((number_fb > 0)
            && !((M6051_DATA *) buses[btd->bus].driverdata)->cmd32_pending) {

            result =
                ioctl(buses[btd->bus].device.file.fd, FIONREAD, &temp);
            if (result == -1) {
                syslog_bus(btd->bus, DBG_ERROR,
                           "ioctl() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }

            while (temp > 0) {
                readByte(btd->bus, 0, &rr);

                result =
                    ioctl(buses[btd->bus].device.file.fd, FIONREAD, &temp);
                if (result == -1) {
                    syslog_bus(btd->bus, DBG_ERROR,
                               "ioctl() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }

                syslog_bus(btd->bus, DBG_INFO,
                           "FB M6051: oops; ignoring unread byte: %d ",
                           rr);
            }
            SendByte = 192 + akt_S88;
            writeByte(btd->bus, SendByte, pause_between_cmd);
            buses[btd->bus].watchdog = 8;
            readByte(btd->bus, 0, &rr);
            temp = rr;
            temp <<= 8;
            buses[btd->bus].watchdog = 9;
            readByte(btd->bus, 0, &rr);
            setFBmodul(btd->bus, akt_S88, temp | rr);
            akt_S88++;
            if (akt_S88 > number_fb)
                akt_S88 = 1;
        }
        buses[btd->bus].watchdog = 10;
        check_reset_fb(btd->bus);
        /* fprintf(stderr, " ende\n"); */
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
