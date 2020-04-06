/* $Id: loconet.h 1431 2009-12-27 18:33:49Z mtrute $ */

#ifndef _LOCONET_H
#define _LOCONET_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/

#define LN_FLAG_ECHO 1     /* loconet interface sends commands back to rs232 */
#define LN_FLAG_MS100 2    /* MS100 compatible settings (implicit no echo)   */
#define LN_FLAG_GETTIME 4  /* update the internal TIME device from loconet   */

typedef struct _LOCONET_DATA {
    int number_fb;              /* used internally */
    int number_ga;              /* used internally */
    int number_gl;              /* used internally */
    
    unsigned char loconetID;    /* Sender ID       */
    unsigned int flags;         /* use echo */

    unsigned int sent_packets;  /* statistics */
    unsigned int recv_packets;

    int ibufferin;       /* pointer for incoming characters */
    int ibufferout;      /* pointer to read from the buffer */
    unsigned char ibuffer[256]; /* input buffer for loconet packets */
    unsigned int  slotmap[128];    /* slot to decoder address mapping */
} LOCONET_DATA;

int readConfig_LOCONET(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);
int init_bus_LOCONET(bus_t);
int getDescription_LOCONET(char *reply);
void *thr_sendrec_LOCONET(void *);

/*****************************************************************************
 *                                                                           *
 *      (C) Copyright 2001 Ron W. Auld                                       *
 *                                                                           *
 *      This program is free software; you can redistribute it or            *
 *      modify it under the terms of the GNU General Public License          *
 *      as published by the Free Software Foundation; either version         *
 *      2 of the License, or (at your option) any later version.             *
 *                                                                           *
 *      This program is distributed in the hope that it will be useful,      *
 *      but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 *      MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 *      GNU General Public License for more details at www.gnu.org           *
 *                                                                           *
 *****************************************************************************
 *                                                                           *
 *  Some of this code is based on work done by John Kabat and I thank him    *
 *  for the use of that code and his contributions to the understanding      *
 *  and use of the Digitrax LocoNet.                                         *
 *                                                                           *
 *****************************************************************************
 *
 * File Name:     loconet.h
 * Module:        Generic Loconet (r) message definitions.
 * Language:
 * Contributors:  Ron W. Auld (RwA), John Kabat
 * Remarks:
 *
 *    This file contains the definitions and structures pertinant to the
 *    Loconet (r) message protocol.
 *
 * Version Control:
 * $Log$
 * Revision 1.6  2007/02/26 13:27:34  mtrute
 * Internal cleanups only
 *
 * Changed the long int for busnumbers and sessionids to bus_t and
 * sessionid_t respectivly (and found a few occurances where these
 * parameters were or were used as int)
 *
 * whoever uses long ints for flags in function return values should
 * piiip
 *
 * deleted the sa_restorer line (hello Gerard!)
 *
 * Revision 1.5  2006/01/25 19:37:42  mtrute
 * added ms100 code and GA support for LocoIO
 *
 * Revision 1.4  2006/01/03 21:45:56  schmischi
 * change some 'int' to 'long int' for x86_64
 *
 * Revision 1.3  2006/01/02 09:57:00  mtrute
 * several huge changes
 *
 *  major extension for loconet (dump loconet packets in clear text)
 *    thanks to code from Ron W. Auld (llnmon package, see loconet_hackers
 *    yahoo group).
 *
 *  getting loconet SV now sometimes works fine, but sometime blocks the
 *    the sending of commands (reading packets still works fine).
 *
 *  Sessions can now wait for results from the driver (see srcp-session.*
 *    and zimo.c/loconet.c how to use it). Mostly untested.
 *
 * Revision 1.2  2001/05/31 17:40:05  rauld
 * Added options and code needed for llnmon to use a Locobuffer interface instead
 * of an MS-100.
 *
 * Revision 1.1.1.1  2001/01/16 12:55:59  rauld
 * Initial import into CVS
 *
 *
 */

/*** INCLUDES ***/

/*** CONSTANTS ****/

/*** TYPES ***/

/* do we need to define the data type 'byte'? */
typedef unsigned char byte;

