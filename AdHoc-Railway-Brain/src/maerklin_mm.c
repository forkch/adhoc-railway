/*
 * maerklin_mm.c
 *
 *  Created on: Jan 12, 2014
 *      Author: fork
 */

#include "maerklin_mm.h"
#include "global.h"
#include "main.h"
#include "debug.h"
#include "pwm.h"

void init_maerklin_mm() {

	//maerklin_mm init
	initLocoData();
	initPortData();

	currentRefreshCycleLocoIdx = 79;
	currentRefreshCycleFunction = 0;

	solenoidQueueIdxEnter = 0; 		// new Solenoid inserted
	solenoidQueueIdxFront = 0; 		// Solenoid to activate
	solenoidQueueIdxFront2 = 0; 	// Solenoid to deactivate

	newLocoQueueIdxEnter  = 0;
	newLocoQueueIdxFront = 0;

	deactivatingSolenoid = 0;
	solenoidToDeactivate = 0;

	startOneLocoRefresh = 0;
	startSecondSolenoidTransmition = 0;
	functionRefreshSent = 0;

	newLoco = 0;
	mmChangeDirection = 192;

	for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
		solenoidQueue[i].timerDetected = 0;
		solenoidQueue[i].active = 0;
	}

	for (int i = 0; i < MAX_NEW_LOCO_QUEUE; i++) {
		newLocoQueue[i].newLocoIdx = -1;
		newLocoQueue[i].newLocoSpeed = 0;
		newLocoQueue[i].newLocoFunction = -1;
	}

	//Initialize PWM Channel 1
	initPWM();
}

uint8_t findNextLocoToRefreshAndUpdateRefreshCycleFunction(uint8_t i, struct LocoData locoData[80],
		uint8_t locoToRefresh, uint8_t startIdx) {
	if (functionRefreshSent == 0) {
		// search next active loco in refresh queue
		while (1) {
			if (i == 0) {
				// refreshed all locos for given function --> next function
				currentRefreshCycleFunction = (currentRefreshCycleFunction + 1)
						% 4;
			}
			if (locoData[i].active == 1) {
				currentRefreshCycleLocoIdx = i;
				locoToRefresh = i;
				break;
			}
			i = (i + 1) % 80;
			if (i == startIdx)
				break;
		}
	} else {
		locoToRefresh = currentRefreshCycleLocoIdx;
	}
	return locoToRefresh;
}

int hasLocomotiveSpeedOrFunctionChanged() {
	return newLocoQueue[newLocoQueueIdxFront].newLocoSpeed == 1
			|| newLocoQueue[newLocoQueueIdxFront].newLocoFunction != 0;
}

volatile unsigned char getCurrentPwmQueueIndexForNextCommand() {
	return (pwmQueueIdx + 1) % 2;
}

void prepareMaerklinMMDataForPWM() {

	unsigned char queueIdxLoc = getCurrentPwmQueueIndexForNextCommand();

	if (startOneLocoRefresh == 0) {
		if (startSecondSolenoidTransmition == 0) {
			// handle NEW loco command with highest priority
			prepareNewLococomandWithHighestPrio(queueIdxLoc);
		} else {
			// handle solenoid command (or deactivate solenoid)
			prepareSolenoidCommandOrDeactivateSolenoid(queueIdxLoc);
		}
	} else {
		// do a loco refresh
		prepareLocoRefresh(locoData, queueIdxLoc);
	}
}

void prepareNewLococomandWithHighestPrio(unsigned char queueIdxLoc) {
	// handle NEW loco command with highest priority
	if (!newLocoQueueEmpty()) {

		// is there something new
		if (hasLocomotiveSpeedOrFunctionChanged()) {
			if (newLocoQueue[newLocoQueueIdxFront].newLocoSpeed == 1) {
				sendLocoPacketNoFunctionUpdate(newLocoQueue[newLocoQueueIdxFront].newLocoIdx,
						queueIdxLoc, 0);
			} else if (newLocoQueue[newLocoQueueIdxFront].newLocoSpeed == 0
					&& newLocoQueue[newLocoQueueIdxFront].newLocoFunction
							!= 0) {
				sendLocoPacket(newLocoQueue[newLocoQueueIdxFront].newLocoIdx,
						queueIdxLoc, 0, newLocoQueue[newLocoQueueIdxFront].newLocoFunction);
			}

			pwm_mode[queueIdxLoc] = MODE_LOCO;
			// notify PWM that we're finished preparing a new packet
			prepareNewDataWhileSending = 0;
		} else {
			newLocoQueuePop();
		}
	}
}

