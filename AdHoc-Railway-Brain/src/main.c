/*
 * main.c
 *
 *  Created on: 2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#include "main.h"
#include "booster.h"
#include "pwm.h"
#include "spi.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "fifo.h"
#include "ib_parser.h"

//Logging ist grundsätzlich ausgeschaltet
//unsigned char logLevel = LOG_OFF;
unsigned char logLevel = LOG_DEBUG;

int solenoidQueueIdxEnter;
int booster_report_counter = 0;
unsigned char cmdAvail = 0;

//State pointer
void (*stateMain)() = idleSolenoidDoLoco;
void (*stateLoco)() = refreshMM2Loco;

int main() {
//	log_debug("DEBUGTEXT");
	init();

#ifdef DEBUG_IOS
	debug_init();
#endif

	// start UART (USB-Schnittstelle)
	uart_init();


	//enable Timer/Counter1
	PRR0 &= ~(1 << PRTIM1);
	//enable USART0
	PRR0 &= ~(1 << PRUSART0);
	//enable SPI module
	PRR0 &= ~(1 << PRSPI);

	//Shutdown Timer/Counter3
	PRR1 |= (1 << PRTIM3);
	//Shutdown USART1
	PRR0 |= (1 << PRUSART1);


	//disable JTAG Interface
	MCUCR = (1 << JTD);
	MCUCR = (1 << JTD);

/*
	//Pull-up enabled
	MCUCR &= ~(1 << PUD);
	DDRC &= ~(1 << DDC1);
	DDRC &= ~(1 << DDC2);
	DDRC &= ~(1 << DDC3);
	PORTC |= (1 << PORTC1) | (1 << PORTC2) | (1 << PORTC3);
	_delay_ms(1000);
*/


	// serial Input/Output Booster-Steuerung / -Anzeige (GO, SHORT, LED-STATUS)
	SPI_MasterInitOutput();



	// Enable Interrupts
	sei();

	init_boosters();

	cli();

	initLocoData();
	initPortData();

	//Initialize PWM Channel 1
	initPWM();

	//PWM
	OCR1AH = (uint8_t) (MM2_LOCO_TOP >> 8);
	OCR1AL = (uint8_t) MM2_LOCO_TOP;

	setPWMWait();

	TIMSK1 |= (1 << OCIE1B);		// Timer 1 Output Compare B Match Interrupt enabled
	TCCR1A |= (1 << COM1B1); 		// ACTIVATE PWM


	//Init Timer0
	TIMSK0 |= (1 << TOIE0); 		// interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; 	// use defined prescaler value

	//MFX UID SNIFFER
	//Init Timer2
	TCCR2B |= (1 << CS22);			//Prescaler = clk/64 => 3.2µs

	//Init Interrupt 2
//	DDRB = 1<<PB2;					//Set PB2 as input (Using for interrupt 2)
	PORTB |= 1<<PB2;				//Enable PB2 pull-up resistor

//	EIMSK |= 1<<INT2;				//Enable interrupt 2
	EICRA |= 0<<ISC21 | 1<<ISC20;	//Trigger INT2 on any edge
	//MFX UID SNIFFER



	sei();

	replys("XRS\r");  //bm?



	//Do this forever
	while (1) {


/*
		SPI_MasterTransmitGO(0xFF);
		SPI_MasterTransmitDebug(0xFF);
		_delay_ms(1000);
		SPI_MasterTransmitGO(0x00);
		SPI_MasterTransmitDebug(0x00);
		_delay_ms(1000);
*/


		cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(receivedCmdString);
		}

//		if (timer0_interrupt > 5) {
//			booster_report_counter++;
//			if (booster_report_counter > 50) {
//				report_boosterstate();
//				booster_report_counter = 0;
//			}
//		}


		if (prepareNextData == 1) {
			prepareDataForPWM();
		}

		//check shorts
		//check_shorts();

	}
	cli();
	return 0;
}




void processASCIIData() {

#ifdef LOGGING
	log_debug("Command received");
	log_debug(receivedCmdString);
#endif

	uint8_t ret = parse_ib_cmd(receivedCmdString);

	if (!ret) {
#ifdef LOGGING
		log_error("Command not recognized\n");
#endif
		return;
	}
}


void prepareDataForPWM() {

	// Index der commandQueue auf das "leere" Array ändern
	prepareQueueIdx = (pwmQueueIdx + 1) % 2;
//	log_debug("Idx switch");

	// Aufruf State-Machine (bis die Daten vorbereitet sind)
	while (!nextDataPrepared)
		(*stateMain)();

	prepareNextData = 0;
	nextDataPrepared = 0;
/*	switch (pwm_mode[prepareQueueIdx]) {
	case MODE_MM2_SOLENOID:
		log_debug("nextDataPrepared: Solenoid");
		break;
	case MODE_MM2_LOCO:
		log_debug("nextDataPrepared: Loco");
		break;
	case MODE_MFX:
		log_debug("nextDataPrepared: MFX");
		break;
	case MODE_DCC:
		log_debug("nextDataPrepared: DCC");
		break;
	}*/

}

//===========================
// Interrupt Service Routines
//===========================

