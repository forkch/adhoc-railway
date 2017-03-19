/* cvs: $Id: m605x.h 983 2007-12-16 09:06:42Z gscholz $             */

/*
 * Vorliegende Software unterliegt der General Public License,
 * Version 2, 1991. (c) Matthias Trute, 2000-2001.
 *
 */


#ifndef _M605X_H
#define _M605X_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/

#define M6020_MODE            0x0001    /* Subtyp zum M605X */

typedef struct _M6051_DATA {
    int number_fb;
    int number_ga;
    int number_gl;
    int cmd32_pending;
    int flags;
    unsigned int ga_min_active_time;
    unsigned int pause_between_cmd;
    unsigned int pause_between_bytes;
} M6051_DATA;

int readconfig_m605x(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

int init_line6051(bus_t bus);
int init_bus_M6051(bus_t bus);
int init_gl_M6051(gl_state_t *gl);
int init_ga_M6051(ga_state_t *ga);
int getDescription_M6051(char *reply);
void *thr_sendrec_M6051(void *);

#endif
