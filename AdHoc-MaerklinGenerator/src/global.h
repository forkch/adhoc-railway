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

//damit DeltaLocos mit im Decoder gespeichertem Speed beim Starten gestoppt werden
//#define ACTIVATE_DELTALOCOS_ON_INIT

//#define ACTIVATE_ALL_LOCOS_MM2_ON_INIT

//Testen einer Weiche, es wird abwechselnd Port 0 und 1 gesetzt.
//#define AUTO_SOLENOID
#define AUTO_SOLENOID_ADDRESS 1
#define AUTO_SOLENOID_PORT 0
#define AUTO_SOLENOID_TOP 200

#define PWM2
//#define DEVEL_BOARD
//#define DEBUG
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
#define MM_INTER_PACKET_PAUSE_LOCO 7				// 7  Packets @Loco => 1.414ms (1 Packet => 1 Trit => 202탎)
#define MM_INTER_DOUBLE_PACKET_PAUSE_LOCO 5 		// 5  Packets @Loco => 1.010ms (1 Packet => 1 Trit => 202탎)
#define MM_DOUBLE_PACKET_LENGTH_LOCO (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO)
#define MM_COMMAND_LENGTH_LOCO (MM_DOUBLE_PACKET_LENGTH_LOCO + MM_INTER_DOUBLE_PACKET_PAUSE_LOCO)
#define LOCOCMD_REPETITIONS 2
#define NEW_LOCOCMD_REPETITIONS 6					// MAX LOCO COMMAND LENGTH => (((2*18)+7)+5)*6 = 288 PACKETS

// SOLENOID
#define MM_INTER_PACKET_PAUSE_SOLENOID 8			//  8 Packets @Solenoid => 0.792ms (1 Packet => 1 Trit => 99탎)
#define MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID 15 	// 15 Packets @Solenoid => 1.485ms (1 Packet => 1 Trit => 99탎)
#define MM_END_PAUSE_SOLENOID 26					// 26 Packets @Solenoid => 2.574ms (1 Packet => 1 Trit => 99탎)
#define MM_DOUBLE_PACKET_LENGTH_SOLENOID (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_SOLENOID)
#define MM_COMMAND_LENGTH_SOLENOID (MM_DOUBLE_PACKET_LENGTH_SOLENOID + MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID)
#define SOLENOIDCMD_REPETITIONS 2					// MAX SOLENOID COMMAND LENGHT => ((((2*18)+8)+15)*2)+26 = 144 Packets

// ACHTUNG: Die L둵ge der Queue wird in der main.h  bestimmt. Je nach Definition der Packet-Parameter muss die Definition ge둵dert werden.

typedef struct LocoData {
	unsigned char address;
	unsigned char encodedSpeed;
	unsigned char deltaSpeed;
	unsigned char numericSpeed;
	unsigned char direction;
	unsigned char fl :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	char active :1;
	char isNewProtocol :1;
	unsigned char refreshState;
};

extern struct LocoData locoData[80];
extern struct LocoData* newLoco;
extern unsigned char mmChangeDirection;


typedef struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

#define MAX_SOLENOID_QUEUE 50


extern struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
extern int solenoidQueueIdxEnter;

extern unsigned char portData[8];
extern unsigned char deltaSpeedData[16];

#endif /* GLOBAL_H_ */
