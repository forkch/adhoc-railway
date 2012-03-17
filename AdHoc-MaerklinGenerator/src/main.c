#include <avr/io.h>
#include <avr/interrupt.h>
#include <util/delay.h>
#include <string.h>
#include <stdlib.h>

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
	flash_twice_red();
	debug_init();

	SPI_MasterInitOutput();

	init_boosters();
	initLocoData();
	initPortData();

	//Initialize PWM Channel 1
	initPWM();

	//Loco FREQUENCY
	pwm_mode = MODE_LOCO;
	TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
	ICR1 = LOCO_TOP;
	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

	//init Timer0
	TIMSK0 |= (1 << TOIE0); // interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; // use defined prescaler value

	for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
		solenoidQueue[i].timerDetected = 0;
		solenoidQueue[i].active = 0;
	}
	// start UART
	uart_init();

	sei();

#ifdef DEBUG
	log_info("AdHoc-Maerklin Generator V0.1");
	log_info("Have Fun :-)\n");

	log_debug3("MM_PACKET_LENGTH: ", MM_PACKET_LENGTH);
	log_debug3("MM_INTER_PACKET_PAUSE: ", MM_INTER_PACKET_PAUSE);
	log_debug3("MM_DOUBLE_PACKET_LENGTH: ", MM_DOUBLE_PACKET_LENGTH);
	log_debug3("MM_INTER_DOUBLE_PACKET_PAUSE: ", MM_INTER_DOUBLE_PACKET_PAUSE);
	log_debug3("MM_COMMAND_LENGTH: ", MM_COMMAND_LENGTH);
	log_debug3("MM_COMMAND_LENGTH*LOCOCMD_REPETITIONS: ",
			MM_COMMAND_LENGTH * LOCOCMD_REPETITIONS);
#endif
	replys("ready\n");
	//Do this forever
	while (1) {

		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(cmd);
		}

		if (timer0_interrupt == 0) {

			for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
				if (solenoidQueue[i].active
						&& solenoidQueue[i].timerDetected == 0) {

					// fairly recent solenoid command...deferr deactivation to next cycle
					solenoidQueue[i].timerDetected = 1;
				} else if (solenoidQueue[i].active
						&& solenoidQueue[i].timerDetected == 1) {

					// deactivate solenoid
					deactivatingSolenoid = 1;

					solenoidToDeactivate = i;

				}
			}
		} else {
		}
		if (prepareNextData == 1)
			prepareDataForPWM();

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
void all_loco() {

	for (uint8_t i = 0; i < 80; i++) {
		locoData[i].active = 1;
		locoData[i].encodedSpeed = 0b11001101;

	}
}
void prepareDataForPWM() {

	unsigned char queueIdxLoc = (pwmQueueIdx + 1) % 2;

	// handle NEW loco command with highest priority
	if (newLocoIdx != -1) {
		sendLocoPacket(newLocoIdx, queueIdxLoc, 0);

		newLocoIdx = -1;
		return;
	}

	if (!solenoidQueueEmpty() || deactivatingSolenoid) {

		// set index accordingly (solenoid to switch or to deactivate)
		uint8_t solenoidIdx =
				deactivatingSolenoid == 1 ?
						solenoidToDeactivate : solenoidQueueIdxFront;

		if (locoCmdsSent == SOLENOID_WAIT // wait a number of loco commands until a solenoid is deactivated
		|| !deactivatingSolenoid) { // or if its a new solenoid command

			sendSolenoidPacket(solenoidIdx, queueIdxLoc);
			return;
		}
	}

	// do a loco refresh
	uint8_t locoToRefresh = -1;
	uint8_t i = (currentRefreshCycleLocoIdx + 1) % 80;

	// search next active loco in refresh queue
	while (1) {
		if (locoData[i].active == 1) {
			currentRefreshCycleLocoIdx = i;
			locoToRefresh = i;
			break;
		}
		i = (i + 1) % 80;
	}

	sendLocoPacket(locoToRefresh, queueIdxLoc, 1);

}

