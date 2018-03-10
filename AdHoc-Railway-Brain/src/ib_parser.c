/*
 * ib_parser.c
 *
 *  Created on: 18.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#include <avr/io.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>

#include "global.h"
#include "main.h"
#include "ib_parser.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "booster.h"
#include "spi.h"


//================================================================================
// Intellibox Command Parser
//================================================================================
uint8_t parse_ib_cmd(char* receivedCmdString) {
	char delimiter[] = " ,";
	char **tokens;
	int count = 0;

	for (int i = 0; i < strlen(receivedCmdString); i++) {
		if (receivedCmdString[i] == ',' || receivedCmdString[i] == ' ')
			count++;
	}

	tokens = malloc(count * sizeof(char*));
	int j = 0;
	tokens[j] = strtok(receivedCmdString, delimiter);

	while (tokens[j] != NULL) {

		j++;
		tokens[j] = strtok(NULL, delimiter);

	}

	uint8_t ret;
	if (strcasecmp(tokens[0], "XGO") == 0 || strcasecmp(tokens[0], "X!") == 0) {
		ret = ib_go_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XSTOP") == 0
			|| strcasecmp(tokens[0], "X.") == 0) {
		ret = ib_stop_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XBS") == 0) {
		ret = ib_booster_state_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XT") == 0) {
		ret = ib_solenoid_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XTS") == 0) {
		ret = ib_solenoid_config_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XL") == 0) {
		ret = ib_loco_set_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XLS") == 0) {
		ret = ib_loco_config_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XDB") == 0) {
		ret = ib_debug_level_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XLOCREMOVE") == 0) {
		ret = ib_remove_loc_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XHALT") == 0) {
		ret = ib_loc_halt_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XBUFFERINFO") == 0) {
		ret = ib_buffer_info_cmd(tokens, j);
	//XGETUID benötigt zusätzliche externe Hardware! Der Anmeldevorgang einer mfx-Lok wird 'abgehört'.
	} else if (strcasecmp(tokens[0], "XGETUID") == 0) {
		ret = ib_get_uid_cmd(tokens, j);
	} else {
		ret = 0;
	}

	free(tokens);
	return ret;
}


//================================================================================
// Booster-Befehle
//================================================================================
uint8_t ib_go_cmd(char** tokens, uint8_t nTokens) {

	uint8_t number = 0;

	if (nTokens != 1 && nTokens != 2) {
		log_error("Command format: XGO {boosternumber}");
		return 0;
	}
#ifdef LOGGING
	log_debug("New Go Command");
#endif

	if (nTokens == 1) {
		//turn on ALL boosters

#ifdef LOGGING
		log_debug("turn on ALL boosters");
#endif
		go_all_boosters();

	} else {
		number = atoi(tokens[1]);
#ifdef LOGGING
		log_debug3("turn on booster ", number);
#endif
		go_booster(number);
	}



	//QUICK-FIX: Neuanmeldung MFX-Loks
/*	for (uint8_t j = 0; (locoDataMFX[j].address != 0 && j < MFX_LOCO_DATA_BUFFER_SIZE); j++){
		locoDataMFX[j].sidAssigned = 0;
		mfxSIDCmdCounter++;
	}
*/
	//damit möglichst bald das Brain sich als MFX-Centrale  meldet
	mfxBrainTimer = BRAIN_MFX_WAIT_TIMERCYCLES;

	return 1;

}

uint8_t ib_stop_cmd(char** tokens, uint8_t nTokens) {

	uint8_t number = 0;

	if (nTokens != 1 && nTokens != 2) {
		log_error("Command format: XSTOP {boosternumber}");
		return 0;
	}
#ifdef LOGGING
	log_debug("New Stop Command");
#endif

	if (nTokens == 1) {
		//turn off ALL boosters
#ifdef LOGGING
		log_debug("turn off ALL boosters");
#endif
		stop_all_boosters();

	} else {
		number = atoi(tokens[1]);
#ifdef LOGGING
		log_debug3("turn off booster ", number);
#endif
		stop_booster(number);

	}
	return 1;
}


uint8_t ib_booster_state_cmd(char** tokens, uint8_t nTokens) {

	if (nTokens != 1) {
		log_error("Command format: XBS");
		return 0;
	}
#ifdef LOGGING
	log_debug("Request Booster States");
#endif

	report_boosterstate();

	return 1;
}



//================================================================================
// Solenoid Configuration
//================================================================================
uint8_t ib_solenoid_config_cmd(char** tokens, uint8_t nTokens) {

	uint16_t solenoidAdr = 0;
	uint8_t solenoidType = 0;

	if (nTokens != 3) {
		log_error("Command format: XTS {turnoutnumber TURNOUT|CUTTER|TURNTABLE}");
		return 0;
	}
#ifdef LOGGING
	log_debug("New Solenoid Configuration");
#endif

	solenoidAdr = atol(tokens[1]);

	if (strcasecmp(tokens[2], "TURNOUT") == 0) {
		solenoidType = TURNOUT;
	} else if (strcasecmp(tokens[2], "CUTTER") == 0) {
		solenoidType = CUTTER;
	} else if (strcasecmp(tokens[2], "TURNTABLE") == 0) {
		solenoidType = TURNTABLE;
	} else {
		return 0;
	}

#ifdef LOGGING
	log_debug3("Decoder-Address: ", solenoidAdr);
	log_debug3("Decoder-Type: ", solenoidType);

#endif

	uint8_t i = 0;
	//Solenoid already configured?
	while (i < MM_SOLENOID_DATA_BUFFER_SIZE) {
		if (solenoidDataMM[i].numAddress == solenoidAdr) {
			log_error("Solenoid already initialized");
			return 0;
		}
		i++;
	}

	// Searching empty place in Solenoid-Buffer
	i = 0;
	while (solenoidDataMM[i].numAddress != 0) {
		i++;
		if (i >= MM_SOLENOID_DATA_BUFFER_SIZE) {
			log_error("Buffer Solenoid full");
			return 0;
		}
	}

	solenoidDataMM[i].numAddress = solenoidAdr;
	solenoidDataMM[i].solenoidType = solenoidType;
	solenoidDataMM[i].boosterNr = 0;
	solenoidDataMM[i].cutterState = 0;



	return 1;

}


//================================================================================
// Solenoid Command
//================================================================================
uint8_t ib_solenoid_cmd(char** tokens, uint8_t nTokens) {

	uint16_t address = 0;
	uint8_t port = 0;
	uint16_t solenoidAdr = 0;
	uint8_t trit1 = 0;
	uint8_t trit2 = 0;
	uint8_t trit3 = 0;
	uint8_t trit4 = 0;
	uint8_t trit5 = 0;

	if (nTokens != 3) {
		log_error("Command format: XT {turnoutnumber r|g|0|1}");
		//SPI_MasterTransmitDebug();
		return 0;
	}
#ifdef LOGGING
	log_debug("New Solenoid Command");
#endif

	solenoidAdr = atol(tokens[1]);
	//Adresse des Decoders berechnen: number 1-4 => address 1, number 5-8 => address 2, ...
	address = (unsigned char) ceilf(solenoidAdr / 4.f);

	uint8_t color = 0;
	if (strcasecmp(tokens[2], "g") == 0 || strcasecmp(tokens[2], "1") == 0) {
		color = 0;
	} else if (strcasecmp(tokens[2], "r") == 0
			|| strcasecmp(tokens[2], "0") == 0) {
		color = 1;
	} else {
		return 0;
	}

	//Port (1-8) des Decoders berechnen
	port = (unsigned char) (solenoidAdr - 1) % 4;
	port *= 2;
	port += color;

#ifdef LOGGING
	log_debug3("Decoder-Address: ", address);
	log_debug3("Decoder-Port: ", port);

#endif


// 5-Trits Weichenadresse berechnen
// Low = 0 (0x00), High = 1 (0x01), Open = 2 (0x10)
	trit1 = address  % 3;
	trit2 = (address / 3) % 3;
	trit3 = (address / 9) % 3;
	trit4 = (address / 27) % 3;
	trit5 = (address / 81) % 3;

// Motorola High-Trit Korrektur (für Adressen-Berechnung)
// High = 3 (0x11)
	if (trit1 == 1)
		trit1 = 3;
	if (trit2 == 1)
		trit2 = 3;
	if (trit3 == 1)
		trit3 = 3;
	if (trit4 == 1)
		trit4 = 3;
	if (trit5 == 1)
		trit5 = 3;


	solenoidQueue[solenoidQueueIdxEnter].tritAddress = ((256 * trit1) + (64 * trit2) + (16 * trit3) + (4 * trit4) + trit5);
	solenoidQueue[solenoidQueueIdxEnter].solenoidType = TURNOUT;

	int i = 0;
	//Solenoid already configured?
	while (i < MM_SOLENOID_DATA_BUFFER_SIZE) {
		if (solenoidDataMM[i].numAddress == solenoidAdr) {
			solenoidDataMM[i].tritAddress = solenoidQueue[solenoidQueueIdxEnter].tritAddress;
			solenoidDataMM[i].port = port;
			solenoidQueue[solenoidQueueIdxEnter].solenoidType = solenoidDataMM[i].solenoidType;
			solenoidQueue[solenoidQueueIdxEnter].cutterState = solenoidDataMM[i].cutterState = 1;
			break;
		}
		i++;
	}

#ifdef LOGGING
	log_debug3("Address: ", address);
	char convStr [6];
	log_debug2("Trit-Calc: ", utoa(solenoidQueue[solenoidQueueIdxEnter].tritAddress, convStr, 10));
#endif

	solenoidQueue[solenoidQueueIdxEnter].port = portData[port];

	enqueue_solenoid();

	return 1;

}


//================================================================================
// Lok-Befehl
//
//Neue Lok konfigurieren
// - in Liste locoProtocolIdx eintragen
// - je nach Protokoll in entsprechenden Buffer eintragen
//   (locoDataMM, locoDataMFX, locoDataDCC)
// - Befehle encodieren
// - speziell: mfx-Decodern muss eine Adresse zugewiesen werden.
//   Ein spezieller Befehl wird encodiert (mfxSIDCommand)
//================================================================================