/* various bit masks */
#define DIRF_DIR          0x20  /* direction bit    */
#define DIRF_F0           0x10  /* Function 0 bit   */
#define DIRF_F4           0x08  /* Function 1 bit   */
#define DIRF_F3           0x04  /* Function 2 bit   */
#define DIRF_F2           0x02  /* Function 3 bit   */
#define DIRF_F1           0x01  /* Function 4 bit   */
#define SND_F8            0x08  /* Sound 4/Function 8 bit */
#define SND_F7            0x04  /* Sound 3/Function 7 bit */
#define SND_F6            0x02  /* Sound 2/Function 6 bit */
#define SND_F5            0x01  /* Sound 1/Function 5 bit */

#define OPC_SW_ACK_CLOSED 0x20  /* command switch closed/open bit   */
#define OPC_SW_ACK_OUTPUT 0x10  /* command switch output on/off bit */

#define OPC_INPUT_REP_CB  0x40  /* control bit, reserved otherwise      */
#define OPC_INPUT_REP_SW  0x20  /* input is switch input, aux otherwise */
#define OPC_INPUT_REP_HI  0x10  /* input is HI, LO otherwise            */

#define OPC_SW_REP_SW     0x20  /* switch input, aux input otherwise    */
#define OPC_SW_REP_HI     0x10  /* input is HI, LO otherwise            */
#define OPC_SW_REP_CLOSED 0x20  /* 'Closed' line is ON, OFF otherwise   */
#define OPC_SW_REP_THROWN 0x10  /* 'Thrown' line is ON, OFF otherwise   */
#define OPC_SW_REP_INPUTS 0x40  /* sensor inputs, outputs otherwise     */

#define OPC_SW_REQ_DIR    0x20  /* switch direction - closed/thrown     */
#define OPC_SW_REQ_OUT    0x10  /* output On/Off                        */

#define OPC_LOCO_SPD_ESTOP 0x01 /* emergency stop command               */

/* Slot Status byte definitions and macros */
/***********************************************************************************
*   D7-SL_SPURGE    ; 1=SLOT purge en,                                             *
*                   ; ALSO adrSEL (INTERNAL use only) (not seen on NET!)           *
*                                                                                  *
*   D6-SL_CONUP     ; CONDN/CONUP: bit encoding-Control double linked Consist List *
*                   ;    11=LOGICAL MID CONSIST , Linked up AND down               *
*                   ;    10=LOGICAL CONSIST TOP, Only linked downwards             *
*                   ;    01=LOGICAL CONSIST SUB-MEMBER, Only linked upwards        *
*                   ;    00=FREE locomotive, no CONSIST indirection/linking        *
*                   ; ALLOWS "CONSISTS of CONSISTS". Uplinked means that           *
*                   ; Slot SPD number is now SLOT adr of SPD/DIR and STATUS        *
*                   ; of consist. i.e. is ;an Indirect pointer. This Slot          *
*                   ; has same BUSY/ACTIVE bits as TOP of Consist. TOP is          *
*                   ; loco with SPD/DIR for whole consist. (top of list).          *
*                   ; BUSY/ACTIVE: bit encoding for SLOT activity                  *
*                                                                                  *
*   D5-SL_BUSY      ; 11=IN_USE loco adr in SLOT -REFRESHED                        *
*                                                                                  *
*   D4-SL_ACTIVE    ; 10=IDLE loco adr in SLOT -NOT refreshed                      *
*                   ; 01=COMMON loco adr IN SLOT -refreshed                        *
*                   ; 00=FREE SLOT, no valid DATA -not refreshed                   *
*                                                                                  *
*   D3-SL_CONDN     ; shows other SLOT Consist linked INTO this slot, see SL_CONUP *
*                                                                                  *
*   D2-SL_SPDEX     ; 3 BITS for Decoder TYPE encoding for this SLOT               *
*                                                                                  *
*   D1-SL_SPD14     ; 011=send 128 speed mode packets                              *
*                                                                                  *
*   D0-SL_SPD28     ; 010=14 step MODE                                             *
*                   ; 001=28 step. Generate Trinary packets for this               *
*                   ;              Mobile ADR                                      *
*                   ; 000=28 step. 3 BYTE PKT regular mode                         *
*                   ; 111=128 Step decoder, Allow Advanced DCC consisting          *
*                   ; 100=28 Step decoder ,Allow Advanced DCC consisting           *
***********************************************************************************/

