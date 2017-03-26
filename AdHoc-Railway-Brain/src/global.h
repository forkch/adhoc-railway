/*
 * global.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef GLOBAL_H_
#define GLOBAL_H_

#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>

// Aktivieren der Debug-Leds an PC4 (gr웢) & PC5 (rot)
// und Switch an PD7
//#define DEBUG_IOS

// Ausgabe der Zust둵de der Booster auf die Leds
#define BOOSTER_STATE_ON_LED

// R웒kmeldung 웑er serielle Schnittstelle
#define LOGGING

// Level/Umfang der R웒kmeldungen
#define LOG_OFF 0
#define LOG_ERROR 1
#define LOG_WARN 2
#define LOG_INFO 3
#define LOG_DEBUG 4

// via Com 둵derbarer LogLevel, Initialwert = LOG_OFF
extern unsigned char logLevel;

// Anzahl Booster, die angeschlossen werden k쉗nen
#define BOOSTER_COUNT 8


// State-Machines
// --------------
// Main
#define SOLENOID_WAIT_TIMERCYCLES 10	// 10 x 13ms => 130ms (timer0 Interrupt)

// Loco
// w둯rend Refresh: Max Anzahl Befehle mit dem gleichen Protokoll
#define REFRESH_MAXNUMBER_MM_CMD 3
#define REFRESH_MAXNUMBER_MFX_CMD 3
#define REFRESH_MAXNUMBER_DCC_CMD 3

// Wiederholung eines neuen Lok-Befehls
#define NEW_MM_LOCOCMD_REPETITIONS 6
#define NEW_MFX_LOCOCMD_REPETITIONS 6
#define NEW_DCC_LOCOCMD_REPETITIONS 6
#define NEW_MFX_SIDCMD_REPETITIONS 4


// MFX UID SNIFFER
// ---------------
// Timer2 z둯lt alle 3.2탎 hoch
// L둵ge Bit 0 => 100탎 (100/3.2 = 31.2)
// L둵ge "halbes" Bit 1 => 50탎 (50/12.8 = 15.6)
#define MFX_THRESHOLD 23


// M둹klin-Motorola Protokoll
// --------------------------
#define MM_PACKET_LENGTH 18					// (18 Bytes) Befehl-/Paket-L둵ge = 9 Trits = 18 Packets
#define MM_PACKET_ADRESS_FN_LENGTH 10
#define MM_PACKET_DATA_LENGTH 8

// LOCO
#define MM_INTER_PACKET_PAUSE_LOCO 7		// 7  Packets @Loco => 1.442ms (1 Packet => 1/2 Trit => 206탎)
#define MM_COMMAND_LENGTH_LOCO (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO)
#define MM_INTER_COMMAND_PAUSE 19			// 19  Packets @Loco => 3.914ms (1 Packet => 1/2 Trit => 206탎)
#define MFX_TO_MM_INTER_CMD_PAUSE 27		// 27  Packets @Loco => 5.562ms (1 Packet => 1/2 Trit => 206탎)
#define DCC_TO_MM_INTER_CMD_PAUSE 19		// 19  Packets @Loco => 3.914ms (1 Packet => 1/2 Trit => 206탎)


// SOLENOID
//#define MM_START_PAUSE_SOLENOID 15					// 15 Packets @Solenoid => 1.542ms (1 Packet => 1/2 Trit => 102.8탎)
#define MM_INTER_PACKET_PAUSE_SOLENOID 8			//  8 Packets @Solenoid => 0.822ms (1 Packet => 1/2 Trit => 102.8탎)
#define MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID 15 	// 15 Packets @Solenoid => 1.542ms (1 Packet => 1/2 Trit => 102.8탎)
//#define MM_END_PAUSE_SOLENOID 15					// 15 Packets @Solenoid => 1.542ms (1 Packet => 1/2 Trit => 102.8탎)
#define MM_DOUBLE_PACKET_LENGTH_SOLENOID (2*MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_SOLENOID)
#define MM_COMMAND_LENGTH_SOLENOID (MM_DOUBLE_PACKET_LENGTH_SOLENOID + MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID)
#define MM_SOLENOIDCMD_REPETITIONS 4				// bisher 2	// MAX SOLENOID COMMAND LENGHT => ((((2*18)+8)+15)*2)+26 = 144 Packets
#define SOLENOID_INTER_CMD_PAUSE 15					//  15 Packets @Solenoid => 1.542ms (1 Packet => 1/2 Trit => 102.8탎)
#define LOCO_TO_SOLENOID_INTER_CMD_PAUSE 15			//  15 Packets @Solenoid => 1.542ms (1 Packet => 1/2 Trit => 102.8탎)
#define MFX_TO_SOLENOID_INTER_CMD_PAUSE 38			//  38 Packets @Solenoid => 3.9ms (1 Packet => 1/2 Trit => 102.8탎)
#define DCC_TO_SOLENOID_INTER_CMD_PAUSE 38			//  38 Packets @Solenoid => 3.9ms (1 Packet => 1/2 Trit => 102.8탎)


// M둹klin mfx-Protokoll
// ---------------------
//Gr쉝se eines Standard-Befehls (ohne Sync)
//Einschr둵kung: 9-Bit Adresse, Fahrbefehl (001) und Funktionen (011) werden immer gemeinsam gesendet.
//minimale L둵ge = 48 Bit
//maximale L둵ge = 57 Bit (ohne Stuffing!)
//(die Encodierung 'komprimiert' das Datenvolumen!)
#define MFX_PACKET_LENGTH 64				// Standard-Befehl (Fahren + Funktionen)
#define MFX_SYNC_LENGTH 6					// ACHTUNG: teilweise fix programmiert! 6 => nicht codiert, 3 => encodiert
#define MFX_START_SYNC_REPETITIONS 2		// ACHTUNG: fix programmiert!
#define MFX_END_SYNC_REPETITIONS 3			// ACHTUNG: fix programmiert!
#define MFX_COMMAND_LENGTH (MFX_START_SYNC_REPETITIONS*MFX_SYNC_LENGTH + MFX_PACKET_LENGTH + MFX_END_SYNC_REPETITIONS*MFX_SYNC_LENGTH)
//#define MFX_TO_MM2_AFTER_CMD_PAUSE 27		// 27 Packets @4*MFX_BASE (4*50탎) => 5.4ms
#define MFX_INTER_CMD_PAUSE 7				// 7  Packets @4*MFX_BASE (4*50탎) => 1.4ms
#define MFX_TO_DCC_AFTER_CMD_PAUSE 7		// 7  Packets @4*MFX_BASE (4*50탎) => 1.4ms
#define MM_TO_MFX_INTER_CMD_PAUSE 21		// 21 Packets @4*MFX_BASE (4*50탎) => 4.2ms
#define DCC_TO_MFX_INTER_CMD_PAUSE 7		// 7  Packets @4*MFX_BASE (4*50탎) => 1.4ms


//Gr쉝se eines SID-Befehls - Vergabe der Schienenadresse (ohne Sync)
//L둵ge = 69 Bit (ohne Stuffing)
#define MFX_SID_PACKET_LENGTH 78			// SID-Befehl (Adress-Zuweisung)
#define MFX_SID_COMMAND_LENGTH (MFX_START_SYNC_REPETITIONS*MFX_SYNC_LENGTH + MFX_SID_PACKET_LENGTH + MFX_END_SYNC_REPETITIONS*MFX_SYNC_LENGTH)


// DCC-Protokoll
// -------------
//Gr쉝se eines Standard-Befehls
//Einschr둵kung: gesendet werden nur die Kommandos:
// - Fahren/Richtung (010 oder 011)
// - Funktionsgruppe 1 (100) => Fn, F1 - F4
// - Funktionsgruppe 2.1 (1011) => F5 - F8
// - Funktionsgruppe 2.2 (1010) => F9 - F12
#define DCC_PACKET_LENGTH 37			// Standard-Befehl (Fahren oder Funktion) mit einer 2-Byte Adresse
#define DCC_SYNC_LENGTH 1
#define DCC_START_SYNC_REPETITIONS 18
#define DCC_END_SYNC_REPETITIONS 1
#define DCC_COMMAND_LENGTH (DCC_START_SYNC_REPETITIONS*DCC_SYNC_LENGTH + DCC_PACKET_LENGTH + DCC_END_SYNC_REPETITIONS*DCC_SYNC_LENGTH)
//#define DCC_TO_MM2_AFTER_CMD_PAUSE 17	// 17 Packets @4*DCC_BASE (4x58탎) => 3.9ms
#define DCC_INTER_CMD_PAUSE 0
#define MM_TO_DCC_INTER_CMD_PAUSE 17	// 17 Packets @4*DCC_BASE (4x58탎) => 3.9ms
#define MFX_TO_DCC_INTER_CMD_PAUSE 0


// ACHTUNG: L둵ge der CommandQueue
// Die L둵ge der Queue wird in der main.h  bestimmt. Je nach Definition der Packet-Parameter muss die Definition ge둵dert werden.
//
// ben쉞igte Gr쉝se der Queue (Stand 2016-06-05):
// - MM2-Loco		=> 62 = MM_COMMAND_LENGTH_LOCO + MM_INTER_COMMAND_PAUSE
// - MM2-Solenoid	=> 266 = MM_START_PAUSE_SOLENOID + (MM_COMMAND_LENGTH_SOLENOID * MM_SOLENOIDCMD_REPETITIONS) + MM_END_PAUSE_SOLENOID
// - MFX-Loco		=> max 115 = MFX_COMMAND_LENGTH + MFX_TO_MM2_AFTER_CMD_PAUSE  (nicht fix wegen Stuffing-Bits)
// - DCC-Loco		=> 81 => DCC_COMMAND_LENGTH + DCC_TO_MM2_AFTER_CMD_PAUSE
//
// die Gr쉝se der CommandQueue wird durch MM2-Solenoid bestimmt




// Loco-Buffers
// ------------
#define MM_LOCO_DATA_BUFFER_SIZE 20
#define MFX_LOCO_DATA_BUFFER_SIZE 20
#define DCC_LOCO_DATA_BUFFER_SIZE 20

#define LOCO_PROTOCOL_INDEX_BUFFER_SIZE 60


#define MAX_NEW_LOCO_QUEUES 50


// Solenoid-Buffer
// ---------------
#define MAX_SOLENOID_QUEUE 100



/*
extern struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
extern int solenoidQueueIdxEnter;

extern unsigned char portData[8];
extern unsigned char deltaSpeedData[16];
*/

