/* $Id: loopback.h 1176 2008-01-29 22:42:17Z schmischi $ */

#ifndef _ADHOCMM_H
#define _ADHOCMM_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/

typedef struct _ADHOCMM_DATA {
    int number_ga;
    int number_gl;
    int number_fb;
    ga_state_t tga[50];
} ADHOCMM_DATA;

int readconfig_ADHOCMM(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);
int init_bus_ADHOCMM(bus_t );
int init_bus_ADHOCMM(bus_t i);
int init_gl_ADHOCMM(gl_state_t *);
int init_ga_ADHOCMM(ga_state_t *);
int getDescription_ADHOCMM(char *reply);
void* thr_sendrec_ADHOCMM(void *);
static int readByte_(bus_t busnumber, int wait, unsigned char *the_byte);

#endif
