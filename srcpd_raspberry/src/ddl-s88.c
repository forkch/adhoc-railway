/***************************************************************************
                          ddl-s88.c  -  description
                             -------------------
    begin                : Wed Aug 1 2001
    copyright            : (C) 2001 by Dipl.-Ing. Frank Schmischke
    email                : frank.schmischke@t-online.de

    This source based on errdcd-source code by Torsten Vogt.
    full header statement below!
 ***************************************************************************/
/*
 * SIG
 * 25.08.12:
 * RaspberryPI Portierung. Nicht ganz sauber, aber einfach:
 * Wenn __arm__ definiert ist wird RaspberryPI angenommen.
 * Für den GPIO Zugriff wird die lib bcm2835 (http://www.open.com.au/mikem/bcm2835/index.html) verwendet.
 * Diese muss installiert sein.
 * 
 * 30.09.13:
 * RaspberryPI Erkennung über "configure" (CPU ARM und cpuinfo BCM2708).
 * Unterscheidung RaspberryPI Board Version 1 und 2.
 *
 * 06.04.15:
 * Erhöhung max. Anzahl FB an einem Bus auf 64 Byte.
 * Eingelesen und verarbeitet werden aber nur soviel wie notwendig.
 *
 * 18.07.16:
 * Delays mit bcm2835_delayMicroseconds. Clock ohne Monostabile Kippstufe (ergibt nicht mehr CPU Last).
 *
 * 24.07.16:
 * Korrekte Berechung Anzahl FB Kontakte. FB Buffer in srcp-fb.c auf 2048.
 *
 * 25.07.16:
 * Auf RaspberryPI wird mittels SPI eingelesen, was die CPU Last stark senkt (>20% au <2% bei 2 S88 Bussen mit je 16 Modulen mit 16 Inputs).
 *
 * 22.08.16:
 * RaspberryPI SPI ohne wiringPi Lib, Konfiguration SPI Devicename.
 */

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

#include "config.h"
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

#ifdef RASPBERRYPI

#include <stdint.h> 
#include <fcntl.h>
#include <sys/ioctl.h>
#include <linux/spi/spidev.h> 
//Auf dem RaspberryPI wird SPI verwendet:
//Zur Konfiguration des zu verwendenden SPI Busses /dev/spi0 oder 1) wird "port" mit 0 / 1 verwendet.
//Die SPI CS Leitungen (/dev/spi0.0, /dev/spi0.1) werden automatisch für mehrere S88 Busse verwendet.
//Da SPI Mode nicht einheitlich ist, kann dieser konfiguriert werden. Es wird clockscale dafür missbraucht.
//
// Pins für /dev/spi0: (2 S88 Busse möglich, 2 SPI CS vorhanden, Mode muss 1 sein)
// S88 Clock : Pin 23 / SPI CLK
// S88 Load & S88 Reset : Müssen aus SPI CS0/1 (Pin 24 & 26) für zwei mögliche S88-Busse erzeugt werden 
// Inputs:
// S88 Data Bus 1 & 2 : Beide an MISO (Pin 21), selektiert über die CE0/1 Leitungen 
//
// Pins für /dev/spi1 (Raspberry Model ab 2, bis zu 3 S88 Busse möglich da 3 SPI CS vorhanden, Mode muss 0 sein):
// S88 Clock : Pin 40 / SPI CLK
// S88 Load & S88 Reset : Muss aus SPI CS0 (Pin 12), CS1 (Pin 11), CS2 (Pin 36).
// Inputs:
// S88 Data Bus 1: an MISO (Pin 35)
  //Frequenz SPI Bus
  #define SPI_HZ 20000
  
#else
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
  static int S88CLOCK_SCALE;
  #define S88_WRITE(x) for (i = 0; i < S88CLOCK_SCALE; i++) outb(x, S88PORT)
  /* possible io-addresses for the parallel port */
  static const unsigned long int LPT_BASE[] = { 0x378, 0x278, 0x3BC };

  /* number of possible parallel ports */
  static const unsigned int LPT_NUM = 3;
#endif

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
#elif RASPBERRYPI
    __ddl_s88->clockscale = 0; //Wird als SPI Mode missbraucht
#else
    __ddl_s88->clockscale = 35;
#endif

    __ddl_s88->refresh = 100;

#ifdef __FreeBSD__
    __ddl_s88->Fd = -1;         /* signal closed Port */
