/* $Id: zimo.c 1456 2010-02-28 20:01:39Z gscholz $ */

/*
  Copyright 2003-2007 Matthias Trute <mtrute@users.sourceforge.net>
  Copyright 2004 Frank Schimschke <schmischi@users.sourceforge.net>
  Copyright 2005 Johann Vieselthaler <tpdap@users.sourceforge.net>
  Copyright 2009 Joerg Rottland <rottland@users.sourceforge.net>
  Copyright 2005-2009 Guido Scholz <gscholz@users.sourceforge.net>

  This file is part of srcpd.

  srcpd is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as
  published by the Free Software Foundation.

  srcpd is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with srcpd.  If not, see <http://www.gnu.org/licenses/>.
*/

/*
 * zimo: Zimo MX1 Treiber
 */

#include <errno.h>
#include <fcntl.h>
#include <sys/time.h>
#include <time.h>
#include <sys/ioctl.h>

#ifdef __CYGWIN__
#include <sys/socket.h>         /*for FIONREAD */
#endif
#ifdef __sun__
#include <sys/filio.h>
#endif

#include "config-srcpd.h"
#include "dcc-address.h"
#include "io.h"
#include "zimo.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-sm.h"
#include "srcp-power.h"
#include "srcp-server.h"
#include "srcp-info.h"
#include "srcp-session.h"
#include "syslogmessage.h"


#define __ZIMO ((zimo_DATA*)buses[busnumber].driverdata)

static long tdiff(struct timeval t1, struct timeval t2)
{
    return (t2.tv_sec * 1000 + t2.tv_usec / 1000 - t1.tv_sec * 1000 -
            t1.tv_usec / 1000);
}