void prepareSolenoidCommandOrDeactivateSolenoid(unsigned char queueIdxLoc) {
	if (!solenoidQueueEmpty() || deactivatingSolenoid) {

		// set index accordingly (solenoid to switch or to deactivate)
		uint8_t solenoidIdx =
				deactivatingSolenoid == 1 ?
						solenoidToDeactivate : solenoidQueueIdxFront;

		if (startOneLocoRefresh == 0 && startSecondSolenoidTransmition == 0) {
			// triggers one loco refresh between first and second solenoid transmission
			startOneLocoRefresh = 1;
		} else {
			startSecondSolenoidTransmition = 0;
		}

		sendSolenoidPacket(solenoidIdx, queueIdxLoc);
		pwm_mode[queueIdxLoc] = MODE_SOLENOID;
		// notify PWM that we're finished preparing a new packet

		prepareNewDataWhileSending = 0;
	}
}

void prepareLocoRefresh(struct LocoData locoData[80], unsigned char queueIdxLoc) {
	// do a loco refresh
	uint8_t locoToRefresh = -1;
	uint8_t i = (currentRefreshCycleLocoIdx + 1) % 80;
	uint8_t startIdx = i;

	locoToRefresh = findNextLocoToRefreshAndUpdateRefreshCycleFunction(i, locoData, locoToRefresh, startIdx);

	sendLocoPacketRefresh(locoToRefresh, queueIdxLoc);
	pwm_mode[queueIdxLoc] = MODE_LOCO;
	if (startOneLocoRefresh > 0) {
		startOneLocoRefresh = 0;
		startSecondSolenoidTransmition = 1;
	}
	// notify PWM that we're finished preparing a new packet
	prepareNewDataWhileSending = 0;
}

//void all_loco() {
//
//	for (uint8_t i = 0; i < 80; i++) {
//		locoData[i].active = 1;
//		locoData[i].encodedSpeed = 0b11001101;
//
//	}
//}

void sendLocoPacketNoFunctionUpdate(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle) {
	sendLocoPacket(actualLocoIdx, queueIdxLoc, isRefreshCycle, -1);
}

void sendLocoPacketRefresh(uint8_t actualLocoIdx, unsigned char queueIdxLoc) {
	// currentRefreshCycleFunction is zero based
	sendLocoPacket(actualLocoIdx, queueIdxLoc, 1, currentRefreshCycleFunction+1);
}


void sendLocoPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle, int8_t functionToUpdate) {
	if (actualLocoIdx != -1) {

		struct LocoData* actualLoco = &locoData[actualLocoIdx];
		unsigned char address = actualLoco->address;
		unsigned char encodedSpeed = actualLoco->encodedSpeed;
		unsigned char deltaSpeed = actualLoco->deltaSpeed;
		unsigned char speed = actualLoco->numericSpeed;

		// address
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

		// function
		commandQueue[queueIdxLoc][8] = actualLoco->fl;
		commandQueue[queueIdxLoc][9] = actualLoco->fl;

		// REFRESH
		if (isRefreshCycle == 1) {

			// NEW PROTOCOL
			if (actualLoco->isNewProtocol == 1) {

				// zuerst Funktion schicken
				if (functionRefreshSent == 0) {
					functionRefreshSent = 1;
					uint8_t function = 0;
					encodedSpeed = encodeFunction(actualLoco, deltaSpeed, speed,
							functionToUpdate);
				} else {
					functionRefreshSent = 0;
				}

			} else {
				// DELTA PROTOCOL
				encodedSpeed = deltaSpeed;
			}
		} else {
			// NEW LOCO SPEED OR FUNCTION

			// NEW FUNCTION
			if ((actualLoco->isNewProtocol == 1) && (functionToUpdate == -1)) {
				encodedSpeed = encodeFunction(actualLoco, deltaSpeed, speed,
						functionToUpdate);
				// NEW SPEED DELTA PROTOCOL
			} else if (actualLoco->isNewProtocol != 1) {
				encodedSpeed = deltaSpeed;
			}

			newLocoQueuePop();

		}

		// speed
		for (uint8_t i = 0; i < 8; i++) {
			commandQueue[queueIdxLoc][10 + i] = (encodedSpeed >> (7 - i)) & 1;
		}

		if (actualLoco->deltaSpeed == mmChangeDirection) {
			actualLoco->deltaSpeed = 0;
			actualLoco->numericSpeed = 0;
			actualLoco->encodedSpeed = 0;
		}

		finish_mm_command_Loco(queueIdxLoc);

#ifdef NEW_LOCOCMD_EXTENDED
		if (!isRefreshCycle) {
			// send loco command 6 times
			for (uint8_t i = 1; i < NEW_LOCOCMD_REPETITIONS; i++) {
				for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
					commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
							commandQueue[queueIdxLoc][j];
				}
			}
		} else {
			// send loco command 2 times
			for (uint8_t i = 1; i < LOCOCMD_REPETITIONS; i++) {
				for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
					commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
							commandQueue[queueIdxLoc][j];
				}
			}
		}