#define STAT1_SL_SPURGE   0x80  /* internal use only, not seen on net */
#define STAT1_SL_CONUP    0x40  /* consist status                     */
#define STAT1_SL_BUSY     0x20  /* used with STAT1_SL_ACTIVE,         */
#define STAT1_SL_ACTIVE   0x10  /*                                    */
#define STAT1_SL_CONDN    0x08  /*                                    */
#define STAT1_SL_SPDEX    0x04  /*                                    */
#define STAT1_SL_SPD14    0x02  /*                                    */
#define STAT1_SL_SPD28    0x01  /*                                    */
#define STAT2_SL_SUPPRESS 0x01  /* 1 = Adv. Consisting supressed      */
#define STAT2_SL_NOT_ID   0x04  /* 1 = ID1/ID2 is not ID usage        */
#define STAT2_SL_NOTENCOD 0x08  /* 1 = ID1/ID2 is not encoded alias   */
#define STAT2_ALIAS_MASK  (STAT2_SL_NOTENCOD | STAT2_SL_NOT_ID)
#define STAT2_ID_IS_ALIAS STAT2_SL_NOT_ID

/* mask and values for consist determination */
#define CONSIST_MASK      (STAT1_SL_CONDN | STAT1_SL_CONUP)
#define CONSIST_MID       (STAT1_SL_CONDN | STAT1_SL_CONUP)
#define CONSIST_TOP       (STAT1_SL_CONDN)
#define CONSIST_SUB       (STAT1_SL_CONUP)
#define CONSIST_NO        (0)
#define CONSIST_STAT(s) (  ((s & CONSIST_MASK) == CONSIST_MID) ? "Mid-Consisted" : \
                         ( ((s & CONSIST_MASK) == CONSIST_TOP) ? "Consist TOP" : \
                          (((s & CONSIST_MASK) == CONSIST_SUB) ? "Sub-Consisted" : \
                           "Not Consisted")))

#define CONSISTED(s) (((s & CONSIST_MASK) == CONSIST_MID) || ((s & CONSIST_MASK) == CONSIST_SUB))

/* mask and values for locomotive use determination */
#define LOCOSTAT_MASK     (STAT1_SL_BUSY  | STAT1_SL_ACTIVE)
#define LOCO_IN_USE       (STAT1_SL_BUSY  | STAT1_SL_ACTIVE)
#define LOCO_IDLE         (STAT1_SL_BUSY)
#define LOCO_COMMON       (STAT1_SL_ACTIVE)
#define LOCO_FREE         (0)
#define LOCO_STAT(s)    (  ((s & LOCOSTAT_MASK) == LOCO_IN_USE) ? "In-Use" : \
                         ( ((s & LOCOSTAT_MASK) == LOCO_IDLE)   ? "Idle" : \
                          (((s & LOCOSTAT_MASK) == LOCO_COMMON) ? "Common" : \
                           "Free")))

/* mask and values for decoder type encoding for this slot */
#define DEC_MODE_MASK     (STAT1_SL_SPDEX | STAT1_SL_SPD14 | STAT1_SL_SPD28)
/* Advanced consisting allowed for the next two */
#define DEC_MODE_128A     (STAT1_SL_SPDEX | STAT1_SL_SPD14 | STAT1_SL_SPD28)
#define DEC_MODE_28A      (STAT1_SL_SPDEX                                  )
/* normal modes */
#define DEC_MODE_128      (STAT1_SL_SPD14 | STAT1_SL_SPD28)
#define DEC_MODE_14       (STAT1_SL_SPD14)
#define DEC_MODE_28TRI    (STAT1_SL_SPD28)
#define DEC_MODE_28       (0)
#define DEC_MODE(s)    (    ((s & DEC_MODE_MASK) == DEC_MODE_128A)  ? "128 (Allow Adv. consisting)" : \
                        (   ((s & DEC_MODE_MASK) == DEC_MODE_28A)   ? "28 (Allow Adv. consisting)" : \
                         (  ((s & DEC_MODE_MASK) == DEC_MODE_128)   ? "128" : \
                          ( ((s & DEC_MODE_MASK) == DEC_MODE_14)    ? "14" : \
                           (((s & DEC_MODE_MASK) == DEC_MODE_28TRI) ? "28 (Motorola)" : "28")))))