// MM-Loco-Buffer
// --------------
//	encCmdAdrFn   => Adresse und FN
//	encCmdData[0] => Speed/Direction
//	encCmdData[1] => F1
//	encCmdData[2] => F2
//	encCmdData[3] => F3
//	encCmdData[4] => F4
typedef struct LocoDataMM {
	unsigned char address;		// M둹klin Motorola Adressen 1-80 (Adresse 80 wird als Idle-Adresse verwendet)
	unsigned char speed;
	unsigned char direction :1;
	unsigned char fn :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	unsigned char isDelta :1;
	unsigned char changingDirectionDelta :1;
	unsigned char encCmdAdrFn[MM_PACKET_ADRESS_FN_LENGTH];
	unsigned char encCmdData[5][MM_PACKET_DATA_LENGTH];
} LDMM;


// MFX-Loco-Buffer
// --------------
typedef struct LocoDataMFX {
	unsigned int UID;		// Decoder-spezifische ID
	unsigned int address;	// wird auch SID genannt / 1 - 16383
	unsigned char speed;
	unsigned char direction :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	unsigned char f5 :1;
	unsigned char f6 :1;
	unsigned char f7 :1;
	unsigned char f8 :1;
	unsigned char f9 :1;
	unsigned char f10 :1;
	unsigned char f11 :1;
	unsigned char f12 :1;
	unsigned char f13 :1;
	unsigned char f14 :1;
	unsigned char f15 :1;
	unsigned char f16 :1;
	unsigned char encCmd[MFX_COMMAND_LENGTH];
} LDMFX;


