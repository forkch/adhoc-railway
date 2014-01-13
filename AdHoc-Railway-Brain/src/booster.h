/*
 * booster.h
 *
 *  Created on: 13.03.2012
 *      Author: fork
 */

#ifndef BOOSTER_H_
#define BOOSTER_H_

#include "global.h"

struct BoosterState {
	char active :1;
	char shortcut :1;
};

struct BoosterState booster_state[BOOSTER_COUNT];

void init_boosters();
void check_for_shorts();
void report_boosterstate();
void stop_booster(int nr);
void stop_booster_short(int nr);
void go_booster(int nr);
void stop_all_boosters();
void go_all_boosters();

inline unsigned char get_booster_spi_state()
{
    unsigned char stateForSPI = 0x00;
    for (int i = 0; i < BOOSTER_COUNT; i++) {
		if (booster_state[i].active == 1)
			stateForSPI |= (1 << i);
		else

			stateForSPI &= ~(1 << i);
	}
    return stateForSPI;
}
#endif /* BOOSTER_H_ */