/* values for track status encoding for this slot */
#define GTRK_PROG_BUSY    0x08      /* 1 = programming track in this master is Busy         */
#define GTRK_MLOK1        0x04      /* 0 = Master is DT200, 1=Master implements LocoNet 1.1 */
#define GTRK_IDLE         0x02      /* 0 = Track paused, B'cast EMERG STOP, 1 = Power On    */

#define FC_SLOT           0x7b      /* Fast clock is in this slot                           */
#define PRG_SLOT          0x7c      /* This slot communicates with the programming track    */

/* values and macros to decode programming messages */
#define PCMD_RW           0x40      /* 1 = write, 0 = read                                  */
#define PCMD_BYTE_MODE    0x20      /* 1 = byte operation, 0 = bit operation (if possible)  */
#define PCMD_TY1          0x10      /* TY1 Programming type select bit                      */
#define PCMD_TY0          0x08      /* TY0 Programming type select bit                      */
#define PCMD_OPS_MODE     0x04      /* 1 = Ops mode, 0 = Service Mode                       */
#define PCMD_RSVRD1       0x02      /* reserved                                             */
#define PCMD_RSVRD0       0x01      /* reserved                                             */

/* programming mode mask */
#define PCMD_MODE_MASK      (PCMD_BYTE_MODE | PCMD_OPS_MODE | PCMD_TY1 | PCMD_TY0)

/*
 *  programming modes
 */
/* Paged mode  byte R/W on Service Track */
#define PAGED_ON_SRVC_TRK       (PCMD_BYTE_MODE)

/* Direct mode byte R/W on Service Track */
#define DIR_BYTE_ON_SRVC_TRK    (PCMD_BYTE_MODE | PCMD_TY0)

/* Direct mode bit  R/W on Service Track */
#define DIR_BIT_ON_SRVC_TRK     (PCMD_TY0)

/* Physical Register byte R/W on Service Track */
#define REG_BYTE_RW_ON_SRVC_TRK (PCMD_TY1)

/* Service Track Reserved function */
#define SRVC_TRK_RESERVED       (PCMD_TY1 | PCMD_TY0)

/* Ops mode byte program - no feedback */
#define OPS_BYTE_NO_FEEDBACK    (PCMD_BYTE_MODE | PCMD_OPS_MODE)

/* Ops mode byte program - feedback */
#define OPS_BYTE_FEEDBACK       (OPS_BYTE_NO_FEEDBACK | PCMD_TY0)

/* Ops mode bit program - no feedback */
#define OPS_BIT_NO_FEEDBACK     (PCMD_OPS_MODE)

/* Ops mode bit program - feedback */
#define OPS_BIT_FEEDBACK        (OPS_BIT_NO_FEEDBACK | PCMD_TY0)

/* Programmer Status error flags */
#define PSTAT_USER_ABORTED  0x08    /* User aborted this command */
#define PSTAT_READ_FAIL     0x04    /* Failed to detect Read Compare Acknowledge from decoder */
#define PSTAT_WRITE_FAIL    0x02    /* No Write acknowledge from decoder                      */
#define PSTAT_NO_DECODER    0x01    /* Service mode programming track empty                   */

/* bit masks for CVH */
#define CVH_CV8_CV9         0x30    /* mask for CV# bits 8 and 9    */
#define CVH_CV7             0x01    /* mask for CV# bit 7           */
#define CVH_D7              0x02    /* MSbit for data value         */

/* build data byte from programmer message */
#define PROG_DATA(ptr)      (((ptr->cvh & CVH_D7) << 6) | (ptr->data7 & 0x7f))

/* build CV # from programmer message */
#define PROG_CV_NUM(ptr)    (((((ptr->cvh & CVH_CV8_CV9) >> 3) | (ptr->cvh & CVH_CV7)) * 128)   \
                            + (ptr->cvl & 0x7f))

/* Locomotive Address Message */
typedef struct locoadr_t {
    byte command;
    byte adr_hi;        /* ms seven bits of loco address (D6-D0)                */
    byte adr_lo;        /* ls seven bits of loco address (D6-D0)                */
    byte chksum;        /* exclusive-or checksum for the message                */
} locoAdrMsg;

/* Switch with/without Acknowledge */
typedef struct switchack_t {
    byte command;
    byte sw1;           /* ls seven bits of switch address (D6-D0)              */
    byte sw2;           /* ms four  bits of switch address (D3-D0)              */
                        /* and control bits                                     */
    byte chksum;        /* exclusive-or checksum for the message                */
} switchAckMsg, switchReqMsg;

