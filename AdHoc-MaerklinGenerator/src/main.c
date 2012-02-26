#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>
#include <string.h>
#include <stdlib.h>

#include "main.h"
#include "pwm.h"
#include "spi.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "fifo.h"
#include "ib_parser.h"

struct LocoData locoData[80];
struct SolenoidData solenoidData[MAX_SOLENOID_QUEUE];
int solenoidDataIdxInsert;

int main() {
	flash_twice_red();
	debug_init();

	SPI_MasterInitOutput();

	initLocoData();
	initPortData();

	//Initialize PWM Channel 1
	initPWM();

	//Loco FREQUENCY
	pwm_mode = MODE_LOCO;
	TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
	ICR1 = 0x192; // counting to TOP takes
	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

	//init Timer0
	TIMSK0 |= (1 << TOIE0); // interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; // use defined prescaler value

	for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
		solenoidData[i].timerDetected = 0;
		solenoidData[i].active = 0;
	}
	// start UART
	uart_init();

	sei();

	log_info("AdHoc-Maerklin Generator V0.1");
	log_info("Have Fun :-)\n");


	SPI_MasterTransmitDebug(0b10101010);
	flash_twice_green();
	flash_twice_red();
	flash_twice_green();
	SPI_MasterTransmitDebug(~0b10101010);

	//Do this forever
	while (1) {


		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(cmd);
		}

		if (timer0_interrupt == 0) {

			for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
				if (solenoidData[i].active
						&& solenoidData[i].timerDetected == 0) {
					solenoidData[i].timerDetected = 1;
				} else if (solenoidData[i].active
						&& solenoidData[i].timerDetected == 1) {
					solenoidToDeactivate = i;

				}
			}
		} else {
		}
		if (prepareNextData == 1)
			prepareDataForPWM();


		//check shorts
		unsigned char shorts = SPI_MasterReceiveShort();
		SPI_MasterTransmitDebug(shorts);
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
	solenoidDataIdxInsert++;
	solenoidDataIdxInsert = solenoidDataIdxInsert % MAX_SOLENOID_QUEUE;

	newSolenoid = 1;
}

void enqueue_loco(uint8_t loco_idx) {
	newLoco = &locoData[loco_idx];
}
void process_loco_cmd(unsigned char* loco_cmd) {

	unsigned char address = loco_cmd[1];
	unsigned char speed = loco_cmd[2];
	unsigned char direction = loco_cmd[3];
	unsigned char functions = loco_cmd[4];
	unsigned char config = loco_cmd[5];

	uint8_t t = address - 1;
	locoData[t].active = (config >> 7) & 1;
	locoData[t].isNewProtocol = (config >> 6) & 1;
	locoData[t].active = 1;
	locoData[t].isNewProtocol = 0;
	if (locoData[t].isNewProtocol) {
		// NEW protocol
	} else {
		// OLD protocol (DELTA)

		if (direction != locoData[t].direction) {
			locoData[t].speed = deltaSpeedData[1];
		} else if (speed == 0) {
			locoData[t].speed = deltaSpeedData[0];
		} else {
			locoData[t].speed = deltaSpeedData[speed + 2];
		}
	}
}

