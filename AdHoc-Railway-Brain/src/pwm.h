/*
 * pwm.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef PWM_H_
#define PWM_H_

#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define PWM_HELP_OUTPUT PC0
#define PWM_HELP_OUTPUT_PORT PORTC
#define PWM_HELP_OUTPUT_DDR DDRC
#define PWM_OUTPUT_PIN PD5
#define PWM_OUTPUT_PORT PORTD
#define PWM_OUTPUT_DDR DDRD

// 16MHz
//#define SOLENOID_TOP 208
//#define LOCO_TOP 416
//#define SOLENOID_1 182
//#define SOLENOID_0 26
//#define LOCO_1 364
//#define LOCO_0 52

// 20 MHz
//#define SOLENOID_1 228
//#define SOLENOID_0 33
//#define LOCO_1 455
//#define LOCO_0 65


// 16MHz
#ifdef DEVEL_BOARD
#define LOCO_INCREMENT 0
#define LOCO_BASE 416
#define SOLENOID_INCREMENT 0
#define SOLENOID_BASE 208
#else

// 20MHz
#define LOCO_INCREMENT 0
#define LOCO_BASE 505 // 505@19.8kHz / 520@19.2kHz
#define SOLENOID_INCREMENT 0
#define SOLENOID_BASE 247   // 247@40.5kHz / 306@32.7kHz / 260@38.4kHz
#endif

uint16_t LOCO_0;
uint16_t LOCO_1;
uint16_t LOCO_TOP;
uint16_t PWM_WAIT;


uint16_t SOLENOID_0;
uint16_t SOLENOID_1;
uint16_t SOLENOID_TOP;

#define MODE_SOLENOID 0
#define MODE_LOCO 1
#define SOLENOID_WAIT 10

volatile unsigned char pwm_mode[2];
volatile unsigned char pwmQueueIdx;
volatile uint16_t actualBit;
uint16_t commandLength;

// ACHTUNG: Queue-Grösse!!
volatile unsigned char commandQueue[2][MM_COMMAND_LENGTH_LOCO * NEW_LOCOCMD_REPETITIONS];
//volatile unsigned char commandQueue[2][MM_COMMAND_LENGTH_SOLENOID * SOLENOIDCMD_REPETITIONS];
//volatile unsigned char commandQueue[2][MM_COMMAND_LENGTH_LOCO * LOCOCMD_REPETITIONS];


void initPWM();
void setPWMOutput(uint16_t duty);
void setSolenoid0();
void setSolenoid1();
void setSolenoidWait();
void setLoco0();
void setLoco1();
void setLocoWait();

#endif /* PWM_H_ */