/* Slot data request */
typedef struct slotreq_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte pad;           /* should be zero                                       */
    byte chksum;        /* exclusive-or checksum for the message                */
} slotReqMsg;

/* Move/Link Slot Message */
typedef struct slotmove_t {
    byte command;
    byte src;           /* source slot number for the move/link                 */
    byte dest;          /* destination slot for the move/link                   */
    byte chksum;        /* exclusive-or checksum for the message                */
} slotMoveMsg, slotLinkMsg;

/* Consist Function Message */
typedef struct consistfunc_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte dirf;          /* direction and light function bits                    */
    byte chksum;        /* exclusive-or checksum for the message                */
} consistFuncMsg;

/* Write slot status message */
typedef struct slotstat_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte stat;          /* status to be written                                 */
    byte chksum;        /* exclusive-or checksum for the message                */
} slotStatusMsg;

/* Long ACK message */
typedef struct longack_t{
    byte command;
    byte opcode;        /* op-code of message getting the response (msb=0)      */
    byte ack1;          /* response code                                        */
    byte chksum;        /* exclusive-or checksum for the message                */
} longAckMsg;

/* Sensor input report */
typedef struct inputrep_t {
    byte command;
    byte in1;           /* first  byte of report                                */
    byte in2;           /* second byte of report                                */
    byte chksum;        /* exclusive-or checksum for the message                */
} inputRepMsg;

/* Turnout sensor state report */
typedef struct swrep_t {
    byte command;
    byte sn1;           /* first  byte of report                                */
    byte sn2;           /* second byte of report                                */
    byte chksum;        /* exclusive-or checksum for the message                */
} swRepMsg;

/* Request Switch function */
typedef struct swreq_t {
    byte command;
    byte sw1;           /* first  byte of request                               */
    byte sw2;           /* second byte of request                               */
    byte chksum;        /* exclusive-or checksum for the message                */
} swReqMsg;

/* Set slot sound functions */
typedef struct locosnd_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte snd;           /* sound/function request                               */
    byte chksum;        /* exclusive-or checksum for the message                */
} locoSndMsg;

/* Set slot direction and F0-F4 functions */
typedef struct locodirf_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte dirf;          /* direction & function request                         */
    byte chksum;        /* exclusive-or checksum for the message                */
} locoDirfMsg;

/* Set slot speed functions */
typedef struct locospd_t {
    byte command;
    byte slot;          /* slot number for this request                         */
    byte spd;           /* speed request                                        */
    byte chksum;        /* exclusive-or checksum for the message                */
} locoSpdMsg;

/* Read/Write Slot data messages */
typedef struct rwslotdata_t {
    byte command;
    byte mesg_size;     /* ummmmm, size of the message in bytes?                */
    byte slot;          /* slot number for this request                         */
    byte stat;          /* slot status                                          */
    byte adr;           /* loco address                                         */
    byte spd;           /* command speed                                        */
    byte dirf;          /* direction and F0-F4 bits                             */
    byte trk;           /* track status                                         */
    byte ss2;           /* slot status 2 (tells how to use ID1/ID2 & ADV Consist*/
    byte adr2;          /* loco address high                                    */
    byte snd;           /* Sound 1-4 / F5-F8                                    */
    byte id1;           /* ls 7 bits of ID code                                 */
    byte id2;           /* ms 7 bits of ID code                                 */
    byte chksum;        /* exclusive-or checksum for the message                */
} rwSlotDataMsg;

/* Fast Clock Message */
typedef struct fastclock_t {
    byte command;
    byte mesg_size;     /* ummmmm, size of the message in bytes?                    */
    byte slot;          /* slot number for this request                             */
    byte clk_rate;      /* 0 = Freeze clock, 1 = normal, 10 = 10:1 etc. Max is 0x7f */
    byte frac_minsl;    /* fractional minutes. not for external use.                */
    byte frac_minsh;
    byte mins_60;       /* 256 - minutes   */
    byte track_stat;    /* track status    */
    byte hours_24;      /* 256 - hours     */
    byte days;          /* clock rollovers */
    byte clk_cntrl;     /* bit 6 = 1; data is valid clock info */
                        /*  "  "   0; ignore this reply        */
    byte id1;           /* id1/id2 is device id of last device to set the clock */
    byte id2;           /*  "   "  = zero shows not set has happened            */
    byte chksum;        /* exclusive-or checksum for the message                */
} fastClockMsg;