inline void sendLocoPacket(uint8_t actualLocoIdx, uint8_t queueIdxLoc,
		uint8_t refresh) {
	if (actualLocoIdx != -1) {

		struct LocoData* actualLoco = &locoData[actualLocoIdx];
		unsigned char address = actualLoco->address;
		unsigned char encodedSpeed = actualLoco->encodedSpeed;
		unsigned char speed = actualLoco->speed;

		//unsigned char *commandQueue =
		//		queueIdxLoc == 0 ? commandQueue1 : commandQueue2;

		// address
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[i] = (address >> (7 - i)) & 1;

		// function
		commandQueue[8] = actualLoco->fl;
		commandQueue[9] = actualLoco->fl;

		if (refresh == 0 || actualLoco->isNewProtocol == 0
				|| (actualLoco->refreshState % 2) == 0) {
			// do nothing
		} else {

			unsigned char abcd = encodedSpeed;
			unsigned char efgh = 0xFF;
			unsigned char mask = 0b01010101;

			switch (actualLoco->refreshState) {
			case 1: // F1
				if (actualLoco->f1 == 1) { //ON
					if (speed != 10) {
						efgh = 0b11110011;
					} else {
						efgh = 0b00110011;
					}
				} else {
					if (speed != 2) {
						efgh = 0b11110000;
					} else {
						efgh = 0b11001100;
					}
				}
				break;
			case 3: // F2
				if (actualLoco->f2 == 1) { //ON
					if (speed != 11) {
						efgh = 0b00001111;
					} else {
						efgh = 0b00110011;
					}
				} else {
					if (speed != 3) {
						efgh = 0b00001100;
					} else {
						efgh = 0b11001100;
					}
				}
				break;
			case 5: // F3
				if (actualLoco->f3 == 1) { //ON
					if (speed != 13) {
						efgh = 0b00111111;
					} else {
						efgh = 0b00110011;
					}
				} else {
					if (speed != 5) {
						efgh = 0b00111100;
					} else {
						efgh = 0b11001100;
					}
				}
				break;
			case 7: // F4
				if (actualLoco->f4 == 1) { //ON
					if (speed != 14) {
						efgh = 0b11111111;
					} else {
						efgh = 0b00110011;
					}
				} else {
					if (speed != 14) {
						efgh = 0b11111100;
					} else {
						efgh = 0b11001100;
					}
				}
				break;
			}

			encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);

//			if (actualLocoIdx == 76) {
//
//				if (actualLoco->refreshState == 1) {
//					log_debug("F1");
//				}
//				if (actualLoco->refreshState == 3) {
//					log_debug("F2");
//				}
//				if (actualLoco->refreshState == 5) {
//					log_debug("F3");
//				}
//				if (actualLoco->refreshState == 7) {
//					log_debug("F4");
//				}
//
//				log_debug("MASK");
//				for (uint8_t i = 0; i < 8; i++) {
//					if ((mask >> (7 - i)) & 1)
//						uart_putc('1');
//					else
//						uart_putc('0');
//				}
//				send_nl();
//
//				log_debug("ABCD");
//				for (uint8_t i = 0; i < 8; i++) {
//					if ((abcd >> (7 - i)) & 1)
//						uart_putc('1');
//					else
//						uart_putc('0');
//				}
//				send_nl();
//
//				log_debug("EFGH");
//				for (uint8_t i = 0; i < 8; i++) {
//					if ((efgh >> (7 - i)) & 1)
//						uart_putc('1');
//					else
//						uart_putc('0');
//				}
//				send_nl();
//
//				log_debug("encodedSpeed");
//				for (uint8_t i = 0; i < 8; i++) {
//					if ((encodedSpeed >> (7 - i)) & 1)
//						uart_putc('1');
//					else
//						uart_putc('0');
//				}
//				send_nl();
//			}

		}

//		if (actualLocoIdx == 76) {
//
//			if (actualLoco->refreshState == 1) {
//				log_debug("F1");
//			}else if (actualLoco->refreshState == 3) {
//				log_debug("F2");
//			}else if (actualLoco->refreshState == 5) {
//				log_debug("F3");
//			}else if (actualLoco->refreshState == 7) {
//				log_debug("F4");
//			}else {
//				log_debug("SPEED");
//			}
//
//			uart_puts("encodedSpeed ");
//			for (uint8_t i = 0; i < 8; i++) {
//				if ((encodedSpeed >> (7 - i)) & 1)
//					uart_putc('1');
//				else
//					uart_putc('0');
//			}
//			send_nl();
//		}

		actualLoco->refreshState = (actualLoco->refreshState + 1) % 8;

		// speed
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[10 + i] = (encodedSpeed >> (7 - i)) & 1;

		finish_mm_command(queueIdxLoc);

		for (uint8_t i = 1; i < LOCOCMD_REPETITIONS; i++) {
			for (uint8_t j = 0; j < MM_COMMAND_LENGTH; j++) {
				commandQueue[i * MM_COMMAND_LENGTH + j] = commandQueue[j];
			}
		}

		pwm_mode = MODE_LOCO;
		isLocoCommand = 1;
		locoCmdsSent = (locoCmdsSent + 1) % (SOLENOID_WAIT + 1);
	}
