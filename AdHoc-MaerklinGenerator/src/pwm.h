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

//#define SOLENOID_TOP 208
//#define LOCO_TOP 416
//#define SOLENOID_0 182
//#define SOLENOID_1 26
//#define LOCO_0 364
//#define LOCO_1 52


#define SOLENOID_TOP 208
#define LOCO_TOP 416
#define SOLENOID_1 182
#define SOLENOID_0 26
#define LOCO_1 364
#define LOCO_0 52


void initPWM();
void setPWMOutput(uint16_t duty);
void setSolenoid0();
void setSolenoid1();
void setSolenoidWait();
void setLoco0();
void setLoco1();
void setLocoWait();


#endif /* PWM_H_ */