uint8_t ib_loco_config_cmd(char** tokens, uint8_t nTokens) {

	uint16_t locoAdr;
	uint32_t decoderUID=0;
	uint8_t trit1;
	uint8_t trit2;
	uint8_t trit3;
	uint8_t trit4;
	char protocol[4];
	unsigned char address = 0;


	if (nTokens < 3) {
		log_error("Command format: XLS {loconumber MM|MM2|MFX|DCC [mfxUID]}");
		return 0;
	}

	locoAdr = atol(tokens[1]);
	strcpy(protocol, tokens[2]);

	if (strcasecmp(protocol, "MFX") == 0)  {
		if (nTokens != 4) {
			log_error("Command format: XLS {loconumber MM|MM2|MFX|DCC mfxUID}");
			return 0;
		}
		decoderUID = atol(tokens[3]);
	}


#ifdef LOGGING
	log_debug("New Loco Config Command");
#endif

	int i = 0;
	//Lok schon angemeldet?
	while (i < LOCO_PROTOCOL_INDEX_BUFFER_SIZE) {
		if (locoProtocolIdx[i].address == locoAdr) {
			log_error("Loco already initialized");
			return 0;
		}
		i++;
	}


	//leerer Platz in LokProtokollIndex-Liste suchen oder Idle-Lok-Adresse überschreiben
	i = 0;
	if (locoProtocolIdx[0].address != 80) {
		while (locoProtocolIdx[i].address != 0) {
			i++;
			if (i >= LOCO_PROTOCOL_INDEX_BUFFER_SIZE) {
				log_error("Buffer LocoBufferIndex full");
				return 0;
			}
		}
	}

	locoProtocolIdx[i].address = locoAdr;

	int j = 0;
	if (strcasecmp(protocol, "MM") == 0 || strcasecmp(protocol, "MM2") == 0) {
		//leerer Platz in MärklinMotorola LocDataBuffer suchen
		if (mm2IdleModeActive){
			j = 0;
			mm2IdleModeActive = 0;
		} else {
			while (locoDataMM[j].address != 0){
				j++;
				if (j >= MM_LOCO_DATA_BUFFER_SIZE){
					locoProtocolIdx[i].address = 0;
					log_error("Buffer LocoMM full");
					return 0;
				}
			}
		}
		locoDataMM[j].address = locoAdr;
		locoDataMM[j].speed = 0;
		locoDataMM[j].direction = 1;
		locoDataMM[j].fn = 0;
		locoDataMM[j].f1 = 0;
		locoDataMM[j].f2 = 0;
		locoDataMM[j].f3 = 0;
		locoDataMM[j].f4 = 0;
		locoDataMM[j].changingDirectionDelta = 0;

		if (strcasecmp(protocol, "MM") == 0){
			locoDataMM[j].isDelta = 1;
			locoProtocolIdx[i].protocol = MM;
		} else {
			locoDataMM[j].isDelta = 0;
			locoProtocolIdx[i].protocol = MM2;
		}


		//Märklin-Motorola Befehl "Fahren/Fn" encodieren
		//----------------------------------------------

		if (locoAdr < 80) {
			// 4-Trits Lokadresse berechnen
			// Low = 0 (0x00), High = 1 (0x01), Open = 2 (0x10)
			trit1 = locoAdr  % 3;
			trit2 = (locoAdr / 3) % 3;
			trit3 = (locoAdr / 9) % 3;
			trit4 = (locoAdr / 27) % 3;

			// Motorola High-Trit Korrektur (für Adressen-Berechnung)
			// High = 3 (0x11)
			if (trit1 == 1)
				trit1 = 3;
			if (trit2 == 1)
				trit2 = 3;
			if (trit3 == 1)
				trit3 = 3;
			if (trit4 == 1)
				trit4 = 3;

			address = ((64 * trit1) + (16 * trit2) + (4 * trit3) + trit4);
		}
		else {
			//Adressen grösser 79 aus Lookup-Tabelle
			address = mmAddressExt[locoAdr - 80];
		}

		//Adresse
		for (uint8_t k = 0; k < 8; k++)
			locoDataMM[j].encCmdAdrFn[k] = (address >> (7 - k)) & 1;

		//Fn
		locoDataMM[j].encCmdAdrFn[8] = 0;
		locoDataMM[j].encCmdAdrFn[9] = 0;

		//Speed
		if (locoDataMM[j].isDelta == 1) {
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[j].encCmdData[0][k] = 0;
		} else {
			//Speed = 0 im neuen MM-Format
			locoDataMM[j].encCmdData[0][0] = 0;
			locoDataMM[j].encCmdData[0][1] = 0;
			locoDataMM[j].encCmdData[0][2] = 0;
			locoDataMM[j].encCmdData[0][3] = 1;
			locoDataMM[j].encCmdData[0][4] = 0;
			locoDataMM[j].encCmdData[0][5] = 0;
			locoDataMM[j].encCmdData[0][6] = 0;
			locoDataMM[j].encCmdData[0][7] = 1;
		}


		//Märklin-Motorola Befehl "F1" encodieren
		//---------------------------------------
		locoDataMM[j].encCmdData[1][0] = 0;
		locoDataMM[j].encCmdData[1][1] = 1;
		locoDataMM[j].encCmdData[1][2] = 0;
		locoDataMM[j].encCmdData[1][3] = 1;
		locoDataMM[j].encCmdData[1][4] = 0;
		locoDataMM[j].encCmdData[1][5] = 0;
		locoDataMM[j].encCmdData[1][6] = 0;
		locoDataMM[j].encCmdData[1][7] = 0;

		//Märklin-Motorola Befehl "F2" encodieren
		//---------------------------------------
		locoDataMM[j].encCmdData[2][0] = 0;
		locoDataMM[j].encCmdData[2][1] = 0;
		locoDataMM[j].encCmdData[2][2] = 0;
		locoDataMM[j].encCmdData[2][3] = 0;
		locoDataMM[j].encCmdData[2][4] = 0;
		locoDataMM[j].encCmdData[2][5] = 1;
		locoDataMM[j].encCmdData[2][6] = 0;
		locoDataMM[j].encCmdData[2][7] = 0;

		//Märklin-Motorola Befehl "F3" encodieren
		//---------------------------------------
		locoDataMM[j].encCmdData[3][0] = 0;
		locoDataMM[j].encCmdData[3][1] = 0;
		locoDataMM[j].encCmdData[3][2] = 0;
		locoDataMM[j].encCmdData[3][3] = 1;
		locoDataMM[j].encCmdData[3][4] = 0;
		locoDataMM[j].encCmdData[3][5] = 1;
		locoDataMM[j].encCmdData[3][6] = 0;
		locoDataMM[j].encCmdData[3][7] = 0;

		//Märklin-Motorola Befehl "F4" encodieren
		//---------------------------------------
		locoDataMM[j].encCmdData[4][0] = 0;
		locoDataMM[j].encCmdData[4][1] = 1;
		locoDataMM[j].encCmdData[4][2] = 0;
		locoDataMM[j].encCmdData[4][3] = 1;
		locoDataMM[j].encCmdData[4][4] = 0;
		locoDataMM[j].encCmdData[4][5] = 1;
		locoDataMM[j].encCmdData[4][6] = 0;
		locoDataMM[j].encCmdData[4][7] = 0;


	} else if (strcasecmp(protocol, "MFX") == 0) {

		//leerer Platz in MärklinMFX LocDataBuffer suchen
		while (locoDataMFX[j].address != 0){
			j++;
			if (j >= MFX_LOCO_DATA_BUFFER_SIZE){
				locoProtocolIdx[i].address = 0;
				log_error("Buffer LocoMFX full");
				return 0;
			}
		}
		locoProtocolIdx[i].protocol = MFX;

		locoDataMFX[j].UID = decoderUID;
		locoDataMFX[j].address = locoAdr;
		locoDataMFX[j].speed = 0;
		locoDataMFX[j].direction = 1;
		locoDataMFX[j].f1 = 0;
		locoDataMFX[j].f2 = 0;
		locoDataMFX[j].f3 = 0;
		locoDataMFX[j].f4 = 0;
		locoDataMFX[j].f5 = 0;
		locoDataMFX[j].f6 = 0;
		locoDataMFX[j].f7 = 0;
		locoDataMFX[j].f8 = 0;
		locoDataMFX[j].f9 = 0;
		locoDataMFX[j].f10 = 0;
		locoDataMFX[j].f11 = 0;
		locoDataMFX[j].f12 = 0;
		locoDataMFX[j].f13 = 0;
		locoDataMFX[j].f14 = 0;
		locoDataMFX[j].f15 = 0;
		locoDataMFX[j].f16 = 0;
		locoDataMFX[j].sidAssigned = 0;


		//MärklinMFX-Befehl Fahren & Funktionen encodieren, direction = 1 (vorwärts)
		encodeMFXCmd (j, 1);

		mfxSIDCmdCounter++;


	} else if (strcasecmp(protocol, "DCC") == 0) {

		//leerer Platz in DCC LocDataBuffer suchen
		while (locoDataDCC[j].address != 0){
			j++;
			if (j >= DCC_LOCO_DATA_BUFFER_SIZE){
				locoProtocolIdx[i].address = 0;
				log_error("Buffer LocoDCC full");
				return 0;
			}
		}

		locoProtocolIdx[i].protocol = DCC;

		locoDataDCC[j].address = locoAdr;
		locoDataDCC[j].speed = 0;
		locoDataDCC[j].direction = 1;
		locoDataDCC[j].fn = 0;
		locoDataDCC[j].f1 = 0;
		locoDataDCC[j].f2 = 0;
		locoDataDCC[j].f3 = 0;
		locoDataDCC[j].f4 = 0;
		locoDataDCC[j].f5 = 0;
		locoDataDCC[j].f6 = 0;
		locoDataDCC[j].f7 = 0;
		locoDataDCC[j].f8 = 0;
		locoDataDCC[j].f9 = 0;
		locoDataDCC[j].f10 = 0;
		locoDataDCC[j].f11 = 0;
		locoDataDCC[j].f12 = 0;
		locoDataDCC[j].speed14Mode = 0;

		if (locoAdr >= 1 && locoAdr <= 127) {
			locoDataDCC[j].longAddress = 0;
		} else {
			locoDataDCC[j].longAddress = 1;
		}

		locoDataDCC[j].refreshEncCmdIdx = 0;

		//DCC-Befehle Adressbereich encodieren
		//------------------------------------

		uint8_t k = 0;
		//fix 18x Sync
		for (; k < 18; k++)
			locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 1;

		//Packet-Start-Bit "0"
		locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 0;
		k++;	//Beginn Adresse

		if (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			//Address-Byte
			locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 0;
			k++;
			for (uint8_t n = 0; n < 7; n++, k++)
				locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = (locoAdr >> (6 - n)) & 1;

		} else {										// lange Adresse
			//Address-Byte 1
			locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 1;
			locoDataDCC[j].encCmd[0][k+1] = locoDataDCC[j].encCmd[1][k+1] = locoDataDCC[j].encCmd[2][k+1] = locoDataDCC[j].encCmd[3][k+1] = 1;
			k += 2;
			for (uint8_t n = 0; n < 6; n++, k++)
				locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = (locoAdr >> (13 - n)) & 1;

			//Inter-Address-Bytes-Bit "0"
			locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 0;
			k++;	// Beginn Adresse

			//Address-Byte 2
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = (locoAdr >> (7 - n)) & 1;
		}

		//Data-Byte-Start-Bit "0"
		locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[3][k] = 0;

		uint8_t m = k;		//aktueller k-Index speichern


		//DCC-Befehl Fahren encodieren
		//----------------------------

		//Speed/Direction (vorwärts)
		locoDataDCC[j].encCmd[0][k+1] = 0;
		locoDataDCC[j].encCmd[0][k+2] = 1;
		locoDataDCC[j].encCmd[0][k+3] = 1;
		k +=4;		//Beginn Speed Daten

		for (uint8_t n = 0; n < 5; n++, k++)
			locoDataDCC[j].encCmd[0][k] = 0;

		//Data-Byte-Start-Bit "0"
		locoDataDCC[j].encCmd[0][k] = 0;
		k++; 		//Beginn Error Detection Data Byte

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[0][19 + n] ^ locoDataDCC[j].encCmd[0][28 + n];

		} else {										// lange Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[0][k] = locoDataDCC[j].encCmd[0][19 + n] ^ locoDataDCC[j].encCmd[0][28 + n] ^ locoDataDCC[j].encCmd[0][37 + n];
		}

		//Packet-End-Bit "1"
		locoDataDCC[j].encCmd[0][k] = 1;

		//fix 1x Sync
		locoDataDCC[j].encCmd[0][k+1] = 1;



		//DCC-Befehl FktGrp1 encodieren (Fn, F1 - F4)
		//-----------------------------
		k = m;

		//FktGrp1
		locoDataDCC[j].encCmd[1][k+1] = 1;
		locoDataDCC[j].encCmd[1][k+2] = 0;
		locoDataDCC[j].encCmd[1][k+3] = 0;
		k += 4; 	//Beginn FktGrp1 Daten

		for (uint8_t n = 0; n < 5; n++, k++)
			locoDataDCC[j].encCmd[1][k] = 0;

		//Data-Byte-Start-Bit "0"
		locoDataDCC[j].encCmd[1][k] = 0;
		k++;			//Beginn Error Detetction Data Byte

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[1][19 + n] ^ locoDataDCC[j].encCmd[1][28 + n];

		} else {										// lange Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[1][k] = locoDataDCC[j].encCmd[1][19 + n] ^ locoDataDCC[j].encCmd[1][28 + n] ^ locoDataDCC[j].encCmd[1][37 + n];
		}

		//Packet-End-Bit "1"
		locoDataDCC[j].encCmd[1][k] = 1;

		//fix 1x Sync
		locoDataDCC[j].encCmd[1][k+1] = 1;



		//DCC-Befehl FktGrp2.1 encodieren (F5 - F8)
		//-------------------------------
		k = m;

		//FktGrp2.1
		locoDataDCC[j].encCmd[2][k+1] = 1;
		locoDataDCC[j].encCmd[2][k+2] = 0;
		locoDataDCC[j].encCmd[2][k+3] = 1;
		locoDataDCC[j].encCmd[2][k+4] = 1;
		k += 5; 	//Beginn FktGrp2.1 Daten

		for (uint8_t n = 0; n < 4; n++, k++)
			locoDataDCC[j].encCmd[2][k] = 0;

		//Data-Byte-Start-Bit "0"
		locoDataDCC[j].encCmd[2][k] = 0;
		k++;			//Beginn Error Detetction Data Byte

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[2][19 + n] ^ locoDataDCC[j].encCmd[2][28 + n];

		} else {										// lange Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[2][k] = locoDataDCC[j].encCmd[2][19 + n] ^ locoDataDCC[j].encCmd[2][28 + n] ^ locoDataDCC[j].encCmd[2][37 + n];
		}

		//Packet-End-Bit "1"
		locoDataDCC[j].encCmd[2][k] = 1;

		//fix 1x Sync
		locoDataDCC[j].encCmd[2][k+1] = 1;


		//DCC-Befehl FktGrp2.2 encodieren (F9 - F12)
		//-------------------------------
		k = m;

		//FktGrp2.2
		locoDataDCC[j].encCmd[3][k+1] = 1;
		locoDataDCC[j].encCmd[3][k+2] = 0;
		locoDataDCC[j].encCmd[3][k+3] = 1;
		locoDataDCC[j].encCmd[3][k+4] = 0;
		k += 5;		//Beginn FktGrp2.2 Daten

		for (uint8_t n = 0; n < 4; n++, k++)
			locoDataDCC[j].encCmd[3][k] = 0;

		//Data-Byte-Start-Bit "0"
		locoDataDCC[j].encCmd[3][k] = 0;
		k++;			//Beginn Error Detetction Data Byte

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[3][k] = locoDataDCC[j].encCmd[3][19 + n] ^ locoDataDCC[j].encCmd[3][28 + n];

		} else {										// lange Adresse
			for (uint8_t n = 0; n < 8; n++, k++)
				locoDataDCC[j].encCmd[3][k] = locoDataDCC[j].encCmd[3][19 + n] ^ locoDataDCC[j].encCmd[3][28 + n] ^ locoDataDCC[j].encCmd[3][37 + n];
		}

		//Packet-End-Bit "1"
		locoDataDCC[j].encCmd[3][k] = 1;

		//fix 1x Sync
		locoDataDCC[j].encCmd[3][k+1] = 1;

	} else {
		return 0;
	}



