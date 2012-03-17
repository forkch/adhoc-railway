/*
 * pwm.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "pwm.h"

void initPWM() {

	LOCO_0 = 65 + LOCO_INCREMENT;
	LOCO_1 = 7 * LOCO_0;
	LOCO_TOP = 8 * LOCO_0;

	SOLENOID_0 = 33 + SOLENOID_INCREMENT;
	SOLENOID_1 = 7 * SOLENOID_0;
	SOLENOID_TOP = 8 * SOLENOID_0;

	PWM_HELP_OUTPUT_DDR |= (1 << PWM_HELP_OUTPUT);

	PWM_OUTPUT_DDR |= (1 << PWM_OUTPUT_PIN);

	//fast PWM with Prescaler = 8
	TCCR1A = (1 << COM1A1) | (1 << WGM11);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);

	ICR1 = SOLENOID_TOP;
	TIMSK1 |= (1 << OCIE1A);

}

void setPWMOutput(uint16_t duty) {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = duty;
}

void setSolenoid0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = SOLENOID_0;
}

void setSolenoid1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = SOLENOID_1;
}

void setSolenoidWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}

void setLoco0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = LOCO_0;

}
void setLoco1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = LOCO_1;
}

void setLocoWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}
