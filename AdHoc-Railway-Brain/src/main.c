/*
 * main.c
 *
 *  Created on: 2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *     Version: 26. März 2017
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

	//unused Pins as Inputs configured
//	DDRA &= ~(1<<DDA0);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA1);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA2);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA3);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA4);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA5);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA6);		// used as PortInterrupt
//	DDRA &= ~(1<<DDA7);		// used as PortInterrupt

	DDRB &= ~(1<<DDB0);
	DDRB &= ~(1<<DDB1);
//	DDRB &= ~(1<<DDB2);		// used as Interrupt 2
	DDRB &= ~(1<<DDB3);
	DDRB &= ~(1<<DDB4);
//	DDRB &= ~(1<<DDB5);		// used as MOSI
//	DDRB &= ~(1<<DDB6);		// used as MISO
//	DDRB &= ~(1<<DDB7);		// used as SCK

//	DDRC &= ~(1<<DDC0);		// PWM Help Output
//	DDRC &= ~(1<<DDC1);		// Go: Write Booster On/Off
//	DDRC &= ~(1<<DDC2);		// Short: Read Short-Booster-States
//	DDRC &= ~(1<<DDC3);		// Debug-LED: Write LED-Output
	DDRC &= ~(1<<DDC4);
	DDRC &= ~(1<<DDC5);
	DDRC &= ~(1<<DDC6);
	DDRC &= ~(1<<DDC7);

//	DDRD &= ~(1<<DDD0);		// UART RX
//	DDRD &= ~(1<<DDD1);		// UART TX
//	DDRD &= ~(1<<DDD2);		// UART RTS
//	DDRD &= ~(1<<DDD3);		// UART CTS
//	DDRD &= ~(1<<DDD4);		// PWM Output
//	DDRD &= ~(1<<DDD5);		// OC1A??
	DDRD &= ~(1<<DDD6);
	DDRD &= ~(1<<DDD7);

	//unused Pins Pullup activated
//	PORTA |= 1<<PORTA0;
//	PORTA |= 1<<PORTA1;
//	PORTA |= 1<<PORTA2;
//	PORTA |= 1<<PORTA3;
//	PORTA |= 1<<PORTA4;
//	PORTA |= 1<<PORTA5;
//	PORTA |= 1<<PORTA6;
//	PORTA |= 1<<PORTA7;

	PORTB |= 1<<PORTB0;
	PORTB |= 1<<PORTB1;
//	PORTB |= 1<<PORTB2;
	PORTB |= 1<<PORTB3;
	PORTB |= 1<<PORTB4;
//	PORTB |= 1<<PORTB5;
//	PORTB |= 1<<PORTB6;
//	PORTB |= 1<<PORTB7;

//	PORTC |= 1<<PORTC0;
//	PORTC |= 1<<PORTC1;
//	PORTC |= 1<<PORTC2;
//	PORTC |= 1<<PORTC3;
	PORTC |= 1<<PORTC4;
	PORTC |= 1<<PORTC5;
	PORTC |= 1<<PORTC6;
	PORTC |= 1<<PORTC7;

//	PORTD |= 1<<PORTD0;
//	PORTD |= 1<<PORTD1;
//	PORTD |= 1<<PORTD2;
//	PORTD |= 1<<PORTD3;
//	PORTD |= 1<<PORTD4;
//	PORTD |= 1<<PORTD5;
	PORTD |= 1<<PORTD6;
	PORTD |= 1<<PORTD7;


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

	setMM2PWMWait();

	TIMSK1 |= (1 << OCIE1B);		// Timer 1 Output Compare B Match Interrupt enabled
	TCCR1A |= (1 << COM1B1); 		// ACTIVATE PWM


	//Init Timer0
	TIMSK0 |= (1 << TOIE0); 		// interrupt enable - here overflow
	TCCR0B |= TIMER0_PRESCALER; 	// use defined prescaler value

	//MFX UID SNIFFER
	//Init Timer2
	TCCR2B |= (1 << CS22);			//Prescaler = clk/64 => 3.2µs

	//Init Interrupt 2
	DDRB &= ~(1<<DDB2);				//Set PB2 as input (Using for interrupt 2)
//	PORTB &= ~(1<<PORTB2);			//Disable PB2 pull-up resistor
	PORTB |= 1<<PORTB2;				//Enable PB2 pull-up resistor

//	EIMSK |= 1<<INT2;				//Enable interrupt 2
	EICRA |= 0<<ISC21 | 1<<ISC20;	//Trigger INT2 on any edge
	//MFX UID SNIFFER

	//PA0 - PA7: set as input (using for Short-Detection)
	DDRA &= ~(1<<DDA0);
	DDRA &= ~(1<<DDA1);
	DDRA &= ~(1<<DDA2);
	DDRA &= ~(1<<DDA3);
	DDRA &= ~(1<<DDA4);
	DDRA &= ~(1<<DDA5);
	DDRA &= ~(1<<DDA6);
	DDRA &= ~(1<<DDA7);

	//PA0 - PA7: disable  pull-up resistor
	PORTA &= ~(1<<PORTA0);
	PORTA &= ~(1<<PORTA1);
	PORTA &= ~(1<<PORTA2);
	PORTA &= ~(1<<PORTA3);
	PORTA &= ~(1<<PORTA4);
	PORTA &= ~(1<<PORTA5);
	PORTA &= ~(1<<PORTA6);
	PORTA &= ~(1<<PORTA7);


	//Init PCINT0 => Short-Detection
	PCICR |= 1<<PCIE0;				//Pin Change Interrupt Enable 0
//	PCICR &= ~(1<<PCIE0);			//Pin Change Interrupt Disable 0
	PCMSK0 |= 1<<PCINT0 | 1<<PCINT1 | 1<<PCINT2 | 1<<PCINT3 | 1<<PCINT4 | 1<<PCINT5 | 1<<PCINT6 | 1<<PCINT7; 	//Pin Change Enable



	sei();

	replys("XRS\r");  //bm?



	//Do this forever
	while (1) {
		cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processASCIIData(receivedCmdString);
		}

		if (prepareNextData == 1) {
			prepareDataForPWM();
		}

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
		log_error("Unable to comply\n");
#endif
		return;
	}
}


void prepareDataForPWM() {

	// Index der commandQueue auf das "leere" Array ändern
	prepareQueueIdx = (pwmQueueIdx + 1) % 2;

	// Aufruf State-Machine (bis die Daten vorbereitet sind)
	while (!nextDataPrepared)
		(*stateMain)();

	prepareNextData = 0;
	nextDataPrepared = 0;
}



//===========================
// Interrupt Service Routines
//===========================

//Interrupt ausgelöst durch Booster-Short (PCINT0 - PCINT7)
ISR(PCINT0_vect)
{
	//Interrupt deaktivieren damit nicht zuviele ausgelöst werden
	//Reaktivierung erfolgt mit Interrupt Timer0
	PCICR &= ~(1<<PCIE0);				//Pin Change Interrupt Disable 0
	short_detected = 1;
	check_shorts();

}


// Verzögerung für Deaktivierung Solenoid und Short-Detection
// Timer0 overflow interrupt handler (13ms 20MHz / Timer0Prescaler 1024 / 8Bit Counter)
// 20MHZ/1024*255 = 13ms
ISR( TIMER0_OVF_vect) {
	solenoidTimer++;
	mfxBrainTimer++;
	if (cutterTimer > 0) {
		cutterTimer++;
		if (cutterTimer > CUTTER_WAIT_TIMERCYCLES){
			int y = 0;
			while (y < MM_SOLENOID_DATA_BUFFER_SIZE) {
				if (solenoidDataMM[y].cutterState == 1) {
					solenoidQueue[solenoidQueueIdxEnter].cutterState = 2;
					solenoidDataMM[y].cutterState = 0;
					solenoidQueue[solenoidQueueIdxEnter].solenoidType = solenoidDataMM[y].solenoidType;
					solenoidQueue[solenoidQueueIdxEnter].port = portData[solenoidDataMM[y].port];
					solenoidQueue[solenoidQueueIdxEnter].tritAddress = solenoidDataMM[y].tritAddress;
					enqueue_solenoid();
					break;
				}
				y++;
			}

		}
	}

	// Short detected?
	if (short_detected == 1){
		short_detected = 0;
	} else {
		//Interrupt für Short-Detection wieder aktivieren, nach minimum 13 ms Wartezeit
		PCICR |= 1<<PCIE0;				//Pin Change Interrupt Enable 0
	}

}

// PWM
ISR( TIMER1_COMPB_vect) {

	if (prepareNextData == 1 && pwmOutputIdx == 0) {
		//PWM in MM2-Lok-Modus setzen
		OCR1AH = (uint8_t) (MM2_LOCO_TOP >> 8);
		OCR1AL = (uint8_t) (MM2_LOCO_TOP);
		setMM2PWMWait();
		return;
	}

	if (prepareNextData == 0 && pwmOutputIdx == 0) {
		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		prepareNextData = 1;

		switch (pwm_mode[pwmQueueIdx]) {
		case MODE_MM2_SOLENOID:
			OCR1AH = (uint8_t) (MM2_SOLENOID_TOP >> 8);
			OCR1AL = (uint8_t) (MM2_SOLENOID_TOP);
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
		case 9: setMM2PWMWait(); break;
		}
		break;
	case MODE_MM2_LOCO:
		switch (b) {
		case 0: setMM2Loco0(); break;
		case 1: setMM2Loco1(); break;
		case 9: setMM2PWMWait(); break;
		}
		break;
	case MODE_MFX:
		switch (b) {
		case 1:
			setMFX1();
			break;
		case 2:
			setMFX2();
			break;
		case 3:
			setMFX3();
			break;
		case 4:
			setMFX4();
			break;
		case 9:
			setMFXPWMWait();
			break;
		}
		break;
	case MODE_DCC:
		switch (b) {
		case 0:
			setDCC0();
			break;
		case 1:
			setDCC1();
			break;
		case 9:
			setDCCPWMWait();
			break;
		}
		break;
	}
	pwmOutputIdx = (pwmOutputIdx + 1) % pwmOutputCmdLength;
}


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

	//"0"- oder halbes "1"-Bit? (100µs oder 50µs)
	// 22*3.2µs =  70.4µs
	if ( i <= 22) {
		bit1 = 1;
		stuffingCounter++;
	} else 	if (i > 22 ) {
		bit0 = 1;
		stuffingCounter=0;
	} else {
		stuffingCounter=0;
	}

	switch (mfxSnifferState) {
	//Sync => 010010
	case 1:
		if (bit0 == 1) {
			mfxSnifferState=2;
		// wenn das erste Null nicht richtig erkannt wurde
		} else if (bit1 == 1) {
			mfxSnifferState=3;
		}
		mfxUID = 0;
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
			mfxSnifferStateExit=mfxSnifferState;
			mfxExitTime=i;
			mfxSnifferState=1;
		}
		break;
	case 27:
		if (bit1 == 1) {
			mfxSnifferState=28;
		}
		else {
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

	solenoidTimer = 0;
	cutterTimer = 0;
	mfxBrainTimer = 0;

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

	mfxSIDCmdCounter = 0;

//	mmChangeDirection = 192;			//im alten Märklin-Format wird Geschwindigkeit 1 als Richtungswechsel interpretiert. Als Trit codiert 1 -> 192

	initIdleLocoData();

	initBrainMFXData();

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

	// MM Loopup-Table Addresses > 79 (80 bis 255)
	// -------------------------------------------
	// Adressen 1 bis 79 werden berechnet - darum immer Index + 80 berücksichtigen!
	mmAddressExt[0] = 0b00000000;	// Kodierung für Adresse 80
	mmAddressExt[1] = 0b01000000;	// Kodierung für Adresse 81
	mmAddressExt[2] = 0b01100000;	// Kodierung für Adresse 82
	mmAddressExt[3] = 0b10010111;	// ....
	mmAddressExt[4] = 0b01110000;
	mmAddressExt[5] = 0b01001000;
	mmAddressExt[6] = 0b01101000;
	mmAddressExt[7] = 0b01011000;
	mmAddressExt[8] = 0b01111000;
	mmAddressExt[9] = 0b01000100;
	mmAddressExt[10] = 0b01100100;
	mmAddressExt[11] = 0b01010100;
	mmAddressExt[12] = 0b01110100;
	mmAddressExt[13] = 0b01001100;
	mmAddressExt[14] = 0b01101100;
	mmAddressExt[15] = 0b01011100;
	mmAddressExt[16] = 0b01111100;
	mmAddressExt[17] = 0b01000010;
	mmAddressExt[18] = 0b01100010;
	mmAddressExt[19] = 0b01010010;
	mmAddressExt[20] = 0b01110010;
	mmAddressExt[21] = 0b01001010;
	mmAddressExt[22] = 0b01101010;
	mmAddressExt[23] = 0b01011010;
	mmAddressExt[24] = 0b01111010;
	mmAddressExt[25] = 0b01000110;
	mmAddressExt[26] = 0b01100110;
	mmAddressExt[27] = 0b01010110;
	mmAddressExt[28] = 0b01110110;
	mmAddressExt[29] = 0b01001110;
	mmAddressExt[30] = 0b01101110;
	mmAddressExt[31] = 0b01011110;
	mmAddressExt[32] = 0b01111110;
	mmAddressExt[33] = 0b01000001;
	mmAddressExt[34] = 0b01100001;
	mmAddressExt[35] = 0b01010001;
	mmAddressExt[36] = 0b01110001;
	mmAddressExt[37] = 0b01001001;
	mmAddressExt[38] = 0b01101001;
	mmAddressExt[39] = 0b01011001;
	mmAddressExt[40] = 0b01111001;
	mmAddressExt[41] = 0b01000101;
	mmAddressExt[42] = 0b01100101;
	mmAddressExt[43] = 0b10011111;
	mmAddressExt[44] = 0b01110101;
	mmAddressExt[45] = 0b01001101;
	mmAddressExt[46] = 0b01101101;
	mmAddressExt[47] = 0b01011101;
	mmAddressExt[48] = 0b01111101;
	mmAddressExt[49] = 0b01000011;
	mmAddressExt[50] = 0b01100011;
	mmAddressExt[51] = 0b01010011;
	mmAddressExt[52] = 0b01110011;
	mmAddressExt[53] = 0b01001011;
	mmAddressExt[54] = 0b01101011;
	mmAddressExt[55] = 0b01011011;
	mmAddressExt[56] = 0b01111011;
	mmAddressExt[57] = 0b01000111;
	mmAddressExt[58] = 0b01100111;
	mmAddressExt[59] = 0b01010111;
	mmAddressExt[60] = 0b01110111;
	mmAddressExt[61] = 0b01001111;
	mmAddressExt[62] = 0b01101111;
	mmAddressExt[63] = 0b01011111;
	mmAddressExt[64] = 0b01111111;
	mmAddressExt[65] = 0b00010000;
	mmAddressExt[66] = 0b00011000;
	mmAddressExt[67] = 0b00010100;
	mmAddressExt[68] = 0b00011100;
	mmAddressExt[69] = 0b00010010;
	mmAddressExt[70] = 0b00011010;
	mmAddressExt[71] = 0b00010110;
	mmAddressExt[72] = 0b00011110;
	mmAddressExt[73] = 0b00010001;
	mmAddressExt[74] = 0b00011001;
	mmAddressExt[75] = 0b00010101;
	mmAddressExt[76] = 0b00011101;
	mmAddressExt[77] = 0b00010011;
	mmAddressExt[78] = 0b00011011;
	mmAddressExt[79] = 0b00010111;
	mmAddressExt[80] = 0b00011111;
	mmAddressExt[81] = 0b11010000;
	mmAddressExt[82] = 0b11011000;
	mmAddressExt[83] = 0b11010100;
	mmAddressExt[84] = 0b11011100;
	mmAddressExt[85] = 0b11010010;
	mmAddressExt[86] = 0b11011010;
	mmAddressExt[87] = 0b11010110;
	mmAddressExt[88] = 0b11011110;
	mmAddressExt[89] = 0b11010001;
	mmAddressExt[90] = 0b11011001;
	mmAddressExt[91] = 0b11010101;
	mmAddressExt[92] = 0b11011101;
	mmAddressExt[93] = 0b11010011;
	mmAddressExt[94] = 0b11011011;
	mmAddressExt[95] = 0b11010111;
	mmAddressExt[96] = 0b11011111;
	mmAddressExt[97] = 0b10010000;
	mmAddressExt[98] = 0b10011000;
	mmAddressExt[99] = 0b10010100;
	mmAddressExt[100] = 0b10011100;
	mmAddressExt[101] = 0b10010010;
	mmAddressExt[102] = 0b10011010;
	mmAddressExt[103] = 0b10010110;
	mmAddressExt[104] = 0b10011110;
	mmAddressExt[105] = 0b10010001;
	mmAddressExt[106] = 0b10011001;
	mmAddressExt[107] = 0b10010101;
	mmAddressExt[108] = 0b10011101;
	mmAddressExt[109] = 0b10010011;
	mmAddressExt[110] = 0b10011011;
	mmAddressExt[111] = 0b01010000;
	mmAddressExt[112] = 0b01010101;
	mmAddressExt[113] = 0b00000100;
	mmAddressExt[114] = 0b00000110;
	mmAddressExt[115] = 0b00000101;
	mmAddressExt[116] = 0b00000111;
	mmAddressExt[117] = 0b11000100;
	mmAddressExt[118] = 0b11000110;
	mmAddressExt[119] = 0b11000101;
	mmAddressExt[120] = 0b11000111;
	mmAddressExt[121] = 0b10000100;
	mmAddressExt[122] = 0b10000110;
	mmAddressExt[123] = 0b10000101;
	mmAddressExt[124] = 0b10000111;
	mmAddressExt[125] = 0b00110100;
	mmAddressExt[126] = 0b00110110;
	mmAddressExt[127] = 0b00110101;
	mmAddressExt[128] = 0b00110111;
	mmAddressExt[129] = 0b11110100;
	mmAddressExt[130] = 0b11110110;
	mmAddressExt[131] = 0b11110101;
	mmAddressExt[132] = 0b11110111;
	mmAddressExt[133] = 0b10110100;
	mmAddressExt[134] = 0b10110110;
	mmAddressExt[135] = 0b10110101;
	mmAddressExt[136] = 0b10110111;
	mmAddressExt[137] = 0b00100100;
	mmAddressExt[138] = 0b00100110;
	mmAddressExt[139] = 0b00100101;
	mmAddressExt[140] = 0b00100111;
	mmAddressExt[141] = 0b11100100;
	mmAddressExt[142] = 0b11100110;
	mmAddressExt[143] = 0b11100101;
	mmAddressExt[144] = 0b11100111;
	mmAddressExt[145] = 0b10100100;
	mmAddressExt[146] = 0b10100110;
	mmAddressExt[147] = 0b10100101;
	mmAddressExt[148] = 0b10100111;
	mmAddressExt[149] = 0b00000001;
	mmAddressExt[150] = 0b11000001;
	mmAddressExt[151] = 0b10000001;
	mmAddressExt[152] = 0b00110001;
	mmAddressExt[153] = 0b11110001;
	mmAddressExt[154] = 0b10110001;
	mmAddressExt[155] = 0b00100001;
	mmAddressExt[156] = 0b11100001;
	mmAddressExt[157] = 0b10100001;
	mmAddressExt[158] = 0b00001101;
	mmAddressExt[159] = 0b11001101;
	mmAddressExt[160] = 0b10001101;
	mmAddressExt[161] = 0b00111101;
	mmAddressExt[162] = 0b11111101;
	mmAddressExt[163] = 0b10111101;
	mmAddressExt[164] = 0b00101101;
	mmAddressExt[165] = 0b11101101;
	mmAddressExt[166] = 0b10101101;
	mmAddressExt[167] = 0b00001001;
	mmAddressExt[168] = 0b11001001;
	mmAddressExt[169] = 0b10001001;
	mmAddressExt[170] = 0b00111001;
	mmAddressExt[171] = 0b11111001;
	mmAddressExt[172] = 0b10111001;
	mmAddressExt[173] = 0b00101001;
	mmAddressExt[174] = 0b11101001;
	mmAddressExt[175] = 0b10101001;


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

void initBrainMFXData(){

	uint16_t crc;
	uint8_t mfxCommandLength;
	int cidx;
	int tidx;
	int EinsHalbiert;
	int stfngCnt;

	//Broadcast-Address 0
	tmpMFXcmd[0] = 1;
	tmpMFXcmd[1] = 0;
	tmpMFXcmd[2] = 0;
	tmpMFXcmd[3] = 0;
	tmpMFXcmd[4] = 0;
	tmpMFXcmd[5] = 0;
	tmpMFXcmd[6] = 0;
	tmpMFXcmd[7] = 0;
	tmpMFXcmd[8] = 0;

	//Command Central
	tmpMFXcmd[9] = 1;
	tmpMFXcmd[10] = 1;
	tmpMFXcmd[11] = 1;
	tmpMFXcmd[12] = 1;
	tmpMFXcmd[13] = 0;
	tmpMFXcmd[14] = 1;

	//UID-Brain (fiktiv)
	tmpMFXcmd[15] = 1;
	tmpMFXcmd[16] = 1;
	tmpMFXcmd[17] = 1;
	tmpMFXcmd[18] = 1;
	tmpMFXcmd[19] = 1;
	tmpMFXcmd[20] = 1;
	tmpMFXcmd[21] = 1;
	tmpMFXcmd[22] = 1;
	tmpMFXcmd[23] = 1;
	tmpMFXcmd[24] = 1;
	tmpMFXcmd[25] = 1;
	tmpMFXcmd[26] = 1;
	tmpMFXcmd[27] = 1;
	tmpMFXcmd[28] = 1;
	tmpMFXcmd[29] = 1;
	tmpMFXcmd[30] = 1;
	tmpMFXcmd[31] = 1;
	tmpMFXcmd[32] = 1;
	tmpMFXcmd[33] = 1;
	tmpMFXcmd[34] = 1;
	tmpMFXcmd[35] = 1;
	tmpMFXcmd[36] = 1;
	tmpMFXcmd[37] = 1;
	tmpMFXcmd[38] = 1;
	tmpMFXcmd[39] = 1;
	tmpMFXcmd[40] = 1;
	tmpMFXcmd[41] = 1;
	tmpMFXcmd[42] = 1;
	tmpMFXcmd[43] = 1;
	tmpMFXcmd[44] = 1;
	tmpMFXcmd[45] = 1;
	tmpMFXcmd[46] = 1;

	//Neuanmeldezähler (fiktive Zahl)
	tmpMFXcmd[47] = 1;
	tmpMFXcmd[48] = 1;
	tmpMFXcmd[49] = 1;
	tmpMFXcmd[50] = 1;
	tmpMFXcmd[51] = 1;
	tmpMFXcmd[52] = 1;
	tmpMFXcmd[53] = 1;
	tmpMFXcmd[54] = 1;
	tmpMFXcmd[55] = 1;
	tmpMFXcmd[56] = 1;
	tmpMFXcmd[57] = 1;
	tmpMFXcmd[58] = 1;
	tmpMFXcmd[59] = 1;
	tmpMFXcmd[60] = 1;
	tmpMFXcmd[61] = 1;
	tmpMFXcmd[62] = 1;

		//CRC Init
	tmpMFXcmd[63] = 0;
	tmpMFXcmd[64] = 0;
	tmpMFXcmd[65] = 0;
	tmpMFXcmd[66] = 0;
	tmpMFXcmd[67] = 0;
	tmpMFXcmd[68] = 0;
	tmpMFXcmd[69] = 0;
	tmpMFXcmd[70] = 0;

	//CRC Berechnung
	mfxCommandLength = 71;
	crc = 0x007f;

	 for (int k = 0; k < mfxCommandLength; k++)
	  {
	    crc = (crc << 1) + tmpMFXcmd[k];
	    if ((crc & 0x0100) > 0)
	      crc = (crc & 0x00FF) ^ 0x07;
	  }

	//CRC Schreiben
	tmpMFXcmd[63] = (crc / 128) % 2;
	tmpMFXcmd[64] = (crc / 64) % 2;
	tmpMFXcmd[65] = (crc / 32) % 2;
	tmpMFXcmd[66] = (crc / 16) % 2;
	tmpMFXcmd[67] = (crc / 8) % 2;
	tmpMFXcmd[68] = (crc / 4) % 2;
	tmpMFXcmd[69] = (crc / 2) % 2;
	tmpMFXcmd[70] = crc % 2;

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
	brainEncCmd[0] = 3;
	brainEncCmd[1] = 4;
	brainEncCmd[2] = 2;
	brainEncCmd[3] = 3;
	brainEncCmd[4] = 4;
	brainEncCmd[5] = 2;

	for (tidx = 0; tidx < (mfxCommandLength + 12); tidx++){	// 12 => 2x Sync am Ende.
		if (tidx == (mfxCommandLength + 11)) {
			brainEncCmd[cidx] = 4;
			brainEncCmd[cidx+1] = 6;	// 6 => Ende des MFX-Befehls
			break;
		} else if (tmpMFXcmd[tidx] == 1 && EinsHalbiert == 0) {
			brainEncCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0  && tmpMFXcmd[tidx + 1] == 0) {
			brainEncCmd[cidx] = 4;
			tidx++;
			cidx++;
		} else  if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 1) {
			brainEncCmd[cidx] = 3;
			cidx++;
			EinsHalbiert = 1;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 0 && EinsHalbiert == 1) {
			brainEncCmd[cidx] = 2;
			tidx++;
			cidx++;
			EinsHalbiert = 0;
		} else if (tmpMFXcmd[tidx] == 1 && tmpMFXcmd[tidx + 1] == 1 && EinsHalbiert == 1) {
			brainEncCmd[cidx] = 1;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 0 && tmpMFXcmd[tidx + 1] == 2) {
			brainEncCmd[cidx] = 3;
			tidx++;
			cidx++;
		} else if (tmpMFXcmd[tidx] == 2 && tmpMFXcmd[tidx + 1] == 0) {
			brainEncCmd[cidx] = 2;
			tidx++;
			cidx++;
			if (tidx == (mfxCommandLength + 11)) {
				brainEncCmd[cidx] = 6;	// 6 => Ende des MFX-Befehls
				break;
			}
		}
	}



}



//===================
//States Main-Machine
//===================

void idleSolenoidDoLoco() {
	//Brain MFX Command "Zentrale"
	if (mfxBrainTimer > BRAIN_MFX_WAIT_TIMERCYCLES){
	mfxBrainTimer = 0;
	prepareMFXBrainCmd();
	} else if (!solenoidQueueEmpty()) {
		stateMain = activateSolenoid;
	} else {
		(*stateLoco)();
	}
}

void activateSolenoid() {
	if (solenoidQueue[solenoidQueueIdxFront].solenoidType == TURNOUT){
		prepareSolenoidPacket(1);
		stateMain = waitSolenoidAfterActivation;
		solenoidTimer = 0;
		//no immediate Deactivation for a cutter
	} else if (solenoidQueue[solenoidQueueIdxFront].solenoidType == CUTTER){
		if (solenoidQueue[solenoidQueueIdxFront].cutterState == 1){
			prepareSolenoidPacket(1);
			cutterTimer = 1;	//start counter
		} else if (solenoidQueue[solenoidQueueIdxFront].cutterState == 2) {
			prepareSolenoidPacket(0);
			cutterTimer = 0;	//stop counter
		}
		solenoidQueuePop();
		stateMain = waitSolenoidAfterDeactivation;
		solenoidTimer = 0;
	}
}

void waitSolenoidAfterActivation() {
	if (solenoidTimer > SOLENOID_WAIT_TIMERCYCLES) {  // timer0_interupt wird alle 13ms hochgezählt
		stateMain = deactivateSolenoid;
	} else {
		(*stateLoco)();
	}
}

void deactivateSolenoid() {
	prepareSolenoidPacket(0);
	solenoidQueuePop();
	stateMain = waitSolenoidAfterDeactivation;
	solenoidTimer = 0;
}

void waitSolenoidAfterDeactivation() {
	if (solenoidTimer > SOLENOID_WAIT_TIMERCYCLES) {  // timer0_interupt wird alle 13ms hochgezählt
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
		} else if (mfxSIDCmdCounter > 0) {
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
		} else if (mfxSIDCmdCounter > 0) {
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
/*		} else if (mfxBrainTimer > BRAIN_MFX_WAIT_TIMERCYCLES){
			mfxBrainTimer = 0;
			prepareMFXBrainCmd();
			stateLoco = refreshDCCLoco;*/
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
/*	} else if (mfxBrainTimer > BRAIN_MFX_WAIT_TIMERCYCLES){
		mfxBrainTimer = 0;
		prepareMFXBrainCmd();
		stateLoco = refreshDCCLoco;*/
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
		} else if (mfxSIDCmdCounter > 0) {
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

	if (newLocoHiPrioQueue){
		//Funktionsbefehle zeigen keine Wirkung, wenn sie nicht "zeitnah" mit einem Speed-Befehl geschickt werden.
		//Darum im Wechsel der Speed-Befehl (fix) mit dem neuen Befehl.
		if (dccLocoRepetitionCmdCounter % 2 == 0) {
			prepareNewLocoPacket(DCC, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].bufferIdx, 0);
		} else {
			prepareNewLocoPacket(DCC, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].bufferIdx, newLocoCmdHiPrio[newLocoCmdHiPrioIdxFront].encCmdIdx);
		}
	} else {
		//Funktionsbefehle zeigen keine Wirkung, wenn sie nicht "zeitnah" mit einem Speed-Befehl geschickt werden.
		//Darum im Wechsel der Speed-Befehl (fix) mit dem neuen Befehl.
		if (dccLocoRepetitionCmdCounter % 2 == 0) {
			prepareNewLocoPacket(DCC, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].bufferIdx, 0);
		} else {
			prepareNewLocoPacket(DCC, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].bufferIdx, newLocoCmdLoPrio[newLocoCmdLoPrioIdxFront].encCmdIdx);
		}
	}

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

	//noch nicht gesendeter "SID zuweisen"-Befehl suchen
	uint8_t i = 0;
	while (locoDataMFX[i].sidAssigned != 0) {
		i++;
		if (i >= MFX_LOCO_DATA_BUFFER_SIZE){
			mfxSIDCmdCounter = 0;
			mfxSIDRepetitionCmdCounter = 0;
			stateLoco = refreshMM2Loco;
			return;
		}
	}
	encodeMFXSIDCmd(i);

	prepareSIDLocoPacket();

	mfxSIDRepetitionCmdCounter++;
	if (mfxSIDRepetitionCmdCounter >= NEW_MFX_SIDCMD_REPETITIONS) {
		mfxSIDRepetitionCmdCounter = 0;
		stateLoco = refreshMM2Loco;
		mfxSIDCmdCounter--;
		locoDataMFX[i].sidAssigned = 1;

	} else {
		stateLoco = newMFXSIDsendDCC;
	}

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