// notify PWM that we're finished preparing a new packet
	prepareNextData = 0;
}

inline void sendSolenoidPacket(uint8_t solenoidIdx, uint8_t queueIdxLoc) {

	unsigned char address = solenoidQueue[solenoidIdx].address;
	unsigned char port = solenoidQueue[solenoidIdx].port;

	//unsigned char *commandQueue = commandQueue1;
//			queueIdxLoc == 0 ? commandQueue1 : commandQueue2;
	// address
	for (uint8_t i = 0; i < 8; i++)
		commandQueue[i] = (address >> (7 - i)) & 1;

	// unused
	commandQueue[8] = 0;
	commandQueue[9] = 0;

	// port
	for (uint8_t i = 0; i < 6; i++)
		commandQueue[10 + i] = (port >> (5 - i)) & 1;

	if (!deactivatingSolenoid) {
		// new command --> activate port
//#ifdef DEBUG
//		log_debug3("activating decoder ", address);
//#endif
		_delay_us(50);

		commandQueue[16] = 1;
		commandQueue[17] = 1;
		solenoidQueue[solenoidIdx].active = 1;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	} else {
		// active solenoid --> deactivate port
#ifdef DEBUG
		log_debug3("deactivating decoder ", solenoidIdx);
#endif
		commandQueue[16] = 0;
		commandQueue[17] = 0;
		solenoidQueue[solenoidIdx].active = 0;
		solenoidQueue[solenoidIdx].timerDetected = 0;
	}

	finish_mm_command(queueIdxLoc);

	if (!deactivatingSolenoid) {
		// if new solenoid update queue
		solenoidQueuePop();
	} else {
		// switch off deactivation
		deactivatingSolenoid = 0;
	}

	pwm_mode = MODE_SOLENOID;
	locoCmdsSent = 0;

	// notify PWM that we're finished preparing a new packet
	prepareNextData = 0;
}

void finish_mm_command(uint8_t queueIdxLoc) {

	//unsigned char *commandQueue =
	//		queueIdxLoc == 0 ? commandQueue1 : commandQueue2;
// pause
	for (uint8_t i = 0; i < MM_INTER_PACKET_PAUSE; i++) {
		commandQueue[MM_PACKET_LENGTH + i] = 2;
	}

//copy packet
	for (uint8_t i = 0; i < MM_PACKET_LENGTH; i++)
		commandQueue[MM_PACKET_LENGTH + MM_INTER_PACKET_PAUSE + i] =
				commandQueue[i];

// add intra double packet pause
	for (uint8_t i = 0; i < MM_INTER_DOUBLE_PACKET_PAUSE; i++) {
		commandQueue[MM_DOUBLE_PACKET_LENGTH + i] = 2;
	}

}

// *** Interrupt Service Routines *****************************************

// Timer0 overflow interrupt handler (~16.384ms 16MHz@1024 precale factor)
ISR( TIMER0_OVF_vect) {
	timer0_interrupt = (timer0_interrupt + 1) % 4;
}

/********* PWM CODE **************/

ISR( TIMER1_COMPA_vect) {

	cli();
	if (prepareNextData == 1 && actualBit == 0) {
		setSolenoidWait();
		sei();
		return;
	}

	uint8_t commandLength = MM_COMMAND_LENGTH * LOCOCMD_REPETITIONS;
	if (actualBit == 0) {
		//pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		//prepareNextData = 1;
		if (pwm_mode == MODE_SOLENOID) {
			//SOLENDOID
			ICR1 = SOLENOID_TOP;
			commandLength = MM_COMMAND_LENGTH;
		} else {
			//LOCO FREQUENCY
			ICR1 = LOCO_TOP;
		}
	}

	if (actualBit > commandLength) {
		setSolenoidWait();
		actualBit = 0;
		prepareNextData = 1;
		sei();
		return;
	}

	//unsigned char *commandQueue =
	//		pwmQueueIdx == 0 ? commandQueue1 : commandQueue2;
	unsigned char b = commandQueue[actualBit];

	if (b == 0) {
		pwm_mode == MODE_SOLENOID ? setSolenoid0() : setLoco0();
	} else if (b == 1) {
		pwm_mode == MODE_SOLENOID ? setSolenoid1() : setLoco1();
	} else if (b == 2) {
		pwm_mode == MODE_SOLENOID ? setSolenoidWait() : setLocoWait();
	}

	actualBit++;
	sei();

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
		locoData[i].direction = 0;
		locoData[i].refreshState = 0;
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

