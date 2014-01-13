/*
 * pwm.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "global.h"
#include "pwm.h"
#include "debug.h"
#include "main.h"

void initPWM() {

	pwmQueueIdx = 0;
	actualBit = 0;
	commandLength = 0;

	//Loco FREQUENCY
	pwm_mode[0] = MODE_LOCO;
	pwm_mode[1] = MODE_LOCO;

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

	//fast PWM with Prescaler = 1
#ifdef PWM2
	TCCR1A = (1 << WGM11)| (1<<WGM10);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS10);
#else
	TCCR1A = (1 << WGM11);
	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS10);
#endif


#ifdef PWM2
	OCR1AH = (uint8_t) (LOCO_TOP) >> 8;
	OCR1AL = (uint8_t) (LOCO_TOP);
#else
	ICR1H = (uint8_t) (LOCO_TOP >> 8);
	ICR1L = (uint8_t) (LOCO_TOP);
#endif

#ifdef PWM2
	OCR1AH = (uint8_t) (LOCO_TOP >> 8);
	OCR1AL = (uint8_t) LOCO_TOP;

	setLocoWait();

	TIMSK1 |= (1 << OCIE1B);
	TCCR1A |= (1 << COM1B1); // ACTIVATE PWM
#else
	ICR1H = (uint8_t) (LOCO_TOP >> 8);
	ICR1L = (uint8_t) LOCO_TOP;

	setLocoWait();

	TIMSK1 |= (1 << OCIE1A);
	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM
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


/********* PWM CODE **************/

#ifdef PWM2
ISR( TIMER1_COMPB_vect) {
#else
	ISR( TIMER1_COMPA_vect) {
#endif

	if (prepareNewDataWhileSending == 1 && actualBit == 0) {
		setLocoWait();
		return;
	}

	if (actualBit == 0) {
		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		prepareNewDataWhileSending = 1;
		if (pwm_mode[pwmQueueIdx] == MODE_SOLENOID) {
			//SOLENOID
#ifdef PWM2
			OCR1AH = (uint8_t) (SOLENOID_TOP >> 8);
			OCR1AL = (uint8_t) (SOLENOID_TOP);
#else
			ICR1H = (uint8_t) (SOLENOID_TOP >> 8);
			ICR1L = (uint8_t) (SOLENOID_TOP);
			TCNT1H = 0;
			TCNT1L = 0;
#endif

			commandLength = (MM_COMMAND_LENGTH_SOLENOID
					* SOLENOIDCMD_REPETITIONS) + MM_END_PAUSE_SOLENOID;

		} else {
			//LOCO FREQUENCY
#ifdef PWM2
			OCR1AH = (uint8_t) (LOCO_TOP >> 8);
			OCR1AL = (uint8_t) (LOCO_TOP);
#else
			ICR1H = (uint8_t) (LOCO_TOP >> 8);
			ICR1L = (uint8_t) (LOCO_TOP);
			TCNT1H = 0;
			TCNT1L = 0;
#endif
			commandLength = MM_COMMAND_LENGTH_LOCO
					* NEW_LOCOCMD_REPETITIONS;
		}
	}

	unsigned char b = commandQueue[pwmQueueIdx][actualBit];

	if (b == 0) {
		pwm_mode[pwmQueueIdx] == MODE_SOLENOID ? setSolenoid0() : setLoco0();
	} else if (b == 1) {
		pwm_mode[pwmQueueIdx] == MODE_SOLENOID ? setSolenoid1() : setLoco1();
	} else if (b == 2) {
		pwm_mode[pwmQueueIdx] == MODE_SOLENOID ?
				setSolenoidWait() : setLocoWait();
	}

	actualBit = (actualBit + 1) % commandLength;

}
