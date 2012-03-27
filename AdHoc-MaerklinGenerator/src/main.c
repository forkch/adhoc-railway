#include "main.h"
#include "booster.h"
#include "pwm.h"
#include "spi.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "fifo.h"
#include "ib_parser.h"

unsigned char debugLevel = DEBUG_DEBUG;
struct LocoData locoData[80];
struct SolenoidData solenoidQueue[MAX_SOLENOID_QUEUE];
int solenoidQueueIdxEnter;

int main() {

	init();
	debug_init();

	// start UART
	uart_init();

	SPI_MasterInitOutput();
	SPI_MasterTransmitDebug(0x00);

	sei();
	init_boosters();
	cli();
	initLocoData();
	initPortData();

	//Initialize PWM Channel 1
	initPWM();

	//Loco FREQUENCY
	pwm_mode[0] = MODE_LOCO;
	pwm_mode[1] = MODE_LOCO;

	locoCommandLength[0] = LOCOCMD_REPETITIONS;
	locoCommandLength[1] = LOCOCMD_REPETITIONS;

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

	//init Timer0
	TIMSK0 |= (1 << TOIE0); // interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; // use defined prescaler value

#ifdef AUTO_SOLENOID
	OCR2A = (uint8_t) AUTO_SOLENOID_TOP;
	TCCR2A = (1 << WGM21);  // CTC Mode
	TIMSK2 = (1 << OCIE2A); // interrupt enable - here CompareA
	TCCR2B = (1 << CS20)  | (1 << CS21)  | (1 << CS22); // prescaling 1024
	//TCCR2B = (1 << CS21) | (1 << CS22); // prescaling 256
	//TCCR2B = (1 << CS20) | (1 << CS22); // prescaling 128
	//TCCR2B = (1 << CS20)  | (1 << CS21); // prescaling 32
	//TCCR2B = (1 << CS21); // prescaling 8
#endif

	for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
		solenoidQueue[i].timerDetected = 0;
		solenoidQueue[i].active = 0;
	}

	sei();

#ifdef SEND_STOP_ALL_LOCO_ON_INIT
	sendStopAllLoco();
#endif

#ifdef DEBUG
	log_info("-----------------------------");
	log_info("AdHoc-Maerklin Generator V0.1");
	log_info("Have Fun :-)\n");

	log_debug3("MM_PACKET_LENGTH: ", MM_PACKET_LENGTH);
	log_debug3("MM_INTER_PACKET_PAUSE: ", MM_INTER_PACKET_PAUSE);
	log_debug3("MM_DOUBLE_PACKET_LENGTH: ", MM_DOUBLE_PACKET_LENGTH);
	log_debug3("MM_INTER_DOUBLE_PACKET_PAUSE: ", MM_INTER_DOUBLE_PACKET_PAUSE);
	log_debug3("MM_COMMAND_LENGTH: ", MM_COMMAND_LENGTH);
	log_debug3("MM_COMMAND_LENGTH*LOCOCMD_REPETITIONS: ",
			MM_COMMAND_LENGTH * LOCOCMD_REPETITIONS);
	log_debug3("MM_COMMAND_LENGTH*SOLENOIDCMD_REPETITIONS: ",
			MM_COMMAND_LENGTH * SOLENOIDCMD_REPETITIONS);
#endif

	replys("XRS\r");

	//Do this forever
	while (1) {


		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(cmd);
		}

		//Verzšgerung Weichen-Deaktivierung in 13ms-Schritten
#ifdef AUTO_SOLENOID
		if (timer0_interrupt > 40) {
#else
		if (timer0_interrupt > 15) {
#endif
			timer0_interrupt = 0;
			for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
				if (solenoidQueue[i].active
						&& solenoidQueue[i].timerDetected == 0) {

					// fairly recent solenoid command...defer deactivation to next cycle
					solenoidQueue[i].timerDetected = 1;
				} else if (solenoidQueue[i].active
						&& solenoidQueue[i].timerDetected == 1
						&& !deactivatingSolenoid) {

					// deactivate solenoid
					deactivatingSolenoid = 1;

					solenoidToDeactivate = i;
					break;
				}

			}
		}

		if (prepareNextData == 1) {
			prepareDataForPWM();
		}

		//check shorts
		check_shorts();

	}
	cli();
	return 0;
}

void processASCIIData() {

#ifdef DEBUG
	log_debug("Command received");
	log_debug(cmd);
#endif

	uint8_t ret = parse_ib_cmd(cmd);

	if (!ret) {
#ifdef DEBUG
		log_error("Command not recognized\n");
#endif
		return;
	}
}

