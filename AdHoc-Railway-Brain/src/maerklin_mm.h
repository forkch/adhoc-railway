/*
 * maerklin_mm.h
 *
 *  Created on: Jan 12, 2014
 *      Author: fork
 */

#ifndef MAERKLIN_MM_H_
#define MAERKLIN_MM_H_

#include "main.h"
#include "global.h"

uint8_t currentRefreshCycleLocoIdx;
uint8_t currentRefreshCycleFunction;

uint8_t deactivatingSolenoid;
uint8_t solenoidToDeactivate;

unsigned char startOneLocoRefresh;
unsigned char startSecondSolenoidTransmition;
unsigned char functionRefreshSent;

unsigned char portData[8];
unsigned char deltaSpeedData[16];
unsigned char mmChangeDirection;

void init_maerklin_mm();

void prepareMaerklinMMDataForPWM();
void prepareLocoRefresh(struct LocoData locoData[80], unsigned char queueIdxLoc);
void prepareSolenoidCommandOrDeactivateSolenoid(unsigned char queueIdxLoc);
void prepareNewLococomandWithHighestPrio(unsigned char queueIdxLoc);
void sendLocoPacketNoFunctionUpdate(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle);
void sendLocoPacketRefresh(uint8_t actualLocoIdx, unsigned char queueIdxLoc);
void sendLocoPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle, int8_t functionToUpdate);
void sendSolenoidPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc);
uint8_t encodeFunction(struct LocoData* actualLoco, unsigned char deltaSpeed,
		unsigned char speed, uint8_t function);

void finish_mm_command_Loco(unsigned char queueIdxLoc);
void finish_mm_command_Solenoid(unsigned char queueIdxLoc);


void initPortData();
void initLocoData();
void initActiveLocoData(uint8_t number, unsigned char isNewProtocol);
void sendStopAllLoco();


#endif /* MAERKLIN_MM_H_ */
