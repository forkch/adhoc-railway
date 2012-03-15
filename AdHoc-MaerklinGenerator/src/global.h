/*
 * global.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef GLOBAL_H_
#define GLOBAL_H_

#define DEBUG
//#define DEBUG_EXTREME

#define DEBUG_OFF 0
#define DEBUG_ERROR 1
#define DEBUG_WARN 2
#define DEBUG_INFO 3
#define DEBUG_DEBUG 4

extern unsigned char debugLevel;

#define BOOSTER_COUNT 8
#define LOCOCMD_REPETITIONS 4

typedef struct LocoData {
	unsigned char address;
	unsigned char encodedSpeed;
	unsigned char speed;
	unsigned char direction;
	unsigned char fl :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	char active :1;
	char isNewProtocol :1;
	unsigned char refreshState;
	unsigned char repetitions;
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

#define MAX_SOLENOID_QUEUE 10


extern struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
extern int solenoidQueueIdxEnter;

extern unsigned char portData[8];
extern unsigned char deltaSpeedData[16];

#endif /* GLOBAL_H_ */