//		log_debug3("pwmMM2Loc: ", locoDataMMRefreshLocoIdx);
//		log_debug3("pwmMM2Cmd: ", locoDataMMRefreshEncCmdIdx);

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

		locoDataDCC[locoDataDCCRefreshLocoIdx].refreshEncCmdIdx = (locoDataDCC[locoDataDCCRefreshLocoIdx].refreshEncCmdIdx + 1) % 4;
		locoDataDCCRefreshEncCmdIdx = locoDataDCC[locoDataDCCRefreshLocoIdx].refreshEncCmdIdx;

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

		if (locoDataDCC[locoDataDCCRefreshLocoIdx].longAddress == 0){
			for (uint8_t k = 0; k < DCC_COMMAND_LENGTH_STD; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[locoDataDCCRefreshLocoIdx].encCmd[locoDataDCCRefreshEncCmdIdx][k];
		} else {
			for (uint8_t k = 0; k < DCC_COMMAND_LENGTH_EXT; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[locoDataDCCRefreshLocoIdx].encCmd[locoDataDCCRefreshEncCmdIdx][k];
		}

		//		log_debug3("pwmDCCLoc: ", locoDataDCCRefreshLocoIdx);
		//		log_debug3("pwmDCCCmd: ", locoDataDCCRefreshEncCmdIdx);

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

		if (locoDataDCC[newBufferIdx].longAddress == 0){
			for (uint8_t k = 0; k < DCC_COMMAND_LENGTH_STD; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[newBufferIdx].encCmd[NewEncCmdIdx][k];
//			log_info3("fn ", locoDataDCC[newBufferIdx].encCmd[NewEncCmdIdx][31]);
		} else {
			for (uint8_t k = 0; k < DCC_COMMAND_LENGTH_EXT; i++, k++)
				pwmCmdQueue[prepareQueueIdx][i] = locoDataDCC[newBufferIdx].encCmd[NewEncCmdIdx][k];
		}

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

void prepareMFXBrainCmd() {

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

	for (uint8_t k = 0; brainEncCmd[k] != 6; i++, k++)
		pwmCmdQueue[prepareQueueIdx][i] = brainEncCmd[k];

//	log_debug("Brain MFX ID");

	//Befehlslänge speichern
	pwmCmdLength[prepareQueueIdx] = i;
	pwm_mode[prepareQueueIdx] = MODE_MFX;

	nextDataPrepared = 1;

}


void prepareSolenoidPacket(unsigned char activate) {

	unsigned int address = solenoidQueue[solenoidQueueIdxFront].tritAddress;
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