int readanswer(bus_t bus, char cmd, char *buf, int maxbuflen,
               long timeout_ms)
{
    int i, cnt = 0;
    int result;
    char c, lc = '\r';
    struct timeval ts, tn;

    gettimeofday(&ts, NULL);
    while (true) {
        result = ioctl(buses[bus].device.file.fd, FIONREAD, &i);
        if (result == -1) {
            syslog_bus(bus, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
            return -1;
        }
        if (i > 0) {
            readByte(bus, 0, (unsigned char *) &c);
            syslog_bus(bus, DBG_INFO, "zimo read %02X", c);

            if ((lc == '\r' || lc == '\n') && c == cmd)
                cnt = 1;

            if (cnt == 1) {
                if (c == '\r' || c == '\n')
                    return cnt;
                if (cnt > maxbuflen)
                    return -2;
                buf[cnt - 1] = c;
                cnt++;
            }
            /* syslog_bus(bus, DBG_INFO, "%ld", tdiff(ts,tn)); */
            gettimeofday(&tn, NULL);
            if (tdiff(ts, tn) > timeout_ms)
                return 0;
            lc = c;
        }
        else if (usleep(1000) == -1) {
            syslog_bus(bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }
}

int readconfig_ZIMO(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    buses[busnumber].driverdata = malloc(sizeof(struct _zimo_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_ZIMO;
    buses[busnumber].init_func = &init_bus_ZIMO;
    buses[busnumber].thr_func = &thr_sendrec_ZIMO;
    strcpy(buses[busnumber].description, "SM GL POWER LOCK DESCRIPTION");

    __ZIMO->number_fb = 0;      /* max 31 */
    __ZIMO->number_ga = 256;    /* M: max 63, N: max 2044, Z: max ? */
    __ZIMO->number_gl = 80;

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
                __ZIMO->number_fb = atoi((char *) txt);
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

        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ZIMO->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ZIMO->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }                           /* while */

    if (init_GL(busnumber, __ZIMO->number_gl)) {
        __ZIMO->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for locomotives");
    }

    if (init_GA(busnumber, __ZIMO->number_ga)) {
        __ZIMO->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessoires");
    }

    if (init_FB(busnumber, __ZIMO->number_fb)) {
        __ZIMO->number_fb = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }
    return (1);
}


int init_linezimo(bus_t bus, char *name)
{
    int fd;
    struct termios interface;

    syslog_bus(bus, DBG_INFO, "Try opening serial line %s for 9600 baud\n",
               name);
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

/* Initialisiere den Bus, signalisiere Fehler */
/* Einmal aufgerufen mit busnummer als einzigem Parameter */
/* return code wird ignoriert (vorerst) */
int init_bus_ZIMO(bus_t bus)
{
    static char *protocols = "MN";
    buses[bus].protocols = protocols;
    syslog_bus(bus, DBG_INFO, "Zimo init: debug %d, device %s",
               buses[bus].debuglevel, buses[bus].device.file.path);
    buses[bus].device.file.fd =
        init_linezimo(bus, buses[bus].device.file.path);
    syslog_bus(bus, DBG_INFO, "Zimo init done");
    return 0;
}


static void handle_power_command(bus_t bus)
{
    char msg[5];

    buses[bus].power_changed = 0;
    snprintf(msg, sizeof(msg), "S%c\r",
             (buses[bus].power_state) ? 'E' : 'A');
    writeString(bus, msg, 0);
    infoPower(bus, msg);
    enqueueInfoMessage(msg);
}


static void handle_sm_command(bus_t bus)
{
    int addr;
    int returnvalue = -1;
    unsigned int error, cv, val;
    struct _SM smtmp;
    char msg[20];

    dequeueNextSM(bus, &smtmp);
    /* syslog_bus(bus, DBG_INFO, "UNQUEUE SM, cmd:%d addr:%d type:%d typeaddr:%d bit:%04X ",smtmp.command,smtmp.addr,smtmp.type,smtmp.typeaddr,smtmp.bit); */
    addr = smtmp.addr;
    if (addr == 0 && smtmp.type == CV
        && (smtmp.typeaddr >= 0 && smtmp.typeaddr < 255)) {
        switch (smtmp.command) {
            case SET:
                syslog_bus(bus, DBG_INFO, "SM SET #%d %02X",
                           smtmp.typeaddr, smtmp.value);
                snprintf(msg, sizeof(msg), "RN%02X%02X\r",
                         smtmp.typeaddr, smtmp.value);
                writeString(bus, msg, 0);

                session_lock_wait(bus);
                if (readanswer(bus, 'Q', msg, 20, 1000) > 3) {
                    sscanf(&msg[1], "%2X%2X%2X", &error, &cv, &val);
                    if (!error && cv == smtmp.typeaddr
                        && val == smtmp.value)
                        returnvalue = 0;
                }
                session_endwait(bus, val);

                gettimeofday(&smtmp.tv, NULL);
                setSM(bus, smtmp.type, addr, smtmp.typeaddr,
                      smtmp.bit, smtmp.value, 0);
                break;
            case GET:
                syslog_bus(bus, DBG_INFO, "SM GET #%d", smtmp.typeaddr);
                snprintf(msg, sizeof(msg), "Q%02X\r", smtmp.typeaddr);
                writeString(bus, msg, 0);

                session_lock_wait(bus);
                if (readanswer(bus, 'Q', msg, 20, 10000) > 3) {
                    /* sscanf(&msg[1],"%2X%2X%2X",&error,&cv,&val); */
                    sscanf(&msg[1], "%*3c%2X%2X", &cv, &val);
                    syslog_bus(bus, DBG_INFO,
                               "SM GET ANSWER: error %d, cv %d, val %d",
                               error, cv, val);
                    /* if(!error && cv==smtmp.typeaddr) */
                    returnvalue = val;
                }
                session_endwait(bus, returnvalue);
                break;
        }
    }
    buses[bus].watchdog = 4;
}


static void handle_gl_command(bus_t bus)
{
    gl_state_t gltmp, glakt;
    int addr, temp, i;
    int result;
    char databyte1, databyte2, databyte3;
    char rr;
    char msg[20];

    dequeueNextGL(bus, &gltmp);
    addr = gltmp.id;
    cacheGetGL(bus, addr, &glakt);

    databyte1 = (gltmp.direction ? 0 : 32);
    databyte1 |= (gltmp.funcs & 0x01) ? 16 : 0;

    if (glakt.n_fs == 128)
        databyte1 |= 12;
    if (glakt.n_fs == 28)
        databyte1 |= 8;
    if (glakt.n_fs == 14)
        databyte1 |= 4;

    databyte2 = gltmp.funcs >> 1;
    databyte3 = '\0';

    if (addr > 128) {
        snprintf(msg, sizeof(msg), "E%04X\r", addr);
        syslog_bus(bus, DBG_INFO, "%s", msg);
        writeString(bus, msg, 0);
        addr = 0;
        i = readanswer(bus, 'E', msg, 20, 40);
        syslog_bus(bus, DBG_INFO, "read %d", i);

        switch (i) {
            case 8:
                sscanf(&msg[1], "%02X", &addr);
                break;
            case 10:
                sscanf(&msg[3], "%02X", &addr);
                break;
        }
    }
    if (addr > 0) {
        snprintf(msg, sizeof(msg), "F%c%02X%02X%02X%02X%02X\r",
                 glakt.protocol, addr, gltmp.speed, databyte1, databyte2,
                 databyte3);
        syslog_bus(bus, DBG_INFO, "%s", msg);
        writeString(bus, msg, 0);

        result = ioctl(buses[bus].device.file.fd, FIONREAD, &temp);
        if (result == -1) {
            syslog_bus(bus, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        while (temp > 0) {
            readByte(bus, 0, (unsigned char *) &rr);

            result = ioctl(buses[bus].device.file.fd, FIONREAD, &temp);
            if (result == -1) {
                syslog_bus(bus, DBG_ERROR,
                           "ioctl() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            syslog_bus(bus, DBG_INFO,
                       "ignoring unread byte: %d (%c)", rr, rr);
        }
        cacheSetGL(bus, addr, gltmp);
    }
}


static void handle_ga_command(bus_t bus)
{
    ga_state_t gatmp;
    struct timeval akt_time;
    unsigned int nmra_addr;
    unsigned char address, databyte;
    char msg[20];

    dequeueNextGA(bus, &gatmp);

    gettimeofday(&akt_time, NULL);
    gatmp.tv[gatmp.port] = akt_time;
    setGA(bus, gatmp.id, gatmp);

    /* We assume SRCP addresses are according to Lenz-DCC address scheme */
    lenz2_to_nmra2(gatmp.id, gatmp.port, &nmra_addr, &databyte);

    address = nmra_addr & 0xff;

    /* set bit 7 if necessary, "N" and "Z" are valid protocols */
    if ((gatmp.protocol != 'M') && (nmra_addr > 255))
        databyte |= 0x80;

    /* set bit 3 for "on" */
    if (gatmp.action != 0)
        databyte |= 0x08;

    snprintf(msg, sizeof(msg), "M%c%02X%02X\r", gatmp.protocol,
             address, databyte);
    syslog_bus(bus, DBG_DEBUG, "MX1 message: %s", msg);
    writeString(bus, msg, 0);
}


/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "Zimo bus terminated.");

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

void *thr_sendrec_ZIMO(void *v)
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

    syslog_bus(btd->bus, DBG_INFO, "Zimo bus started (device = %s)",
               buses[btd->bus].device.file.path);
    buses[btd->bus].watchdog = 1;

    while (true) {
        pthread_testcancel();

        /* POWER command has arrived */
        if (buses[btd->bus].power_changed == 1)
            handle_power_command(btd->bus);

        /* GL command has arrived */
        if (!queue_GL_isempty(btd->bus))
            handle_gl_command(btd->bus);

        /* GA command has arrived */
        if (!queue_GA_isempty(btd->bus))
            handle_ga_command(btd->bus);

        /* SM command has arrived */
        if (!queue_SM_isempty(btd->bus))
            handle_sm_command(btd->bus);

        /* wait some time and start again */
        if (usleep(1000) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
