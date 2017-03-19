/**
 * This software is published under the terms of the GNU General Public
 * License, Version 2.0
 * Gerard van der Sel 
 *
 * Version 2.1: 20071006: Re-release of SLX852 and error recovery
 * Version 2.0: 20070418: Release of SLX852
 * Version 1.4: 20070315: Communication with the SLX852
 * Version 1.3: 20070213: Communication with events
 * Version 1.2: 20060526: Text reformatting and error checking
 * Version 1.1: 20060505: Configuration of fb addresses from srcpd.conf
 * Version 1.0: 20050601: Release of Selectrix protocol
 * Version 0.4: 20050521: Feedback response
 * Version 0.3: 20050521: Controlling a switch/signal
 * Version 0.2: 20050514: Controlling a engine
 * Version 0.1: 20050508: Connection with CC-2000 and power on/off
 * Version 0.0: 20050501: Translated file from file M605X which compiles
 */

/**
 * This software does the translation for a selectrix central centre
 * An old Central centre is the default selection
 * In the XML-file the control centre can be changed to the new CC-2000
 *   A CC-2000 can program a engine
 *   (Control centre of MUT and Uwe Magnus are CC-2000 compatible).
 */

#include <errno.h>
#include <unistd.h>
#include <stdbool.h>

#include "portio.h"
#include "config-srcpd.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "srcp-server.h"
#include "srcp-error.h"
#include "srcp-sm.h"
#include "srcp-gl.h"
#include "srcp-ga.h"
#include "srcp-fb.h"
#include "selectrix.h"
#include "syslogmessage.h"
#include "ttycygwin.h"

int syncSXbus(bus_t busnumber);
void commandreadSXbus(bus_t busnumber, int SXadres);
int readSXbus(bus_t busnumber);
void writeSXbus(bus_t busnumber, int SXadres, int SXdata);

/* Macro definition */
#define __selectrix ((SELECTRIX_DATA *)buses[busnumber].driverdata)
#define __selectrixt ((SELECTRIX_DATA *)buses[btd->bus].driverdata)
#define __checkSXflag(flag) ((__selectrix->SXflags & (flag)) == (flag))

