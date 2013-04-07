/***************************************************************************
                            ib.h  -  description
                             -------------------
    begin                : Thu Apr 19 2001
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
#ifndef _IB_H
#define _IB_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/


typedef struct _IB_DATA {
    int number_ga;
    int number_gl;
    int number_fb;
    int last_bit;
    int last_type;
    int last_typeaddr;
    int last_value;
    ga_state_t tga[50];
    int working_IB;
    int emergency_on_ib;
    unsigned int pause_between_cmd;
} IB_DATA;

int readConfig_IB(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);
int init_bus_IB(bus_t busnumber);
int init_gl_IB(gl_state_t *gl);
int init_ga_IB(ga_state_t *ga);
void* thr_sendrec_IB(void *);

/* IB uses 126 speed steps internally. */
#define SPEED_STEPS 126

/* values for power_state */

#define POWER_OFF 0
#define POWER_ON  1

static const char P50_DISABLE[] = "xZzA1";
static const char P50_ENABLE[] = "ZzA0";

/* general purpose P50Xb commands */

#define XLok        0x80
#define XLkDisp     0x83
#define XLokSts     0x84
#define XLokCfg     0x85
#define XFunc       0x88
#define XFuncX      0x89
#define XFunc34     0x8A
#define XFuncSts    0x8C
#define XFuncXSts   0x8D
#define XFunc34Sts  0x8E
#define XLimit      0x8F
#define XTrnt       0x90
#define XTrntFree   0x93
#define XTrntSts    0x94
#define XTrntGrp    0x95
#define XSensor     0x98
#define XSensOff    0x99
#define X88PGet     0x9C
#define X88PSet     0x9D
#define Xs88PTim    0x9E
#define Xs88Cnt     0x9F
#define XVer        0xA0
#define XP50XCh     0xA1
#define XStatus     0xA2
#define XSOSet      0xA3
#define XSOGet      0xA4
#define XHalt       0xA5
#define XPwrOff     0xA6
#define XPwrOn      0xA7
#define XLokoNet    0xC0
#define XNOP        0xC4
#define XP50Len1    0xC6
#define XP50Len2    0xC7
#define XEvent      0xC8
#define XEvtLok     0xC9
#define XEvtTrn     0xCA
#define XEvtSen     0xCB
#define XEvtIR      0xCC
#define XEvtLN      0xCD
#define XEvtPT      0xCE
#define XEvtTkR     0xCF
#define XEvtMem     0xD0
#define XEvtLSY     0xD1

/* programming track P50X commands */

#define XPT_DCCEWr  0xDC
#define XPT_FMZEWr  0xDD
#define XDCC_PD     0xDE
#define XDCC_PA     0xDF
#define XPT_Sts     0xE0
#define XPT_On      0xE1
#define XPT_Off     0xE2
#define XPT_SXRd    0xE4
#define XPT_SXWr    0xE5
#define XPT_SXSr    0xE6
#define XPT_FMZSr   0xE7
#define XPT_FMZWr   0xE8
#define XPT_MrkSr   0xE9
#define XPT_DCCSr   0xEA
#define XPT_DCCQA   0xEB
#define XPT_DCCRR   0xEC
#define XPT_DCCWR   0xED
#define XPT_DCCRP   0xEE
#define XPT_DCCWP   0xEF
#define XPT_DCCRD   0xF0
#define XPT_DCCWD   0xF1
#define XPT_DCCRB   0xF2
#define XPT_DCCWB   0xF3
#define XPT_DCCQD   0xF4
#define XPT_DCCRL   0xF5
#define XPT_DCCWL   0xF6
#define XPT_DCCRA   0xF7
#define XPT_DCCWA   0xF8
#define XPT_U750    0xF9
#define XPT_U755    0xFA
#define XPT_U760    0xFB
#define XPT_Src     0xFC
#define XPT_Ctrl    0xFD
#define XPT_Term    0xFE

/* IB error codes */

#define XBADPARAM   0x02
#define XPWOFF      0x06
#define XNOTSPC     0x07
#define XNOLSPC     0x08
#define XNODATA     0x0A
#define XNOSLOT     0x0B
#define XBADLNP     0x0C
#define XLKBUSY     0x0D
#define XBADTNP     0x0E
#define XBADSOV     0x0F
#define XNOISPC     0x10
#define XLOWTSP     0x40
#define XLKHALT     0x41
#define XLKPOFF     0x42

#endif
