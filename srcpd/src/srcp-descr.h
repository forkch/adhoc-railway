/* $Id: srcp-descr.h 758 2007-02-26 13:27:41Z mtrute $ */

/*
 * Vorliegende Software unterliegt der General Public License,
 * Version 2, 1991. (c) Matthias Trute, 2000-2001.
 *
 */

#ifndef _SRCP_DESCR_H
#define _SRCP_DESCR_H

#include "config-srcpd.h"

int startup_DESCRIPTION(void);
int describeBus(bus_t bus, char *reply);
#endif