void enqueue_solenoid() {
	solenoidQueueIdxEnter++;
	solenoidQueueIdxEnter = solenoidQueueIdxEnter % MAX_SOLENOID_QUEUE;
}

void enqueue_loco(uint8_t loco_idx) {
	newLocoIdx = loco_idx;
}

//void all_loco() {
//
//	for (uint8_t i = 0; i < 80; i++) {
//		locoData[i].active = 1;
//		locoData[i].encodedSpeed = 0b11001101;
//
//	}
//}

void prepareDataForPWM() {

	unsigned char queueIdxLoc = (pwmQueueIdx + 1) % 2;

	if (StartOneLocoRefresh == 0){

		if (StartSecondSolenoidTransmition == 0){

			// handle NEW loco command with highest priority
			if (newLocoIdx != -1) {

				if (newLocoSpeed == 1 || newLocoFunction != 0) {
					//log_debug("here");
					// is there something new
					if (newLocoSpeed == 1) {
						//log_debug3("newLocoSpeed: ", newLocoSpeed);
						sendLocoPacket(newLocoIdx, queueIdxLoc, 0, 0);
					} else if (newLocoSpeed == 0 && newLocoFunction != 0) {
						//log_debug3("newLocoFunction: ", newLocoFunction);
						sendLocoPacket(newLocoIdx, queueIdxLoc, 0, newLocoFunction);
					}

					newLocoIdx = -1;
					newLocoSpeed = 0;
					newLocoFunction = 0;

					pwm_mode[queueIdxLoc] = MODE_LOCO;
					// notify PWM that we're finished preparing a new packet
					prepareNextData = 0;
					return;
				} else {
					newLocoIdx = -1;
					newLocoSpeed = 0;
					newLocoFunction = 0;

				}
			}
		}

		if (!solenoidQueueEmpty() || deactivatingSolenoid) {

			// set index accordingly (solenoid to switch or to deactivate)
			uint8_t solenoidIdx =
					deactivatingSolenoid == 1 ?
							solenoidToDeactivate : solenoidQueueIdxFront;

			if (StartOneLocoRefresh == 0 && StartSecondSolenoidTransmition == 0){
				StartOneLocoRefresh = 1;
			} else {
				StartSecondSolenoidTransmition = 0;
			}

			sendSolenoidPacket(solenoidIdx, queueIdxLoc);
			pwm_mode[queueIdxLoc] = MODE_SOLENOID;
			// notify PWM that we're finished preparing a new packet

			prepareNextData = 0;
			return;
		}
	}

	// do a loco refresh
	uint8_t locoToRefresh = -1;
	uint8_t i = (currentRefreshCycleLocoIdx + 1) % 80;
	uint8_t startIdx = i;

	if (functionRefreshSent == 0){
		// search next active loco in refresh queue
		while (1) {
			if (i == 0){
				currentRefreshCycleFunction = (currentRefreshCycleFunction + 1) % 4;
			}
			if (locoData[i].active == 1) {
				currentRefreshCycleLocoIdx = i;
				locoToRefresh = i;
				break;
			}
			i = (i + 1) % 80;
			if (i == startIdx)
				break;
		}

	} else {
		locoToRefresh = currentRefreshCycleLocoIdx;
	}


	sendLocoPacket(locoToRefresh, queueIdxLoc, 1, 0);
	pwm_mode[queueIdxLoc] = MODE_LOCO;

	if (StartOneLocoRefresh > 0){
		StartOneLocoRefresh = 0;
		StartSecondSolenoidTransmition = 1;
	}
	// notify PWM that we're finished preparing a new packet
	prepareNextData = 0;

}

