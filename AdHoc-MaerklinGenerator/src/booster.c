/*
 * booser.c
 *
 *  Created on: 13.03.2012
 *      Author: fork
 */


#include "booster.h"
#include "global.h"
#include "debug.h"

void check_shorts() {

	unsigned char shorts = SPI_MasterReceiveShort();


	SPI_MasterTransmitDebug( shorts);
}

