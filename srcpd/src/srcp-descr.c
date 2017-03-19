/* $Id: srcp-descr.c 1016 2007-12-27 15:58:33Z gscholz $ */

/*
 * Vorliegende Software unterliegt der General Public License,
 * Version 2, 1991. (c) Matthias Trute, 2000-2001.
 */

#include <stdio.h>

#include "srcp-descr.h"
#include "srcp-error.h"
#include "config-srcpd.h"

int startup_DESCRIPTION(void)
{
    return 0;
}

int describeBus(bus_t bus, char *reply)
{
    sprintf(reply, "%lu.%.3lu 100 INFO %ld DESCRIPTION %s\n",
            buses[bus].power_change_time.tv_sec,
            buses[bus].power_change_time.tv_usec / 1000, bus,
            buses[bus].description);
    return SRCP_INFO;
}
