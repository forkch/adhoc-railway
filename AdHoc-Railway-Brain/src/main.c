#include "main.h"
#include "booster.h"
#include "pwm.h"
#include "spi.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "fifo.h"
#include "ib_parser.h"
#include "maerklin_mm.h"

unsigned char debugLevel = DEBUG_ERROR;

int main() {

	init();

	sei();

#ifdef SEND_STOP_ALL_LOCO_ON_INIT
	sendStopAllLoco();
#endif

#ifdef DEBUG
	log_info("-----------------------------");
	log_info("AdHoc-Maerklin Generator V0.1");
	log_info("Have Fun :-)\n");

	log_debug3("MM_PACKET_LENGTH: ", MM_PACKET_LENGTH);
	log_debug3("MM_INTER_PACKET_PAUSE_LOCO: ", MM_INTER_PACKET_PAUSE_LOCO);
	log_debug3("MM_DOUBLE_PACKET_LENGTH_LOCO: ", MM_DOUBLE_PACKET_LENGTH_LOCO);
	log_debug3("MM_INTER_DOUBLE_PACKET_PAUSE_LOCO: ",
	MM_INTER_DOUBLE_PACKET_PAUSE_LOCO);
	log_debug3("MM_COMMAND_LENGTH_LOCO: ", MM_COMMAND_LENGTH_LOCO);
	log_debug3("MM_COMMAND_LENGTH_LOCO*LOCOCMD_REPETITIONS: ",
	MM_COMMAND_LENGTH_LOCO * LOCOCMD_REPETITIONS);

	log_debug3("MM_INTER_PACKET_PAUSE_SOLENOID: ",
	MM_INTER_PACKET_PAUSE_SOLENOID);
	log_debug3("MM_DOUBLE_PACKET_LENGTH_SOLENOID: ",
	MM_DOUBLE_PACKET_LENGTH_SOLENOID);
	log_debug3("MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID: ",
	MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID);
	log_debug3("MM_COMMAND_LENGTH_SOLENOID: ", MM_COMMAND_LENGTH_SOLENOID);
	log_debug3("MM_COMMAND_LENGTH_SOLENOID*SOLENOIDCMD_REPETITIONS: ",
	MM_COMMAND_LENGTH_SOLENOID * SOLENOIDCMD_REPETITIONS);
#endif

	replys("XRS\r");

	//Do this forever
	while (1) {

		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(cmd);
		}

		//Verz�gerung Weichen-Deaktivierung in 13ms-Schritten
		if (timer0_interrupt > 5 && !deactivatingSolenoid) {
			timer0_interrupt = 0;
			if (solenoidQueue[solenoidQueueIdxFront2].active
					&& solenoidQueue[solenoidQueueIdxFront2].timerDetected
							== 0) {

				// fairly recent solenoid command...defer deactivation to next cycle
				solenoidQueue[solenoidQueueIdxFront2].timerDetected = 1;

			} else if (solenoidQueue[solenoidQueueIdxFront2].active
					&& solenoidQueue[solenoidQueueIdxFront2].timerDetected == 1
					&& !deactivatingSolenoid) {

				// deactivate solenoid
				deactivatingSolenoid = 1;

				solenoidToDeactivate = solenoidQueueIdxFront2;
				solenoidQueueIdxFront2++;
				solenoidQueueIdxFront2 = solenoidQueueIdxFront2
						% MAX_SOLENOID_QUEUE;
			}
		}

		if (prepareNewDataWhileSending == 1) {
			prepareMaerklinMMDataForPWM();
		}

		//check shorts
		check_for_shorts();

	}
	cli();
	return 0;
}

void initDatastructures() {
	solenoidQueueIdxEnter = 0; // new Solenoid inserted
	solenoidQueueIdxFront = 0; // Solenoid to activate
	solenoidQueueIdxFront2 = 0; // Solenoid to deactivate
	newLocoQueueIdxEnter = 0;
	newLocoQueueIdxFront = 0;
	newLoco = 0;
	for (int i = 0; i < MAX_SOLENOID_QUEUE; i++) {
		solenoidQueue[i].timerDetected = 0;
		solenoidQueue[i].active = 0;
	}
	for (int i = 0; i < MAX_NEW_LOCO_QUEUE; i++) {
		newLocoQueue[i].newLocoIdx = -1;
		newLocoQueue[i].newLocoSpeed = 0;
		newLocoQueue[i].newLocoFunction = -1;
	}
}

void init() {
	debug_init();

	// start UART
	uart_init();

	SPI_MasterInitOutput();
	SPI_MasterTransmitDebug(0x00);

	sei();
	init_boosters();
	cli();

	init_maerklin_mm();

	//init Timer0
	TIMSK0 |= (1 << TOIE0); // interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; // use defined prescaler value

#ifdef AUTO_SOLENOID
	OCR2A = (uint8_t) AUTO_SOLENOID_TOP;
	TCCR2A = (1 << WGM21);  // CTC Mode
	TIMSK2 = (1 << OCIE2A);// interrupt enable - here CompareA
	TCCR2B = (1 << CS20) | (1 << CS21) | (1 << CS22);// prescaling 1024
	//TCCR2B = (1 << CS21) | (1 << CS22); // prescaling 256
	//TCCR2B = (1 << CS20) | (1 << CS22); // prescaling 128
	//TCCR2B = (1 << CS20)  | (1 << CS21); // prescaling 32
	//TCCR2B = (1 << CS21); // prescaling 8
#endif


	timer0_interrupt = 0;

	prepareNewDataWhileSending = 1;
	SolenoidTESTport = AUTO_SOLENOID_PORT;

	initDatastructures();
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
	//newLocoIdx = loco_idx;
	newLocoQueue[newLocoQueueIdxEnter].newLocoIdx = loco_idx;
	newLocoQueueIdxEnter++;
	newLocoQueueIdxEnter = newLocoQueueIdxEnter % MAX_NEW_LOCO_QUEUE;
}


uint8_t newLocoQueueEmpty() {
	return newLocoQueueIdxEnter == newLocoQueueIdxFront;
}

void newLocoQueuePop() {
	newLocoQueueIdxFront++;
	newLocoQueueIdxFront = newLocoQueueIdxFront % MAX_NEW_LOCO_QUEUE;

}

uint8_t solenoidQueueEmpty() {
	return solenoidQueueIdxEnter == solenoidQueueIdxFront;
}

void solenoidQueuePop() {
	solenoidQueueIdxFront++;
	solenoidQueueIdxFront = solenoidQueueIdxFront % MAX_SOLENOID_QUEUE;
}


// *** Interrupt Service Routines *****************************************

// Timer0 overflow interrupt handler (13ms 20MHz / Timer0Prescaler 1024)
ISR( TIMER0_OVF_vect) {
	timer0_interrupt++;
}

#ifdef AUTO_SOLENOID
// Timer2 f�r AutoSolenoid
ISR( TIMER2_COMPA_vect) {
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