#ifdef LOGGING
	char convStr [11];
	log_debug2("Loco Number: ", utoa(locoAdr, convStr, 10));
	log_debug2("Protocol: ", protocol);
	if (nTokens == 4) {
		log_debug2("UID: ", ultoa(decoderUID, convStr, 10));
	}

#endif

	return 1;

}


//================================================================================
//Neuer Befehl für eine Lok
// - Werte in den Lok-Buffern (locoDataMM, locoDataMFX, locoDataDCC) aktualisieren
// - damit die Änderung sofort ausgegeben wird, Eintrag in die newLocoCmdHiPrio-
//   oder newLocoCmdLoPrio-Queues eintragen
// - Hohe Priorität: Verlangsamen, F4 (ABV aus), Emergency Stop
// - Tiefe Priorität: Beschleunigen, andere Funktionen
//================================================================================
uint8_t ib_loco_set_cmd(char** tokens, uint8_t nTokens) {

	uint16_t locoAdr = 0;
	uint16_t t = 0;
	uint8_t speed = 0;
	uint16_t speedCalc = 0;
	uint8_t direction = 0;
	uint8_t fn = 0;
	uint8_t f1 = 0;
	uint8_t f2 = 0;
	uint8_t f3 = 0;
	uint8_t f4 = 0;
	uint8_t f5 = 0;
	uint8_t f6 = 0;
	uint8_t f7 = 0;
	uint8_t f8 = 0;
	uint8_t f9 = 0;
	uint8_t f10 = 0;
	uint8_t f11 = 0;
	uint8_t f12 = 0;
	uint8_t f13 = 0;
	uint8_t f14 = 0;
	uint8_t f15 = 0;
	uint8_t f16 = 0;
	unsigned char newSpeedFnCmd = 0;
	unsigned char encSpeed = 0;
	unsigned char encSpeedTemp = 0;


	if (nTokens < 4) {
		log_error(
				"Command format: XL {loconumber speed direction} [fn F1 F2 F3 F4 F5 ... F16]");
		return 0;
	}

#ifdef LOGGING
	log_debug("New Loco Set Command");
#endif

	locoAdr = atol(tokens[1]);
	speed = atoi(tokens[2]);
	if (nTokens > 3) {
		direction = atoi(tokens[3]);
		if (nTokens > 4) {
			fn = atoi(tokens[4]);
			if (nTokens > 5) {
				f1 = atoi(tokens[5]);
				if (nTokens > 6) {
					f2 = atoi(tokens[6]);
					if (nTokens > 7) {
						f3 = atoi(tokens[7]);
						if (nTokens > 8) {
							f4 = atoi(tokens[8]);
							if (nTokens > 9) {
								f5 = atoi(tokens[9]);
								if (nTokens > 10) {
									f6 = atoi(tokens[10]);
									if (nTokens > 11) {
										f7 = atoi(tokens[11]);
										if (nTokens > 12) {
											f8 = atoi(tokens[12]);
											if (nTokens > 13) {
												f9 = atoi(tokens[13]);
												if (nTokens > 14) {
													f10 = atoi(tokens[14]);
													if (nTokens > 15) {
														f11 = atoi(tokens[15]);
														if (nTokens > 16) {
															f12 = atoi(tokens[16]);
															if (nTokens > 17) {
																f13 = atoi(tokens[17]);
																if (nTokens > 18) {
																	f14 = atoi(tokens[18]);
																	if (nTokens > 19) {
																		f15 = atoi(tokens[19]);
																		if (nTokens > 20) {
																			f16 = atoi(tokens[20]);
																		}
																	}
																}
															}
														}
													}
												}
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	//Lok-Adresse im LocoProtocolIdx suchen, damit man weiss welches Protokoll die Lok hat
	uint8_t i = 0;
	while (locoProtocolIdx[i].address != locoAdr){
		i++;
		if (i >= LOCO_PROTOCOL_INDEX_BUFFER_SIZE){
			log_error("Loco not initialized");
			return 0;
		}
	}

	uint8_t j = 0;
	if (locoProtocolIdx[i].protocol == MM || locoProtocolIdx[i].protocol == MM2) {
		//Lok-Adresse im locoDataMM-Buffer suchen
		while (locoDataMM[j].address != locoAdr){
			j++;
			if (j >= MM_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoMM");
				return 0;
			}
		}

		//Speed umrechnen für MM & MM2 (128er Auflösung auf 15er)
		// 0 => 0
		// 1-6 => 1
		// 7-16 => 2
		// 17-26 => 3
		// |   |    |
		// 117-126 => 13
		// 127 => 14
		if (speed > 0) {
			speed = (speed + 13) / 10;
		}

		// Notstop
		if (direction == 2) {
			speed = 0;
			f4 = 1;
			locoDataMM[j].speed = 0;
			locoDataMM[j].f4 = 1;
			enqueue_loco_hiprio(MM2, j, 0);
			newSpeedFnCmd = 1;

		//Sicherstellen, dass Richtungsänderung im Buffer zuerst ausgeführt wird und nicht von einem neuen Befehl überschrieben wird
		//Richtungsänderung nur wenn letzter Speed == 0
		} else if ((direction != locoDataMM[j].direction) && (locoDataMM[j].speed == 0)) {
			if (locoDataMM[j].isDelta == 1) {
				enqueue_loco_hiprio(MM, j, 0);
				locoDataMM[j].changingDirectionDelta = 1;
			} else {
				enqueue_loco_hiprio(MM2, j, 0);
			}
			locoDataMM[j].direction = direction;
			newSpeedFnCmd = 1;

		// Neuer Speed wenn Richtung gleich
		} else if (direction == locoDataMM[j].direction){
			if (locoDataMM[j].speed < speed){
				enqueue_loco_loprio(MM2, j, 0);
			} else if (locoDataMM[j].speed > speed){
				enqueue_loco_hiprio(MM2, j, 0);
			}
			locoDataMM[j].speed = speed;
			newSpeedFnCmd = 1;
		} else {
			log_error("Loco-Command not allowed");
			return 0;
		}

		//Fn nur ändern wenn auch empfangen wurde
		if (nTokens > 4) {
			if (locoDataMM[j].fn != fn){
				locoDataMM[j].fn = fn;
				locoDataMM[j].encCmdAdrFn[8] = fn;
				locoDataMM[j].encCmdAdrFn[9] = fn;
				enqueue_loco_loprio(MM2, j, 0);
			}
		}

		encSpeed = mmSpeedData[speed];

		//Märklin-Motorola Befehl encodieren
		//----------------------------------------------
		if (newSpeedFnCmd == 1) {
			//"Fahren" & Fn encodieren
			//Speed
			if (locoDataMM[j].isDelta == 1) {
				if (locoDataMM[j].changingDirectionDelta)
					encSpeed = mmChangeDirection;
				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[0][k] = (encSpeed >> (7 - k)) & 1;
			} else {

				// NEW MM protocol change bits E F G H
				unsigned char efgh = 0xFF;
				unsigned char mask = 0b01010101;
				if (locoDataMM[j].direction == 0) {
					if (speed <= 14 && speed >= 7) {
						efgh = 0b11001100;
					} else if (speed <= 6 && speed >= 0) {
						efgh = 0b11001111;
					}
				} else {
					if (speed <= 14 && speed >= 7) {
						efgh = 0b00110000;
					} else if (speed <= 6 && speed >= 0) {
						efgh = 0b00110011;
					}
				}

				// merge new E F G H values
				unsigned char abcd = encSpeed;
				encSpeed = abcd ^ ((abcd ^ efgh) & mask);

				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[0][k] = (encSpeed >> (7 - k)) & 1;
			}

			//"F1" encodieren
			encSpeedTemp = encodeMM2Function(1, locoDataMM[j].f1, encSpeed, speed);
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[j].encCmdData[1][k] = (encSpeedTemp >> (7 - k)) & 1;
			//"F2" encodieren
			encSpeedTemp = encodeMM2Function(2, locoDataMM[j].f2, encSpeed, speed);
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[j].encCmdData[2][k] = (encSpeedTemp >> (7 - k)) & 1;
			//"F3" encodieren
			encSpeedTemp = encodeMM2Function(3, locoDataMM[j].f3, encSpeed, speed);
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[j].encCmdData[3][k] = (encSpeedTemp >> (7 - k)) & 1;
			//"F4" encodieren
			encSpeedTemp = encodeMM2Function(4, locoDataMM[j].f4, encSpeed, speed);
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[j].encCmdData[4][k] = (encSpeedTemp >> (7 - k)) & 1;

		}


		//Fkt nur ändern wenn auch empfangen wurde
		if (nTokens > 4) {
			//Märklin-Motorola Befehl "F1" encodieren
			//---------------------------------------
			if (locoDataMM[j].f1 != f1) {
				encSpeedTemp = encodeMM2Function(1, f1, encSpeed, speed);
				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[1][k] = (encSpeedTemp >> (7 - k)) & 1;
				locoDataMM[j].f1 = f1;
				enqueue_loco_loprio(MM2, j, 1);
			}

			//Märklin-Motorola Befehl "F2" encodieren
			//---------------------------------------
			if (locoDataMM[j].f2 != f2){
				encSpeedTemp = encodeMM2Function(2, f2, encSpeed, speed);
				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[2][k] = (encSpeedTemp >> (7 - k)) & 1;
				locoDataMM[j].f2 = f2;
				enqueue_loco_loprio(MM2, j, 2);
			}

			//Märklin-Motorola Befehl "F3" encodieren
			//---------------------------------------
			if (locoDataMM[j].f3 != f3){
				encSpeedTemp = encodeMM2Function(3, f3, encSpeed, speed);
				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[3][k] = (encSpeedTemp >> (7 - k)) & 1;
				locoDataMM[j].f3 = f3;
				enqueue_loco_loprio(MM2, j, 3);
			}
		}

		//Fkt nur ändern wenn auch empfangen wurde und bei Notstop automatisch F4 aktivieren (RangierModus)
		if (nTokens > 4 || direction == 2) {
			//Märklin-Motorola Befehl "F4" encodieren
			//---------------------------------------
			if (locoDataMM[j].f4 != f4 || direction == 2){
				encSpeedTemp = encodeMM2Function(4, f4, encSpeed, speed);
				for (uint8_t k = 0; k < 8; k++)
					locoDataMM[j].encCmdData[4][k] = (encSpeedTemp >> (7 - k)) & 1;
				locoDataMM[j].f4 = f4;
				enqueue_loco_hiprio(MM2, j, 4);
			}
		}

	} else if (locoProtocolIdx[i].protocol == MFX){
		//Lok-Adresse im locoDataMFX-Buffer suchen
		while (locoDataMFX[j].address != locoAdr){
			j++;
			if (j >= MFX_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoMFX");
				return 0;
			}
		}

		// Notstop
		if (direction == 2) {
			speed = 0;
			locoDataMFX[j].speed = 0;
			enqueue_loco_hiprio(MFX, j, 0);

		// Richtungsänderung nur wenn letzter Speed == 0
		} else if ((direction != locoDataMFX[j].direction) && (locoDataMFX[j].speed == 0)) {
			enqueue_loco_hiprio(MFX, j, 0);
			locoDataMFX[j].direction = direction;

		// Neuer Speed wenn Richtung gleich
		} else if (direction == locoDataMFX[j].direction){
			if (locoDataMFX[j].speed < speed){
				enqueue_loco_loprio(MFX, j, 0);
			} else if (locoDataMFX[j].speed > speed){
				enqueue_loco_hiprio(MFX, j, 0);
			}
			locoDataMFX[j].speed = speed;
		} else {
			log_error("Loco-Command not allowed");
			return 0;
		}

		//Funktionen wurden gesendet
		if (nTokens > 4) {
			//MFX Befehl "F1"
			if (locoDataMFX[j].f1 != f1) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f1 = f1;
			}

			//MFX Befehl "F2"
			if (locoDataMFX[j].f2 != f2) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f2 = f2;
			}

			//MFX Befehl "F3"
			if (locoDataMFX[j].f3 != f3) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f3 = f3;
			}

			//MFX Befehl "F4"
			if (locoDataMFX[j].f4 != f4) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f4 = f4;
			}

			//MFX Befehl "F5"
			if (locoDataMFX[j].f5 != f5) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f5 = f5;
			}

			//MFX Befehl "F6"
			if (locoDataMFX[j].f6 != f6) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f6 = f6;
			}

			//MFX Befehl "F7"
			if (locoDataMFX[j].f7 != f7) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f7 = f7;
			}

			//MFX Befehl "F8"
			if (locoDataMFX[j].f8 != f8) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f8 = f8;
			}

			//MFX Befehl "F9"
			if (locoDataMFX[j].f9 != f9) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f9 = f9;
			}

			//MFX Befehl "F10"
			if (locoDataMFX[j].f10 != f10) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f10 = f10;
			}

			//MFX Befehl "F11"
			if (locoDataMFX[j].f11 != f11) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f11 = f11;
			}

			//MFX Befehl "F12"
			if (locoDataMFX[j].f12 != f12) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f12 = f12;
			}

			//MFX Befehl "F13"
			if (locoDataMFX[j].f13 != f13) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f13 = f13;
			}

			//MFX Befehl "F14"
			if (locoDataMFX[j].f14 != f14) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f14 = f14;
			}

			//MFX Befehl "F15"
			if (locoDataMFX[j].f15 != f15) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f15 = f15;
			}

			//MFX Befehl "F16"
			if (locoDataMFX[j].f16 != f16) {
				enqueue_loco_loprio(MFX, j, 0);
				locoDataMFX[j].f16 = f16;
			}
		}

		//MärklinMFX-Befehl Fahren & Funktionen encodieren
		encodeMFXCmd (j, direction);


	} else if (locoProtocolIdx[i].protocol == DCC){
		//Lok-Adresse im locoDataDCC-Buffer suchen
		while (locoDataDCC[j].address != locoAdr){
			j++;
			if (j >= DCC_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoDCC");
				return 0;
			}
		}

		//Speed umrechnen für DCC (128er Auflösung auf 29er)
		// 0 => 0
		// 1-5 => 1
		// 6-9 => 2
		// 10-14 => 3
		// |   |    |
		// 122-126 => 27
		// 127 => 28
		speedCalc = speed;
		speedCalc = (speedCalc + 4) * 3 / 14;
		speed = (uint8_t)speedCalc;

		// Notstop
		if (direction == 2) {
			speed = 0;
			locoDataDCC[j].speed = 0;
			enqueue_loco_hiprio(DCC, j, 0);

		// Richtungsänderung nur wenn letzter Speed == 0
		} else if ((direction != locoDataDCC[j].direction) && (locoDataDCC[j].speed == 0)) {
			enqueue_loco_hiprio(DCC, j, 0);
			locoDataDCC[j].direction = direction;

		// Neuer Speed wenn Richtung gleich
		} else if (direction == locoDataDCC[j].direction){
			if (locoDataDCC[j].speed < speed){
				enqueue_loco_loprio(DCC, j, 0);
			} else if (locoDataDCC[j].speed > speed){
				enqueue_loco_hiprio(DCC, j, 0);
			}
			locoDataDCC[j].speed = speed;
		} else {
			log_error("Loco-Command not allowed");
			return 0;
		}

		//Funktionen wurden gesendet
		if (nTokens > 4) {
			//DCC Befehl "Fn"
			if (locoDataDCC[j].fn != fn) {
				enqueue_loco_loprio(DCC, j, 1);
				locoDataDCC[j].fn = fn;
			}

			//DCC Befehl "F1"
			if (locoDataDCC[j].f1 != f1) {
				enqueue_loco_loprio(DCC, j, 1);
				locoDataDCC[j].f1 = f1;
			}

			//DCC Befehl "F2"
			if (locoDataDCC[j].f2 != f2) {
				enqueue_loco_loprio(DCC, j, 1);
				locoDataDCC[j].f2 = f2;
			}

			//DCC Befehl "F3"
			if (locoDataDCC[j].f3 != f3) {
				enqueue_loco_loprio(DCC, j, 1);
				locoDataDCC[j].f3 = f3;
			}

			//DCC Befehl "F4"
			if (locoDataDCC[j].f4 != f4) {
				enqueue_loco_loprio(DCC, j, 1);
				locoDataDCC[j].f4 = f4;
			}

			//DCC Befehl "F5"
			if (locoDataDCC[j].f5 != f5) {
				enqueue_loco_loprio(DCC, j, 2);
				locoDataDCC[j].f5 = f5;
			}

			//DCC Befehl "F6"
			if (locoDataDCC[j].f6 != f6) {
				enqueue_loco_loprio(DCC, j, 2);
				locoDataDCC[j].f6 = f6;
			}

			//DCC Befehl "F7"
			if (locoDataDCC[j].f7 != f7) {
				enqueue_loco_loprio(DCC, j, 2);
				locoDataDCC[j].f7 = f7;
			}

			//DCC Befehl "F8"
			if (locoDataDCC[j].f8 != f8) {
				enqueue_loco_loprio(DCC, j, 2);
				locoDataDCC[j].f8 = f8;
			}

			//DCC Befehl "F9"
			if (locoDataDCC[j].f9 != f9) {
				enqueue_loco_loprio(DCC, j, 3);
				locoDataDCC[j].f9 = f9;
			}

			//DCC Befehl "F10"
			if (locoDataDCC[j].f10 != f10) {
				enqueue_loco_loprio(DCC, j, 3);
				locoDataDCC[j].f10 = f10;
			}

			//DCC Befehl "F11"
			if (locoDataDCC[j].f11 != f11) {
				enqueue_loco_loprio(DCC, j, 3);
				locoDataDCC[j].f11 = f11;
			}

			//DCC Befehl "F12"
			if (locoDataDCC[j].f12 != f12) {
				enqueue_loco_loprio(DCC, j, 3);
				locoDataDCC[j].f12 = f12;
			}
		}


		//Offset für Index bei langen Adressen setzen
		uint8_t q = 0;
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			q = 0;
		} else {										// lange Adresse
			q = 9;
		}


		//DCC-Befehl Fahren encodieren
		//----------------------------
		uint8_t dummySpeed = 0;

		//Speed/Direction
		locoDataDCC[j].encCmd[0][28+q] = 0;
		locoDataDCC[j].encCmd[0][29+q] = 1;
		locoDataDCC[j].encCmd[0][30+q] = locoDataDCC[j].direction;

		if (direction == 2) {
			//Nothalt: E-Stop
			locoDataDCC[j].encCmd[0][31+q] = 0;
			locoDataDCC[j].encCmd[0][32+q] = 0;
			locoDataDCC[j].encCmd[0][33+q] = 0;
			locoDataDCC[j].encCmd[0][34+q] = 0;
			locoDataDCC[j].encCmd[0][35+q] = 1;
		} else {
			dummySpeed = dccSpeed28Data[speed];
			for (uint8_t n = 0; n < 5; n++)
				locoDataDCC[j].encCmd[0][31+n+q] =  (dummySpeed >> (4 - n)) & 1;
		}

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
			for (uint8_t n = 0; n < 8; n++)
				locoDataDCC[j].encCmd[0][37 + n] = locoDataDCC[j].encCmd[0][19 + n] ^ locoDataDCC[j].encCmd[0][28 + n];

		} else {										// lange Adresse
			for (uint8_t n = 0; n < 8; n++)
				locoDataDCC[j].encCmd[0][46 + n] = locoDataDCC[j].encCmd[0][19 + n] ^ locoDataDCC[j].encCmd[0][28 + n] ^ locoDataDCC[j].encCmd[0][37 + n];
		}


		//Funktionen wurden gesendet
		if (nTokens > 4) {
			//DCC-Befehl FktGrp1 encodieren (Fn, F1 - F4)
			//-----------------------------

			locoDataDCC[j].encCmd[1][31+q] = locoDataDCC[j].fn;
			locoDataDCC[j].encCmd[1][32+q] = locoDataDCC[j].f4;
			locoDataDCC[j].encCmd[1][33+q] = locoDataDCC[j].f3;
			locoDataDCC[j].encCmd[1][34+q] = locoDataDCC[j].f2;
			locoDataDCC[j].encCmd[1][35+q] = locoDataDCC[j].f1;

//			log_debug3("LocoEncDCCFn: ", locoDataDCC[j].encCmd[1][31+q]);

			//Error Detection Data Byte (Address-Byte xor Data-Byte)
			if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[1][37 + n] = locoDataDCC[j].encCmd[1][19 + n] ^ locoDataDCC[j].encCmd[1][28 + n];

			} else {										// lange Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[1][46 + n] = locoDataDCC[j].encCmd[1][19 + n] ^ locoDataDCC[j].encCmd[1][28 + n] ^ locoDataDCC[j].encCmd[1][37 + n];
			}


			//DCC-Befehl FktGrp2.1 encodieren (F5 - F8)
			//-------------------------------

			locoDataDCC[j].encCmd[2][32+q] = locoDataDCC[j].f8;
			locoDataDCC[j].encCmd[2][33+q] = locoDataDCC[j].f7;
			locoDataDCC[j].encCmd[2][34+q] = locoDataDCC[j].f6;
			locoDataDCC[j].encCmd[2][35+q] = locoDataDCC[j].f5;

			//Error Detection Data Byte (Address-Byte xor Data-Byte)
			if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[2][37 + n] = locoDataDCC[j].encCmd[2][19 + n] ^ locoDataDCC[j].encCmd[2][28 + n];

			} else {										// lange Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[2][46 + n] = locoDataDCC[j].encCmd[2][19 + n] ^ locoDataDCC[j].encCmd[2][28 + n] ^ locoDataDCC[j].encCmd[2][37 + n];
			}


			//DCC-Befehl FktGrp2.2 encodieren (F9 - F12)
			//-------------------------------

			locoDataDCC[j].encCmd[3][32+q] = locoDataDCC[j].f12;
			locoDataDCC[j].encCmd[3][33+q] = locoDataDCC[j].f11;
			locoDataDCC[j].encCmd[3][34+q] = locoDataDCC[j].f10;
			locoDataDCC[j].encCmd[3][35+q] = locoDataDCC[j].f9;

			//Error Detection Data Byte (Address-Byte xor Data-Byte)
			if  (locoDataDCC[j].longAddress == 0) {			// kurze Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[3][37 + n] = locoDataDCC[j].encCmd[3][19 + n] ^ locoDataDCC[j].encCmd[3][28 + n];

			} else {										// lange Adresse
				for (uint8_t n = 0; n < 8; n++)
					locoDataDCC[j].encCmd[3][46 + n] = locoDataDCC[j].encCmd[3][19 + n] ^ locoDataDCC[j].encCmd[3][28 + n] ^ locoDataDCC[j].encCmd[3][37 + n];
			}
		}

	} else {
		log_error("LocoProtocolIdx wrong protocol");
		return 0;
	}