#else
		for (uint8_t i = 1; i < LOCOCMD_REPETITIONS; i++) {
			for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
				commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
				commandQueue[queueIdxLoc][j];
			}
		}
#endif

	}
}

uint8_t encodeFunction(struct LocoData* actualLoco, unsigned char deltaSpeed,
		unsigned char speed, uint8_t function) {

	unsigned char abcd = deltaSpeed;
	unsigned char efgh = 0xFF;
	unsigned char mask = 0b01010101;

	switch (function) {
	case 1: // F1
		if (actualLoco->f1 == 1) { //ON

			//log_debug("F1 ON");
			if (speed != 10) {
				efgh = 0b11110011;
			} else {
				efgh = 0b00110011;
				void initPortData();
				void initLocoData();
				void initActiveLocoData(uint8_t number, unsigned char isNewProtocol);
				void sendStopAllLoco();

			}
		} else {
			//log_debug("F1 OFF");
			if (speed != 2) {
				efgh = 0b11110000;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 2: // F2
		if (actualLoco->f2 == 1) { //ON
			//log_debug("F2 ON");
			if (speed != 11) {
				efgh = 0b00001111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			//log_debug("F2 OFF");
			if (speed != 3) {
				efgh = 0b00001100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 3: // F3
		if (actualLoco->f3 == 1) { //ON
			//log_debug("F3 ON");
			if (speed != 13) {
				efgh = 0b00111111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			//log_debug("F3 OFF");
			if (speed != 5) {
				efgh = 0b00111100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 4: // F4
		if (actualLoco->f4 == 1) { //ON
			//log_debug("F4 ON");
			if (speed != 14) {
				efgh = 0b11111111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			if (speed != 6) {
				//log_debug("F4 OFF");
				efgh = 0b11111100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	}
	uint8_t encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);
	return encodedSpeed;
}

void sendSolenoidPacket(uint8_t solenoidIdx, unsigned char queueIdxLoc) {

	unsigned char address = solenoidQueue[solenoidIdx].address;
	unsigned char port = solenoidQueue[solenoidIdx].port;

	// address
	for (uint8_t i = 0; i < 8; i++)
		commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

	// unused
	commandQueue[queueIdxLoc][8] = 0;
	commandQueue[queueIdxLoc][9] = 0;

	// port
	for (uint8_t i = 0; i < 6; i++)
		commandQueue[queueIdxLoc][10 + i] = (port >> (5 - i)) & 1;

	if (!deactivatingSolenoid) {
		// new command --> activate port
#ifdef DEBUG
		log_debug3("activating decoder ", address);
#endif

		commandQueue[queueIdxLoc][16] = 1;
		commandQueue[queueIdxLoc][17] = 1;
		solenoidQueue[solenoidIdx].active = 1;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	} else {
		// active solenoid --> deactivate port
#ifdef DEBUG
		log_debug3("deactivating decoder ", address);
#endif
		commandQueue[queueIdxLoc][16] = 0;
		commandQueue[queueIdxLoc][17] = 0;
		solenoidQueue[solenoidIdx].active = 0;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	}

	// Doppelpacket inkl. Pausen erstellen
	finish_mm_command_Solenoid(queueIdxLoc);

	// Doppelpacket-Repetitionen erstellen
	for (uint8_t i = 1; i < SOLENOIDCMD_REPETITIONS; i++) {
		for (uint8_t j = 0; j < MM_COMMAND_LENGTH_SOLENOID; j++) {
			commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_SOLENOID + j] =
					commandQueue[queueIdxLoc][j];
		}
	}

	// End-Pause erstellen
	for (uint8_t i = 0; i < MM_END_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][(MM_COMMAND_LENGTH_SOLENOID
				* SOLENOIDCMD_REPETITIONS) + i] = 2;
	}

	if (startOneLocoRefresh == 0) {

		if (!deactivatingSolenoid) {
			// if new solenoid update queue
			solenoidQueuePop();
		} else {
			// switch off deactivation
			deactivatingSolenoid = 0;
		}

	}

}

void finish_mm_command_Loco(unsigned char queueIdxLoc) {

// pause
	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE_LOCO; i++) {
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + i] = 2;
	}

// copy packet
	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO
				+ i] = commandQueue[queueIdxLoc][i];

// add intra double packet pause
	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE_LOCO; i++) {
		commandQueue[queueIdxLoc][MM_DOUBLE_PACKET_LENGTH_LOCO + i] = 2;
	}

}

void finish_mm_command_Solenoid(unsigned char queueIdxLoc) {

// pause
	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + i] = 2;
	}

// copy packet
	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH
				+ MM_INTER_PACKET_PAUSE_SOLENOID + i] =
				commandQueue[queueIdxLoc][i];

// add intra double packet pause
	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][MM_DOUBLE_PACKET_LENGTH_SOLENOID + i] = 2;
	}

}