void prepareDataForPWM() {

	unsigned char queueIdxLoc = 0; // = (pwmQueueIdx + 1) % 2;

	if (solenoidDataIdxInsert != solenoidDataIdxPop
			|| solenoidToDeactivate != -1) {

		// set index accordingly (solenoid to switch or to deactivate)
		uint8_t solenoidIdx =
				solenoidToDeactivate == -1 ?
						solenoidDataIdxPop : solenoidToDeactivate;

		// there is a new solenoid to handle!!
		unsigned char address = solenoidData[solenoidIdx].address;
		unsigned char port = solenoidData[solenoidIdx].port;

		if (locoCmdsSent == SOLENOID_WAIT
				|| address != previousSolenoidDecoder) {
			previousSolenoidDecoder = address;

			/*send_nl();
			 send_number(address);
			 send_nl();
			 send_number(port);
			 send_nl();
			 */
			// address
			for (uint8_t i = 0; i < 8; i++)
				commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

			// unused
			commandQueue[queueIdxLoc][8] = 0;
			commandQueue[queueIdxLoc][9] = 0;

			// port
			for (uint8_t i = 0; i < 6; i++)
				commandQueue[queueIdxLoc][10 + i] = (port >> (5 - i)) & 1;

			if (solenoidToDeactivate == -1) {
				// new command --> activate port
				commandQueue[queueIdxLoc][16] = 1;
				commandQueue[queueIdxLoc][17] = 1;
				solenoidData[solenoidIdx].active = 1;
				solenoidData[solenoidIdx].timerDetected = 0;
			} else {
				// active solenoid --> deactivate port
				commandQueue[queueIdxLoc][16] = 0;
				commandQueue[queueIdxLoc][17] = 0;
				solenoidData[solenoidIdx].active = 0;
				solenoidData[solenoidIdx].timerDetected = 0;
			}

			// pause
			commandQueue[queueIdxLoc][18] = 2;
			commandQueue[queueIdxLoc][19] = 2;
			commandQueue[queueIdxLoc][20] = 2;
			commandQueue[queueIdxLoc][21] = 2;
			commandQueue[queueIdxLoc][22] = 2;
			commandQueue[queueIdxLoc][23] = 2;

			for (uint8_t i = 0; i < 24; i++)
				commandQueue[queueIdxLoc][24 + i] =
						commandQueue[queueIdxLoc][i];

			commandQueue[queueIdxLoc][48] = 2;
			commandQueue[queueIdxLoc][49] = 2;
			commandQueue[queueIdxLoc][50] = 2;
			commandQueue[queueIdxLoc][51] = 2;
			commandQueue[queueIdxLoc][52] = 2;
			commandQueue[queueIdxLoc][53] = 2;

			if (solenoidToDeactivate == -1) {
				// if new solenoid update stack
				solenoidDataIdxPop++;
				solenoidDataIdxPop = solenoidDataIdxPop % MAX_SOLENOID_QUEUE;
			} else {
				solenoidToDeactivate = -1;
			}
			pwm_mode = MODE_SOLENOID;
			isLocoCommand = 0;
			newSolenoid = 0;
			locoCmdsSent = 0;
			//flash_once_red();

			prepareNextData = 0;
			return;
		}
	}
	struct LocoData* actualLoco = 0;

	if (newLoco != 0) {
		// there's a new loco command
		actualLoco = newLoco;
		newLoco = 0;
	} else {
		// do a loco refresh
		uint8_t i = (currentLocoIdx + 1) % 80;

		while (1) {
			//transmitUSART('s');
			if (locoData[i].active == 1) {
				currentLocoIdx = i;
				actualLoco = &locoData[i];
				break;
			}
			i = (i + 1) % 80;
		}

	}
	if (actualLoco != 0) {
		unsigned char address = actualLoco->address;
		unsigned char speed = actualLoco->speed;

		// address
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

		// function
		commandQueue[queueIdxLoc][8] = 0;
		commandQueue[queueIdxLoc][9] = 0;

		// speed
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][10 + i] = (speed >> (7 - i)) & 1;

		// pause
		commandQueue[queueIdxLoc][18] = 2;
		commandQueue[queueIdxLoc][19] = 2;
		commandQueue[queueIdxLoc][20] = 2;
		commandQueue[queueIdxLoc][21] = 2;
		commandQueue[queueIdxLoc][22] = 2;
		commandQueue[queueIdxLoc][23] = 2;

		for (uint8_t i = 0; i < 24; i++)
			commandQueue[queueIdxLoc][24 + i] = commandQueue[queueIdxLoc][i];

		commandQueue[queueIdxLoc][48] = 2;
		commandQueue[queueIdxLoc][49] = 2;
		commandQueue[queueIdxLoc][50] = 2;
		commandQueue[queueIdxLoc][51] = 2;
		commandQueue[queueIdxLoc][52] = 2;
		commandQueue[queueIdxLoc][53] = 2;

		pwm_mode = MODE_LOCO;
		isLocoCommand = 1;
		if (locoCmdsSent == SOLENOID_WAIT) {
			//flash_once_green();
		}
		locoCmdsSent = (locoCmdsSent + 1) % (SOLENOID_WAIT + 1);
	}

	prepareNextData = 0;

}

