/***************************************************************************
                          ddl-s88.h  -  description
                             -------------------
    begin                : Wed Aug 1 2001
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
#ifndef _DDL_S88_H
#define _DDL_S88_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/

/* maximal number of bytes read from one s88-bus */
#define S88_MAXPORTSB 62
/* maximal number of s88-busses */
#define S88_MAXBUSSES 4
/* maximal number of ports */
#define S88_MAXPORTS S88_MAXPORTSB*8*S88_MAXBUSSES

typedef struct _DDL_S88_DATA {
    int number_fb[4];
    int port;
    int refresh;
    int clockscale;
    /* timestamp, until when the s88data are valid */
    struct timeval s88valid;
#ifdef __FreeBSD__
    /* MAM 01/06/03: Emulate inb, outb and ioperm with the help of a
     * file descriptor, which must be stored somewhere */
    int Fd;
#endif

} DDL_S88_DATA;

int readconfig_DDL_S88(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

int init_bus_S88(bus_t);
void *thr_sendrec_S88(void *);
void *thr_sendrec_dummy(void *v);

#ifdef __FreeBSD__
/* MAM 01/06/03: Emulate inb, outb and ioperm with the help of a file
 * descriptor */
#define ioperm(a,b,c) FBSD_ioperm(a,b,c,busnumber)
#define inb(a) FBSD_inb(a,busnumber)
#define outb(a,b) FBSD_outb(a,b,busnumber)
int FBSD_ioperm(int, int, int, bus_t);
unsigned char FBSD_inb(int, bus_t);
unsigned char FBSD_outb(unsigned char, int, bus_t);
#endif

#endif
