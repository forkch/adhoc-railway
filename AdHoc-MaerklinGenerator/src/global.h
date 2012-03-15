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

#define LOCO_REPETITIONS 2

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


typedef struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

#define MAX_SOLENOID_QUEUE 20

//forward declarations
extern struct LocoData locoData[80];
extern struct LocoData* newLoco;

extern struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
extern int solenoidQueueIdxEnter;

extern unsigned char portData[8];
extern unsigned char deltaSpeedData[16];
extern unsigned char mmChangeDirection;

#endif /* GLOBAL_H_ */