/******* INIT DATA *********/
void initPortData() {

	portData[0] = 0;
	portData[1] = 48;
	portData[2] = 12;
	portData[3] = 60;
	portData[4] = 3;
	portData[5] = 51;
	portData[6] = 15;
	portData[7] = 63;

}

void initLocoData() {

	deltaSpeedData[0] = 0; // STOP
	deltaSpeedData[1] = 48; // 1
	deltaSpeedData[2] = 240; // 2
	deltaSpeedData[3] = 12;
	deltaSpeedData[4] = 204;
	deltaSpeedData[5] = 60;
	deltaSpeedData[6] = 252;
	deltaSpeedData[7] = 3;
	deltaSpeedData[8] = 195;
	deltaSpeedData[9] = 51;
	deltaSpeedData[10] = 243;
	deltaSpeedData[11] = 15;
	deltaSpeedData[12] = 207;
	deltaSpeedData[13] = 63;
	deltaSpeedData[14] = 255; //14
	deltaSpeedData[15] = 1; //DEBUG

	locoData[0].address = 192;
	locoData[1].address = 128;
	locoData[2].address = 48;
	locoData[3].address = 240;
	locoData[4].address = 176;
	locoData[5].address = 32;
	locoData[6].address = 224;
	locoData[7].address = 160;
	locoData[8].address = 12;
	locoData[9].address = 204;

	locoData[10].address = 140;
	locoData[11].address = 60;
	locoData[12].address = 252;
	locoData[13].address = 188;
	locoData[14].address = 44;
	locoData[15].address = 236;
	locoData[16].address = 172;
	locoData[17].address = 8;
	locoData[18].address = 200;
	locoData[19].address = 136;
	locoData[20].address = 56;
	locoData[21].address = 248;
	locoData[22].address = 184;
	locoData[23].address = 40;
	locoData[24].address = 232;
	locoData[25].address = 168;
	locoData[26].address = 3;
	locoData[27].address = 195;
	locoData[28].address = 131;
	locoData[29].address = 34;
	locoData[30].address = 243;
	locoData[31].address = 179;
	locoData[32].address = 35;
	locoData[33].address = 227;
	locoData[34].address = 163;
	locoData[35].address = 15;
	locoData[36].address = 207;
	locoData[37].address = 143;
	locoData[38].address = 63;
	locoData[39].address = 255;
	locoData[40].address = 191;
	locoData[41].address = 47;
	locoData[42].address = 239;
	locoData[43].address = 175;
	locoData[44].address = 11;
	locoData[45].address = 203;
	locoData[46].address = 139;
	locoData[47].address = 59;
	locoData[48].address = 251;
	locoData[49].address = 187;
	locoData[50].address = 43;
	locoData[51].address = 235;
	locoData[52].address = 171;
	locoData[53].address = 2;
	locoData[54].address = 194;
	locoData[55].address = 130;
	locoData[56].address = 50;
	locoData[57].address = 242;
	locoData[58].address = 178;
	locoData[59].address = 34;
	locoData[60].address = 226;
	locoData[61].address = 162;
	locoData[62].address = 14;
	locoData[63].address = 206;
	locoData[64].address = 142;
	locoData[65].address = 62;
	locoData[66].address = 254;
	locoData[67].address = 190;
	locoData[68].address = 46;
	locoData[69].address = 238;
	locoData[70].address = 174;
	locoData[71].address = 10;
	locoData[72].address = 202;
	locoData[73].address = 138;
	locoData[74].address = 58;
	locoData[75].address = 250;
	locoData[76].address = 186;
	locoData[77].address = 42;
	locoData[78].address = 234;
	locoData[79].address = 0;

	for (uint8_t i = 0; i < 80; i++) {

		locoData[i].active = 0;
		locoData[i].isNewProtocol = 1;
		locoData[i].encodedSpeed = 17; // direction=1 , speed=0
		locoData[i].deltaSpeed = deltaSpeedData[0];
		locoData[i].fl = 0;
		locoData[i].f1 = 0;
		locoData[i].f2 = 0;
		locoData[i].f3 = 0;
		locoData[i].f4 = 0;
		locoData[i].direction = 1;
		locoData[i].refreshState = 0;

	}

	locoData[1].isNewProtocol = 0;
	locoData[5].isNewProtocol = 0;
	locoData[7].isNewProtocol = 0;
	locoData[17].isNewProtocol = 0;
	locoData[19].isNewProtocol = 0;
	locoData[23].isNewProtocol = 0;
	locoData[25].isNewProtocol = 0;
	locoData[53].isNewProtocol = 0;
	locoData[55].isNewProtocol = 0;
	locoData[59].isNewProtocol = 0;
	locoData[61].isNewProtocol = 0;
	locoData[71].isNewProtocol = 0;
	locoData[73].isNewProtocol = 0;
	locoData[77].isNewProtocol = 0;
	locoData[79].isNewProtocol = 0;

#ifdef ACTIVATE_DELTALOCOS_ON_INIT
	// dirty: Deltas are active from the start
	initActiveLocoData(1, 0);
	initActiveLocoData(5, 0);
	initActiveLocoData(7, 0);
	initActiveLocoData(17, 0);
	initActiveLocoData(19, 0);
	initActiveLocoData(23, 0);
	initActiveLocoData(25, 0);
	initActiveLocoData(53, 0);
	initActiveLocoData(55, 0);
	initActiveLocoData(59, 0);
	initActiveLocoData(61, 0);
	initActiveLocoData(71, 0);
	initActiveLocoData(73, 0);
	initActiveLocoData(77, 0);
	initActiveLocoData(79, 0);

#else
	initActiveLocoData(78, 1);

#endif

}

