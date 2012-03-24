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
#define LOCO_BASE 520	//65
#define SOLENOID_INCREMENT 0
#define SOLENOID_BASE 260 //33
#endif

uint16_t LOCO_0;
uint16_t LOCO_1;
uint16_t LOCO_TOP;
uint16_t PWM_WAIT;


uint16_t SOLENOID_0;
uint16_t SOLENOID_1;
uint16_t SOLENOID_TOP;

void initPWM();
void setPWMOutput(uint16_t duty);
void setSolenoid0();
void setSolenoid1();
void setSolenoidWait();
void setLoco0();
void setLoco1();
void setLocoWait();

#endif /* PWM_H_ */