#ifdef LOGGING
	char convStr [6];
	log_debug2("Loco Number: ", utoa(locoAdr, convStr, 10));
	switch (locoProtocolIdx[i].protocol){
	case MM:
		log_debug("Protocol: MM");
		break;
	case MM2:
		log_debug("Protocol: MM2");
		break;
	case MFX:
		log_debug("Protocol: MFX");
		break;
	case DCC:
		log_debug("Protocol: DCC");
		break;
	}
	log_debug3("Loco Speed: ", speed);
	log_debug3("Loco Direction: ", direction);
	log_debug3("Loco Fn: ", fn);
	log_debug3("Loco F1: ", f1);
	log_debug3("Loco F2: ", f2);
	log_debug3("Loco F3: ", f3);
	log_debug3("Loco F4: ", f4);
	log_debug3("Loco isDelta: ", locoDataMM[t].isDelta);

#endif

	return 1;

}


//================================================================================
// Lok abmelden/löschen
//================================================================================
uint8_t ib_remove_loc_cmd(char** tokens, uint8_t nTokens) {

	uint16_t locoAdr;

	if (nTokens != 2) {
		log_error("Command format: XLOCREMOVE {loconumber}");
		return 0;
	}

	locoAdr = atol(tokens[1]);

	//Lok-Adresse im LocoProtocolIdx suchen, damit man weiss welches Protokoll die Lok hat
	uint8_t i = 0;
	while (locoProtocolIdx[i].address != locoAdr){
		i++;
		if (i >= LOCO_PROTOCOL_INDEX_BUFFER_SIZE){
			log_error("Loco not initialized");
			return 0;
		}
	}

	uint8_t j = 0;
	if (locoProtocolIdx[i].protocol == MM || locoProtocolIdx[i].protocol == MM2) {
		//Lok-Adresse im locoDataMM-Buffer suchen
		while (locoDataMM[j].address != locoAdr){
			j++;
			if (j >= MM_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoMM");
				break;
			}
		}

		if (j < MM_LOCO_DATA_BUFFER_SIZE){
			//Lok im locoDataMM-Bufferlöschen
			uint8_t k = j;
			while (k < (MM_LOCO_DATA_BUFFER_SIZE-1)){
				locoDataMM[k] = locoDataMM[k+1];
				k++;
			}
			locoDataMM[k].address = 0;
		}

#ifdef LOGGING
	log_debug3("MM/MM2 Loc removed: ", locoAdr);
#endif



	} else if (locoProtocolIdx[i].protocol == MFX){
		//Lok-Adresse im locoDataMFX-Buffer suchen
		while (locoDataMFX[j].address != locoAdr){
			j++;
			if (j >= MFX_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoMFX");
				break;
			}
		}

		if (j < MFX_LOCO_DATA_BUFFER_SIZE){
			//Lok im locoDataMFX-Bufferlöschen
			uint8_t k = j;
			while (k < (MFX_LOCO_DATA_BUFFER_SIZE-1)){
				locoDataMFX[k] = locoDataMFX[k+1];
				k++;
			}
			locoDataMFX[k].address = 0;
		}

#ifdef LOGGING
	log_debug3("MFX Loc removed: ", locoAdr);
#endif



	} else if (locoProtocolIdx[i].protocol == DCC){
		//Lok-Adresse im locoDataDCC-Buffer suchen
		while (locoDataDCC[j].address != locoAdr){
			j++;
			if (j >= DCC_LOCO_DATA_BUFFER_SIZE){
				log_error("Loco not found in Buffer LocoDCC");
				break;
			}
		}
		if (j < DCC_LOCO_DATA_BUFFER_SIZE){
			//Lok im locoDataDCC-Bufferlöschen
			uint8_t k = j;
			while (k < (DCC_LOCO_DATA_BUFFER_SIZE-1)){
				locoDataDCC[k] = locoDataDCC[k+1];
				k++;
			}
			locoDataDCC[k].address = 0;
		}

#ifdef LOGGING
	log_debug3("DCC Loc removed: ", locoAdr);
#endif

	} else {
		log_error("Unkown protocol");
	}

	//Lok-Adresse im LocoProtocolIdx löschen
//	uint8_t k = i;
	while (i < (LOCO_PROTOCOL_INDEX_BUFFER_SIZE-1)){
		locoProtocolIdx[i] = locoProtocolIdx[i+1];
		i++;
	}
	locoProtocolIdx[i].address = 0;

	//Rückmeldung Lok wurde abgemeldet
	uart_puts("XLOCREMOVE ");
	send_number_dec(locoAdr);
	uart_puts(" STATUS REMOVED");
//	send_nl();
	send_cr();

	//wenn keine Lok angemeldet IdleLok aktivieren
	if (locoProtocolIdx[0].address == 0) {
		initIdleLocoData();
	}

	return 1;
}



