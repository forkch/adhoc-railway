/*
 * booster.h
 *
 *  Created on: 13.03.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef BOOSTER_H_
#define BOOSTER_H_

#include "global.h"

typedef struct BoosterState {
	char active :1;
	char shortcut :1;
} bs;

struct BoosterState booster_state[BOOSTER_COUNT];


void init_boosters();
void check_shorts();
void report_boosterstate();
void stop_booster(int nr);
void stop_booster_short(int nr);
void go_booster(int nr);
void stop_all_boosters();
void go_all_boosters();
unsigned char get_booster_spi_state();
#endif /* BOOSTER_H_ */