void sendLocoPacket(uint8_t actualLocoIdx, unsigned char queueIdxLoc,
		uint8_t isRefreshCycle, uint8_t updateFunction) {
	if (actualLocoIdx != -1) {

		struct LocoData* actualLoco = &locoData[actualLocoIdx];
		unsigned char address = actualLoco->address;
		unsigned char encodedSpeed = actualLoco->encodedSpeed;
		unsigned char deltaSpeed = actualLoco->deltaSpeed;
		unsigned char speed = actualLoco->numericSpeed;

		// address
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

		// function
		commandQueue[queueIdxLoc][8] = actualLoco->fl;
		commandQueue[queueIdxLoc][9] = actualLoco->fl;

		// REFRESH
		if (isRefreshCycle == 1) {

			// NEW PROTOCOL
			if (actualLoco->isNewProtocol == 1) {

				// zuerst Funktion schicken
				if (functionRefreshSent == 0) {
					functionRefreshSent = 1;
					uint8_t function = 0;
					switch (currentRefreshCycleFunction){
					case 0: //old 1
						function = 1;
						break;
					case 1:  //old 3
						function = 2;
						break;
					case 2:  //old 5
						function = 3;
						break;
					case 3:  //old 7
						function = 4;
						break;
					}
					encodedSpeed = encodeFunction(actualLoco, deltaSpeed, speed,
							function);
				} else {
					functionRefreshSent = 0;
				}

			// DELTA PROTOCOL
			} else {
				encodedSpeed = deltaSpeed;
			}

		// NEW LOCO SPEED OR FUNCTION
		} else {

			// NEW FUNCTION
			if ((actualLoco->isNewProtocol == 1) && (updateFunction != 0)) {
				encodedSpeed = encodeFunction(actualLoco, deltaSpeed, speed,
							newLocoFunction);
			// NEW SPEED DELTA PROTOCOL
			} else if (actualLoco->isNewProtocol != 1) {
				encodedSpeed = deltaSpeed;
			}
		}

		// speed
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][10 + i] = (encodedSpeed >> (7 - i)) & 1;

		finish_mm_command_Loco(queueIdxLoc);

#ifdef NEW_LOCOCMD_EXTENDED
		if (!isRefreshCycle){
			for (uint8_t i = 1; i < NEW_LOCOCMD_REPETITIONS; i++) {
				for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
					commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
							commandQueue[queueIdxLoc][j];
				}
			}
			locoCommandLength[queueIdxLoc] = MM_COMMAND_LENGTH_LOCO * NEW_LOCOCMD_REPETITIONS;
		} else {
			for (uint8_t i = 1; i < LOCOCMD_REPETITIONS; i++) {
				for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
					commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
							commandQueue[queueIdxLoc][j];
				}
			}
			locoCommandLength[queueIdxLoc] =  MM_COMMAND_LENGTH_LOCO * LOCOCMD_REPETITIONS;
		}
#else
		for (uint8_t i = 1; i < LOCOCMD_REPETITIONS; i++) {
			for (uint8_t j = 0; j < MM_COMMAND_LENGTH_LOCO; j++) {
				commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_LOCO + j] =
						commandQueue[queueIdxLoc][j];
			}
		}
		locoCommandLength[queueIdxLoc] =  MM_COMMAND_LENGTH_LOCO * LOCOCMD_REPETITIONS;
#endif

	}
}

