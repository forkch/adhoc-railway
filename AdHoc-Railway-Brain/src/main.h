/*
 * main.h
 *
 *  Created on: 19.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef MAIN_H_
#define MAIN_H_

#include "global.h"


volatile uint16_t actualData;

typedef enum PWM_MODE {
	MODE_MM2_SOLENOID, MODE_MM2_LOCO, MODE_MFX, MODE_DCC
} PM;

#define SOLENOID_WAIT 10

//clk/1024 => 20MHz/1024 => 51µs
#define TIMER0_PRESCALER      (1 << CS02) | (1 << CS00)

volatile unsigned char timer0_interrupt;

//MFX UID SNIFFER
volatile uint8_t mfxSnifferState;
volatile uint8_t mfxSnifferStateExit;
volatile uint8_t mfxExitTime;
volatile uint8_t stuffingCounter;
volatile unsigned long int mfxUID;



// PWM-Queue
// ---------
// ACHTUNG: Queue-Gršsse!!
// siehe Defintion der Packet-Gršssen der unterschiedlichen Protokolle in global.h
//volatile unsigned char pwmCmdQueue[2][MM_START_PAUSE_SOLENOID + (MM_COMMAND_LENGTH_SOLENOID * MM_SOLENOIDCMD_REPETITIONS) + MM_END_PAUSE_SOLENOID];
volatile unsigned char pwmCmdQueue[2][300];

volatile uint16_t pwmCmdLength[2];
volatile unsigned char pwm_mode[2];
volatile unsigned char pwmQueueIdx;				// Index in commandQueue fŸr die PWM-Ausgabe
volatile unsigned char prepareQueueIdx;			// Index in commandQueue fŸr die Daten-Vorbereitung (prepareNextData)
volatile uint16_t pwmOutputIdx;					// pwmCommandQueue Index bei der PWM-Ausgabe
uint16_t pwmOutputCmdLength;					// LŠnge des PWM-Befehls, der gerade ausgegeben wird

volatile unsigned char prepareNextData;			// Flag von PWM-IRS => die nŠchste Daten kšnnen in der pwmCommandQueue vorbereitet werden
volatile unsigned char nextDataPrepared;		// Flag von Daten Vorbereitung -> die nŠchsten Daten in der pwmCommandQueue fŸr die PWM sind bereit


// UART
// ----
// Empfangener String wird aus dem infifo-Buffer in dieses Array kopiert
char receivedCmdString[100];


// Buffers
// -------
// angemeldete Loks je Protokoll
struct LocoDataMM locoDataMM[MM_LOCO_DATA_BUFFER_SIZE];
struct LocoDataMFX locoDataMFX[MFX_LOCO_DATA_BUFFER_SIZE];
struct LocoDataDCC locoDataDCC[DCC_LOCO_DATA_BUFFER_SIZE];

// Indexliste um zu einer Adresse den richtigen Buffer (MM, MFX oder DCC) zu finden
struct LocoProtocolIdx locoProtocolIdx[LOCO_PROTOCOL_INDEX_BUFFER_SIZE];


// MFX Spezial
// -----------
unsigned char mfxSIDEncCmd[MFX_SID_COMMAND_LENGTH];		// encodierter MFX-Befehl um eine SID zu vergeben
unsigned char tmpMFXcmd[MFX_SID_COMMAND_LENGTH];		// Hilfsarray Encodierung MFX
unsigned int mfxSIDCmdCounter;							// MFX-Befehle "SID zuweisen" zum AusfŸhren bereit

unsigned char mm2IdleModeActive;						// wenn noch keine Lok angemeldet ist. Wird gelšscht sobald eine MM/MM2-Lok angemeldet wird


// Queues
// ------
// Loco
// neue Lok-Befehle mit unterschiedlicher PrioritŠt
struct NewLocoCmdHiPrio newLocoCmdHiPrio[MAX_NEW_LOCO_QUEUES];
struct NewLocoCmdLoPrio newLocoCmdLoPrio[MAX_NEW_LOCO_QUEUES];

int newLocoCmdHiPrioIdxEnter;
uint8_t newLocoCmdHiPrioIdxFront;
int newLocoCmdLoPrioIdxEnter;
uint8_t newLocoCmdLoPrioIdxFront;

unsigned char newLocoHiPrioQueue;					// Flag neuer Lok-Befehl in HighPriorityQueue

// Solenoid
struct SolenoidQueue solenoidQueue[MAX_SOLENOID_QUEUE];
int solenoidQueueIdxEnter;
uint8_t solenoidQueueIdxFront;

// Lookup Tables
// -------------
unsigned char portData[8];				// ternŠre Adressierung der Ports eines Weichen-Decoders
unsigned char mmSpeedData[16];			// ternŠre Code der Geschwindigkeit im alten MŠrklin-Motorola-Format
unsigned char dccSpeed28Data[32];		// Code der Geschwindigkeit fŸr 28 Stufen im DCC-Protokoll

// MM Spezial
// ----------
unsigned char mmChangeDirection;		// im alten MŠrklin-Format wird Geschwindigkeit 1 als Richtungswechsel interpretiert. Als Trit codiert 1 -> 192


// State-Machines
// --------------
//Main
uint8_t solenoidCmdCounter;
uint8_t solenoidDeactivatingTimerCycleCounter;

//Loco
uint8_t mmLocoCmdCounter;
uint8_t mfxLocoCmdCounter;
uint8_t dccLocoCmdCounter;
uint8_t mmLocoRepetitionCmdCounter;
uint8_t mfxLocoRepetitionCmdCounter;
uint8_t dccLocoRepetitionCmdCounter;
uint8_t mfxSIDRepetitionCmdCounter;

//uint8_t transitionToDCC;				// Flag: wenn ins DCC-Protokoll gewechselt wird, muss die vorhergehende Pause angepasst werden
uint8_t lastProtocoll;					// Protokoll des letzten gesendeten Befehls
uint8_t refreshMMSpeedFn;				// Flag: Zyklischer Wechsel zwischen Speed/Fn- und F1/F2/F3/F4-Refresh im Motorola Protokoll

uint8_t locoDataMMRefreshLocoIdx;
uint8_t locoDataMFXRefreshLocoIdx;
uint8_t locoDataDCCRefreshLocoIdx;
uint8_t locoDataMMRefreshEncCmdIdx;
uint8_t locoDataDCCRefreshEncCmdIdx;


/****** Funtion Declarations ******/
void init();