#endif

    strcpy(buses[busnumber].description, "FB POWER");
    __ddl_s88->number_fb[0] = 0;
    __ddl_s88->number_fb[1] = 0;
    __ddl_s88->number_fb[2] = 0;
    __ddl_s88->number_fb[3] = 0;
    __ddl_s88->max_number_fb = 0;
#ifdef RASPBERRYPI
    __ddl_s88->spiFds[0] = 0;
    __ddl_s88->spiFds[1] = 0;
    __ddl_s88->spiFds[2] = 0;
    __ddl_s88->spiFds[3] = 0;
#endif

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
                if (__ddl_s88->number_fb[0] > __ddl_s88->max_number_fb) {
                    __ddl_s88->max_number_fb = __ddl_s88->number_fb[0];
                }
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_2") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[1] = atoi((char *) txt);
                if (__ddl_s88->number_fb[1] > __ddl_s88->max_number_fb) {
                    __ddl_s88->max_number_fb = __ddl_s88->number_fb[1];
                }
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_3") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[2] = atoi((char *) txt);
                if (__ddl_s88->number_fb[2] > __ddl_s88->max_number_fb) {
                    __ddl_s88->max_number_fb = __ddl_s88->number_fb[2];
                }
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_4") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_s88->number_fb[3] = atoi((char *) txt);
                if (__ddl_s88->number_fb[3] > __ddl_s88->max_number_fb) {
                    __ddl_s88->max_number_fb = __ddl_s88->number_fb[3];
                }
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }

    if ((__ddl_s88->max_number_fb * 2) > S88_MAXPORTSB) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Zu viele S88 FB's konfiguriert!");
        return 0;
    }
#ifdef RASPBERRYPI
    if ((__ddl_s88->port < 0) || (__ddl_s88->port > 1)) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Auf RaspberryPI sind nur /dev/spi0 und spi1 vorhanden, es wurde %d konfiguriert!", __ddl_s88->port);
        return 0;
    }
#endif    
    
    for (i = 0; i < S88_MAXBUSSES; i++) {
        if (init_FB(busnumber + i, __ddl_s88->number_fb[i] * 16)) {
            __ddl_s88->number_fb[i] = 0;
            syslog_bus(busnumber + i, DBG_ERROR,
                       "Can't create array for s88-feedback "
                       "channel %d", i + 1);
        }
        else {
            syslog_bus(busnumber + i, DBG_INFO,
                       "%d feeback contacts for channel %d successfully "
                       "initialized.", __ddl_s88->number_fb[i] * 16,
                       i + 1);
            #ifdef S88_DEBUG
            printf("Busnr %d, %d feeback contacts for channel %d successfully "
                       "initialized.\n", busnumber + i, __ddl_s88->number_fb[i] * 16,
                       i + 1);
            #endif
        }
    }
    #ifdef S88_DEBUG
    printf("Max S88 number_fb = %d\n", __ddl_s88->max_number_fb);
    #endif

    return (S88_MAXBUSSES);
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
    int S88PORT = __ddl_s88->port;
#ifndef RASPBERRYPI
    S88CLOCK_SCALE = __ddl_s88->clockscale;
#endif
    memset(__ddl_s88->s88dataBuffer, 1, sizeof(__ddl_s88->s88dataBuffer)); //Am Anfang von alles 1 (s88 offener Eingang) ausgehen
    __ddl_s88->aktS88DataBufferIndex = 0;

#ifdef linux
    syslog_bus(busnumber, DBG_INFO, "init_bus DDL(Linux) S88");
#else
#ifdef __FreeBSD__
    syslog_bus(busnumber, DBG_INFO, "init_bus DDL(FreeBSD) S88");
#endif
#endif