void initActiveLocoData(uint8_t number, unsigned char isNewProtocol) {

	uint8_t speed = 0;
	uint8_t direction = 1;

	locoData[number].isNewProtocol = isNewProtocol != 0;
	locoData[number].active = 1;
	locoData[number].refreshState = 0;
	locoData[number].numericSpeed = 0;
	locoData[number].direction = direction;
	locoData[number].encodedSpeed = speed;
	locoData[number].deltaSpeed = speed;
	locoData[number].fl = 0;
	locoData[number].f1 = 0;
	locoData[number].f2 = 0;
	locoData[number].f3 = 0;
	locoData[number].f4 = 0;

	if (locoData[number].isNewProtocol) {
		// NEW MM protocol change bits E F G H
		unsigned char efgh = 0xFF;
		unsigned char mask = 0b01010101;
		if (direction == 0) {
			if (speed <= 14 && speed >= 7) {
				efgh = 0b11001100;
			} else if (speed <= 6 && speed >= 0) {
				efgh = 0b11001111;
			}
		} else {
			if (speed <= 14 && speed >= 7) {
				efgh = 0b00110000;
			} else if (speed <= 6 && speed >= 0) {
				efgh = 0b00110011;
			}
		}

		// merge new E F G H values
		unsigned char abcd = locoData[number].encodedSpeed;
		locoData[number].encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);
	}
}

void sendStopAllLoco() {

	unsigned char queueIdxLoc;

	//alle Loks stoppen
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacketNoFunctionUpdate(i, queueIdxLoc, 0);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNewDataWhileSending = 0;
		while (prepareNewDataWhileSending == 0) {
			;
		}
	}

	//alle F1 ausschalten
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 1);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNewDataWhileSending = 0;
		while (prepareNewDataWhileSending == 0) {
			;
		}
	}

	//alle F2 ausschalten
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 2);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNewDataWhileSending = 0;
		while (prepareNewDataWhileSending == 0) {
			;
		}
	}

	//alle F3 ausschalten
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 3);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNewDataWhileSending = 0;
		while (prepareNewDataWhileSending == 0) {
			;
		}
	}

	//alle F4 ausschalten
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 4);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNewDataWhileSending = 0;
		while (prepareNewDataWhileSending == 0) {
			;
		}
	}

}

