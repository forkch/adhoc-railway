/* $Id: srcp-power.c 1456 2010-02-28 20:01:39Z gscholz $ */

/*
 * Vorliegende Software unterliegt der General Public License,
 * Version 2, 1991. (c) Matthias Trute, 2000-2001.
 *
 * Änderungen:
 * 2020-04-04	m2	BoosterNr wird berücksichtigt
 *
 */

#include <string.h>
#include <stdio.h>
#include <stdlib.h>

#include "config-srcpd.h"
#include "srcp-error.h"
#include "srcp-info.h"
#include "srcp-power.h"


int setPower(bus_t bus, int state, char *msg)
{
    gettimeofday(&buses[bus].power_change_time, NULL);
    buses[bus].power_state = (state == -1) ? 0 : state;
    /* buses[bus].power_state = state; */

    strcpy(buses[bus].power_msg, msg);

    /* msg = BoosterNr / Änderung: booster_state speichert Zustände der einzelnen Booster / 2020-04-04 m2 */

    if (strncmp(msg, "0", 1) == 0){
    	buses[bus].booster_state[0] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "1", 1) == 0){
    	buses[bus].booster_state[1] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "2", 1) == 0){
    	buses[bus].booster_state[2] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "3", 1) == 0){
    	buses[bus].booster_state[3] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "4", 1) == 0){
    	buses[bus].booster_state[4] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "5", 1) == 0){
    	buses[bus].booster_state[5] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "6", 1) == 0){
    	buses[bus].booster_state[6] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else if (strncmp(msg, "7", 1) == 0){
    	buses[bus].booster_state[7] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }
    else {
    	buses[bus].booster_state[0] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[1] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[2] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[3] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[4] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[5] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[6] = (buses[bus].power_state == 0) ?  'O' : 'A';
    	buses[bus].booster_state[7] = (buses[bus].power_state == 0) ?  'O' : 'A';
    }

    /* Korrektur power_state auf Grund aller booster_state / 2020-04-04 m2 */
	if (buses[bus].booster_state[0] == 'A' || buses[bus].booster_state[1] == 'A' || buses[bus].booster_state[2] == 'A' ||
			buses[bus].booster_state[3] == 'A' || buses[bus].booster_state[4] == 'A' || buses[bus].booster_state[5] == 'A' ||
			buses[bus].booster_state[6] == 'A' || buses[bus].booster_state[7] == 'A') {
		buses[bus].power_state = 1;
	}
	else {
		buses[bus].power_state = 0;
	}

    buses[bus].power_changed = (state == -1) ? 0 : 1;
    /* buses[bus].power_changed = 1; */
    /* Resume thread to transmit power change */
    resume_bus_thread(bus);
    return SRCP_OK;
}

int getPower(bus_t bus)
{
    return buses[bus].power_state;
}

int infoPower(bus_t bus, char *msg)
{
/*    sprintf(msg, "%lu.%.3lu 100 INFO %ld POWER %s %s\n",
            buses[bus].power_change_time.tv_sec,
            buses[bus].power_change_time.tv_usec / 1000, bus,
            buses[bus].power_state ? "ON" : "OFF", buses[bus].power_msg);*/
    sprintf(msg, "%lu.%.3lu 100 INFO %ld POWER %s 0 %c 1 %c 2 %c 3 %c 4 %c 5 %c 6 %c 7 %c\n",
            buses[bus].power_change_time.tv_sec,
            buses[bus].power_change_time.tv_usec / 1000, bus,
            buses[bus].power_state ? "ON" : "OFF", buses[bus].booster_state[0], buses[bus].booster_state[1],
            buses[bus].booster_state[2], buses[bus].booster_state[3], buses[bus].booster_state[4],
			buses[bus].booster_state[5], buses[bus].booster_state[6], buses[bus].booster_state[7]);
    return SRCP_INFO;
}

int initPower(bus_t bus)
{
    return SRCP_OK;
}

int termPower(bus_t bus)
{
    if (1 == getPower(bus))
        return setPower(bus, 0, "Device Termination");

    return SRCP_OK;
}
