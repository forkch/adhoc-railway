/**
 * This software is published under the terms of the GNU General Public
 * License, Version 2.0
 * Gerard van der Sel 
 *
 */


#ifndef _Selectrix_H
#define _Selectrix_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/


/* Read and Write command for the interface (Send with the address) */
#define SXread      0x00    /* Read command on the Selectrixbus */
#define SXwrite     0x80    /* Write command on the Selectrixbus */
#define SXempty     0x5a    /* Fill character for reception */

/* Maximum number off addresses on the SX-bus */
#define SXmax       112   /* Number of addresses on the SX-bus */
#define SXcc2000    104   /* Number of addresses on the SX-bus with a CC-2000 */

/* Addresses on the SX_bus */
#define SXcontrol   127   /* Power on/off address (all SX controllers) */

/* Control addresses for a CC2000 */
#define SXstatus    109   /* Status address */
#define SXcommand   106   /* Command address */
#define SXprog2     105   /* Program parameter 2 */
#define SXprog1     104   /* Program parameter 1 */

/* Status bits */
#define SXstpower   0x80  /* Power on track */
#define SXstready   0x40  /* CC2000 ready */
#define SXstprogram 0x20  /* Programming ready */
#define SXstshort   0x10  /* Track shorted */
#define SXstmode    0x0f  /* Function mode (internal) */

/* Commandbits */
#define SXcmdstart  0x80  /* Start command */
#define SXcmdprog   0x40  /* Start programming */
                   /* bit 5 - 4 must be 0x00 */
#define SXcmddcod   0x08  /* 0= read 1= write */
#define SXcmdmodus  0x01  /* Bit 2 - 1 001 = Selectrix */

/* Control addresses for a SLX852 */
#define RautenhsCC  126   /* Rautenhaus control address */

#define RautenhsB0  0x00  /* Select bus 0 */
#define RautenhsB1  0x02  /* Select bus 1 */

/* Rautenhaus control bits (for address 126) */
#define cntrlON     128   /* Start Rautenhaus protocol */
#define cntrlOFF     64   /* Stop Rautenhaus protocol */
#define fdbckON      32   /* Changes on then SX bus are reported automatically */
#define fdbckOFF     16   /* Stop reporting changes on the SX bus */
#define clkOFF0       8   /* Don't check address 111 (clock) on bus 1 */
#define clkOFF1       4   /* Don't check address 111 (clock) on bus 0 */

/* Constants for SXflags */
/* Central is a special */
#define CC2000_MODE       0x0001    /* CC2000 commanding the SX-bus */
#define Rautenhaus_MODE   0x0002    /* Rautenhaus commanding the SX-bus */

#define Rautenhaus_FDBCK  0x0010    /* Automatic feedback report */
#define Rautenhaus_DBL    0x0020    /* Two busses are controlled */
#define Rautenhaus_ADR    0x0040    /* Last byte was an address */
#define Rautenhaus_RTBS   0x0080    /* Last selected bus */

#define Connection        0x0100    /* Got a replay from the interface */

/* Array with the size of two SX-busses */
/* typedef int SX_BUS[SXmax]; */
typedef int SX_BUS[256];            /* Space for addresses/data on the SX-bus */

/* Structure of a Selectrix bus */
typedef struct _SELECTRIX_DATA {
    int number_fb;
    int number_ga;
    int number_gl;
    int SXflags;
    int stateInterface;             /* Reply state of the interface */
    int currentFB;                  /* holds current number of the feedback */
    int max_address;
    SX_BUS fb_adresses;
    SX_BUS bus_data;
} SELECTRIX_DATA;

/* Initialisation of the structure */
int readconfig_Selectrix(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

/* Methods for Selectrix */
int init_lineSelectrix(bus_t bus);
int init_bus_Selectrix(bus_t bus);
int init_gl_Selectrix(gl_state_t *gl);
int init_ga_Selectrix(ga_state_t *ga);
int init_fb_Selectrix(bus_t busnumber, int addr, const char protocol, int index);
void *thr_commandSelectrix(void *);
void *thr_feedbackSelectrix(void *);
void sig_processSelectrix(bus_t busnumber);

#endif