uint8_t encodeFunction(struct LocoData* actualLoco, unsigned char deltaSpeed,
		unsigned char speed, uint8_t function) {

	unsigned char abcd = deltaSpeed;
	unsigned char efgh = 0xFF;
	unsigned char mask = 0b01010101;

	switch (function) {
	case 1: // F1
		if (actualLoco->f1 == 1) { //ON

			//log_debug("F1 ON");
			if (speed != 10) {
				efgh = 0b11110011;
			} else {
				efgh = 0b00110011;
			}
		} else {
			//log_debug("F1 OFF");
			if (speed != 2) {
				efgh = 0b11110000;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 2: // F2
		if (actualLoco->f2 == 1) { //ON
			//log_debug("F2 ON");
			if (speed != 11) {
				efgh = 0b00001111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			//log_debug("F2 OFF");
			if (speed != 3) {
				efgh = 0b00001100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 3: // F3
		if (actualLoco->f3 == 1) { //ON
			//log_debug("F3 ON");
			if (speed != 13) {
				efgh = 0b00111111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			//log_debug("F3 OFF");
			if (speed != 5) {
				efgh = 0b00111100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	case 4: // F4
		if (actualLoco->f4 == 1) { //ON
			//log_debug("F4 ON");
			if (speed != 14) {
				efgh = 0b11111111;
			} else {
				efgh = 0b00110011;
			}
		} else {
			if (speed != 6) {
				//log_debug("F4 OFF");
				efgh = 0b11111100;
			} else {
				efgh = 0b11001100;
			}
		}
		break;
	}
	uint8_t encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);
	return encodedSpeed;
}

void sendSolenoidPacket(uint8_t solenoidIdx, unsigned char queueIdxLoc) {

	unsigned char address = solenoidQueue[solenoidIdx].address;
	unsigned char port = solenoidQueue[solenoidIdx].port;

	// address
	for (uint8_t i = 0; i < 8; i++)
		commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

	// unused
	commandQueue[queueIdxLoc][8] = 0;
	commandQueue[queueIdxLoc][9] = 0;

	// port
	for (uint8_t i = 0; i < 6; i++)
		commandQueue[queueIdxLoc][10 + i] = (port >> (5 - i)) & 1;

	if (!deactivatingSolenoid) {
		// new command --> activate port
#ifdef DEBUG
		log_debug3("activating decoder ", address);
#endif

		commandQueue[queueIdxLoc][16] = 1;
		commandQueue[queueIdxLoc][17] = 1;
		solenoidQueue[solenoidIdx].active = 1;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	} else {
		// active solenoid --> deactivate port
#ifdef DEBUG
		log_debug3("deactivating decoder ", solenoidIdx);
#endif
		commandQueue[queueIdxLoc][16] = 0;
		commandQueue[queueIdxLoc][17] = 0;
		solenoidQueue[solenoidIdx].active = 0;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	}

	// Doppelpacket inkl. Pausen erstellen
	finish_mm_command_Solenoid(queueIdxLoc);

	// Doppelpacket-Repetitionen erstellen
	for (uint8_t i = 1; i < SOLENOIDCMD_REPETITIONS; i++) {
		for (uint8_t j = 0; j < MM_COMMAND_LENGTH_SOLENOID; j++) {
			commandQueue[queueIdxLoc][i * MM_COMMAND_LENGTH_SOLENOID + j] =
					commandQueue[queueIdxLoc][j];
		}
	}

	// End-Pause erstellen
	for (uint8_t i = 0; i < MM_END_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][(MM_COMMAND_LENGTH_SOLENOID * SOLENOIDCMD_REPETITIONS) + i] = 2;
	}

	if (StartOneLocoRefresh == 0){

		if (!deactivatingSolenoid) {
			// if new solenoid update queue
			solenoidQueuePop();
		} else {
			// switch off deactivation
			deactivatingSolenoid = 0;
		}

	}
//	locoCmdsSent = 0;

}

void finish_mm_command_Loco(unsigned char queueIdxLoc) {

// pause
	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE_LOCO; i++) {
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + i] = 2;
	}

// copy packet
	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO + i] =
				commandQueue[queueIdxLoc][i];

// add intra double packet pause
	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE_LOCO; i++) {
		commandQueue[queueIdxLoc][MM_DOUBLE_PACKET_LENGTH_LOCO + i] = 2;
	}

}

void finish_mm_command_Solenoid(unsigned char queueIdxLoc) {

// pause
	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + i] = 2;
	}

// copy packet
	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_SOLENOID + i] =
				commandQueue[queueIdxLoc][i];

// add intra double packet pause
	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID; i++) {
		commandQueue[queueIdxLoc][MM_DOUBLE_PACKET_LENGTH_SOLENOID + i] = 2;
	}

}


//
//void finish_mm_command(unsigned char queueIdxLoc) {
//
//// pause
//	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE_LOCO; i++) {
//		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + i] = 2;
//	}
//
////copy packet
//	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
//		commandQueue[queueIdxLoc][MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE_LOCO + i] =
//				commandQueue[queueIdxLoc][i];
//
//// add intra double packet pause
//	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE_LOCO; i++) {
//		commandQueue[queueIdxLoc][MM_DOUBLE_PACKET_LENGTH_LOCO + i] = 2;
//	}
//
//}



// *** Interrupt Service Routines *****************************************

// Timer0 overflow interrupt handler (13ms 20MHz / Timer0Prescaler 1024)
ISR( TIMER0_OVF_vect) {
	timer0_interrupt++;
}

#ifdef AUTO_SOLENOID
// Timer2 fŸr AutoSolenoid
ISR( TIMER2_COMPA_vect){
	timer2_interrupt++;
	if (timer2_interrupt == 250) {
		timer2_interrupt = 0;
		solenoidQueue[solenoidQueueIdxEnter].address = locoData[AUTO_SOLENOID_ADDRESS - 1].address;
		solenoidQueue[solenoidQueueIdxEnter].port = portData[SolenoidTESTport];
		solenoidQueue[solenoidQueueIdxEnter].active = 0;
		solenoidQueue[solenoidQueueIdxEnter].timerDetected = 0;
		solenoidQueue[solenoidQueueIdxEnter].deactivate = 0;
		enqueue_solenoid();
		SolenoidTESTport = (SolenoidTESTport + 1) % 2;
	}
}
#endif

