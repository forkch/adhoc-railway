/***************************************************************************
                          srcp-sm.h  -  description
                             -------------------
    begin                : Mon Aug 12 2002
    copyright            : (C) 2002 by Dipl.-Ing. Frank Schmischke
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

#ifndef _SRCP_SM_H
#define _SRCP_SM_H

#include <sys/time.h>
#include "config-srcpd.h"

enum COMMAND {
    SET = 0,
    GET,
    VERIFY,
    INIT,
    TERM
} sm_command_t;

enum TYPE {
    REGISTER = 0,
    PAGE,
    CV,
    CV_BIT
} sm_type_t;

/* Loco decoder */
typedef struct _SM {
    char protocol[6];  /* currently only NMRA is supported */
                       /* (for IB, but not completely, work in progress) */
    int type;
    int command;
    int protocolversion;
    int addr;
    int typeaddr;
    int bit;                    /* bit to set/get for CVBIT */
    int value;
    struct timeval tv;          /* time of change */
} sm_t;

int enqueueSM(bus_t busnumber, int command, int type, int addr, int typeaddr,
            int bit, int value);
int queue_SM_isempty(bus_t busnumber);
int dequeueNextSM(bus_t, sm_t*);

int getSM(bus_t busnumber, int addr, sm_t*);
int setSM(bus_t busnumber, int type, int addr, int typeaddr, int bit,
          int value, int return_value);
int infoSM(bus_t busnumber, int command, int type, int addr, int typeaddr,
           int bit, int value, char *info);

#endif
