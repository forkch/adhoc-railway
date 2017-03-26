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

	MM2_LOCO_0 = MM2_LOCO_BASE;
	MM2_LOCO_1 = 7 * MM2_LOCO_0;
	MM2_LOCO_TOP = 8 * MM2_LOCO_0;

	MM2_SOLENOID_0 = MM2_SOLENOID_BASE;
	MM2_SOLENOID_1 = 7 * MM2_SOLENOID_0;
	MM2_SOLENOID_TOP = 8 * MM2_SOLENOID_0;

	MFX_1 = MFX_BASE;
	MFX_2 = MFX_BASE;
	MFX_3 = 2 * MFX_BASE;
	MFX_4 = 2 * MFX_BASE;

	DCC_0 = 2 * DCC_BASE;
	DCC_1 = DCC_BASE;



	PWM_HELP_OUTPUT_DDR |= (1 << PWM_HELP_OUTPUT);

	PWM_OUTPUT_DDR |= (1 << PWM_OUTPUT_PIN);

	//fast PWM with Prescaler = 1
	TCCR1A = (1 << WGM11)| (1<<WGM10);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS10);

	OCR1AH = (uint8_t) (MM2_LOCO_TOP) >> 8;
	OCR1AL = (uint8_t) (MM2_LOCO_TOP);
}


// nicht benutzt
void setPWMOutput(uint16_t duty) {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);

	OCR1B = duty;
}



void setMM2Solenoid0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);

	OCR1BH = (uint8_t) (MM2_SOLENOID_0 >> 8);
	OCR1BL = (uint8_t) (MM2_SOLENOID_0);
}



void setMM2Solenoid1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (MM2_SOLENOID_1 >> 8);
	OCR1BL = (uint8_t) (MM2_SOLENOID_1);
}



void setMM2Loco0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);

	OCR1BH = (uint8_t) (MM2_LOCO_0 >> 8);
	OCR1BL = (uint8_t) (MM2_LOCO_0);
}



void setMM2Loco1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);

	OCR1BH = (uint8_t) (MM2_LOCO_1 >> 8);
	OCR1BL = (uint8_t) (MM2_LOCO_1);
}



void setMFX1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (MFX_1 >> 8);
	OCR1BL = (uint8_t) (MFX_1);
}



void setMFX2() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (MFX_2 >> 8);
	OCR1BL = (uint8_t) (MFX_2);
}



void setMFX3() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (MFX_3 >> 8);
	OCR1BL = (uint8_t) (MFX_3);
}



void setMFX4() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (MFX_4 >> 8);
	OCR1BL = (uint8_t) (MFX_4);
}



void setDCC0() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (DCC_0 >> 8);
	OCR1BL = (uint8_t) (DCC_0);
}



void setDCC1() {
	PWM_HELP_OUTPUT_PORT |= (1 << PWM_HELP_OUTPUT);
	OCR1BH = (uint8_t) (DCC_1 >> 8);
	OCR1BL = (uint8_t) (DCC_1);
}



void setPWMWait() {
	PWM_HELP_OUTPUT_PORT &= ~(1 << PWM_HELP_OUTPUT);

	OCR1BH = 0;
	OCR1BL = 0;
}