void initPortData();
void initLocoData();
void initIdleLocoData();

void processASCIIData();
void enqueue_solenoid();
void enqueue_loco_hiprio(unsigned char protocol, uint8_t bufferIdx, uint8_t encCmdIdx);
void enqueue_loco_loprio(unsigned char protocol, uint8_t bufferIdx, uint8_t encCmdIdx);

uint8_t solenoidQueueEmpty();
void solenoidQueuePop();
uint8_t newLocoHiPrioQueueEmpty();
void newLocoHiPrioQueuePop();
uint8_t newLocoLoPrioQueueEmpty();
void newLocoLoPrioQueuePop();

void prepareDataForPWM();
void prepareRefreshLocoPacket(uint8_t protocol);
void prepareNewLocoPacket(unsigned char newProtocol, unsigned char newBufferIdx, unsigned char NewEncCmdIdx);
void prepareSIDLocoPacket();
void prepareSolenoidPacket(unsigned char activate);


//States Main-Machine
void idleSolenoidDoLoco();
void activateSolenoid();
void waitSolenoidAfterActivation();
void deactivateSolenoid();
void waitSolenoidAfterDeactivation();


//States Loco-Machine
void refreshMM2Loco();
void refreshMFXLoco();
void refreshDCCLoco();
void newMM2sendMM2();
void newMM2sendMFX();
void newMM2sendDCC();
void newMFXsendMM2();
void newMFXsendMFX();
void newMFXsendDCC();
void newDCCsendMM2();
void newDCCsendMFX();
void newDCCsendDCC();
void newMFXSIDsendMM2();
void newMFXSIDsendMFX();
void newMFXSIDsendDCC();


#endif /* MAIN_H_ */