/* Programmer Task Message (used in Start and Final Reply, both )*/
typedef struct progtask_t {
    byte command;
    byte mesg_size;     /* ummmmm, size of the message in bytes?                    */
    byte slot;          /* slot number for this request - slot 124 is programmer    */
    byte pcmd;          /* programmer command                                       */
    byte pstat;         /* programmer status error flags in reply message           */
    byte hopsa;         /* Ops mode - 7 high address bits of loco to program        */
    byte lopsa;         /* Ops mode - 7 low  address bits of loco to program        */
    byte trk;           /* track status. Note: bit 3 shows if prog track is busy    */
    byte cvh;           /* hi 3 bits of CV# and msb of data7                        */
    byte cvl;           /* lo 7 bits of CV#                                         */
    byte data7;         /* 7 bits of data to program, msb is in cvh above           */
    byte pad2;
    byte pad3;
    byte chksum;        /* exclusive-or checksum for the message                */
} progTaskMsg;

/* peer-peer transfer message */
typedef struct peerxfer_t {
    byte command;
    byte mesg_size;     /* ummmmm, size of the message in bytes?                */
    byte src;           /* source of transfer                                   */
    byte dst_l;         /* ls 7 bits of destination                             */
    byte dst_h;         /* ms 7 bits of destination                             */
    byte pxct1;
    byte d1;            /* data byte 1                                          */
    byte d2;            /* data byte 2                                          */
    byte d3;            /* data byte 3                                          */
    byte d4;            /* data byte 4                                          */
    byte pxct2;
    byte d5;            /* data byte 5                                          */
    byte d6;            /* data byte 6                                          */
    byte d7;            /* data byte 7                                          */
    byte d8;            /* data byte 8                                          */
    byte chksum;        /* exclusive-or checksum for the message                */
} peerXferMsg;

/* send packet immediate message */
typedef struct sendpkt_t {
    byte command;
    byte mesg_size;     /* ummmmm, size of the message in bytes?                */
    byte val7f;         /* fixed value of 0x7f                                  */
    byte reps;          /* repeat count                                         */
    byte dhi;           /* high bits of data bytes                              */
    byte im1;
    byte im2;
    byte im3;
    byte im4;
    byte im5;
    byte chksum;        /* exclusive-or checksum for the message                */
} sendPktMsg;

/* loconet opcodes */
#define OPC_GPBUSY        0x81
#define OPC_GPOFF         0x82
#define OPC_GPON          0x83
#define OPC_IDLE          0x85
#define OPC_LOCO_SPD      0xa0
#define OPC_LOCO_DIRF     0xa1
#define OPC_LOCO_SND      0xa2
#define OPC_SW_REQ        0xb0
#define OPC_SW_REP        0xb1
#define OPC_INPUT_REP     0xb2
#define OPC_UNKNOWN       0xb3
#define OPC_LONG_ACK      0xb4
#define OPC_SLOT_STAT1    0xb5
#define OPC_CONSIST_FUNC  0xb6
#define OPC_UNLINK_SLOTS  0xb8
#define OPC_LINK_SLOTS    0xb9
#define OPC_MOVE_SLOTS    0xba
#define OPC_RQ_SL_DATA    0xbb
#define OPC_SW_STATE      0xbc
#define OPC_SW_ACK        0xbd
#define OPC_LOCO_ADR      0xbf
#define OPC_PEER_XFER     0xe5
#define OPC_SL_RD_DATA    0xe7
#define OPC_IMM_PACKET    0xed
#define OPC_IMM_PACKET_2  0xee
#define OPC_WR_SL_DATA    0xef
#define OPC_MASK          0x7f  /* mask for acknowledge opcodes */


/*** STATIC DATA ***/

/*** PROTOTYPES ***/

/*** FUNCTIONS ***/

#define LOCO_ADR(a1, a2)   (((a1 & 0x7f) * 128) + (a2 & 0x7f))
#define SENSOR_ADR(a1, a2) (((a2 & 0x0f) * 128) + (a1 & 0x7f))


#endif