//================================================================================
// alle Loks Notstop
//================================================================================
uint8_t ib_loc_halt_cmd(char** tokens, uint8_t nTokens) {


	uint8_t speed = 0;
	unsigned char encSpeed = 0;
	unsigned char encSpeedTemp = 0;
	uint8_t f4 = 0;


	if (nTokens != 1) {
		log_error("Command format: XHALT");
	}

	//MM/MM2-Loks stoppen
	uint8_t i = 0;
	while (locoDataMM[i].address != 0){

		f4 = 1;
		speed = 0;
		locoDataMM[i].speed = 0;
		locoDataMM[i].f4 = 1;
		enqueue_loco_hiprio(MM2, i, 0);

		encSpeed = 0;
		//"Fahren" & Fn encodieren
		//Speed
		if (locoDataMM[i].isDelta == 1) {
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[i].encCmdData[0][k] = 0;
		} else {

			if (locoDataMM[i].direction == 0) {
				encSpeed = 0b01000101;
			} else {
				encSpeed = 0b00010001;
			}
			for (uint8_t k = 0; k < 8; k++)
				locoDataMM[i].encCmdData[0][k] = (encSpeed >> (7 - k)) & 1;
		}

		encSpeed = 0;
		//"F1" encodieren
		encSpeedTemp = encodeMM2Function(1, locoDataMM[i].f1, encSpeed, speed);
		for (uint8_t k = 0; k < 8; k++)
			locoDataMM[i].encCmdData[1][k] = (encSpeedTemp >> (7 - k)) & 1;
		//"F2" encodieren
		encSpeedTemp = encodeMM2Function(2, locoDataMM[i].f2, encSpeed, speed);
		for (uint8_t k = 0; k < 8; k++)
			locoDataMM[i].encCmdData[2][k] = (encSpeedTemp >> (7 - k)) & 1;
		//"F3" encodieren
		encSpeedTemp = encodeMM2Function(3, locoDataMM[i].f3, encSpeed, speed);
		for (uint8_t k = 0; k < 8; k++)
			locoDataMM[i].encCmdData[3][k] = (encSpeedTemp >> (7 - k)) & 1;

		//Märklin-Motorola Befehl "F4" encodieren
		//---------------------------------------
		encSpeedTemp = encodeMM2Function(4, f4, encSpeed, speed);
		for (uint8_t k = 0; k < 8; k++)
			locoDataMM[i].encCmdData[4][k] = (encSpeedTemp >> (7 - k)) & 1;
		enqueue_loco_hiprio(MM2, i, 4);

		i++;
		if (i >= MM_LOCO_DATA_BUFFER_SIZE){
			break;
		}
	}

	//MFX-Loks stoppen
	i = 0;
	while (locoDataMFX[i].address != 0){

		speed = 0;
		locoDataMFX[i].speed = 0;
		enqueue_loco_hiprio(MFX, i, 0);

		//MFX Command encode with direction = 2 => Notstop
		encodeMFXCmd (i, 2);

		i++;
		if (i >= MFX_LOCO_DATA_BUFFER_SIZE){
			break;
		}
	}


	//DCC-Loks stoppen
	i = 0;
	while (locoDataDCC[i].address != 0){

		speed = 0;
		locoDataDCC[i].speed = 0;
		enqueue_loco_hiprio(DCC, i, 0);

		//Nothalt: E-Stop
		locoDataDCC[i].encCmd[0][31] = 0;
		locoDataDCC[i].encCmd[0][32] = 0;
		locoDataDCC[i].encCmd[0][33] = 0;
		locoDataDCC[i].encCmd[0][34] = 0;
		locoDataDCC[i].encCmd[0][35] = 1;

		//Error Detection Data Byte (Address-Byte xor Data-Byte)
		for (uint8_t k = 0; k < 8; k++)
			locoDataDCC[i].encCmd[0][37 + k] = locoDataDCC[i].encCmd[0][19 + k] ^ locoDataDCC[i].encCmd[0][28 + k];

		i++;
		if (i >= DCC_LOCO_DATA_BUFFER_SIZE){
			break;
		}
	}


#ifdef LOGGING
	log_debug("All Locs Emergency Stop");
#endif

	return 1;
}


