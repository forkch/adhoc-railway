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


void initPWM();
void setPWMOutput(uint16_t duty);
void setSolenoid0();
void setSolenoid1();
void setSolenoidWait();
void setLoco0();
void setLoco1();
void setLocoWait();


#endif /* PWM_H_ */