// *** Interrupt Service Routine *****************************************

// Timer0 overflow interrupt handler (~16.384ms 16MHz@1024 precale factor)
ISR( TIMER0_OVF_vect) {
	timer0_interrupt = (timer0_interrupt + 1) % 4;
}

/********* PWM CODE **************/

ISR( TIMER1_COMPA_vect) {

	if (prepareNextData == 1 && actualBit == 0) {
		setSolenoidWait();
		return;
	}

	if (actualBit == 0) {
		//		if (pwmQueueIdx == 0)
		//			pwmQueueIdx = 1;
		//		else
		//			pwmQueueIdx = 0;
		//		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		//pwmQueueIdx = 0;

		//uint8_t pwmQueueIdxLoc = pwmQueueIdx;
		//pwmQueueIdxLoc = (pwmQueueIdxLoc + 1) % 2;

		if (pwm_mode == MODE_SOLENOID) {
			//SOLENDOID
			ICR1H = 0x00;
			ICR1L = 0xCC; // counting to TOP=204
		} else {
			//	//LOCO FREQUENCY
			ICR1 = 0x198; // counting to TOP=408
		}

	}

	if (actualBit > 53) {
		setSolenoidWait();
		actualBit = 0;
		prepareNextData = 1;
		return;
	}

	unsigned char b = commandQueue[0][actualBit];

	if (b == 0) {
		isLocoCommand == 0 ? setSolenoid0() : setLoco0();
	} else if (b == 1) {
		isLocoCommand == 0 ? setSolenoid1() : setLoco1();
	} else if (b == 2) {
		isLocoCommand == 0 ? setSolenoidWait() : setLocoWait();
	}

	actualBit++;

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

	deltaSpeedData[0] = 0;
	deltaSpeedData[1] = 192;
	deltaSpeedData[2] = 48;
	deltaSpeedData[3] = 240;
	deltaSpeedData[4] = 12;
	deltaSpeedData[5] = 204;
	deltaSpeedData[6] = 60;
	deltaSpeedData[7] = 252;
	deltaSpeedData[8] = 3;
	deltaSpeedData[9] = 195;
	deltaSpeedData[10] = 51;
	deltaSpeedData[11] = 243;
	deltaSpeedData[12] = 15;
	deltaSpeedData[13] = 207;
	deltaSpeedData[14] = 63;
	deltaSpeedData[15] = 255;

	mm2SpeedData[0] = 0;
	mm2SpeedData[1] = 0;
	mm2SpeedData[2] = 0;
	mm2SpeedData[3] = 0;
	mm2SpeedData[4] = 0;
	mm2SpeedData[5] = 0;
	mm2SpeedData[6] = 0;
	mm2SpeedData[7] = 0;
	mm2SpeedData[8] = 0;
	mm2SpeedData[9] = 0;
	mm2SpeedData[10] = 0;
	mm2SpeedData[11] = 0;
	mm2SpeedData[12] = 0;
	mm2SpeedData[13] = 0;
	mm2SpeedData[14] = 0;
	mm2SpeedData[15] = 0;

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
		locoData[i].direction = 0;
	}
	locoData[0].active = 1;
}
//----------------------------------------------------------------------------
// wdt_init - Watchdog Init used to disable the CPU watchdog
//         placed in Startcode, no call needed
#include <avr/wdt.h>

void wdt_init(void) __attribute__((naked))
__attribute__((section(".init1")));

void wdt_init(void) {
	MCUSR = 0;
	wdt_disable();
}