//============================================================
uint8_t ib_debug_level_cmd(char** tokens, uint8_t nTokens) {

	if (nTokens != 2) {
		log_error("Command format: XDB {level} (level = 0-4)");
		return 0;
	}

	logLevel = atoi(tokens[1]);
#ifdef LOGGING
	log_debug("New Debug Level Command");
#endif
	return 1;
}




uint8_t ib_get_uid_cmd(char** tokens, uint8_t nTokens) {

	char cmd[5];

	if (nTokens != 2) {
		log_error("Command format: XGETUID {START|STOP}");
		return 0;
	}

	strcpy(cmd, tokens[1]);

	if (strcasecmp(cmd, "START") == 0){
		//Booster stoppen und PWM deaktivieren
//		stop_all_boosters();
//		TIMSK1 &= ~(1 << OCIE1B);	// Timer 1 Output Compare B Match Interrupt disabled (PWM Timer)
//		TCCR1A &= ~(1 << COM1B1); 	// DeACTIVATE PWM

		EIMSK |= 1<<INT2;			// Enable interrupt 2 (MFX Signal Input)

#ifdef LOGGING
	log_debug("MFX UID Sniffer started");
#endif

	} else if (strcasecmp(cmd, "STOP") == 0){
		//PWM aktivieren
//		TIMSK1 |= (1 << OCIE1B);	// Timer 1 Output Compare B Match Interrupt enabled (PWM Timer)
//		TCCR1A |= (1 << COM1B1); 	// ACTIVATE PWM

		EIMSK &= ~(1<<INT2);		// Disable interrupt 2 (MFX Signal Input)

#ifdef LOGGING
	log_debug("MFX UID Sniffer stopped");
#endif

	} else {

	log_error("MFX UID Sniffer unkown command");

	}

	return 1;
}



