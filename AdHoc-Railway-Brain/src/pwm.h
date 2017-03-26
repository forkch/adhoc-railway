/*
 * pwm.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef PWM_H_
#define PWM_H_

#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define PWM_HELP_OUTPUT PC0
#define PWM_HELP_OUTPUT_PORT PORTC
#define PWM_HELP_OUTPUT_DDR DDRC
#define PWM_OUTPUT_PIN PD4	//ist im Schema falsch!
#define PWM_OUTPUT_PORT PORTD
#define PWM_OUTPUT_DDR DDRD

// 20MHz => 1Takt = 50ns
// Timings gem둺s Messung M둹klin Mobil Station 60653 2016-06-03
// MM2
#define MM2_LOCO_BASE 515		// 515@19.417kHz / MM2_LOCO_BASE entspricht 25.75탎
#define MM2_SOLENOID_BASE 257	// 257@38.9kHz	/ MM2_SOLENOID_BASE entspricht 12.85탎
// MFX
#define MFX_BASE 1000 			// 1000@10kHz T = 100탎  / MFX_BASE entspricht 50탎
// DCC
#define DCC_BASE 1160			// 1160@8621Hz T = 116탎 / DCC_BASE entspricht 58탎



uint16_t MM2_LOCO_0;
uint16_t MM2_LOCO_1;
uint16_t MM2_LOCO_TOP;
//uint16_t PWM_WAIT;


uint16_t MM2_SOLENOID_0;
uint16_t MM2_SOLENOID_1;
uint16_t MM2_SOLENOID_TOP;

uint16_t MFX_1;
uint16_t MFX_2;
uint16_t MFX_3;
uint16_t MFX_4;
uint16_t MFX_TOP;

uint16_t DCC_0;
uint16_t DCC_1;
uint16_t DCC_TOP;


void initPWM();
// nicht benutzt
//void setPWMOutput(uint16_t duty);
void setMM2Solenoid0();
void setMM2Solenoid1();
void setMM2SolenoidWait();
void setMM2Loco0();
void setMM2Loco1();
void setMM2PWMWait();

void setMFX1();
void setMFX2();
void setMFX3();
void setMFX4();
void setMFXPWMWait();

void setDCC0();
void setDCC1();
void setDCCPWMWait();



#endif /* PWM_H_ */
