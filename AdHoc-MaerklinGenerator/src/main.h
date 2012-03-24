/*
 * main.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef MAIN_H_
#define MAIN_H_

#include "global.h"

#define OSCI_TOP 3

#define OSCI_DATA_LENGTH 200
volatile unsigned char OsciData[OSCI_DATA_LENGTH];
volatile unsigned char newOsciData;
volatile uint16_t actualData;

#define MODE_SOLENOID 0
#define MODE_LOCO 1
#define SOLENOID_WAIT 10

#define TIMER0_PRESCALER      (1 << CS02) | (1 << CS00)

volatile unsigned char timer0_interrupt;

uint8_t currentRefreshCycleLocoIdx;

int solenoidQueueIdxEnter;
uint8_t solenoidQueueIdxFront;

uint8_t deactivatingSolenoid;
uint8_t solenoidToDeactivate;

volatile unsigned char pwm_mode[2];
volatile unsigned char pwmQueueIdx;
volatile uint16_t actualBit;
uint16_t commandLength;
volatile unsigned char prepareNextData;

volatile unsigned char commandQueue[2][MM_COMMAND_LENGTH * LOCOCMD_REPETITIONS];

char cmd[64]; // RS232 Input

struct LocoData locoData[80];
struct LocoData* newLoco;
int newLocoIdx;
uint8_t newLocoSpeed;
uint8_t newLocoFunction;

struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
unsigned char portData[8];
unsigned char deltaSpeedData[16];
unsigned char mmChangeDirection;

/****** Funtion Declarations ******/
inline void init() {

	newOsciData = 0;
	actualData = 0;

	timer0_interrupt = 0;

	currentRefreshCycleLocoIdx = 79;

	solenoidQueueIdxEnter = 0;
	solenoidQueueIdxFront = 0;

	deactivatingSolenoid = 0;
	solenoidToDeactivate = 0;

	pwmQueueIdx = 0;
	actualBit = 0;
	commandLength = 0;
	prepareNextData = 1;

	newLoco = 0;
	newLocoIdx = -1;
	newLocoSpeed = 0;
	newLocoFunction = 0;
	mmChangeDirection = 192;
}

void initPortData();
void initLocoData();
void prepareDataForPWM();
void sendLocoPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle, uint8_t refreshFunction);
void sendSolenoidPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc);
uint8_t encodeFunction(struct LocoData* actualLoco, unsigned char deltaSpeed,
		unsigned char speed, uint8_t function);
void finish_mm_command(unsigned char queueIdxLoc);
void processASCIIData();
void enqueue_solenoid();
void enqueue_loco(uint8_t);

inline uint8_t solenoidQueueEmpty() {
	return solenoidQueueIdxEnter == solenoidQueueIdxFront;
}

inline void solenoidQueuePop() {
	solenoidQueueIdxFront++;
	solenoidQueueIdxFront = solenoidQueueIdxFront % MAX_SOLENOID_QUEUE;

}

void all_loco();

#endif /* MAIN_H_ */