#ifdef RASPBERRYPI
      #ifdef S88_DEBUG
      printf("Init S88 Busnr %d\n", busnumber);
      #endif
      //RaspberryPI SPI init
      for (i=0; i<S88_MAXBUSSES; i++) {
        if (__ddl_s88->number_fb[i] > 0) {
          char devName[20];
          sprintf(devName, "/dev/spidev%d.%d", __ddl_s88->port, i);
          __ddl_s88->spiFds[i] = open (devName, O_RDWR);
          if (__ddl_s88->spiFds[i] < 0) {
            syslog_bus(busnumber, DBG_FATAL, "Open SPI Device %s fail!", devName);
            return 1;
          }
          // Set SPI parameters.
          //Leider sind diese nicht einheitlich, nicht mal auf dem selben Rechner:
          //RaspberryPI SPI0 ist CLK Ruhepegel 0 und Daten Lesen bei negativer Flanke Mode 1
          //Bei SPI1 ist dies Mode 0, Mode 1 liest bei positiver Flanke .... :-(
          //-> Mode muss konfigurierbar sein, ich missbraucht "clockscale" dazu
          int mode = __ddl_s88->clockscale & 0x03;
          if (ioctl (__ddl_s88->spiFds[i], SPI_IOC_WR_MODE, &mode) < 0) {
            syslog_bus(busnumber, DBG_FATAL, "SPI Mode Change failure: %s", strerror(errno));
            return 1;
          }
          uint8_t spiBPW = 8;
          if (ioctl (__ddl_s88->spiFds[i], SPI_IOC_WR_BITS_PER_WORD, &spiBPW) < 0) {
            syslog_bus(busnumber, DBG_FATAL, "SPI BPW Change failure: %s", strerror(errno));
            return 1;
          }
          int speed = SPI_HZ;
          if (ioctl (__ddl_s88->spiFds[i], SPI_IOC_WR_MAX_SPEED_HZ, &speed) < 0) {
            syslog_bus(busnumber, DBG_FATAL, "SPI Speed Change failure: %s", strerror(errno));
            return 1;
          }
        }
      }
#else
    /* is the port disabled from user, everything is fine */
    if (!S88PORT) {
        syslog_bus(busnumber, DBG_INFO, "s88 port is disabled.");
        return 1;
    }
    /* test, whether S88DEV is a valid io-address for a parallel device */
    int isin = 0;               /* reminder for checking */
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
#endif //RASPBERRYPI
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
    int i, j, k, w, inbyte;
    struct timeval nowtime;
    unsigned int s88data[S88_MAXBUSSES][S88_MAXPORTSB];        /* valid bus-data */
    int S88PORT = __ddl_s88->port;
    int S88REFRESH = 1000 * __ddl_s88->refresh;

    gettimeofday(&nowtime, NULL);
    if ((nowtime.tv_sec > __ddl_s88->s88valid.tv_sec) ||
        ((nowtime.tv_sec == __ddl_s88->s88valid.tv_sec) &&
         (nowtime.tv_usec > __ddl_s88->s88valid.tv_usec))) {
        /* data is out of date - get new data from the bus */

        /* initialize the s88data array */
        memset(s88data, 0, sizeof(s88data));

#ifndef RASPBERRYPI
        if (S88PORT) {
            /* if port is disabled do nothing */
            /* load the bus */
            ioperm(S88PORT, 3, 1);
            /*TODO: check ioperm return value (should be 0) */
#endif
            #ifdef S88_DEBUG
            printf("Reading S88 Bytes=%d\n", __ddl_s88->max_number_fb * 2);
            #endif
#ifdef RASPBERRYPI
            for (i=0; i<S88_MAXBUSSES; i++) {
              if (__ddl_s88->spiFds[i] > 0) {
                struct spi_ioc_transfer spi;
                //Mentioned in spidev.h but not used in the original kernel documentation
                //test program )-:
                memset (&spi, 0, sizeof (spi)) ;
                spi.rx_buf = (unsigned long)__ddl_s88->s88dataBuffer[i][__ddl_s88->aktS88DataBufferIndex];
                spi.len = __ddl_s88->number_fb[i] * 2;
                spi.delay_usecs = 0;
                spi.speed_hz = SPI_HZ;
                spi.bits_per_word = 8;
                if (ioctl (__ddl_s88->spiFds[i], SPI_IOC_MESSAGE(1), &spi) < 0) {
                  syslog_bus(busnumber, DBG_FATAL, "Error SPI Transfer ioctl.");
                  return;
                }
              }
            }
            //Damit ist schon alles eingelesen!
#else
            S88_WRITE(S88_LOAD);
            S88_WRITE(S88_LOAD | S88_CLOCK);
            S88_WRITE(S88_QUIET);
            S88_WRITE(S88_RESET);
            S88_WRITE(S88_QUIET);
            /* reading the data */
            for (j = 0; j < (__ddl_s88->max_number_fb * 2); j++) {
              for (k = 0; k < 8; k++) {
                  /* interpreting the four buses */
                  /* reading from port */
                  inbyte = inb(S88PORT + 1);
                  __ddl_s88->s88dataBuffer[0][j][k][__ddl_s88->aktS88DataBufferIndex] = inbyte&S88_DATA1;
                  __ddl_s88->s88dataBuffer[1][j][k][__ddl_s88->aktS88DataBufferIndex] = !(inbyte&S88_DATA2);
                  __ddl_s88->s88dataBuffer[2][j][k][__ddl_s88->aktS88DataBufferIndex] = inbyte&S88_DATA3;
                  __ddl_s88->s88dataBuffer[3][j][k][__ddl_s88->aktS88DataBufferIndex] = inbyte&S88_DATA4;
                  /* getting the next data */
                  S88_WRITE(S88_CLOCK);
                  S88_WRITE(S88_QUIET);
              }
            }
#endif
            __ddl_s88->aktS88DataBufferIndex++;
            if (__ddl_s88->aktS88DataBufferIndex >= ANZ_S88_WIEDERHOLUNGEN) {
              __ddl_s88->aktS88DataBufferIndex = 0;
            }
            //Mehrheitsentscheid
            for (i=0;i<S88_MAXBUSSES;i++) {
              #ifdef S88_DEBUG
              printf("S88 Mehrheitsentscheid Bus=%d, Bytes=%d\n", i, (__ddl_s88->number_fb[i] * 2));
              #endif
              for (j=0;j<(__ddl_s88->number_fb[i] * 2);j++) {
                for (k=0;k<8;k++) {
                  int count = 0;
                  for (w=0; w<ANZ_S88_WIEDERHOLUNGEN;w++) {
#ifdef RASPBERRYPI
                    if (__ddl_s88->s88dataBuffer[i][w][j] & BIT_VALUES[k])
#else
                    if (__ddl_s88->s88dataBuffer[i][j][k][w])
#endif
                    {
                      count++;
                    }
                  }
                  if (count > (ANZ_S88_WIEDERHOLUNGEN / 2)) {
                    s88data[i][j]+=BIT_VALUES[k];
                  }
                }
              }
            }
            #ifdef S88_DEBUG
            printf("S88 setFBmodul\n");
            #endif
            for (i=0;i<S88_MAXBUSSES;i++) {
              for (j=0;j<(__ddl_s88->number_fb[i] * 2);j++) {
                setFBmodul(busnumber + i, j + 1, s88data[i][j]);
              }
            }
            #ifdef S88_DEBUG
            printf("S88 setFBmodul End\n");
            #endif
            nowtime.tv_usec += S88REFRESH;
            __ddl_s88->s88valid.tv_usec = nowtime.tv_usec % 1000000;
            __ddl_s88->s88valid.tv_sec =
            nowtime.tv_sec + nowtime.tv_usec / 1000000;
#ifndef RASPBERRYPI
        }
#endif
    }
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    #ifdef S88_DEBUG
    printf("S88end_bus_thread\n");
    #endif
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
#ifdef RASPBERRYPI
    int i;
    for (i=0; i<S88_MAXBUSSES; i++) {
      if (__ddl_s88t->spiFds[i] > 0) {
        close(__ddl_s88t->spiFds[i]);
      }
    }
