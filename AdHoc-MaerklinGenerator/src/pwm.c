/*
 * pwm.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "global.h"
#include "pwm.h"
#include "debug.h"

void initPWM() {

	LOCO_0 = LOCO_BASE + LOCO_INCREMENT;
	LOCO_1 = 7 * LOCO_0;
	LOCO_TOP = 8 * LOCO_0;
	PWM_WAIT = 0;

	SOLENOID_0 = SOLENOID_BASE + SOLENOID_INCREMENT;
	SOLENOID_1 = 7 * SOLENOID_0;
	SOLENOID_TOP = 8 * SOLENOID_0;

	PWM_HELP_OUTPUT_DDR |= (1 << PWM_HELP_OUTPUT);

#ifdef PWM2
	PWM_OUTPUT_DDR |= (1 << PD4);
#else
	PWM_OUTPUT_DDR |= (1 << PWM_OUTPUT_PIN);
#endif

	//fast PWM with Prescaler = 8
#ifdef PWM2
	TCCR1A = (1 << WGM11)| (1<<WGM10);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);
#else
	TCCR1A = (1 << WGM11);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);
#endif

	// OLD
#ifdef PWM2
	OCR1AH = (uint8_t) (SOLENOID_TOP) >> 8;
	OCR1AL = (uint8_t) (SOLENOID_TOP);
#else
	ICR1H = (uint8_t) (SOLENOID_TOP >> 8);
	ICR1L = (uint8_t) (SOLENOID_TOP);
#endif

}

void setPWMOutput(uint16_t duty) {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);

#ifdef PWM2
	OCR1B = duty;
#else
	OCR1AH = (uint8_t) (duty >> 8);
	OCR1AL = (uint8_t) (duty);
#endif

}

void setSolenoid0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = (uint8_t) (SOLENOID_0 >> 8);
	OCR1BL = (uint8_t) (SOLENOID_0);
#else
	OCR1AH = (uint8_t) (SOLENOID_0 >> 8);
	OCR1AL = (uint8_t) (SOLENOID_0);
#endif
}

void setSolenoid1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = (uint8_t) (SOLENOID_1 >> 8);
	OCR1BL = (uint8_t) (SOLENOID_1);
#else
	OCR1AH = (uint8_t) (SOLENOID_1 >> 8);
	OCR1AL = (uint8_t) (SOLENOID_1);
#endif
}

void setSolenoidWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = 0;
	OCR1BL = 0;
#else
	OCR1AH = 0;
	OCR1AL = 0;
#endif
}

void setLoco0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = (uint8_t) (LOCO_0 >> 8);
	OCR1BL = (uint8_t) (LOCO_0);
#else
	OCR1AH = (uint8_t) (LOCO_0 >> 8);
	OCR1AL = (uint8_t) (LOCO_0);
#endif

}
void setLoco1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = (uint8_t) (LOCO_1 >> 8);
	OCR1BL = (uint8_t) (LOCO_1);
#else
	OCR1AH = (uint8_t) (LOCO_1 >> 8);
	OCR1AL = (uint8_t) (LOCO_1);
#endif
}

void setLocoWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);
#ifdef PWM2
	OCR1BH = 0;
	OCR1BL = 0;
#else
	OCR1AH = 0;
	OCR1AL = 0;
#endif
}
