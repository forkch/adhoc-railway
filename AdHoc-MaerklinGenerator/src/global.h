/*
 * global.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef GLOBAL_H_
#define GLOBAL_H_

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>


//#define MM_OSCI
#define activePrepare_OSCI

#define PWM2
//#define DEVEL_BOARD
//#define DEBUG
//#define DEBUG_EXTREME

#define DEBUG_OFF 0
#define DEBUG_ERROR 1
#define DEBUG_WARN 2
#define DEBUG_INFO 3
#define DEBUG_DEBUG 4

extern unsigned char debugLevel;

#define BOOSTER_COUNT 8


#define MM_PACKET_LENGTH 18
#define MM_INTER_PACKET_PAUSE 6
#define MM_INTER_DOUBLE_PACKET_PAUSE 160

#define MM_DOUBLE_PACKET_LENGTH (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE)

#define MM_COMMAND_LENGTH (MM_DOUBLE_PACKET_LENGTH + MM_INTER_DOUBLE_PACKET_PAUSE)

#define LOCOCMD_REPETITIONS 2
#define SOLENOIDCMD_REPETITIONS 2


typedef struct LocoData {
	unsigned char address;
	unsigned char encodedSpeed;
	unsigned char deltaSpeed;
	unsigned char numericSpeed;
	unsigned char direction;
	unsigned char fl :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	char active :1;
	char isNewProtocol :1;
	unsigned char refreshState;
};

extern struct LocoData locoData[80];
extern struct LocoData* newLoco;
extern unsigned char mmChangeDirection;


typedef struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

#define MAX_SOLENOID_QUEUE 5


extern struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
extern int solenoidQueueIdxEnter;

extern unsigned char portData[8];
extern unsigned char deltaSpeedData[16];

#endif /* GLOBAL_H_ */
