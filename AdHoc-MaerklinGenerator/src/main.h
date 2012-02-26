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

uint8_t currentLocoIdx = 79;

uint16_t locoCmdsSent = 0;

uint8_t debugCounter = 0;

uint8_t solenoidDataIdxPop = 0;
int8_t solenoidToDeactivate = -1;
uint8_t previousSolenoidDecoder = 0;

volatile unsigned char pwmQueueIdx = 0;
unsigned char commandQueue[2][54];

volatile uint8_t actualBit = 0;

char cmd[64];
unsigned char prepareNextData = 1;
uint8_t newSolenoid = 0;

// define forward declarations
struct LocoData locoData[80];
struct LocoData* newLoco = 0;

struct SolenoidData solenoidData[MAX_SOLENOID_QUEUE];
int solenoidDataIdxInsert = 0;
unsigned char portData[8];
unsigned char deltaSpeedData[16];
unsigned char mm2SpeedData[16];

/****** Funtion Declarations ******/
void initPortData();
void initLocoData();
void prepareDataForPWM();
void processASCIIData();
void process_solenoid_cmd(char*);
void process_loco_cmd(unsigned char*);
void enqueue_solenoid();
void enqueue_loco(uint8_t);

#endif /* MAIN_H_ */
