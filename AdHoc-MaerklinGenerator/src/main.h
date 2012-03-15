/*
 * main.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef MAIN_H_
#define MAIN_H_

#include "global.h"

#define MODE_SOLENOID 0
#define MODE_LOCO 1
#define SOLENOID_WAIT 10

#define TIMER0_PRESCALER      (1 << CS02) | (1 << CS00)
volatile unsigned char timer0_interrupt = 1;

unsigned char pwm_mode = 0;
unsigned char isLocoCommand = 1;

#define MAX_COMMAND_QUEUE 1

uint8_t currentRefreshCycleLocoIdx = 79;

uint16_t locoCmdsSent = 0;

uint8_t debugCounter = 0;

int solenoidQueueIdxEnter = 0;
uint8_t solenoidQueueIdxFront = 0;

uint8_t deactivatingSolenoid = 0;
uint8_t solenoidToDeactivate = 0;
uint8_t previousSolenoidDecoder = 0;

volatile unsigned char pwmQueueIdx = 0;

#define MM_PACKET_LENGTH 18
#define MM_INTER_PACKET_PAUSE 6
#define MM_INTER_DOUBLE_PACKET_PAUSE 6

#define MM_DOUBLE_PACKET_LENGTH 2*MM_PACKET_LENGTH+MM_INTER_PACKET_PAUSE

#define MM_COMMAND_LENGTH MM_DOUBLE_PACKET_LENGTH + MM_INTER_DOUBLE_PACKET_PAUSE

unsigned char commandQueue[2][MM_COMMAND_LENGTH];

volatile uint8_t actualBit = 0;

char cmd[64];
unsigned char prepareNextData = 1;
uint8_t newSolenoid = 0;

// define forward declarations
struct LocoData locoData[80];
struct LocoData* newLoco = 0;
int newLocoIdx = -1;

uint8_t locoHiPrioQueue[80];
uint8_t locoHiPrioQueueEnter = 0;
uint8_t locoHiPrioQueueFront = 0;

uint8_t locoLoPrioQueue[80];
uint8_t locoLoPrioQueueEnter = 0;
uint8_t locoLoPrioQueueFront = 0;

struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
unsigned char portData[8];
unsigned char deltaSpeedData[16];
unsigned char mmChangeDirection = 192;

/****** Funtion Declarations ******/
void initPortData();
void initLocoData();
void prepareDataForPWM();
inline void sendLocoPacket(uint8_t actualLocoIdx, uint8_t queueIdxLoc, uint8_t refresh);
inline void sendSolenoidPacket(uint8_t actualLocoIdx, uint8_t queueIdxLoc);
void finish_mm_command();
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