uint8_t encodeMM2Function(uint8_t functionNr, unsigned char functionState, unsigned char deltaSpeed,
		unsigned char speed) {

	unsigned char abcd = deltaSpeed;
	unsigned char efgh = 0xFF;
	unsigned char mask = 0b01010101;

	switch (functionNr) {
	case 1: // F1
		if (functionState == 1) { //ON

			//log_debug("F1 ON");
			if (speed != 10) {
				efgh = 0b11110011;
			} else {
				efgh = 0b00110011; //*
			}
		} else {
			//log_debug("F1 OFF");
			if (speed != 2) {
				efgh = 0b11110000;
			} else {
				efgh = 0b11001100; //*
			}
		}
		break;
	case 2: // F2
		if (functionState == 1) { //ON
			//log_debug("F2 ON");
			if (speed != 11) {
				efgh = 0b00001111;
			} else {
				efgh = 0b00110011; //*
			}
		} else {
			//log_debug("F2 OFF");
			if (speed != 3) {
				efgh = 0b00001100;
			} else {
				efgh = 0b11001100; //*
			}
		}
		break;
	case 3: // F3
		if (functionState == 1) { //ON
			//log_debug("F3 ON");
			if (speed != 13) {
				efgh = 0b00111111;
			} else {
				efgh = 0b00110011; //*
			}
		} else {
			//log_debug("F3 OFF");
			if (speed != 5) {
				efgh = 0b00111100;
			} else {
				efgh = 0b11001100; //*
			}
		}
		break;
	case 4: // F4
		if (functionState == 1) { //ON
			//log_debug("F4 ON");
			if (speed != 14) {
				efgh = 0b11111111;
			} else {
				efgh = 0b00110011; //*
			}
		} else {
			if (speed != 6) {
				//log_debug("F4 OFF");
				efgh = 0b11111100;
			} else {
				efgh = 0b11001100; //*
			}
		}
		break;
	}
	//* Exceptions for Compatibility with the old Märklin Protocol-Format

	uint8_t encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);
	return encodedSpeed;
}



//------------------------------------------------
//MärklinMFX-Befehl Fahren & Funktionen encodieren
//nur 9-Bit Adressen realisiert
//------------------------------------------------
uint8_t encodeMFXCmd(uint8_t mfxIdx, uint8_t direction) {


	uint16_t crc;
	uint8_t mfxCommandLength;
	int cidx;
	int tidx;
	int EinsHalbiert;
	int stfngCnt;

	//SID (Lok-Adresse)
	tmpMFXcmd[0] = 1;
	tmpMFXcmd[1] = 1;
	tmpMFXcmd[2] = 0;

	//SID-Adresse (Lok-Adresse)
	for (uint8_t k = 0; k < 9; k++)
		tmpMFXcmd[3 + k] = (locoDataMFX[mfxIdx].address >> (8 - k)) & 1;

	//Kommando Fahren
	tmpMFXcmd[12] = 0;
	tmpMFXcmd[13] = 0;
	tmpMFXcmd[14] = 1;

	//Richtung
	tmpMFXcmd[15] = locoDataMFX[mfxIdx].direction;

	//Fahrstufe
	if (direction == 2 ) {
		//Fahrstufe 1 => Nothalt
		tmpMFXcmd[16] = 0;
		tmpMFXcmd[17] = 0;
		tmpMFXcmd[18] = 0;
		tmpMFXcmd[19] = 0;
		tmpMFXcmd[20] = 0;
		tmpMFXcmd[21] = 0;
		tmpMFXcmd[22] = 1;

	} else {
		for (uint8_t k = 0; k < 7; k++)
			tmpMFXcmd[16 + k] = (locoDataMFX[mfxIdx].speed >> (6 - k)) & 1;
	}

	//Kommando Fkt
	tmpMFXcmd[23] = 0;
	tmpMFXcmd[24] = 1;
	tmpMFXcmd[25] = 1;

	//alle Fkt
	tmpMFXcmd[26] = 1;

	//Fkt 1 - 16
	tmpMFXcmd[27] = locoDataMFX[mfxIdx].f16;
	tmpMFXcmd[28] = locoDataMFX[mfxIdx].f15;
	tmpMFXcmd[29] = locoDataMFX[mfxIdx].f14;
	tmpMFXcmd[30] = locoDataMFX[mfxIdx].f13;
	tmpMFXcmd[31] = locoDataMFX[mfxIdx].f12;
	tmpMFXcmd[32] = locoDataMFX[mfxIdx].f11;
	tmpMFXcmd[33] = locoDataMFX[mfxIdx].f10;
	tmpMFXcmd[34] = locoDataMFX[mfxIdx].f9;
	tmpMFXcmd[35] = locoDataMFX[mfxIdx].f8;
	tmpMFXcmd[36] = locoDataMFX[mfxIdx].f7;
	tmpMFXcmd[37] = locoDataMFX[mfxIdx].f6;
	tmpMFXcmd[38] = locoDataMFX[mfxIdx].f5;
	tmpMFXcmd[39] = locoDataMFX[mfxIdx].f4;
	tmpMFXcmd[40] = locoDataMFX[mfxIdx].f3;
	tmpMFXcmd[41] = locoDataMFX[mfxIdx].f2;
	tmpMFXcmd[42] = locoDataMFX[mfxIdx].f1;

	//CRC Init
	tmpMFXcmd[43] = 0;
	tmpMFXcmd[44] = 0;
	tmpMFXcmd[45] = 0;
	tmpMFXcmd[46] = 0;
	tmpMFXcmd[47] = 0;
	tmpMFXcmd[48] = 0;
	tmpMFXcmd[49] = 0;
	tmpMFXcmd[50] = 0;

	//CRC Berechnung
	mfxCommandLength = 51;
	crc = 0x007f;

	 for (int k = 0; k < mfxCommandLength; k++)
	  {
	    crc = (crc << 1) + tmpMFXcmd[k];
	    if ((crc & 0x0100) > 0)
	      crc = (crc & 0x00FF) ^ 0x07;
	  }

	//CRC Schreiben
	tmpMFXcmd[43] = (crc / 128) % 2;
	tmpMFXcmd[44] = (crc / 64) % 2;
	tmpMFXcmd[45] = (crc / 32) % 2;
	tmpMFXcmd[46] = (crc / 16) % 2;
	tmpMFXcmd[47] = (crc / 8) % 2;
	tmpMFXcmd[48] = (crc / 4) % 2;
	tmpMFXcmd[49] = (crc / 2) % 2;
	tmpMFXcmd[50] = crc % 2;

	//Stuffing (nach 8x "1" wird ein "0" eingefügt)
	stfngCnt = 0;

	for (int k = 0; k < mfxCommandLength; k++){
		if (tmpMFXcmd[k] == 1) {
			stfngCnt++;
		} else {
			stfngCnt = 0;
		}
		if (stfngCnt == 8){
			for (int l = mfxCommandLength-1; l > k; l--){
				tmpMFXcmd[l+1] = tmpMFXcmd[l];
			}
			tmpMFXcmd[k+1] = 0;
			mfxCommandLength++;
			stfngCnt = 0;
		}
	}

	//2x Sync am Ende
	tmpMFXcmd[mfxCommandLength] = 0;
	tmpMFXcmd[mfxCommandLength+1] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+2] = 0;
	tmpMFXcmd[mfxCommandLength+3] = 0;
	tmpMFXcmd[mfxCommandLength+4] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+5] = 0;
	tmpMFXcmd[mfxCommandLength+6] = 0;
	tmpMFXcmd[mfxCommandLength+7] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+8] = 0;
	tmpMFXcmd[mfxCommandLength+9] = 0;
	tmpMFXcmd[mfxCommandLength+10] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+11] = 0;

	//Befehl encodieren
	//
	// mfx "0" => __ oder ––
	// mfx "1" => _– oder –_
	//
	// für die Signalgenerierung wird ein PWM verwendet
	// die mfx-Informationseinheiten werden dabei teilweise "aufgeteilt"
	// –_   => enc"1" entspricht mfx "1"
	// –__  => enc"2" entspricht mfx 'halbes' "1" + "0"
	// ––_  => enc"3" entspricht mfx "0" + 'halbes' "1"
	// ––__ => enc"4" entspricht mfx "0" + "0"

	cidx = 6;			//encoded MFX-Command Index
	tidx = 0;			//temporary MFX-Command Index
	EinsHalbiert = 0;

	//fix 2x Sync
	locoDataMFX[mfxIdx].encCmd[0] = 3;
	locoDataMFX[mfxIdx].encCmd[1] = 4;
	locoDataMFX[mfxIdx].encCmd[2] = 2;
	locoDataMFX[mfxIdx].encCmd[3] = 3;
	locoDataMFX[mfxIdx].encCmd[4] = 4;
	locoDataMFX[mfxIdx].encCmd[5] = 2;

	for (tidx = 0; tidx < (mfxCommandLength + 12); tidx++){	// 12 => 2x Sync am Ende.
		if (tidx == (mfxCommandLength + 11)) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 4;
			locoDataMFX[mfxIdx].encCmd[cidx+1] = 6;	// 6 => Ende des MFX-Befehls
			break;
		} else if (tmpMFXcmd[tidx] == 1 && EinsHalbiert == 0) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0  && tmpMFXcmd[tidx + 1] == 0) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 4;
			tidx++;
			cidx++;
		} else  if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 1) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 3;
			cidx++;
			EinsHalbiert = 1;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 0 && EinsHalbiert == 1) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 2;
			tidx++;
			cidx++;
			EinsHalbiert = 0;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 1 && EinsHalbiert == 1) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 2) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 3;
			tidx++;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 2 && tmpMFXcmd[tidx + 1] == 0) {
			locoDataMFX[mfxIdx].encCmd[cidx] = 2;
			tidx++;
			cidx++;
			if (tidx == (mfxCommandLength + 11)) {
				locoDataMFX[mfxIdx].encCmd[cidx] = 6;	// 6 => Ende des MFX-Befehls
				break;
			}
		}
	}

	return 1;
}


