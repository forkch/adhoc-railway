/*
 * booser.c
 *
 *  Created on: 13.03.2012
 *      Author: fork
 */

#include "spi.h"
#include "booster.h"
#include "global.h"
#include "debug.h"

void init_boosters() {
	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 0;
		booster_state[i].shortcut = 0;
	}
}
void check_shorts() {

	//unsigned char shorts = SPI_MasterReceiveShort();
	unsigned char shorts = 0;
	for (int i = 0; i < BOOSTER_COUNT; i++) {
		shorts++;
	}

}

void stop_booster(int nr) {

	booster_state[nr].active = 0;

	unsigned char stateForSPI = get_booster_spi_state();
	SPI_MasterTransmitDebug(stateForSPI);

}

void go_booster(int nr) {

	booster_state[nr].active = 1;

	unsigned char stateForSPI = get_booster_spi_state();
	SPI_MasterTransmitDebug(stateForSPI);
}

void stop_all_boosters() {

	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 0;
	}

	unsigned char stateForSPI = get_booster_spi_state();
	SPI_MasterTransmitDebug(stateForSPI);

}

void go_all_boosters() {

	for (int i = 0; i < BOOSTER_COUNT; i++) {
		booster_state[i].active = 1;
	}

	unsigned char stateForSPI = get_booster_spi_state();
	SPI_MasterTransmitDebug(stateForSPI);
}
