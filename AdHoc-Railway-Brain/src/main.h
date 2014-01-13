/*
 * main.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef MAIN_H_
#define MAIN_H_

#include "global.h"


#define TIMER0_PRESCALER      (1 << CS02) | (1 << CS00)

volatile unsigned char timer0_interrupt;
volatile unsigned char timer2_interrupt;

volatile unsigned char prepareNewDataWhileSending;

char cmd[64]; // RS232 Input

volatile unsigned char SolenoidTESTport;


// ACHTUNG: Die L�nge der Queue wird in der main.h  bestimmt. Je nach Definition der Packet-Parameter muss die Definition ge�ndert werden.

struct LocoData {
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

struct LocoData locoData[80];
struct LocoData* newLoco;
unsigned char mmChangeDirection;

struct NewLoco {
	int newLocoIdx;
	uint8_t newLocoSpeed;
	uint8_t newLocoFunction;
};

#define MAX_NEW_LOCO_QUEUE 200

struct NewLoco newLocoQueue[MAX_NEW_LOCO_QUEUE];
int newLocoQueueIdxEnter;
int newLocoQueueIdxFront;

struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

#define MAX_SOLENOID_QUEUE 100

struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
int solenoidQueueIdxEnter;
int solenoidQueueIdxFront;
int solenoidQueueIdxFront2;
/****** Funtion Declarations ******/

inline void init() {

	timer0_interrupt = 0;

	prepareNewDataWhileSending = 1;
	SolenoidTESTport = AUTO_SOLENOID_PORT;
}

void processASCIIData();
void enqueue_solenoid();
void enqueue_loco(uint8_t);

uint8_t newLocoQueueEmpty();
void newLocoQueuePop();
uint8_t solenoidQueueEmpty();
void solenoidQueuePop();


void all_loco();

#endif /* MAIN_H_ */