// DCC-Loco-Buffer
// --------------
// encCmd[0] => Speed/Direction
// encCmd[1] => FktGrp 1 / Fn, F1 - F4
// encCmd[2] => FktGrp 2.1 / F5 - F8
// encCmd[3] => FktGrp 2.2 / F9 - F12
typedef struct LocoDataDCC {
	unsigned int address;		// Uhlenbrock-Decoder kurzeAdresse: 1-127 langeAdresse: 1-9999
	unsigned char speed;
	unsigned char direction :1;
	unsigned char fn :1;
	unsigned char f1 :1;
	unsigned char f2 :1;
	unsigned char f3 :1;
	unsigned char f4 :1;
	unsigned char f5 :1;
	unsigned char f6 :1;
	unsigned char f7 :1;
	unsigned char f8 :1;
	unsigned char f9 :1;
	unsigned char f10 :1;
	unsigned char f11 :1;
	unsigned char f12 :1;
	unsigned char speed14Mode :1;
	unsigned char encCmd[4][DCC_COMMAND_LENGTH];
} LDDCC;


// MM-Solenoid-Queue
// -----------------
typedef struct SolenoidQueue {
	unsigned int address;		// Trit-Adresse (Motorola-Format)
	unsigned char port;			// Ausgangs-Port (1-8) des Decoders
	unsigned char boosterNr;	// an welchem Booster-Kreis die Weiche liegt
} SD;


// Index-Queues
// ------------
typedef struct LocoProtocolIdx {
	unsigned int address;
	unsigned char protocol;
} LBI;

typedef struct NewLocoCmdHiPrio {
	unsigned char protocol;
	unsigned char bufferIdx;
	unsigned char encCmdIdx;
} NLCHP;

typedef struct NewLocoCmdLoPrio {
	unsigned char protocol;
	unsigned char bufferIdx;
	unsigned char encCmdIdx;
} NLCLP;


#endif /* GLOBAL_H_ */
