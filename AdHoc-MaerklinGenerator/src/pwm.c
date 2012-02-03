/*
 * pwm.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "pwm.h"

void initPWM() {

	PWM_HELP_OUTPUT_DDR |= (1 << PWM_HELP_OUTPUT);

	DDRB |= (1 << PB1);

	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);
	ICR1H = 0x00;
	ICR1L = 0xCC; // counting to TOP takes

	TIMSK |= (1 << OCIE1A);

	OCR1AH = 0x00;
	TCCR1A = (1 << WGM11); //fast PWM with Prescaler = 8
}

void setPWMOutput(uint16_t duty) {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = duty;
}

void setSolenoid0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = 24;
}

void setSolenoid1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = 180;
}

void setSolenoidWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}

void setLoco0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = 48;

}
void setLoco1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1A = 260;
}

void setLocoWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
	OCR1A = 0;
}