/********* PWM CODE **************/

#ifdef PWM2
ISR( TIMER1_COMPB_vect) {
#else
	ISR( TIMER1_COMPA_vect) {
#endif

	if (prepareNextData == 1 && actualBit == 0) {
		setLocoWait();
		return;
	}

	if (actualBit == 0) {
		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		prepareNextData = 1;
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

			commandLength = (MM_COMMAND_LENGTH_SOLENOID * SOLENOIDCMD_REPETITIONS) + MM_END_PAUSE_SOLENOID;

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
			commandLength = locoCommandLength[pwmQueueIdx];
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

/******* INIT DATA *********/

void initPortData() {

	portData[0] = 0;
	portData[1] = 48;
	portData[2] = 12;
	portData[3] = 60;
	portData[4] = 3;
	portData[5] = 51;
	portData[6] = 15;
	portData[7] = 63;

}

void initLocoData() {

	deltaSpeedData[0] = 0; // STOP
	deltaSpeedData[1] = 48; // 1
	deltaSpeedData[2] = 240; // 2
	deltaSpeedData[3] = 12;
	deltaSpeedData[4] = 204;
	deltaSpeedData[5] = 60;
	deltaSpeedData[6] = 252;
	deltaSpeedData[7] = 3;
	deltaSpeedData[8] = 195;
	deltaSpeedData[9] = 51;
	deltaSpeedData[10] = 243;
	deltaSpeedData[11] = 15;
	deltaSpeedData[12] = 207;
	deltaSpeedData[13] = 63;
	deltaSpeedData[14] = 255; //14
	deltaSpeedData[15] = 1; //DEBUG

	locoData[0].address = 192;
	locoData[1].address = 128;
	locoData[2].address = 48;
	locoData[3].address = 240;
	locoData[4].address = 176;
	locoData[5].address = 32;
	locoData[6].address = 224;
	locoData[7].address = 160;
	locoData[8].address = 12;
	locoData[9].address = 204;

	locoData[10].address = 140;
	locoData[11].address = 60;
	locoData[12].address = 252;
	locoData[13].address = 188;
	locoData[14].address = 44;
	locoData[15].address = 236;
	locoData[16].address = 172;
	locoData[17].address = 8;
	locoData[18].address = 200;
	locoData[19].address = 136;
	locoData[20].address = 56;
	locoData[21].address = 248;
	locoData[22].address = 184;
	locoData[23].address = 40;
	locoData[24].address = 232;
	locoData[25].address = 168;
	locoData[26].address = 3;
	locoData[27].address = 195;
	locoData[28].address = 131;
	locoData[29].address = 34;
	locoData[30].address = 243;
	locoData[31].address = 179;
	locoData[32].address = 35;
	locoData[33].address = 227;
	locoData[34].address = 163;
	locoData[35].address = 15;
	locoData[36].address = 207;
	locoData[37].address = 143;
	locoData[38].address = 63;
	locoData[39].address = 255;
	locoData[40].address = 191;
	locoData[41].address = 47;
	locoData[42].address = 239;
	locoData[43].address = 175;
	locoData[44].address = 11;
	locoData[45].address = 203;
	locoData[46].address = 139;
	locoData[47].address = 59;
	locoData[48].address = 251;
	locoData[49].address = 187;
	locoData[50].address = 43;
	locoData[51].address = 235;
	locoData[52].address = 171;
	locoData[53].address = 2;
	locoData[54].address = 194;
	locoData[55].address = 130;
	locoData[56].address = 50;
	locoData[57].address = 242;
	locoData[58].address = 178;
	locoData[59].address = 34;
	locoData[60].address = 226;
	locoData[61].address = 162;
	locoData[62].address = 14;
	locoData[63].address = 206;
	locoData[64].address = 142;
	locoData[65].address = 62;
	locoData[66].address = 254;
	locoData[67].address = 190;
	locoData[68].address = 46;
	locoData[69].address = 238;
	locoData[70].address = 174;
	locoData[71].address = 10;
	locoData[72].address = 202;
	locoData[73].address = 138;
	locoData[74].address = 58;
	locoData[75].address = 250;
	locoData[76].address = 186;
	locoData[77].address = 42;
	locoData[78].address = 234;
	locoData[79].address = 0;

	for (uint8_t i = 0; i < 80; i++) {

		locoData[i].active = 0;
		locoData[i].isNewProtocol = 1;
		locoData[i].encodedSpeed = 17; // direction=1 , speed=0
		locoData[i].deltaSpeed = deltaSpeedData[0];
		locoData[i].fl = 0;
		locoData[i].f1 = 0;
		locoData[i].f2 = 0;
		locoData[i].f3 = 0;
		locoData[i].f4 = 0;
		locoData[i].direction = 1;
		locoData[i].refreshState = 0;

	}

	locoData[1].isNewProtocol = 0;
	locoData[5].isNewProtocol = 0;
	locoData[7].isNewProtocol = 0;
	locoData[17].isNewProtocol = 0;
	locoData[19].isNewProtocol = 0;
	locoData[23].isNewProtocol = 0;
	locoData[25].isNewProtocol = 0;
	locoData[53].isNewProtocol = 0;
	locoData[55].isNewProtocol = 0;
	locoData[59].isNewProtocol = 0;
	locoData[61].isNewProtocol = 0;
	locoData[71].isNewProtocol = 0;
	locoData[73].isNewProtocol = 0;
	locoData[77].isNewProtocol = 0;
	locoData[79].isNewProtocol = 0;

#ifdef ACTIVATE_DELTALOCOS_ON_INIT
	// dirty: Deltas are active from the start
	initActiveLocoData(1, 0);
	initActiveLocoData(5, 0);
	initActiveLocoData(7, 0);
	initActiveLocoData(17, 0);
	initActiveLocoData(19, 0);
	initActiveLocoData(23, 0);
	initActiveLocoData(25, 0);
	initActiveLocoData(53, 0);
	initActiveLocoData(55, 0);
	initActiveLocoData(59, 0);
	initActiveLocoData(61, 0);
	initActiveLocoData(71, 0);
	initActiveLocoData(73, 0);
	initActiveLocoData(77, 0);
	initActiveLocoData(79, 0);

#else
	initActiveLocoData(78, 1);

#endif

}

void initActiveLocoData(uint8_t number, unsigned char isNewProtocol){

	uint8_t speed = 0;
	uint8_t direction = 1;

	locoData[number].isNewProtocol = isNewProtocol!=0;
	locoData[number].active = 1;
	locoData[number].refreshState = 0;
	locoData[number].numericSpeed = 0;
	locoData[number].direction = direction;
	locoData[number].encodedSpeed = speed;
	locoData[number].deltaSpeed = speed;
	locoData[number].fl = 0;
	locoData[number].f1 = 0;
	locoData[number].f2 = 0;
	locoData[number].f3 = 0;
	locoData[number].f4 = 0;

	if (locoData[number].isNewProtocol) {
		// NEW MM protocol change bits E F G H
		unsigned char efgh = 0xFF;
		unsigned char mask = 0b01010101;
		if (direction == 0) {
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
		unsigned char abcd = locoData[number].encodedSpeed;
		locoData[number].encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);
	}
}



void sendStopAllLoco() {

	unsigned char queueIdxLoc;

	//alle Loks stoppen
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 0);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNextData = 0;
		while (prepareNextData == 0){
			;
		}
	}

	//alle F1 ausschalten
	newLocoFunction = 1;
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 1);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNextData = 0;
		while (prepareNextData == 0){
			;
		}
	}

	//alle F2 ausschalten
	newLocoFunction = 2;
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 1);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNextData = 0;
		while (prepareNextData == 0){
			;
		}
	}

	//alle F3 ausschalten
	newLocoFunction = 3;
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 1);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNextData = 0;
		while (prepareNextData == 0){
			;
		}
	}

	//alle F4 ausschalten
	newLocoFunction = 4;
	for (uint8_t i = 0; i < 80; i++) {
		queueIdxLoc = (pwmQueueIdx + 1) % 2;
		sendLocoPacket(i, queueIdxLoc, 0, 1);
		pwm_mode[queueIdxLoc] = MODE_LOCO;
		// notify PWM that we're finished preparing a new packet
		prepareNextData = 0;
		while (prepareNextData == 0){
			;
		}
	}

	newLocoFunction = 0;
}

//----------------------------------------------------------------------------
// wdt_init - Watchdog Init used to disable the CPU watchdog
//         placed in Startcode, no call needed
//#include <avr/wdt.h>
//
//void wdt_init(void) __attribute__((naked))
//__attribute__((section(".init1")));
//
//void wdt_init(void) {
//	MCUSR = 0;
//	wdt_disable();
//}