#endif
    
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

    {
        //Thread als Realtime setzen, Prio aber tiefer als bei DDL-Thread.
        int policy;
        struct sched_param sparam;
        int result = pthread_getschedparam(pthread_self(), &policy, &sparam);
        if (result != 0) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "pthread_getschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
        sparam.sched_priority = 5;
        result =
            pthread_setschedparam(pthread_self(), SCHED_FIFO, &sparam);
        if (result != 0) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "pthread_setschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
    }

    unsigned long int sleepusec = 10000;

    int S88REFRESH =
        ((DDL_S88_DATA *) buses[btd->bus].driverdata)->refresh;
    /* set refresh-cycle */
    if (sleepusec < S88REFRESH * 1000)
        sleepusec = S88REFRESH * 1000;

    syslog_bus(btd->bus, DBG_INFO, "DDL_S88 bus startet (device = %04x).",
               __ddl_s88t->port);

    while (1) {
        #ifdef S88_DEBUG
        printf("S88 Thread Pos 1\n");
        #endif
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

        #ifdef S88_DEBUG
        printf("S88 Thread Pos 2\n");
        #endif
        check_reset_fb(btd->bus);
        #ifdef S88_DEBUG
        printf("S88 Thread Pos 3\n");
        #endif
        if (usleep(sleepusec) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        #ifdef S88_DEBUG
        printf("S88 Thread Pos 4\n");
        #endif
        s88load(btd->bus);
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    #ifdef S88_DEBUG
    printf("S88 Thread pthread_cleanup_pop\n");
    #endif
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