/*******************************************************************
* readconfig_Selectrix:
* Reads selectrix specific XML nodes and sets up bus specific data.
* Called by register_bus().
********************************************************************/
int readconfig_Selectrix(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    int i, offset;
    int portindex = 0;

    syslog_bus(busnumber, DBG_INFO, "Reading Selectrix specific data.");

    buses[busnumber].driverdata = malloc(sizeof(struct _SELECTRIX_DATA));
    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    /* Selectrix specific data to the global data */
    buses[busnumber].type = SERVER_SELECTRIX;
    buses[busnumber].device.file.baudrate = B9600;
    buses[busnumber].init_func = &init_bus_Selectrix;
    buses[busnumber].thr_func = &thr_commandSelectrix;
    buses[busnumber].thr_timer = &thr_feedbackSelectrix;
    buses[busnumber].sigio_reader = &sig_processSelectrix;
    buses[busnumber].init_gl_func = &init_gl_Selectrix;
    buses[busnumber].init_ga_func = &init_ga_Selectrix;
    buses[busnumber].init_fb_func = &init_fb_Selectrix;
    /* Initialise Selectrix part */
    __selectrix->number_gl = 0;
    __selectrix->number_ga = 0;
    __selectrix->number_fb = 0;
    __selectrix->SXflags = 0;
    __selectrix->stateInterface = 0;
    __selectrix->currentFB = 1;
    __selectrix->max_address = 0x100;   /* SXmax; */
    /* Initialise the two array's */
    for (i = 0; i < SXmax; i++) {
        __selectrix->bus_data[i] = 0;   /* Set all outputs to 0 */
        __selectrix->fb_adresses[i] = 255;      /* Set invalid addresses */
    }
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
                __selectrix->number_fb = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __selectrix->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __selectrix->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "controller") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "CC2000") == 0) {
                    __selectrix->SXflags |= CC2000_MODE;
                    /* Last 8 addresses for the CC2000 */
                    /* __selectrix->max_address = SXcc2000; */
                    strcpy(buses[busnumber].description,
                           "GA GL FB SM POWER LOCK DESCRIPTION");
                }
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "interface") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "RTHS_0") == 0) {
                    /* Select  Selectrix mode */
                    __selectrix->SXflags |= Rautenhaus_MODE;
                }
                else if (xmlStrcmp(txt, BAD_CAST "RTHS_1") == 0) {
                    /* Select  Selectrix mode and two buses */
                    __selectrix->SXflags |= Rautenhaus_MODE;
                    __selectrix->SXflags |= Rautenhaus_DBL;
                }
                else if (xmlStrcmp(txt, BAD_CAST "RTHS_2") == 0) {
                    /* Select Rautenhaus mode */
                    __selectrix->SXflags |= Rautenhaus_MODE;
                    __selectrix->SXflags |= Rautenhaus_FDBCK;
                    __selectrix->SXflags |= Rautenhaus_ADR;
                }
                else if (xmlStrcmp(txt, BAD_CAST "RTHS_3") == 0) {
                    /* Select Rautenhaus mode and two buses */
                    __selectrix->SXflags |= Rautenhaus_MODE;
                    __selectrix->SXflags |= Rautenhaus_DBL;
                    __selectrix->SXflags |= Rautenhaus_FDBCK;
                    __selectrix->SXflags |= Rautenhaus_ADR;
                }
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "ports") == 0) {
            portindex = 1;
            xmlNodePtr subchild = child->children;
            xmlChar *subtxt = NULL;
            while (subchild != NULL) {
                if (xmlStrncmp(subchild->name, BAD_CAST "text", 4) == 0) {
                    /* just do nothing, it is only a comment */
                }
                else if (xmlStrcmp(subchild->name, BAD_CAST "port") == 0) {
                    /* Check if on the second SX-bus */
                    offset = 0;
                    xmlChar *pOffset = xmlGetProp(subchild,
                                                  BAD_CAST "sxbus");
                    if (pOffset != NULL) {
                        if (atoi((char *) pOffset) == 1) {
                            offset = 128;
                        }
                    }
                    free(pOffset);
                    /* Get address */
                    subtxt = xmlNodeListGetString(doc,
                                                  subchild->
                                                  xmlChildrenNode, 1);
                    if (subtxt != NULL) {
                        /* Store address and number SXbus */
                        __selectrix->fb_adresses[portindex] =
                            atoi((char *) subtxt) + offset;
                        xmlFree(subtxt);
                        if (__selectrix->number_fb > portindex) {
                            portindex++;
                        }
                    }
                }
                else {
                    syslog_bus(busnumber, DBG_WARN,
                               "WARNING, unknown sub "
                               "tag found: \"%s\"!\n", subchild->name);
                }
                subchild = subchild->next;
            }
        }
        else {
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);
        }
        child = child->next;
    }

    if ((__selectrix->number_gl + __selectrix->number_ga +
         __selectrix->number_fb) < __selectrix->max_address) {
        if (init_GL(busnumber, __selectrix->number_gl)) {
            __selectrix->number_gl = 0;
            syslog_bus(busnumber, DBG_ERROR, "Can't create array "
                       "for locomotives");
        }
        if (init_GA(busnumber, __selectrix->number_ga)) {
            __selectrix->number_ga = 0;
            syslog_bus(busnumber, DBG_ERROR, "Can't create array "
                       "for accessories");
        }
        if (init_FB(busnumber, __selectrix->number_fb * 8)) {
            __selectrix->number_fb = 0;
            syslog_bus(busnumber, DBG_ERROR, "Can't create array "
                       "for feedback");
        }
    }
    else {
        __selectrix->number_gl = 0;
        __selectrix->number_ga = 0;
        __selectrix->number_fb = 0;
        syslog_bus(busnumber, DBG_ERROR, "Too many devices on the SX-bus");
    }
    syslog_bus(busnumber, DBG_WARN,
               "Found %d loco's.", __selectrix->number_gl);
    syslog_bus(busnumber, DBG_WARN,
               "Found %d switches.", __selectrix->number_ga);
    syslog_bus(busnumber, DBG_WARN,
               "Found %d feedback modules.", __selectrix->number_fb);
    if (portindex != 0) {
        for (i = 1; i <= portindex; i++) {
            syslog_bus(busnumber, DBG_WARN,
                       "Found feedback port number %d with address %d.",
                       i, __selectrix->fb_adresses[i]);
        }
    }
    return 1;
}