//---------------------------------------------------
//MärklinMFX-Befehl "SID-Adresse zuweisen" encodieren
//---------------------------------------------------
uint8_t encodeMFXSIDCmd(uint8_t mfxIdx) {

	uint16_t crc;
	uint8_t mfxCommandLength;
	int cidx;
	int tidx;
	int EinsHalbiert;
	int stfngCnt;


	//MärklinMFX-Befehl "SID-Adresse zuweisen"
	//----------------------------------------
	//Broadcast-Adresse
	tmpMFXcmd[0] = 1;
	tmpMFXcmd[1] = 0;
	tmpMFXcmd[2] = 0;
	tmpMFXcmd[3] = 0;
	tmpMFXcmd[4] = 0;
	tmpMFXcmd[5] = 0;
	tmpMFXcmd[6] = 0;
	tmpMFXcmd[7] = 0;
	tmpMFXcmd[8] = 0;

	//Kommando Zuweisung SID
	tmpMFXcmd[9] = 1;
	tmpMFXcmd[10] = 1;
	tmpMFXcmd[11] = 1;
	tmpMFXcmd[12] = 0;
	tmpMFXcmd[13] = 1;
	tmpMFXcmd[14] = 1;

	//SID-Adresse (Lok-Adresse)
	//bei SID-Zuweisung wird immer ein 14Bit-Adressen-Grösse verwendet!
	for (uint8_t k = 0; k < 14; k++)
		tmpMFXcmd[15+k] = (locoDataMFX[mfxIdx].address >> (13 - k)) & 1;

	//UID-Adresse des Decoders
	for (uint8_t k = 0; k < 32; k++)
		tmpMFXcmd[29+k] = (locoDataMFX[mfxIdx].UID >> (31 - k)) & 1;

	//CRC Init
	tmpMFXcmd[61] = 0;
	tmpMFXcmd[62] = 0;
	tmpMFXcmd[63] = 0;
	tmpMFXcmd[64] = 0;
	tmpMFXcmd[65] = 0;
	tmpMFXcmd[66] = 0;
	tmpMFXcmd[67] = 0;
	tmpMFXcmd[68] = 0;

	//CRC Berechnung
	mfxCommandLength = 69;
	crc = 0x007f;

	 for (int k = 0; k < mfxCommandLength; k++)
	  {
	    crc = (crc << 1) + tmpMFXcmd[k];
	    if ((crc & 0x0100) > 0)
	      crc = (crc & 0x00FF) ^ 0x07;
	  }

	//CRC Schreiben
	tmpMFXcmd[61] = (crc / 128) % 2;
	tmpMFXcmd[62] = (crc / 64) % 2;
	tmpMFXcmd[63] = (crc / 32) % 2;
	tmpMFXcmd[64] = (crc / 16) % 2;
	tmpMFXcmd[65] = (crc / 8) % 2;
	tmpMFXcmd[66] = (crc / 4) % 2;
	tmpMFXcmd[67] = (crc / 2) % 2;
	tmpMFXcmd[68] = crc % 2;

	//Stuffing (nach 8x "1" wird eine "0" eingefügt)
	stfngCnt = 0;

	for (int k = 0; k < mfxCommandLength; k++){
		if (tmpMFXcmd[k] == 1) {
			stfngCnt++;
		} else {
			stfngCnt = 0;
		}
		if (stfngCnt == 8){
			for (int l = mfxCommandLength-1; l > k; l--){
				tmpMFXcmd[l+1] = tmpMFXcmd[l];
			}
			tmpMFXcmd[k+1] = 0;
			mfxCommandLength++;
			stfngCnt = 0;
		}
	}

	//2x Sync am Ende
	tmpMFXcmd[mfxCommandLength] = 0;
	tmpMFXcmd[mfxCommandLength+1] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+2] = 0;
	tmpMFXcmd[mfxCommandLength+3] = 0;
	tmpMFXcmd[mfxCommandLength+4] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+5] = 0;
	tmpMFXcmd[mfxCommandLength+6] = 0;
	tmpMFXcmd[mfxCommandLength+7] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+8] = 0;
	tmpMFXcmd[mfxCommandLength+9] = 0;
	tmpMFXcmd[mfxCommandLength+10] = 2;	// 2 = Abweichung von bi-phase-mark-Regel
	tmpMFXcmd[mfxCommandLength+11] = 0;


	//SID-Befehl in MFX encodieren
	//
	// mfx "0" => __ oder ––
	// mfx "1" => _– oder –_
	//
	// für die Signalgenerierung wird ein PWM verwendet
	// die mfx-Informationseinheiten werden dabei teilweise "aufgeteilt"
	// –_   => enc"1" entspricht mfx "1"
	// –__  => enc"2" entspricht mfx 'halbes' "1" + "0"
	// ––_  => enc"3" entspricht mfx "0" + 'halbes' "1"
	// ––__ => enc"4" entspricht mfx "0" + "0"

	cidx = 6;			//encoded MFX-Command Index
	tidx = 0;			//temporary MFX-Command Index
	EinsHalbiert = 0;

	//fix 2x Sync
	mfxSIDEncCmd[0] = 3;
	mfxSIDEncCmd[1] = 4;
	mfxSIDEncCmd[2] = 2;
	mfxSIDEncCmd[3] = 3;
	mfxSIDEncCmd[4] = 4;
	mfxSIDEncCmd[5] = 2;


	for (tidx = 0; tidx < (mfxCommandLength + 12); tidx++){	// 12 => 2x Sync am Ende.
		if (tidx == (mfxCommandLength + 11)) {
			mfxSIDEncCmd[cidx] = 4;
			mfxSIDEncCmd[cidx+1] = 6;	// 6 => Ende des MFX-Befehls
			break;
		} else if (tmpMFXcmd[tidx] == 1 && EinsHalbiert == 0) {
			mfxSIDEncCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0  && tmpMFXcmd[tidx + 1] == 0) {
			mfxSIDEncCmd[cidx] = 4;
			tidx++;
			cidx++;
		} else  if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 1) {
			mfxSIDEncCmd[cidx] = 3;
			cidx++;
			EinsHalbiert = 1;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 0 && EinsHalbiert == 1) {
			mfxSIDEncCmd[cidx] = 2;
			tidx++;
			cidx++;
			EinsHalbiert = 0;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 1 && EinsHalbiert == 1) {
			mfxSIDEncCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 2) {
			mfxSIDEncCmd[cidx] = 3;
			tidx++;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 2 && tmpMFXcmd[tidx + 1] == 0) {
			mfxSIDEncCmd[cidx] = 2;
			tidx++;
			cidx++;
			if (tidx == (mfxCommandLength + 11)) {
				mfxSIDEncCmd[cidx] = 6;	// 6 => Ende des MFX-Befehls
				break;
			}
		}
	}

	return 1;

}



uint8_t ib_buffer_info_cmd(char** tokens, uint8_t nTokens) {

	char value[6];


	// Füllstand LocoProtocolIdx-Buffer
	uint8_t i = 0;
	while (locoProtocolIdx[i].address != 0){
		log_info3("LocoAddress: ", locoProtocolIdx[i].address);
		log_info3("LocoProtocol: ", locoProtocolIdx[i].protocol);
		i++;
		if (i >= LOCO_PROTOCOL_INDEX_BUFFER_SIZE){
			log_info("ProtocolIndex Buffer FULL");
			break;
		}
	}
	log_info3("Protocol Buffer: ", i);


	strcpy(value, tokens[1]);


	// Füllstand MM/MM2-LocoData-Buffer
	i = 0;
	while (locoDataMM[i].address != 0){
		i++;
		if (i >= MM_LOCO_DATA_BUFFER_SIZE){
			log_info("Buffer LocoMM FULL");
			break;
		}
		if (strcasecmp(value, "SPEED") == 0)  {
			log_info3("LocoMM: ", locoDataMM[i-1].address);
			log_info3("Speed: ", locoDataMM[i-1].speed);
		}
	}
	log_info3("Buffer LocoMM: ", i);


	// Füllstand MFX-LocoData-Buffer
	i = 0;
	while (locoDataMFX[i].address != 0){
		i++;
		if (i >= MFX_LOCO_DATA_BUFFER_SIZE){
			log_info("Buffer LocoMFX FULL");
			break;
		}
		if (strcasecmp(value, "SPEED") == 0)  {
			log_info3("LocoMFX: ", locoDataMFX[i-1].address);
			log_info3("Speed: ", locoDataMFX[i-1].speed);
		}
	}
	log_info3("Buffer LocoMFX: ", i);


	// Füllstand DCC-LocoData-Buffer
	i = 0;
	while (locoDataDCC[i].address != 0){
		i++;
		if (i >= DCC_LOCO_DATA_BUFFER_SIZE){
			log_info("Buffer LocoDCC FULL");
			break;
		}
		if (strcasecmp(value, "SPEED") == 0)  {
			log_info3("LocoDCC: ", locoDataDCC[i-1].address);
			log_info3("Speed: ", locoDataDCC[i-1].speed);
		}
	}
	log_info3("Buffer LocoDCC: ", i);

	//Weichen-Buffer
	if (solenoidQueueIdxEnter >= solenoidQueueIdxFront) {
		i = solenoidQueueIdxEnter - solenoidQueueIdxFront;
	} else {
		i = MAX_SOLENOID_QUEUE - solenoidQueueIdxFront + solenoidQueueIdxEnter;
	}
	log_info3("Buffer Solenoid: ", i);

	return 1;
}



