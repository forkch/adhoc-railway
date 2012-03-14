/*
 * pwm.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "pwm.h"

void initPWM() {

	PWM_HELP_OUTPUT_DDR |= (1 << PWM_HELP_OUTPUT);

	PWM_OUTPUT_DDR |= (1 << PWM_OUTPUT_PIN);
	//fast PWM with Prescaler = 8
	TCCR1A = (1 << COM1A1) | (1 << WGM11);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);

	ICR1H = 0x00;
//	ICR1L = 0xCC; // counting to TOP takes
	ICR1L = SOLENOID_TOP;
	TIMSK1 |= (1 << OCIE1A);

}

void setPWMOutput(uint16_t duty) {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = duty;
}

void setSolenoid0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
//	OCR1A = 24;
	OCR1A = SOLENOID_0;
}

void setSolenoid1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
//	OCR1A = 180;
	OCR1A = SOLENOID_1;
}

void setSolenoidWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}

void setLoco0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
//	OCR1A = 48;
	OCR1A = LOCO_0;

}
void setLoco1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
//	OCR1A = 260;
	OCR1A = LOCO_1;
}

void setLocoWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}
