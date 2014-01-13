/*
 * global.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 */

#ifndef GLOBAL_H_
#define GLOBAL_H_

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>

//Ausgabe der Zustand der Booster auf die Debug-LEDs
#define DEBUG_BOOSTER_STATE

//Beim Init werden alle Loks gestoppt und die Funktionen zur�ckgesetzt
//#define SEND_STOP_ALL_LOCO_ON_INIT

//damit DeltaLocos mit im Decoder gespeichertem Speed beim Starten gestoppt werden
//#define ACTIVATE_DELTALOCOS_ON_INIT

//Testen einer Weiche, es wird abwechselnd Port 0 und 1 gesetzt.
//#define AUTO_SOLENOID
#define AUTO_SOLENOID_ADDRESS 1
#define AUTO_SOLENOID_PORT 0
#define AUTO_SOLENOID_TOP 200

#define PWM2
//#define DEVEL_BOARD
#define DEBUG
//#define DEBUG_EXTREME

#define DEBUG_OFF 0
#define DEBUG_ERROR 1
#define DEBUG_WARN 2
#define DEBUG_INFO 3
#define DEBUG_DEBUG 4

extern unsigned char debugLevel;

#define BOOSTER_COUNT 8

//neues Loco-Commando wird einmalig wiederholt ausgegeben
#define NEW_LOCOCMD_EXTENDED

#define MM_PACKET_LENGTH 18

// LOCO
#define MM_INTER_PACKET_PAUSE_LOCO 7				// 7  Packets @Loco => 1.414ms (1 Packet => 1 Trit => 202�s)
#define MM_INTER_DOUBLE_PACKET_PAUSE_LOCO 5 		// 5  Packets @Loco => 1.010ms (1 Packet => 1 Trit => 202�s)
#define MM_DOUBLE_PACKET_LENGTH_LOCO (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO)
#define MM_COMMAND_LENGTH_LOCO (MM_DOUBLE_PACKET_LENGTH_LOCO + MM_INTER_DOUBLE_PACKET_PAUSE_LOCO)
#define LOCOCMD_REPETITIONS 2
#define NEW_LOCOCMD_REPETITIONS 6					// MAX LOCO COMMAND LENGTH => (((2*18)+7)+5)*6 = 288 PACKETS

// SOLENOID
#define MM_INTER_PACKET_PAUSE_SOLENOID 8			//  8 Packets @Solenoid => 0.792ms (1 Packet => 1 Trit => 99�s)
#define MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID 15 	// 15 Packets @Solenoid => 1.485ms (1 Packet => 1 Trit => 99�s)
#define MM_END_PAUSE_SOLENOID 26					// 26 Packets @Solenoid => 2.574ms (1 Packet => 1 Trit => 99�s)
#define MM_DOUBLE_PACKET_LENGTH_SOLENOID (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_SOLENOID)
#define MM_COMMAND_LENGTH_SOLENOID (MM_DOUBLE_PACKET_LENGTH_SOLENOID + MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID)
#define SOLENOIDCMD_REPETITIONS 2					// MAX SOLENOID COMMAND LENGHT => ((((2*18)+8)+15)*2)+26 = 144 Packets

#endif /* GLOBAL_H_ */