//MFX UID SNIFFER
// Interrupt 2 an sPin3
ISR( INT2_vect)
{

	int i = 0;
	uint8_t bit0 = 0;
	uint8_t bit1 = 0; //eigentlich nur "halbes" 1 Bit!!
	i = TCNT2;
	TCNT2 = 0;

	if (stuffingCounter >= 16) {
		stuffingCounter = 0;
		return;
	}

	if (i >= 14 && i <= 17) {
		bit1 = 1;
		stuffingCounter++;
	} else 	if (i >= 29 && i <= 32) {
		bit0 = 1;
		stuffingCounter=0;
	} else {
		stuffingCounter=0;
	}

	switch (mfxSnifferState) {
	//Sync => 010010
	case 1:
/*		if (mfxSnifferStateExit > 27) {
			log_info3("ExitPoint: ", mfxSnifferStateExit);
			log_info3("et: ", mfxExitTime);
			mfxSnifferStateExit = 0;
		}*/
		if (bit0 == 1) {
			mfxSnifferState=2;
			mfxUID = 0;
		}
		break;
	case 2:
		if (bit1 == 1) {
			mfxSnifferState=3;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 3:
		if (bit0 == 1) {
			mfxSnifferState=4;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 4:
		if (bit0 == 1) {
			mfxSnifferState=5;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 5:
		if (bit1 == 1) {
			mfxSnifferState=6;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 6:
		if (bit0 == 1) {
			mfxSnifferState=7;
//			log_info3("CRC: ", mfxSnifferState);
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	//Sync-Wiederholung oder Adresse?
	//Adresse Broadcast => 1100000000 (Achtung: 2 x 1 ergibt erst ein "1 Bit"!)
	case 7:
		if (bit1 == 1) {
			mfxSnifferState=8;
		} else if (bit0 == 1) {
			mfxSnifferState=2;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 8:
		if (bit1 == 1) {
			mfxSnifferState=9;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 9:
		if (bit0 == 1) {
			mfxSnifferState=10;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 10:
		if (bit0 == 1) {
			mfxSnifferState=11;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 11:
		if (bit0 == 1) {
			mfxSnifferState=12;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 12:
		if (bit0 == 1) {
			mfxSnifferState=13;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 13:
		if (bit0 == 1) {
			mfxSnifferState=14;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 14:
		if (bit0 == 1) {
			mfxSnifferState=15;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 15:
		if (bit0 == 1) {
			mfxSnifferState=16;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 16:
		if (bit0 == 1) {
			mfxSnifferState=17;
//			log_info3("HELLO: ", mfxSnifferState);
//			log_info3("AdrBroadcast: ", mfxSnifferState);
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	//Kommando Zuweisung Schienenadresse
	//=> 11111101111  (Achtung: 2 x 1 ergibt erst ein  "1 Bit"!)
	case 17:
		if (bit1 == 1) {
			mfxSnifferState=18;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 18:
		if (bit1 == 1) {
			mfxSnifferState=19;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 19:
		if (bit1 == 1) {
			mfxSnifferState=20;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 20:
		if (bit1 == 1) {
			mfxSnifferState=21;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 21:
		if (bit1 == 1) {
			mfxSnifferState=22;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 22:
		if (bit1 == 1) {
			mfxSnifferState=23;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 23:
		if (bit0 == 1) {
			mfxSnifferState=24;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
//			log_info3("i: ", i);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 24:
		if (bit1 == 1) {
			mfxSnifferState=25;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 25:
		if (bit1 == 1) {
			mfxSnifferState=26;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 26:
		if (bit1 == 1) {
			mfxSnifferState=27;
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 27:
		if (bit1 == 1) {
//			log_info3("KmdSID: ", mfxSnifferState);
			mfxSnifferState=28;
//			log_info3("HELLO: ", mfxSnifferState);
		}
		else {
//			log_info3("KmdSIDabort: ", mfxSnifferState);
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	//SchienenAdresse SID
	//14 Bit lange Adresse vom Steuergerät - interessiert uns hier nicht!  ;-)
	case 28: //Bit 14
		if (bit0 == 1) {
			mfxSnifferState=30;
		}
		if (bit1 == 1) {
			mfxSnifferState=29;
		}
		break;
	case 29:
		if (bit1 == 1) {
			mfxSnifferState=30;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 30: //Bit 13
		if (bit0 == 1) {
			mfxSnifferState=32;
		}
		if (bit1 == 1) {
			mfxSnifferState=31;
		}
		break;
	case 31:
		if (bit1 == 1) {
			mfxSnifferState=32;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 32: //Bit 12
		if (bit0 == 1) {
			mfxSnifferState=34;
		}
		if (bit1 == 1) {
			mfxSnifferState=33;
		}
		break;
	case 33:
		if (bit1 == 1) {
			mfxSnifferState=34;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 34: //Bit 11
		if (bit0 == 1) {
			mfxSnifferState=36;
		}
		if (bit1 == 1) {
			mfxSnifferState=35;
		}
		break;
	case 35:
		if (bit1 == 1) {
			mfxSnifferState=36;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 36: //Bit 10
		if (bit0 == 1) {
			mfxSnifferState=38;
		}
		if (bit1 == 1) {
			mfxSnifferState=37;
		}
		break;
	case 37:
		if (bit1 == 1) {
			mfxSnifferState=38;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 38: //Bit 9
		if (bit0 == 1) {
			mfxSnifferState=40;
		}
		if (bit1 == 1) {
			mfxSnifferState=39;
		}
		break;
	case 39:
		if (bit1 == 1) {
			mfxSnifferState=40;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 40: //Bit 8
		if (bit0 == 1) {
			mfxSnifferState=42;
		}
		if (bit1 == 1) {
			mfxSnifferState=41;
		}
		break;
	case 41:
		if (bit1 == 1) {
			mfxSnifferState=42;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 42: //Bit 7
		if (bit0 == 1) {
			mfxSnifferState=44;
		}
		if (bit1 == 1) {
			mfxSnifferState=43;
		}
		break;
	case 43:
		if (bit1 == 1) {
			mfxSnifferState=44;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 44: //Bit 6
		if (bit0 == 1) {
			mfxSnifferState=46;
		}
		if (bit1 == 1) {
			mfxSnifferState=45;
		}
		break;
	case 45:
		if (bit1 == 1) {
			mfxSnifferState=46;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 46: //Bit 5
		if (bit0 == 1) {
			mfxSnifferState=48;
		}
		if (bit1 == 1) {
			mfxSnifferState=47;
		}
		break;
	case 47:
		if (bit1 == 1) {
			mfxSnifferState=48;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 48: //Bit 4
		if (bit0 == 1) {
			mfxSnifferState=50;
		}
		if (bit1 == 1) {
			mfxSnifferState=49;
		}
		break;
	case 49:
		if (bit1 == 1) {
			mfxSnifferState=50;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 50: //Bit 3
		if (bit0 == 1) {
			mfxSnifferState=52;
		}
		if (bit1 == 1) {
			mfxSnifferState=51;
		}
		break;
	case 51:
		if (bit1 == 1) {
			mfxSnifferState=52;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 52: //Bit 2
		if (bit0 == 1) {
			mfxSnifferState=54;
		}
		if (bit1 == 1) {
			mfxSnifferState=53;
		}
		break;
	case 53:
		if (bit1 == 1) {
			mfxSnifferState=54;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 54: //Bit 1
		if (bit0 == 1) {
			mfxSnifferState=56;
		}
		if (bit1 == 1) {
			mfxSnifferState=55;
		}
		break;
	case 55:
		if (bit1 == 1) {
			mfxSnifferState=56;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	//mfx UID
	//32 Bit langer eindeutiger Identifier
	case 56: //Bit 32
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<31;
			mfxSnifferState=58;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<31;
			mfxSnifferState=57;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 57:
		if (bit1 == 1) {
			mfxSnifferState=58;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 58: //Bit 31
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<30;
			mfxSnifferState=60;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<30;
			mfxSnifferState=59;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 59:
		if (bit1 == 1) {
			mfxSnifferState=60;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 60: //Bit 30
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<29;
			mfxSnifferState=62;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<29;
			mfxSnifferState=61;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 61:
		if (bit1 == 1) {
			mfxSnifferState=62;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 62: //Bit 29
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<28;
			mfxSnifferState=64;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<28;
			mfxSnifferState=63;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 63:
		if (bit1 == 1) {
			mfxSnifferState=64;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 64: //Bit 28
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<27;
			mfxSnifferState=66;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<27;
			mfxSnifferState=65;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 65:
		if (bit1 == 1) {
			mfxSnifferState=66;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 66: //Bit 27
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<26;
			mfxSnifferState=68;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<26;
			mfxSnifferState=67;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 67:
		if (bit1 == 1) {
			mfxSnifferState=68;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 68: //Bit 26
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<25;
			mfxSnifferState=70;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<25;
			mfxSnifferState=69;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 69:
		if (bit1 == 1) {
			mfxSnifferState=70;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 70: //Bit 25
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<24;
			mfxSnifferState=72;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<24;
			mfxSnifferState=71;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 71:
		if (bit1 == 1) {
			mfxSnifferState=72;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 72: //Bit 24
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<23;
			mfxSnifferState=74;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<23;
			mfxSnifferState=73;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 73:
		if (bit1 == 1) {
			mfxSnifferState=74;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 74: //Bit 23
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<22;
			mfxSnifferState=76;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<22;
			mfxSnifferState=75;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 75:
		if (bit1 == 1) {
			mfxSnifferState=76;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 76: //Bit 22
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<21;
			mfxSnifferState=78;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<21;
			mfxSnifferState=77;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 77:
		if (bit1 == 1) {
			mfxSnifferState=78;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 78: //Bit 21
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<20;
			mfxSnifferState=80;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<20;
			mfxSnifferState=79;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 79:
		if (bit1 == 1) {
			mfxSnifferState=80;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 80: //Bit 20
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<19;
			mfxSnifferState=82;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<19;
			mfxSnifferState=81;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 81:
		if (bit1 == 1) {
			mfxSnifferState=82;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 82: //Bit 19
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<18;
			mfxSnifferState=84;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<18;
			mfxSnifferState=83;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 83:
		if (bit1 == 1) {
			mfxSnifferState=84;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 84: //Bit 18
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<17;
			mfxSnifferState=86;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<17;
			mfxSnifferState=85;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 85:
		if (bit1 == 1) {
			mfxSnifferState=86;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 86: //Bit 17
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<16;
			mfxSnifferState=88;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<16;
			mfxSnifferState=87;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 87:
		if (bit1 == 1) {
			mfxSnifferState=88;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 88: //Bit 16
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<15;
			mfxSnifferState=90;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<15;
			mfxSnifferState=89;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 89:
		if (bit1 == 1) {
			mfxSnifferState=90;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 90: //Bit 15
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<14;
			mfxSnifferState=92;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<14;
			mfxSnifferState=91;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 91:
		if (bit1 == 1) {
			mfxSnifferState=92;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 92: //Bit 14
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<13;
			mfxSnifferState=94;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<13;
			mfxSnifferState=93;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 93:
		if (bit1 == 1) {
			mfxSnifferState=94;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 94: //Bit 13
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<12;
			mfxSnifferState=96;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<12;
			mfxSnifferState=95;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 95:
		if (bit1 == 1) {
			mfxSnifferState=96;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 96: //Bit 12
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<11;
			mfxSnifferState=98;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<11;
			mfxSnifferState=97;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 97:
		if (bit1 == 1) {
			mfxSnifferState=98;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 98: //Bit 11
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<10;
			mfxSnifferState=100;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<10;
			mfxSnifferState=99;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 99:
		if (bit1 == 1) {
			mfxSnifferState=100;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 100: //Bit 10
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<9;
			mfxSnifferState=102;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<9;
			mfxSnifferState=101;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 101:
		if (bit1 == 1) {
			mfxSnifferState=102;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 102: //Bit 9
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<8;
			mfxSnifferState=104;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<8;
			mfxSnifferState=103;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 103:
		if (bit1 == 1) {
			mfxSnifferState=104;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 104: //Bit 8
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<7;
			mfxSnifferState=106;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<7;
			mfxSnifferState=105;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 105:
		if (bit1 == 1) {
			mfxSnifferState=106;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 106: //Bit 7
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<6;
			mfxSnifferState=108;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<6;
			mfxSnifferState=107;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 107:
		if (bit1 == 1) {
			mfxSnifferState=108;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 108: //Bit 6
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<5;
			mfxSnifferState=110;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<5;
			mfxSnifferState=109;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 109:
		if (bit1 == 1) {
			mfxSnifferState=110;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 110: //Bit 5
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<4;
			mfxSnifferState=112;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<4;
			mfxSnifferState=111;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 111:
		if (bit1 == 1) {
			mfxSnifferState=112;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 112: //Bit 4
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<3;
			mfxSnifferState=114;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<3;
			mfxSnifferState=113;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 113:
		if (bit1 == 1) {
			mfxSnifferState=114;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 114: //Bit 3
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<2;
			mfxSnifferState=116;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<2;
			mfxSnifferState=115;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 115:
		if (bit1 == 1) {
			mfxSnifferState=116;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 116: //Bit 2
		if (bit0 == 1) {
			mfxUID |= (unsigned long int)0<<1;
			mfxSnifferState=118;
		} else if (bit1 == 1) {
			mfxUID |= (unsigned long int)1<<1;
			mfxSnifferState=117;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 117:
		if (bit1 == 1) {
			mfxSnifferState=118;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 118: //Bit 1
		if (bit0 == 1) {
			mfxUID |= 0;
			mfxSnifferState=120;
		} else if (bit1 == 1) {
			mfxUID |= 1;
			mfxSnifferState=119;
		} else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 119:
		if (bit1 == 1) {
			mfxSnifferState=120;
		}
		else {
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	// jetzt käme noch CRC und ein paar Sync - das interessiert nicht!
	// ENDE
	case 120:
		log_info4("MFX UID = ", mfxUID);
		mfxSnifferStateExit=mfxSnifferState;
		mfxExitTime=i;
		mfxSnifferState=1;
		break;
	}


}


// Timer0 overflow interrupt handler (13ms 20MHz / Timer0Prescaler 1024 / 8Bit Counter)
// 20MHZ/1024*255 = 13ms
ISR( TIMER0_OVF_vect) {
	timer0_interrupt++;
}

// PWM
ISR( TIMER1_COMPB_vect) {

	if (prepareNextData == 1 && pwmOutputIdx == 0) {
		setPWMWait();
//		log_debug("ISR pwmWait");
		return;
	}

	if (pwmOutputIdx == 0) {
		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		prepareNextData = 1;

		switch (pwm_mode[pwmQueueIdx]) {
		case MODE_MM2_SOLENOID:
			OCR1AH = (uint8_t) (MM2_SOLENOID_TOP >> 8);
			OCR1AL = (uint8_t) (MM2_SOLENOID_TOP);
//			log_debug3("PWM MODE Solenoid 256 * ", (pwmCmdLength[pwmQueueIdx] / 256));
//			log_debug3("PWM MODE Solenoid ", (pwmCmdLength[pwmQueueIdx] % 256));
			break;
		case MODE_MM2_LOCO:
			OCR1AH = (uint8_t) (MM2_LOCO_TOP >> 8);
			OCR1AL = (uint8_t) (MM2_LOCO_TOP);
			break;
		// weil die Perioden-Dauer des PWM-Signals bei MFX & DCC variert, geschieht hier nix
//		case MODE_MFX:
//		case MODE_DCC:
		}
		pwmOutputCmdLength = pwmCmdLength[pwmQueueIdx];
	}

	unsigned char b = pwmCmdQueue[pwmQueueIdx][pwmOutputIdx];

	switch  (pwm_mode[pwmQueueIdx]) {
	case MODE_MM2_SOLENOID:
		switch (b) {
		case 0: setMM2Solenoid0(); break;
		case 1: setMM2Solenoid1(); break;
		case 9: setPWMWait(); break;
		}
		break;
	case MODE_MM2_LOCO:
		switch (b) {
		case 0: setMM2Loco0(); break;
		case 1: setMM2Loco1(); break;
		case 9: setPWMWait(); break;
		}
		break;
	case MODE_MFX:
		switch (b) {
		case 1:
			MFX_TOP = 2 * MFX_BASE;
			OCR1AH = (uint8_t) (MFX_TOP >> 8);
			OCR1AL = (uint8_t) (MFX_TOP);
			setMFX1();
			break;
		case 2:
			MFX_TOP = 3 * MFX_BASE;
			OCR1AH = (uint8_t) (MFX_TOP >> 8);
			OCR1AL = (uint8_t) (MFX_TOP);
			setMFX2();
			break;
		case 3:
			MFX_TOP = 3 * MFX_BASE;
			OCR1AH = (uint8_t) (MFX_TOP >> 8);
			OCR1AL = (uint8_t) (MFX_TOP);
			setMFX3();
			break;
		case 4:
			MFX_TOP = 4 * MFX_BASE;
			OCR1AH = (uint8_t) (MFX_TOP >> 8);
			OCR1AL = (uint8_t) (MFX_TOP);
			setMFX4();
			break;
		case 9:
			MFX_TOP = 4 * MFX_BASE;
			OCR1AH = (uint8_t) (MFX_TOP >> 8);
			OCR1AL = (uint8_t) (MFX_TOP);
			setPWMWait();
			break;
		}
		break;
	case MODE_DCC:
		switch (b) {
		case 0:
			DCC_TOP = 4  * DCC_BASE;
			OCR1AH = (uint8_t) (DCC_TOP >> 8);
			OCR1AL = (uint8_t) (DCC_TOP);
			setDCC0();
			break;
		case 1:
			DCC_TOP = 2 * DCC_BASE;
			OCR1AH = (uint8_t) (DCC_TOP >> 8);
			OCR1AL = (uint8_t) (DCC_TOP);
			setDCC1();
			break;
		case 9:
			DCC_TOP = 4  * DCC_BASE;
			OCR1AH = (uint8_t) (DCC_TOP >> 8);
			OCR1AL = (uint8_t) (DCC_TOP);
			setPWMWait();
			break;
		}
		break;
	}
	pwmOutputIdx = (pwmOutputIdx + 1) % pwmOutputCmdLength;
}




void enqueue_solenoid() {
	solenoidQueueIdxEnter++;
	solenoidQueueIdxEnter = solenoidQueueIdxEnter % MAX_SOLENOID_QUEUE;
}

void enqueue_loco_hiprio(unsigned char protocol, uint8_t bufferIdx, uint8_t encCmdIdx) {
	newLocoCmdHiPrio[newLocoCmdHiPrioIdxEnter].protocol = protocol;
	newLocoCmdHiPrio[newLocoCmdHiPrioIdxEnter].bufferIdx = bufferIdx;
	newLocoCmdHiPrio[newLocoCmdHiPrioIdxEnter].encCmdIdx = encCmdIdx;
	newLocoCmdHiPrioIdxEnter++;
	newLocoCmdHiPrioIdxEnter = newLocoCmdHiPrioIdxEnter % MAX_NEW_LOCO_QUEUES;
}

void enqueue_loco_loprio(unsigned char protocol, uint8_t bufferIdx, uint8_t encCmdIdx) {
	newLocoCmdLoPrio[newLocoCmdLoPrioIdxEnter].protocol = protocol;
	newLocoCmdLoPrio[newLocoCmdLoPrioIdxEnter].bufferIdx = bufferIdx;
	newLocoCmdLoPrio[newLocoCmdLoPrioIdxEnter].encCmdIdx = encCmdIdx;
	newLocoCmdLoPrioIdxEnter++;
	newLocoCmdLoPrioIdxEnter = newLocoCmdLoPrioIdxEnter % MAX_NEW_LOCO_QUEUES;
}

uint8_t solenoidQueueEmpty() {
	return solenoidQueueIdxEnter == solenoidQueueIdxFront;
}

void solenoidQueuePop() {
	solenoidQueueIdxFront++;
	solenoidQueueIdxFront = solenoidQueueIdxFront % MAX_SOLENOID_QUEUE;

}

uint8_t newLocoHiPrioQueueEmpty() {
	return newLocoCmdHiPrioIdxEnter == newLocoCmdHiPrioIdxFront;
}

uint8_t newLocoLoPrioQueueEmpty() {
	return newLocoCmdLoPrioIdxEnter == newLocoCmdLoPrioIdxFront;
}

void newLocoHiPrioQueuePop() {
	newLocoCmdHiPrioIdxFront++;
	newLocoCmdHiPrioIdxFront = newLocoCmdHiPrioIdxFront % MAX_NEW_LOCO_QUEUES;
}

void newLocoLoPrioQueuePop() {
	newLocoCmdLoPrioIdxFront++;
	newLocoCmdLoPrioIdxFront = newLocoCmdLoPrioIdxFront % MAX_NEW_LOCO_QUEUES;
}

/******* INIT DATA *********/
inline void init() {

	//MFX UID SNIFFER
	mfxSnifferState = 1;
	mfxSnifferStateExit = 0;
	stuffingCounter = 0;
	mfxUID = 0;

	actualData = 0;

	timer0_interrupt = 0;

	mmLocoCmdCounter = 0;
	mfxLocoCmdCounter = 0;
	dccLocoCmdCounter = 0;


	solenoidQueueIdxEnter = 0; 		// new Solenoid inserted
	solenoidQueueIdxFront = 0; 		// Solenoid to activate

	newLocoCmdHiPrioIdxEnter = 0;
	newLocoCmdHiPrioIdxFront = 0;
	newLocoCmdLoPrioIdxEnter = 0;
	newLocoCmdLoPrioIdxFront = 0;

	pwmQueueIdx = 0;
	pwmOutputIdx = 0;
	pwmOutputCmdLength = 0;
	prepareNextData = 1;
	nextDataPrepared = 0;

	mfxSIDCommandToExecute = 0;

//	mmChangeDirection = 192;			//im alten Märklin-Format wird Geschwindigkeit 1 als Richtungswechsel interpretiert. Als Trit codiert 1 -> 192

	initIdleLocoData();


}


void initIdleLocoData() {
	//INIT MM2-Buffer mit Dummy-Idle-Lok
	//----------------------------------
	uint16_t number = 80;
	uint8_t trit1 = 0;
	uint8_t trit2 = 0;
	uint8_t trit3 = 0;
	uint8_t trit4 = 0;
	locoProtocolIdx[0].address = number;

	locoDataMM[0].address = number;
	locoDataMM[0].speed = 0;
	locoDataMM[0].direction = 0;
	locoDataMM[0].fn = 0;
	locoDataMM[0].f1 = 0;
	locoDataMM[0].f2 = 0;
	locoDataMM[0].f3 = 0;
	locoDataMM[0].f4 = 0;

	// 4-Trits Lokadresse berechnen
	// Low = 0 (0x00), High = 1 (0x01), Open = 2 (0x10)
	trit1 = number  % 3;
	trit2 = (number / 3) % 3;
	trit3 = (number / 9) % 3;
	trit4 = (number / 27) % 3;

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

	unsigned char address = ((64 * trit1) + (16 * trit2) + (4 * trit3) + trit4);

	//Adresse
	for (uint8_t k = 0; k < 8; k++)
		locoDataMM[0].encCmdAdrFn[k] = (address >> (7 - k)) & 1;

	//Fn
	locoDataMM[0].encCmdAdrFn[8] = 0;
	locoDataMM[0].encCmdAdrFn[9] = 0;

	//Data Idle-State
	locoDataMM[0].encCmdData[0][0] = 1;
	locoDataMM[0].encCmdData[0][1] = 0;
	locoDataMM[0].encCmdData[0][2] = 1;
	locoDataMM[0].encCmdData[0][3] = 0;
	locoDataMM[0].encCmdData[0][4] = 1;
	locoDataMM[0].encCmdData[0][5] = 0;
	locoDataMM[0].encCmdData[0][6] = 1;
	locoDataMM[0].encCmdData[0][7] = 0;


	//encF1 bis encF4 enthalten gleiche Daten wie encSpeedFn
	for (uint8_t k = 0; k < 8; k++){
		locoDataMM[0].encCmdData[1][k] = locoDataMM[0].encCmdData[2][k] = locoDataMM[0].encCmdData[3][k] = locoDataMM[0].encCmdData[4][k] = locoDataMM[0].encCmdData[0][k];
	}
	mm2IdleModeActive = 1;

	pwm_mode[0] = MODE_MM2_LOCO;
	pwm_mode[1] = MODE_MM2_LOCO;
}


void initPortData() {

	// MM Lookup-Table Solenoid Decoder Port Adresses
	// ----------------------------------------------
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

	// MM Lookup-Table Speed
	// ---------------------
	mmSpeedData[0] = 0; 		// STOP
	//			 = 192;			//	  / old Protocol-Format: 01 => Reverse
	mmChangeDirection = 192;	//im alten Märklin-Format wird Geschwindigkeit 1 als Richtungswechsel interpretiert. Als Trit codiert 1 -> 192
	mmSpeedData[1] = 48; 		// 1  / old Protocol-Format: 02 => Fahrstufe 1
	mmSpeedData[2] = 240; 		// 2  / old Protocol-Format: 03 => Fahrstufe 2
	mmSpeedData[3] = 12;		// 3  / old Protocol-Format: 04 => Fahrstufe 3
	mmSpeedData[4] = 204;		// ....
	mmSpeedData[5] = 60;
	mmSpeedData[6] = 252;
	mmSpeedData[7] = 3;
	mmSpeedData[8] = 195;
	mmSpeedData[9] = 51;
	mmSpeedData[10] = 243;
	mmSpeedData[11] = 15;
	mmSpeedData[12] = 207;		// ...
	mmSpeedData[13] = 63;		// 13  / old Protocol-Format:  14 => Fahrstufe 13
	mmSpeedData[14] = 255; 		// 14  / old Protocol-Format:  15 => Fahrstufe 14
	mmSpeedData[15] = 1; //DEBUG //bm?


	// DCC Lookup-Table 28 Step Speed Configuration
	// --------------------------------------------
	dccSpeed28Data[0] = 0;
	dccSpeed28Data[1] = 2;
	dccSpeed28Data[2] = 18;
	dccSpeed28Data[3] = 3;
	dccSpeed28Data[4] = 19;
	dccSpeed28Data[5] = 4;
	dccSpeed28Data[6] = 20;
	dccSpeed28Data[7] = 5;
	dccSpeed28Data[8] = 21;
	dccSpeed28Data[9] = 6;
	dccSpeed28Data[10] = 22;
	dccSpeed28Data[11] = 7;
	dccSpeed28Data[12] = 23;
	dccSpeed28Data[13] = 8;
	dccSpeed28Data[14] = 24;
	dccSpeed28Data[15] = 9;
	dccSpeed28Data[16] = 25;
	dccSpeed28Data[17] = 10;
	dccSpeed28Data[18] = 26;
	dccSpeed28Data[19] = 11;
	dccSpeed28Data[20] = 27;
	dccSpeed28Data[21] = 12;
	dccSpeed28Data[22] = 28;
	dccSpeed28Data[23] = 13;
	dccSpeed28Data[24] = 29;
	dccSpeed28Data[25] = 14;
	dccSpeed28Data[26] = 30;
	dccSpeed28Data[27] = 15;
	dccSpeed28Data[28] = 31;

}


//===================
//States Main-Machine
//===================

void idleSolenoidDoLoco() {
	if (!solenoidQueueEmpty()) {
		stateMain = activateSolenoid;
	} else {
		(*stateLoco)();
	}
}

void activateSolenoid() {
	prepareSolenoidPacket(1);
	stateMain = waitSolenoidAfterActivation;
	timer0_interrupt = 0;
}

void waitSolenoidAfterActivation() {
	if (timer0_interrupt > SOLENOID_WAIT_TIMERCYCLES) {  // timer0_interupt wird alle 13ms hochgezählt
		stateMain = deactivateSolenoid;
	} else {
		(*stateLoco)();
	}
}

void deactivateSolenoid() {
	prepareSolenoidPacket(0);
	solenoidQueuePop();
	stateMain = waitSolenoidAfterDeactivation;
	timer0_interrupt = 0;
}

void waitSolenoidAfterDeactivation() {
	if (timer0_interrupt > SOLENOID_WAIT_TIMERCYCLES) {  // timer0_interupt wird alle 13ms hochgezählt
		stateMain = idleSolenoidDoLoco;
	} else {
		(*stateLoco)();
	}
}

//===================
//States Loco-Machine
//===================

void refreshMM2Loco() {
	if (locoDataMM[0].address != 0) {
		if (!newLocoHiPrioQueueEmpty()) {
			newLocoHiPrioQueue = 1;
			switch (newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendMM2;
				break;
			case MFX:
				stateLoco = newMFXsendMFX;
				break;
			case DCC:
				stateLoco = newDCCsendMFX;
				break;
			}
		} else if (mfxSIDCommandToExecute == 1) {
			mfxSIDRepetitionCmdCounter = 0;
			stateLoco = newMFXSIDsendMFX;
		} else if (!newLocoLoPrioQueueEmpty()) {
			newLocoHiPrioQueue = 0;
			switch (newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendMM2;
				break;
			case MFX:
				stateLoco = newMFXsendMFX;
				break;
			case DCC:
				stateLoco = newDCCsendMFX;
				break;
			}
		} else {
			mmLocoCmdCounter++;
			if (mmLocoCmdCounter >= REFRESH_MAXNUMBER_MM_CMD) {
				mmLocoCmdCounter = 0;
				stateLoco = refreshMFXLoco;
			}
			prepareRefreshLocoPacket(MM2);
		}
	} else {
		stateLoco = refreshMFXLoco;
	}
}

void refreshMFXLoco() {
	if (locoDataMFX[0].address != 0) {
		if (!newLocoHiPrioQueueEmpty()) {
			newLocoHiPrioQueue = 1;
			switch (newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendDCC;
				break;
			case MFX:
				stateLoco = newMFXsendMFX;
				break;
			case DCC:
				stateLoco = newDCCsendDCC;
				break;
			}
		} else if (mfxSIDCommandToExecute == 1) {
			mfxSIDRepetitionCmdCounter = 0;
			stateLoco = newMFXSIDsendMFX;
		} else if (!newLocoLoPrioQueueEmpty()) {
			newLocoHiPrioQueue = 0;
			switch (newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendDCC;
				break;
			case MFX:
				stateLoco = newMFXsendMFX;
				break;
			case DCC:
				stateLoco = newDCCsendDCC;
				break;
			}
		} else {
			mfxLocoCmdCounter++;
			if (mfxLocoCmdCounter >= REFRESH_MAXNUMBER_MFX_CMD) {
				mfxLocoCmdCounter = 0;
//				if (locoDataDCC[0].address != 0) {
					stateLoco = refreshDCCLoco;
//				} else {
//					stateLoco = refreshMM2Loco;
//				}
			}
			prepareRefreshLocoPacket(MFX);
		}
	} else {
		stateLoco = refreshDCCLoco;
	}
}

void refreshDCCLoco() {
	if (locoDataDCC[0].address != 0) {
		if (!newLocoHiPrioQueueEmpty()) {
			newLocoHiPrioQueue = 1;
			switch (newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendMM2;
				break;
			case MFX:
				stateLoco = newMFXsendMM2;
				break;
			case DCC:
				stateLoco = newDCCsendDCC;
				break;
			}
		} else if (mfxSIDCommandToExecute == 1) {
			mfxSIDRepetitionCmdCounter = 0;
			stateLoco = newMFXSIDsendMFX;
		} else if (!newLocoLoPrioQueueEmpty()) {
			newLocoHiPrioQueue = 0;
			switch (newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].protocol) {
			case (MM || MM2):
				stateLoco = newMM2sendMM2;
				break;
			case MFX:
				stateLoco = newMFXsendMFX;
				break;
			case DCC:
				stateLoco = newDCCsendDCC;
				break;
			}
		} else {
			dccLocoCmdCounter++;
			if (dccLocoCmdCounter >= REFRESH_MAXNUMBER_DCC_CMD) {
				dccLocoCmdCounter = 0;
				stateLoco = refreshMM2Loco;
			}
			prepareRefreshLocoPacket(DCC);
		}
	} else {
		stateLoco = refreshMM2Loco;
	}
}

void newMM2sendMM2() {

	mmLocoCmdCounter++;
	mmLocoRepetitionCmdCounter++;
	if (mmLocoRepetitionCmdCounter < NEW_MM_LOCOCMD_REPETITIONS && mmLocoCmdCounter >= REFRESH_MAXNUMBER_MM_CMD) {
		mmLocoCmdCounter = 0;
		stateLoco = newMM2sendMFX;
	} else if (mmLocoRepetitionCmdCounter >= NEW_MM_LOCOCMD_REPETITIONS) {
		if (mmLocoCmdCounter < REFRESH_MAXNUMBER_MM_CMD) {
			stateLoco = refreshMM2Loco;
		} else {
			mmLocoCmdCounter = 0;
			stateLoco = refreshMFXLoco;
		}
	}

	if (newLocoHiPrioQueue){
		prepareNewLocoPacket(MM2, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].bufferIdx, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].encCmdIdx);
	}
	else{
		uint8_t idx = newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].bufferIdx;
		prepareNewLocoPacket(MM2, idx, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].encCmdIdx);
		// Delta Spezial: nach Richtungsänderung Geschwindigkeit wieder auf '0' setzen
		if (locoDataMM[idx].changingDirectionDelta == 1){
			locoDataMM[idx].changingDirectionDelta = 0;
			for (uint8_t k = 0; k < 8; k++)
					locoDataMM[idx].encCmdData[0][k] = 0;
		}
	}

	if (mmLocoRepetitionCmdCounter >= NEW_MM_LOCOCMD_REPETITIONS) {
		mmLocoRepetitionCmdCounter = 0;
		if (newLocoHiPrioQueue)
			newLocoHiPrioQueuePop();
		else
			newLocoLoPrioQueuePop();
	}

}

void newMM2sendMFX() {
	if (locoDataMFX[0].address != 0)
		prepareRefreshLocoPacket(MFX);

	stateLoco = newMM2sendDCC;
}

void newMM2sendDCC() {
	if (locoDataDCC[0].address != 0)
		prepareRefreshLocoPacket(DCC);

	stateLoco = newMM2sendMM2;
}

void newMFXsendMM2() {
	prepareRefreshLocoPacket(MM2);
	stateLoco = newMFXsendMFX;
}

void newMFXsendMFX() {

	mfxLocoCmdCounter++;
	mfxLocoRepetitionCmdCounter++;
	if (mfxLocoRepetitionCmdCounter < NEW_MFX_LOCOCMD_REPETITIONS && mfxLocoCmdCounter >= REFRESH_MAXNUMBER_MFX_CMD) {
		mfxLocoCmdCounter = 0;
		stateLoco = newMFXsendDCC;
	} else if (mfxLocoRepetitionCmdCounter >= NEW_MFX_LOCOCMD_REPETITIONS) {
		if (mfxLocoCmdCounter < REFRESH_MAXNUMBER_MFX_CMD) {
			stateLoco = refreshMFXLoco;
		} else {
			mfxLocoCmdCounter = 0;
			stateLoco = refreshDCCLoco;
		}
	}

	if (newLocoHiPrioQueue)
		prepareNewLocoPacket(MFX, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].bufferIdx, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].encCmdIdx);
	else
		prepareNewLocoPacket(MFX, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].bufferIdx, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].encCmdIdx);

	if (mfxLocoRepetitionCmdCounter >= NEW_MFX_LOCOCMD_REPETITIONS) {
			mfxLocoRepetitionCmdCounter = 0;
			if (newLocoHiPrioQueue)
				newLocoHiPrioQueuePop();
			else
				newLocoLoPrioQueuePop();
	}
}

void newMFXsendDCC() {
	if (locoDataDCC[0].address != 0)
		prepareRefreshLocoPacket(DCC);

	stateLoco = newMFXsendMM2;
}

void newDCCsendMM2() {
	prepareRefreshLocoPacket(MM2);
	stateLoco = newDCCsendMFX;
}

void newDCCsendMFX() {
	if (locoDataMFX[0].address != 0)
		prepareRefreshLocoPacket(MFX);

	stateLoco = newDCCsendDCC;
}

void newDCCsendDCC() {

	dccLocoCmdCounter++;
	dccLocoRepetitionCmdCounter++;
	if (dccLocoRepetitionCmdCounter < NEW_DCC_LOCOCMD_REPETITIONS && dccLocoCmdCounter >= REFRESH_MAXNUMBER_DCC_CMD) {
		dccLocoCmdCounter = 0;
		stateLoco = newDCCsendMM2;
	} else if (dccLocoRepetitionCmdCounter >= NEW_DCC_LOCOCMD_REPETITIONS) {
		if (dccLocoCmdCounter < REFRESH_MAXNUMBER_DCC_CMD) {
			stateLoco = refreshDCCLoco;
		} else {
			dccLocoCmdCounter = 0;
			stateLoco = refreshMM2Loco;
		}
	}

	if (newLocoHiPrioQueue)
		prepareNewLocoPacket(DCC, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].bufferIdx, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].encCmdIdx);
	else
		prepareNewLocoPacket(DCC, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].bufferIdx, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].encCmdIdx);

	if (dccLocoRepetitionCmdCounter >= NEW_DCC_LOCOCMD_REPETITIONS) {
			dccLocoRepetitionCmdCounter = 0;
			if (newLocoHiPrioQueue)
				newLocoHiPrioQueuePop();
			else
				newLocoLoPrioQueuePop();
	}
}

void newMFXSIDsendMM2() {
	prepareRefreshLocoPacket(MM2);
	stateLoco = newMFXSIDsendMFX;
}

void newMFXSIDsendMFX() {

	mfxSIDRepetitionCmdCounter++;
	if (mfxSIDRepetitionCmdCounter >= NEW_MFX_SIDCMD_REPETITIONS) {
		mfxSIDRepetitionCmdCounter = 0;
		stateLoco = refreshMM2Loco;
		mfxSIDCommandToExecute = 0;
	} else {
		stateLoco = newMFXSIDsendDCC;
	}
	prepareSIDLocoPacket();

}

void newMFXSIDsendDCC() {
	if (locoDataDCC[0].address != 0)
		prepareRefreshLocoPacket(DCC);

	stateLoco = newMFXSIDsendMM2;
}


void prepareRefreshLocoPacket(uint8_t protocol) {

	if (protocol == MM2) {
		locoDataMMRefreshLocoIdx++;
		if (locoDataMM[locoDataMMRefreshLocoIdx].address == 0) {
			locoDataMMRefreshLocoIdx = 0;
			refreshMMSpeedFn = (refreshMMSpeedFn + 1) % 2;
			if (refreshMMSpeedFn == 0) {
				locoDataMMRefreshEncCmdIdx++;
				if (locoDataMMRefreshEncCmdIdx >= 5)
					locoDataMMRefreshEncCmdIdx = 1;
			}
		}

		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_INTER_COMMAND_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_TO_MM_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_TO_MM_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}
		uint16_t cmdStart = i;

		// Adresse und Fn kopieren
		for (uint8_t k = 0; k < MM_PACKET_ADRESS_FN_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataMM[locoDataMMRefreshLocoIdx].encCmdAdrFn[k];
		// Daten kopieren
		if ((locoDataMM[locoDataMMRefreshLocoIdx].isDelta) || (refreshMMSpeedFn == 1)) {
			//immer nur Speed bei Delta
			for (uint8_t k = 0; k < MM_PACKET_DATA_LENGTH; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataMM[locoDataMMRefreshLocoIdx].encCmdData[0][k];
		} else {
			for (uint8_t k = 0; k < MM_PACKET_DATA_LENGTH; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataMM[locoDataMMRefreshLocoIdx].encCmdData[locoDataMMRefreshEncCmdIdx][k];
		}

		//Pause vor der "Motorola"-Wiederholung einfügen
		for (uint8_t k = 0; k < MM_INTER_PACKET_PAUSE_LOCO; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
		//"Motorola"-Wiederholung einfügen
		for (uint8_t k = 0; k < MM_PACKET_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = pwmCmdQueue[prepareQueueIdx][cmdStart + k];

		//Befehlslänge speichern
		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_MM2_LOCO;

	} else if (protocol == MFX) {

		locoDataMFXRefreshLocoIdx++;
		if (locoDataMFX[locoDataMFXRefreshLocoIdx].address == 0) {
			locoDataMFXRefreshLocoIdx = 0;
		}

		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_TO_MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_TO_MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}

		for (uint8_t k = 0; locoDataMFX[locoDataMFXRefreshLocoIdx].encCmd[k] != 6; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataMFX[locoDataMFXRefreshLocoIdx].encCmd[k];

		//Befehlslänge speichern
		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_MFX;

	} else if (protocol == DCC) {
		locoDataDCCRefreshLocoIdx++;
		if (locoDataDCC[locoDataDCCRefreshLocoIdx].address == 0) {
			locoDataDCCRefreshLocoIdx = 0;
		}
		locoDataDCCRefreshEncCmdIdx = (locoDataDCCRefreshEncCmdIdx + 1) % 4;

		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_TO_DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_TO_DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}

		for (uint8_t k = 0; k < DCC_COMMAND_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[locoDataDCCRefreshLocoIdx].encCmd[locoDataDCCRefreshEncCmdIdx][k];

		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_DCC;

	}
	nextDataPrepared = 1;

}

void prepareNewLocoPacket(unsigned char newProtocol, unsigned char newBufferIdx, unsigned char NewEncCmdIdx) {

	if (newProtocol == MM2) {
		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_INTER_COMMAND_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_TO_MM_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_TO_MM_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}
		uint16_t cmdStart = i;

		// Adresse und Fn kopieren
		for (uint8_t k = 0; k < MM_PACKET_ADRESS_FN_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataMM[newBufferIdx].encCmdAdrFn[k];
		// Daten kopieren
		for (uint8_t k = 0; k < MM_PACKET_DATA_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataMM[newBufferIdx].encCmdData[NewEncCmdIdx][k];
		//Pause vor der "Motorola"-Wiederholung einfügen
		for (uint8_t k = 0; k < MM_INTER_PACKET_PAUSE_LOCO; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
		//"Motorola"-Wiederholung einfügen
		for (uint8_t k = 0; k < MM_PACKET_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = pwmCmdQueue[prepareQueueIdx][cmdStart + k];

		//Befehlslänge speichern
		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_MM2_LOCO;

	} else if (newProtocol == MFX) {
		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_TO_MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_TO_MFX_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}

		for (uint8_t k = 0; locoDataMFX[newBufferIdx].encCmd[k] != 6; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataMFX[newBufferIdx].encCmd[k];

		//Befehlslänge speichern
		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_MFX;

	} else if (newProtocol == DCC) {

		uint16_t i = 0;
		//Pause auf Grund des vorgängigen Befehls bestimmen
		if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
			//Pause hinzufügen
			for (; i < MM_TO_DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
			//Pause hinzufügen
			for (; i < MFX_TO_DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
			//Pause hinzufügen
			for (; i < DCC_INTER_CMD_PAUSE; i++)
				pwmCmdQueue[prepareQueueIdx][i] = 9;
		}

		for (uint8_t k = 0; k < DCC_COMMAND_LENGTH; i++, k++)
			pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[newBufferIdx].encCmd[NewEncCmdIdx][k];

		pwmCmdLength[prepareQueueIdx] = i;
		pwm_mode[prepareQueueIdx] = MODE_DCC;
	}

	nextDataPrepared = 1;
}


void prepareSIDLocoPacket() {

	uint16_t i = 0;
	//Pause auf Grund des vorgängigen Befehls bestimmen
	if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO || pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
		//Pause hinzufügen
		for (; i < MM_TO_MFX_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
		//Pause hinzufügen
		for (; i < MFX_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
		//Pause hinzufügen
		for (; i < DCC_TO_MFX_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	}

	for (uint8_t k = 0; mfxSIDEncCmd[k] != 6; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = mfxSIDEncCmd[k];

	//Befehlslänge speichern
	pwmCmdLength[prepareQueueIdx] = i;
	pwm_mode[prepareQueueIdx] = MODE_MFX;

	nextDataPrepared = 1;
}


void prepareSolenoidPacket(unsigned char activate) {

	unsigned int address = solenoidQueue[solenoidQueueIdxFront].address;
	unsigned char port = solenoidQueue[solenoidQueueIdxFront].port;

	uint16_t i = 0;
	//Pause auf Grund des vorgängigen Befehls bestimmen
	if (pwm_mode[pwmQueueIdx] == MODE_MM2_SOLENOID) {
		//Pause hinzufügen
		for (; i < SOLENOID_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	} else if (pwm_mode[pwmQueueIdx] == MODE_MM2_LOCO) {
		//Pause hinzufügen
		for (; i < LOCO_TO_SOLENOID_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	} else if (pwm_mode[pwmQueueIdx] == MODE_MFX) {
		//Pause hinzufügen
		for (; i < MFX_TO_SOLENOID_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	} else if (pwm_mode[pwmQueueIdx] == MODE_DCC) {
		//Pause hinzufügen
		for (; i < DCC_TO_SOLENOID_INTER_CMD_PAUSE; i++)
			pwmCmdQueue[prepareQueueIdx][i] = 9;
	}
	uint16_t cmdStart = i;

	// address
	for (uint8_t k = 0; k < 10; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = (address >> (9 - k)) & 1;

	// port
	for (uint8_t k = 0; k < 6; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = (port >> (5 - k)) & 1;

	if (activate) {
		// new command --> activate port
#ifdef LOGGING
		log_debug3("activating decoder ", address);
#endif
		pwmCmdQueue[prepareQueueIdx][cmdStart + 16] = 1;
		pwmCmdQueue[prepareQueueIdx][cmdStart + 17] = 1;
	} else {
		// active solenoid --> deactivate port
#ifdef LOGGING
		log_debug3("deactivating decoder ", address);
#endif
		pwmCmdQueue[prepareQueueIdx][cmdStart + 16] = 0;
		pwmCmdQueue[prepareQueueIdx][cmdStart + 17] = 0;
	}
	i += 2;

	// pause
	for (uint8_t k = 0; k < MM_INTER_PACKET_PAUSE_SOLENOID; i++, k++) {
		pwmCmdQueue[prepareQueueIdx][i] = 9;
	}

	// copy packet
	for (uint8_t k = 0; k < MM_PACKET_LENGTH; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = pwmCmdQueue[prepareQueueIdx][cmdStart + k];

	// add intra double packet pause
	for (uint8_t k = 0; k < MM_INTER_DOUBLE_PACKET_PAUSE_SOLENOID; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = 9;

	// Doppelpacket-Repetitionen erstellen
	for (uint8_t n = 1; n < MM_SOLENOIDCMD_REPETITIONS; n++) {
		for (uint8_t k = 0; k < MM_COMMAND_LENGTH_SOLENOID; i++, k++) {
			pwmCmdQueue[prepareQueueIdx][i] = pwmCmdQueue[prepareQueueIdx][cmdStart + k];
		}
	}

	pwmCmdLength[prepareQueueIdx] = i;
	pwm_mode[prepareQueueIdx] = MODE_MM2_SOLENOID;

	nextDataPrepared = 1;
}