/****************************************************************************
* Manage a serial port for communication with a selectrix interface
*****************************************************************************/
/* Opens a serial port */
/* On success the port handle is changed to a value <> -1 */
int init_bus_Selectrix(bus_t busnumber)
{
    static char *protocols = "S";
    buses[busnumber].protocols = protocols;
    syslog_bus(busnumber, DBG_INFO, "Selectrix init: debuglevel %d",
               buses[busnumber].debuglevel);
    if (buses[busnumber].debuglevel <= DBG_DEBUG) {
        open_port(busnumber);
        syslog_bus(busnumber, DBG_INFO,
                   "Selectrix init done, fd=%d",
                   buses[busnumber].device.file.fd);
        syslog_bus(busnumber, DBG_INFO,
                   "Selectrix description: %s",
                   buses[busnumber].description);
        syslog_bus(busnumber, DBG_INFO,
                   "Selectrix flags: %04X (hex)", __selectrix->SXflags);
    }
    else {
        buses[busnumber].device.file.fd = -1;
    }
    return 0;
}

/*******************************************************
*     Device initialisation
********************************************************/
/* Engines */
/* INIT <bus> GL <adr> S 1 31 2 */
int init_gl_Selectrix(gl_state_t * gl)
{
    if ((gl->protocol == 'S') || (gl->protocol == 's')) {
        return ((gl->n_fs == 31) && (gl->protocolversion == 1) &&
                (gl->n_func == 2)) ? SRCP_OK : SRCP_WRONGVALUE;
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

/* Switches, signals, ... */
/* INIT <bus> GA <adr> S */
int init_ga_Selectrix(ga_state_t * ga)
{
    if ((ga->protocol == 'S') || (ga->protocol == 's')) {
        return SRCP_OK;
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

/* Feedback modules */
/* INIT <bus> FB <adr> S <index> */
int init_fb_Selectrix(bus_t busnumber, int adres,
                      const char protocol, int index)
{
    if ((protocol == 'S') || (protocol == 's')) {
        if ((__selectrix->max_address > adres) &&
            (__selectrix->number_fb >= index)) {
            __selectrix->fb_adresses[index] = adres;
            return SRCP_OK;
        }
        else {
            return SRCP_WRONGVALUE;
        }
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

/*******************************************************
*     Rautenhaus setup
********************************************************/
/* Make configuration byte for Rautenhaus interface */
void confRautenhaus(bus_t busnumber)
{
    int configuration;

    /* Check if a Rautenhaus devise is connected */
    if (__checkSXflag(Rautenhaus_MODE)) {
        /* Start with bus 0 selection */
        configuration = RautenhsB0;
        if (__checkSXflag(Rautenhaus_DBL + Rautenhaus_RTBS)) {
            /* Bus 1 selected so change to bus 1. */
            configuration = RautenhsB1;
        }
        else {
            __selectrix->SXflags &= ~Rautenhaus_RTBS;
        }
        if (__checkSXflag(Rautenhaus_FDBCK)) {
            /* Rautenhaus mode */
            configuration |= (cntrlON + fdbckON);
            if (__checkSXflag(CC2000_MODE)) {
                /* CC2000, don't check address 111 */
                /* Don't check bus 0 */
                configuration |= clkOFF0;
                if (__checkSXflag(Rautenhaus_DBL)) {
                    /* Don't check bus 1 */
                    configuration |= clkOFF1;
                }
            }
        }
        else {
            /* Selectrix mode */
            configuration |= (cntrlOFF + fdbckOFF);
        }
        /* Write configuration to the device */
        write_port(busnumber, SXwrite + RautenhsCC);
        write_port(busnumber, configuration);
        syslog_bus(busnumber, DBG_INFO,
                   "Selectrix on bus %ld, Rautenhaus "
                   "configuration is: %02X (hex).",
                   busnumber, configuration);
    }
}

/* Make bus selector for Rautenhaus interface */
void selRautenhaus(bus_t busnumber, int adres)
{
    if (__checkSXflag(Rautenhaus_MODE + Rautenhaus_DBL)) {
        if (adres > 127) {
            /* Addresses 128 ...  255 => bus 1 */
            /* Check if bus 1 selected */
            if (!(__checkSXflag(Rautenhaus_RTBS))) {
                /* No, select bus 1 */
                write_port(busnumber, SXwrite + RautenhsCC);
                write_port(busnumber, RautenhsB1);
                __selectrix->SXflags |= Rautenhaus_RTBS;
                syslog_bus(busnumber, DBG_WARN,
                           "Selectrix on bus %ld, Rautenhaus "
                           "bus 1 selected.", busnumber);
            }
        }
        else {
            /* Addresses 0 ... 127 => bus 0 */
            /* Check if bus 0 selected */
            if (__checkSXflag(Rautenhaus_RTBS)) {
                /* No, select bus 0 */
                write_port(busnumber, SXwrite + RautenhsCC);
                write_port(busnumber, RautenhsB0);
                __selectrix->SXflags &= ~Rautenhaus_RTBS;
                syslog_bus(busnumber, DBG_WARN,
                           "Selectrix on bus %ld, Rautenhaus "
                           "bus 0 selected.", busnumber);
            }
        }
    }
}

/*******************************************************
*     Base communication with the interface (Selectrix)
********************************************************/
/* Read data from the SX-bus (8 bits) */
int readSXbus(bus_t busnumber)
{
    unsigned int rr;

    if (buses[busnumber].device.file.fd != -1) {
        /* Wait until a character arrives */
        rr = read_port(busnumber);
        syslog_bus(busnumber, DBG_DEBUG,
                   "Selectrix on bus %ld, read byte %02X (hex).",
                   busnumber, rr);
        if (rr < 0x100) {
            return rr;
        }
    }
    return 0xFF;                /* Error or closed, return all blocked */
}

void commandreadSXbus(bus_t busnumber, int SXadres)
{
    if (buses[busnumber].device.file.fd != -1) {
        /* Select Rautenhaus bus */
        selRautenhaus(busnumber, SXadres);
        SXadres &= 0x7F;
        /* write SX-address and the read command */
        write_port(busnumber, SXread + SXadres);
        /* extra byte for power to receive data */
        write_port(busnumber, SXempty);
        /* receive data */
    }
    else {
        syslog_bus(busnumber, DBG_ERROR,
                   "Selectrix on bus %ld, address %d not read.",
                   busnumber, SXadres);
    }
}

/* Write data to the SX-bus (8bits) */
void writeSXbus(bus_t busnumber, int SXadres, int SXdata)
{
    if (buses[busnumber].device.file.fd != -1) {
        /* Select Rautenhaus bus */
        selRautenhaus(busnumber, SXadres);
        SXadres &= 0x7F;
        /* write SX-address and the write command */
        write_port(busnumber, SXwrite + SXadres);
        /* write data to the SX-bus */
        write_port(busnumber, SXdata);
    }
    else {
        syslog_bus(busnumber, DBG_ERROR,
                   "Selectrix on bus %ld, byte %02X (hex) not to address %d.",
                   busnumber, SXdata, SXadres);
    }
}

/*******************************************************
*     Decoder reading/programming (Selectrix)
********************************************************/
int chkCC2000Status(bus_t busnumber, int step, int statFlag)
{
    int wait;

    /* Get status byte of the CC2000 */
    commandreadSXbus(busnumber, SXstatus);
    __selectrix->stateInterface = step;
    wait = 10;
    while ((__selectrix->stateInterface < (step + 1)) || (wait == 0)) {
        if (usleep(500) == -1) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        wait--;
    }
    /* Check flag in status byte */
    return (((readSXbus(busnumber) & statFlag) == statFlag) ? -1 : 0);
}

/* Configure CC200 for reading/writing decoders */
int chkCC2000Ready(bus_t busnumber)
{
    int readyCC2000;

    /* Check if ready */
    if (chkCC2000Status(busnumber, 10, SXstready)) {
        /* Check if power off */
        if (!chkCC2000Status(busnumber, 10, SXstpower)) {
            /* Now CC2000 ready */
            readyCC2000 = 0;
        }
        else {
            syslog_bus(busnumber, DBG_DEBUG,
                       "Selectrix on bus %ld, power stil on the track.",
                       busnumber);
            readyCC2000 = 2;
        }
    }
    else {
        syslog_bus(busnumber, DBG_DEBUG,
                   "Selectrix on bus %ld, interface not ready.",
                   busnumber);
        readyCC2000 = 1;
    }
    return readyCC2000;
}

/* Read decoder data to the SX-bus */
int readSXDecoder(bus_t busnumber)
{
    int SXdecoder;
    int waitCount;
    int result;

    /* Check if ready */
    if (chkCC2000Ready(busnumber) == 0) {
        /* Start reading in Selectrix mode */
        writeSXbus(busnumber, SXcommand,
                   SXcmdstart + SXcmdprog + SXcmdmodus);
        result = sleep(250000);
        if (result != 0) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "sleep() interrupted, %d seconds left\n", result);
        }
        /* Stop reading */
        writeSXbus(busnumber, SXcommand, SXcmdstart + SXcmdmodus);
        /* Get lowerbyte of decoder data */
        commandreadSXbus(busnumber, SXprog1);
        __selectrix->stateInterface = 12;
        waitCount = 10;
        while ((__selectrix->stateInterface < (13)) || (waitCount == 0)) {
            if (usleep(500) == -1) {
                syslog_bus(busnumber, DBG_DEBUG,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            waitCount--;
        }
        if (waitCount > 0) {
            SXdecoder = readSXbus(busnumber);
            /* Get higherbyte of decoder data */
            commandreadSXbus(busnumber, SXprog2);
            __selectrix->stateInterface = 12;
            waitCount = 10;
            while ((__selectrix->stateInterface < (13))
                   || (waitCount == 0)) {
                if (usleep(500) == -1) {
                    syslog_bus(busnumber, DBG_DEBUG,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitCount--;
            }
            if (waitCount > 0) {
                SXdecoder = SXdecoder + 256 * readSXbus(busnumber);
                return SXdecoder;
            }
        }
    }
    return -1;                  /* Invalid decoder data */
}

/* Write data on SX-bus to the decoder */
void writeSXDecoder(bus_t busnumber, int SXdecoder)
{
    int timeout;

    /* Check if ready */
    if (chkCC2000Ready(busnumber) == 0) {
        /* Write decoderdata to SX-bus (lower half) */
        writeSXbus(busnumber, SXprog1, SXdecoder & 0xff);
        /* Write decoderdata to SX-bus (upper half) */
        writeSXbus(busnumber, SXprog2, (SXdecoder / 256) & 0xff);
        /* Start Programming in Selectrix mode */
        writeSXbus(busnumber, SXcommand,
                   SXcmdstart + SXcmdprog + SXcmddcod + SXcmdmodus);
        /*  Wait 3 seconds */
        if (usleep(3000000) == -1) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        timeout = 1000;
        while (timeout > 0) {
            if (chkCC2000Status(busnumber, 14, SXstready) == 0) {
                timeout = 0;
            }
            else {
                timeout--;
                if (usleep(500) == -1) {
                    syslog_bus(busnumber, DBG_DEBUG,
                               "usleep() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
            }
        }
        /* Stop programming */
        writeSXbus(busnumber, SXcommand, SXcmdstart + SXcmdmodus);
    }
}

/*******************************************************
*     Command generation (Selectrix)
********************************************************/
/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "Selectrix bus terminated.");
    if (buses[btd->bus].device.file.fd != -1) {
        close_port(btd->bus);
    }

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

void *thr_commandSelectrix(void *v)
{
    int addr, data, state;
    struct _SM smtmp;
    gl_state_t gltmp;
    ga_state_t gatmp;
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));
    if (btd == NULL)
        pthread_exit((void *) 1);       /* Exit thread */
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);

    /* register cleanup routine */
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    syslog_bus(btd->bus, DBG_INFO,
               "Selectrix bus command thread started.");
    buses[btd->bus].watchdog = 0;
    while (1) {
        state = 0;
        pthread_testcancel();
        buses[btd->bus].watchdog = 1;
        /* Start/Stop */
        if (buses[btd->bus].power_changed != 0) {
            state = 1;
            buses[btd->bus].watchdog = 2;
            char msg[1000];
            if ((buses[btd->bus].power_state)) {
                /* Turn power on */
                writeSXbus(btd->bus, SXcontrol, 0x80);
                confRautenhaus(btd->bus);
            }
            else {
                /* Turn power off */
                writeSXbus(btd->bus, SXcontrol, 0x00);
            }
            infoPower(btd->bus, msg);
            enqueueInfoMessage(msg);
            syslog_bus(btd->bus, DBG_WARN,
                       "Selectrix had a power change.");
            buses[btd->bus].power_changed = 0;
        }
        /* Programming */
        /* Only programming if power is off */
        if (buses[btd->bus].power_state == 0) {
            if (!queue_SM_isempty(btd->bus)) {
                state = 2;
                buses[btd->bus].watchdog = 3;
                dequeueNextSM(btd->bus, &smtmp);
                session_lock_wait(btd->bus);
                switch (smtmp.command) {
                    case SET:
                        /* Write data to decoder */
                        writeSXDecoder(btd->bus, smtmp.value);
                        break;
                    case GET:
                    case VERIFY:
                        /* Read data from decoder */
                        smtmp.value = readSXDecoder(btd->bus);
                        break;
                }
                session_endwait(btd->bus, smtmp.value);
            }
        }
        /* Loco decoders */
        if (!queue_GL_isempty(btd->bus)) {
            state = 3;
            buses[btd->bus].watchdog = 4;
            dequeueNextGL(btd->bus, &gltmp);
            /* Address of the engine */
            addr = gltmp.id;
            /* Check if valid address */
            if (__selectrixt->max_address > addr) {
                /* Check: terminating the engine */
                if (gltmp.state == 2) {
                    syslog_bus(btd->bus, DBG_WARN,
                               "Selectrix engine "
                               "with address %d is removed", addr);
                }
                else {
                    /* Direction */
                    switch (gltmp.direction) {
                        case 0:
                            /* Backward */
                            data = 0x20;
                            break;
                        case 1:
                            /* Forward */
                            data = 0x00;
                            break;
                        default:
                            /* Emergency stop or ... */
                            /* Get last direction */
                            data = __selectrixt->bus_data[addr] & 0x20;
                            gltmp.speed = 0;
                            break;
                    }
                    /* Speed, Light, Function */
                    data = data + gltmp.speed +
                        ((gltmp.funcs & 0x01) ? 0x40 : 0) +
                        ((gltmp.funcs & 0x02) ? 0x80 : 0);
                    writeSXbus(btd->bus, addr, data);
                    __selectrixt->bus_data[addr] = data;
                    cacheSetGL(btd->bus, addr, gltmp);
                    syslog_bus(btd->bus, DBG_WARN,
                               "Selectrix "
                               "engine with address %d "
                               "has data %02X (hex).", addr, data);
                }
            }
            else {
                syslog_bus(btd->bus, DBG_ERROR,
                           "Selectrix invalid "
                           "address %d with engine", addr);
            }
        }
        /* Drives solenoids and signals */
        if (!queue_GA_isempty(btd->bus)) {
            state = 4;
            buses[btd->bus].watchdog = 5;
            dequeueNextGA(btd->bus, &gatmp);
            addr = gatmp.id;
            if (__selectrixt->max_address > addr) {
                data = __selectrixt->bus_data[addr];
                /* Select the action to do */
                if (gatmp.action == 0) {
                    /* Set pin to "0" */
                    switch (gatmp.port) {
                        case 1:
                            data &= 0xfe;
                            break;
                        case 2:
                            data &= 0xfd;
                            break;
                        case 3:
                            data &= 0xfb;
                            break;
                        case 4:
                            data &= 0xf7;
                            break;
                        case 5:
                            data &= 0xef;
                            break;
                        case 6:
                            data &= 0xdf;
                            break;
                        case 7:
                            data &= 0xbf;
                            break;
                        case 8:
                            data &= 0x7f;
                            break;
                        default:
                            syslog_bus(btd->bus, DBG_WARN,
                                       "Selectrix invalid "
                                       "port number %d with "
                                       "switch/signal or ...", gatmp.port);
                            break;
                    }
                }
                else {
                    /* Set pin to "1" */
                    switch (gatmp.port) {
                        case 1:
                            data |= 0x01;
                            break;
                        case 2:
                            data |= 0x02;
                            break;
                        case 3:
                            data |= 0x04;
                            break;
                        case 4:
                            data |= 0x08;
                            break;
                        case 5:
                            data |= 0x10;
                            break;
                        case 6:
                            data |= 0x20;
                            break;
                        case 7:
                            data |= 0x40;
                            break;
                        case 8:
                            data |= 0x80;
                            break;
                        default:
                            syslog_bus(btd->bus, DBG_WARN,
                                       "Selectrix invalid "
                                       "port number %d with "
                                       "switch/signal or ...", gatmp.port);
                            break;
                    }
                }
                writeSXbus(btd->bus, addr, data);
                __selectrixt->bus_data[addr] = data;
                syslog_bus(btd->bus, DBG_WARN,
                           "Selectrix address %d "
                           "has new data %02X (hex).", addr, data);
            }
            else {
                syslog_bus(btd->bus, DBG_ERROR,
                           "Selectrix invalid "
                           "address %d with switch/signal or ...", addr);
            }
        }
        /* Feed back contacts */
        if ((__selectrixt->number_fb > 0) &&
            (__selectrixt->stateInterface == 1)) {
            state = 5;
            buses[btd->bus].watchdog = 6;
            /* Fetch the module address */
            addr = __selectrixt->fb_adresses[__selectrixt->currentFB];
            /* Send command to read the SX-bus */
            __selectrixt->stateInterface = 2;
            commandreadSXbus(btd->bus, addr);
        }
        if (state == 0) {
            /* Lock thread till new data to process arrives */
            suspend_bus_thread(btd->bus);
        }
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}

/*******************************************************
*     Timed command generation (Selectrix)
********************************************************/
void *thr_feedbackSelectrix(void *v)
{
    int addr;
    int respondtime;

    bus_t busnumber = (bus_t) v;
    syslog_bus(busnumber, DBG_INFO, "Selectrix "
               "feedback thread started.");
    respondtime = 0;
    while (1) {
        /* Feed back contacts */
        if ((__selectrix->number_fb > 0) &&
            !(__checkSXflag(Rautenhaus_MODE + Rautenhaus_FDBCK))) {
            switch (__selectrix->stateInterface) {
                case 0:
                    /* Fetch the module address */
                    addr =
                        __selectrix->fb_adresses[__selectrix->currentFB];
                    if (__selectrix->max_address > addr) {
                        /* Let thread process a feedback */
                        syslog_bus(busnumber, DBG_INFO,
                                   "Selectrix address %d selected.", addr);
                        __selectrix->stateInterface = 1;
                        resume_bus_thread(busnumber);
                    }
                    else {
                        syslog_bus(busnumber, DBG_INFO,
                                   "Selectrix "
                                   "invalid address %d "
                                   "with feedback index %d.",
                                   addr, __selectrix->currentFB);
                        __selectrix->stateInterface = 0;
                        __selectrix->currentFB = 1;
                    }
                    respondtime = 0;
                    break;
                case 1:
                case 2:
                    respondtime++;
                    if (respondtime > 50) {
                        __selectrix->stateInterface = 0;
                        __selectrix->currentFB = 1;
                        respondtime = 0;
                    }
                    break;
                default:
                    __selectrix->stateInterface = 0;
                    __selectrix->currentFB = 1;
                    respondtime = 0;
                    break;
            }
            /* Process every feedback 4 times per second */
            if (usleep(250000 / __selectrix->number_fb) == -1) {
                syslog_bus(busnumber, DBG_DEBUG,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
        }
        else {
            if (usleep(1000000) == -1) {
                syslog_bus(busnumber, DBG_DEBUG,
                           "usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
        }
    }
}

/*******************************************************
*     Command processing (Selectrix)
********************************************************/
void sig_processSelectrix(bus_t busnumber)
{
    int data, addr;
    int found;
    int dataUP, i;

    __selectrix->SXflags |= Connection;
    syslog_bus(busnumber, DBG_DEBUG, "Selectrix SIGIO processed.");
    /* Read the SX-bus */
    data = readSXbus(busnumber);
    switch (__selectrix->stateInterface) {
            /* Reading Selectrix interface */
        case 2:
            addr = __selectrix->fb_adresses[__selectrix->currentFB];
            syslog_bus(busnumber, DBG_INFO,
                       "Selectrix address %d "
                       "has feedback data %02X (hex).", addr, data);
            __selectrix->bus_data[addr] = data;
            /* Rotate bits 7 ... 0 to 1 ... 8 */
            dataUP = 0;
            for (i = 0; i < 8; i++) {
                dataUP = dataUP * 2;
                dataUP = dataUP + (data & 0x01);
                data = data / 2;
            }
            /* Set the daemon global data */
            /* Use 1, 2, ... as address for feedback */
            setFBmodul(busnumber, __selectrix->currentFB, dataUP);
            /* Use real address for feedback */
            // setFBmodul(busnumber, addr, dataUP);
            /* Select the next module */
            if (__selectrix->currentFB >= __selectrix->number_fb) {
                /* Reset to start */
                __selectrix->currentFB = 1;
            }
            else {
                /* Next */
                __selectrix->currentFB++;
            }
            __selectrix->stateInterface = 0;
            break;
            /* Reading and programming a decoder */
        case 10:
            __selectrix->stateInterface = 11;
            break;
        case 12:
            __selectrix->stateInterface = 13;
            break;
        case 14:
            __selectrix->stateInterface = 15;
            break;
            /* Reading a Rautenhaus interface */
        default:
            if (__checkSXflag(Rautenhaus_MODE + Rautenhaus_FDBCK)) {
                if (__checkSXflag(Rautenhaus_ADR)) {
                    /* 1: SX-bus address */
                    found = true;
                    __selectrix->currentFB = 1;
                    while ((found == true) &&
                           !(__selectrix->currentFB >
                             __selectrix->number_fb)) {
                        if (data ==
                            __selectrix->fb_adresses[__selectrix->
                                                     currentFB]) {
                            found = false;
                            __selectrix->SXflags &= ~Rautenhaus_ADR;
                        }
                        else {
                            __selectrix->currentFB++;
                        }
                    }
                }
                else {
                    /* 0: SX-bus data */
                    addr =
                        __selectrix->fb_adresses[__selectrix->currentFB];
                    syslog_bus(busnumber, DBG_INFO,
                               "Selectrix address %d "
                               "has feedback data %02X (hex).", addr,
                               data);
                    __selectrix->bus_data[addr] = data;
                    /* Rotate bits 7 ... 0 to 1 ... 8 */
                    dataUP = 0;
                    for (i = 0; i < 8; i++) {
                        dataUP = dataUP * 2;
                        dataUP = dataUP + (data & 0x01);
                        data = data / 2;
                    }
                    /* Set the daemon global data */
                    /* Use 1, 2, ... as address for feedback */
                    setFBmodul(busnumber, __selectrix->currentFB, dataUP);
                    /* Use real address for feedback */
                    /* setFBmodul(busnumber, addr, dataUP); */
                    __selectrix->SXflags |= Rautenhaus_ADR;
                }
            }
            else {
                syslog_bus(busnumber, DBG_INFO,
                           "Selectrix discarded data %02X (hex).", data);
                __selectrix->stateInterface = 0;
                __selectrix->currentFB = 1;
            }
            break;
    }
}
