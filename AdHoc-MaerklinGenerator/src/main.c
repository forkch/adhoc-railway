#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define FOSC 16000000
#define BAUD 115200
#define MYUBRR FOSC/16/BAUD-1

#define MODE_SOLENOID 0
#define MODE_LOCO 1
unsigned char pwm_mode = 0;

#define MAX_COMMAND_QUEUE 1
#define MAX_SOLENOID_QUEUE 20

typedef struct LocoData {
	unsigned char address;
	unsigned char speed;
	unsigned char f1;
	unsigned char f2;
	unsigned char f3;
	unsigned char f4;
	char function :1;
	char active :1;
	char isNewProtocol :1;
};

unsigned char portData[8];

struct LocoData locoData[80];

typedef struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

struct SolenoidData solenoidData[MAX_SOLENOID_QUEUE];
uint8_t solenoidDataIdxInsert = 0;
uint8_t solenoidDataIdxPop = 0;

volatile unsigned char pwmQueueIdx = 0;
unsigned char commandQueue[2][54];

volatile uint8_t actualBit = 0;

unsigned char cmd[4];
unsigned char prepareNextData = 1;


/****** Funtion Declarations ******/
void initPWM();
void setPWMOutput(uint16_t duty);
void setSolenoid0();
void setSolenoid1();
void setSolenoidWait();
void setLoco0();
void setLoco1();
void setLocoWait();
void initUSART(unsigned int ubrr);
void transmitUSART(unsigned char data);
unsigned char receiveUSART(void);
unsigned char checkForNewCommand();
void initPortData();
void initLocoData();
void prepareDataForPWM();
void processData();


int main() {

	DDRD = (1 << PD0) | (1 << PD1) | (1 << PD2) | (1 << PD3); /* Output */
	DDRC |= (1 << PC0) | (1 << PC1) | (1 << PC2);

	//Initialize PWM Channel 1
	initPWM();
	initUSART(8);

	setSolenoidWait();

	initLocoData();
	initPortData();

	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

	//SOLENDOID FREQUENCY
	TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
	ICR1H = 0x00;
	ICR1L = 0xCC; // counting to TOP takes
	pwm_mode = MODE_SOLENOID;
	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

	sei();

	//Do this forever
	while (1) {

		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail) {
			processData(cmd);
		}

		if (prepareNextData)
			prepareDataForPWM();

		// TODO: check for timer timeout

	}
	cli();
	return 0;
}


void processData() {
	if (cmd[0] == 'w') {

		//solenoid
		uint8_t t = cmd[1] - 1;
		unsigned char address = locoData[t].address;
		unsigned char port = portData[cmd[2]];

		solenoidData[solenoidDataIdxInsert].address = address;
		solenoidData[solenoidDataIdxInsert].port = port;
		solenoidData[solenoidDataIdxInsert].active = 0;
		solenoidData[solenoidDataIdxInsert].timerDetected = 0;
		solenoidData[solenoidDataIdxInsert].deactivate = 0;

		solenoidDataIdxInsert++;
		solenoidDataIdxInsert = solenoidDataIdxInsert % MAX_SOLENOID_QUEUE;

	} else {
		//TODO: loco

	}

}

void prepareDataForPWM() {

	if (solenoidDataIdxInsert != solenoidDataIdxPop) {
		// there is a new solenoid to handle!!

		unsigned char queueIdxLoc = (pwmQueueIdx + 1) % 2;

		unsigned char address = solenoidData[solenoidDataIdxPop].address;
		unsigned char port = solenoidData[solenoidDataIdxPop].port;

		// address
		for (uint8_t i = 0; i < 8; i++)
			commandQueue[queueIdxLoc][i] = (address >> (7 - i)) & 1;

		// unused
		commandQueue[queueIdxLoc][8] = 0;
		commandQueue[queueIdxLoc][9] = 0;

		// port
		for (uint8_t i = 0; i < 6; i++)
			commandQueue[queueIdxLoc][10 + i] = (port >> (5 - i)) & 1;

		commandQueue[queueIdxLoc][16] = 1;
		commandQueue[queueIdxLoc][17] = 1;

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

		solenoidData[solenoidDataIdxPop].active = 1;

		solenoidDataIdxPop++;
		solenoidDataIdxPop = solenoidDataIdxPop % MAX_SOLENOID_QUEUE;

		prepareNextData = 0;
	}
}



/********* UART CODE **************/

void initUSART(unsigned int ubrr) {
	/* Set baud rate */
	UBRRH = (unsigned char) (ubrr >> 8);
	UBRRL = (unsigned char) ubrr;

	/* Enable receiver and transmitter */
	UCSRB = (1 << RXEN) | (1 << TXEN);
	/* Set frame format: 8data, 2stop bit */
	UCSRC = (1 << URSEL) | (1 << USBS) | (1 << UCSZ0) | (1 << UCSZ1);
}

void transmitUSART(unsigned char data) {
	/* Wait for empty transmit buffer */
	while (!(UCSRA & (1 << UDRE)))
		;
	/* Put data into buffer, sends the data */
	UDR = data;
}

unsigned char receiveUSART(void) {
	/* Wait for data to be received */
	while (!(UCSRA & (1 << RXC)))
		;

	/* Get and return received data from buffer */
	return UDR;
}

unsigned char checkForNewCommand() {
	if (!(UCSRA & (1 << RXC))) {
		return 0;
	}

	unsigned char b;

	for (uint8_t i = 0; i < 4; i++) {
		b = receiveUSART();
		cmd[i] = b;
	}

	return 1;
}


/********* PWM CODE **************/

ISR(TIMER1_COMPA_vect) {

	if (prepareNextData == 1 && actualBit == 0) {
		setSolenoidWait();
		return;
	}

	if (actualBit == 0) {
		pwmQueueIdx = (pwmQueueIdx + 1) % 2;
		prepareNextData = 1;
	}

	if (actualBit > 53) {
		setSolenoidWait();
		actualBit = 0;
		return;
	}

	unsigned char b = commandQueue[pwmQueueIdx][actualBit];

	if (b == 0) {
		pwm_mode == MODE_SOLENOID ? setSolenoid0() : setLoco0();
	} else if (b == 1) {
		pwm_mode == MODE_SOLENOID ? setSolenoid1() : setLoco1();
	} else if (b == 2) {
		pwm_mode == MODE_SOLENOID ? setSolenoidWait() : setLocoWait();
	}

	actualBit++;

}


void initPWM() {

	DDRB |= (1 << PB1);

	TCCR1B = (1 << WGM13) | (1 << WGM12) | (1 << CS11);
	ICR1H = 0x00;
	ICR1L = 0xCC; // counting to TOP takes

	TIMSK |= (1 << OCIE1A);

	OCR1AH = 0x00;
	TCCR1A = (1 << WGM11); //fast PWM with Prescaler = 8
}

void setPWMOutput(uint16_t duty) {
	PORTC |= (1 << PC2);
	OCR1A = duty;
}

void setSolenoid0() {
	PORTC |= (1 << PC2);
	OCR1A = 24;
}

void setSolenoid1() {
	PORTC |= (1 << PC2);
	OCR1A = 180;
}

void setSolenoidWait() {
	PORTC &= ~(1 << PC2);
	OCR1A = 0;
}

void setLoco0() {
	PORTC |= (1 << PC2);
	OCR1A = 48;

}
void setLoco1() {
	PORTC |= (1 << PC2);
	OCR1A = 260;
}

void setLocoWait() {
	PORTC &= ~(1 << PC2);
	OCR1A = 0;
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
}


#define TEST -1
#if TEST == 0
//Pause, Bin 1, Pause, Bin 0,...
ISR(TIMER1_COMPA_vect) {
	if (cycle == 0) {
		setSolenoidWait();
		cycle = 1;
	} else if (cycle == 1) {
		setSolenoid1();
		cycle = 2;
	} else if (cycle == 2) {
		setSolenoidWait();
		cycle = 3;
	} else if (cycle == 3) {
		setSolenoid0();
		cycle = 0;
	}
}

#elif TEST == 1
//Pause, Tern 0, Pause,...
ISR(TIMER1_COMPA_vect) {
	if (cycle == 0) {
		setSolenoidWait();
		cycle = 1;
	} else if (cycle == 1) {
		setSolenoid0();
		cycle = 2;
	} else if (cycle == 2) {
		setSolenoid0();
		cycle = 3;
	} else if (cycle == 3) {
		setSolenoidWait();
		cycle = 0;
	}
}
#elif TEST == 2

//Pause, Tern 1, Pause,...
ISR(TIMER1_COMPA_vect) {
	if (cycle == 0) {
		setSolenoidWait();
		cycle = 1;
	} else if (cycle == 1) {
		setSolenoid1();
		cycle = 2;
	} else if (cycle == 2) {
		setSolenoid1();
		cycle = 3;
	} else if (cycle == 3) {
		setSolenoidWait();
		cycle = 0;
	}
}

#elif TEST == 3
//Pause, Tern OPEN, Pause,...
ISR(TIMER1_COMPA_vect) {
	if (cycle == 0) {
		setSolenoidWait();
		cycle = 1;
	} else if (cycle == 1) {
		setSolenoid1();
		cycle = 2;
	} else if (cycle == 2) {
		setSolenoid0();
		cycle = 0;
	}
}
#elif TEST == 4

ISR(TIMER1_COMPA_vect) {

//HUGE SWITCH JUST FOR TESTING!!!!
	switch (cycle) {

		case 0:
		setSolenoid1(); // ADDR1 OPEN
		break;
		case 1:
		setSolenoid0();
		break;
		case 2:
		setSolenoid1();// ADDR2 1
		break;
		case 3:
		setSolenoid1();
		break;
		case 4:
		setSolenoid0();// ADDR3 0
		break;
		case 5:
		setSolenoid0();
		break;
		case 6:
		setSolenoid1();// ADDR4 1
		break;
		case 7:
		setSolenoid1();
		break;
		case 8:
		setSolenoid0();// ADDR5 0
		break;
		case 9:
		setSolenoid0();
		break;
		case 10:
		setSolenoid0();// D1 0
		break;
		case 11:
		setSolenoid0();
		break;
		case 12:
		setSolenoid0();// D2 0
		break;
		case 13:
		setSolenoid0();
		break;
		case 14:
		setSolenoid0();// D3 0
		break;
		case 15:
		setSolenoid0();
		break;
		case 16:
		setSolenoid1();// ON 1
		break;
		case 17:
		setSolenoid1();
		break;
		case 18:
		setSolenoidWait();
		break;
		case 19:
		setSolenoidWait();
		break;
		case 20:
		setSolenoidWait();
		break;
		case 21:
		setSolenoidWait();
		break;
		case 22:
		setSolenoidWait();
		break;
		case 23:
		setSolenoidWait();
		cycle = 0;
		//STOP PWM
//		TCCR1A &= ~(1 << COM1A1);
		return;
	}
	cycle++;
}

#elif TEST == 5

ISR(TIMER1_COMPA_vect) {

//HUGE SWITCH JUST FOR TESTING!!!!
	switch (cycle) {

		case 0:
		setSolenoid1(); // ADDR1 OPEN
		break;
		case 1:
		setSolenoid0();
		break;
		case 2:
		setSolenoid1();// ADDR2 1
		break;
		case 3:
		setSolenoid1();
		break;
		case 4:
		setSolenoid0();// ADDR3 0
		break;
		case 5:
		setSolenoid0();
		break;
		case 6:
		setSolenoid1();// ADDR4 1
		break;
		case 7:
		setSolenoid1();
		break;
		case 8:
		setSolenoid0();// ADDR5 0
		break;
		case 9:
		setSolenoid0();
		break;
		case 10:
		setSolenoid1();// D1 0
		break;
		case 11:
		setSolenoid1();
		break;
		case 12:
		setSolenoid0();// D2 0
		break;
		case 13:
		setSolenoid0();
		break;
		case 14:
		setSolenoid0();// D3 0
		break;
		case 15:
		setSolenoid0();
		break;
		case 16:
		setSolenoid1();// ON 1
		break;
		case 17:
		setSolenoid1();
		break;
		case 18:
		setSolenoidWait();
		break;
		case 19:
		setSolenoidWait();
		break;
		case 20:
		setSolenoidWait();
		break;
		case 21:
		setSolenoidWait();
		break;
		case 22:
		setSolenoidWait();
		break;
		case 23:
		setSolenoidWait();
		cycle = 0;
		return;
	}
	cycle++;
}

#elif TEST == 6

ISR(TIMER1_COMPA_vect) {

//HUGE SWITCH JUST FOR TESTING!!!!
	switch (cycle) {

		case 0:
		setSolenoid1(); // ADDR1 OPEN
		break;
		case 1:
		setSolenoid0();
		break;
		case 2:
		setSolenoid1();// ADDR2 1
		break;
		case 3:
		setSolenoid1();
		break;
		case 4:
		setSolenoid0();// ADDR3 0
		break;
		case 5:
		setSolenoid0();
		break;
		case 6:
		setSolenoid1();// ADDR4 1
		break;
		case 7:
		setSolenoid1();
		break;
		case 8:
		setSolenoid0();// ADDR5 0
		break;
		case 9:
		setSolenoid0();
		break;
		case 10:
		if (!previousPin) {
			setSolenoid1(); // D1 1
		} else {
			setSolenoid0(); // D1 0
		}
		break;
		case 11:
		if (!previousPin) {
			setSolenoid1(); // D1 1
		} else {
			setSolenoid0(); // D1 0
		}
		break;
		case 12:
		setSolenoid0(); // D2 0
		break;
		case 13:
		setSolenoid0();
		break;
		case 14:
		setSolenoid0();// D3 0
		break;
		case 15:
		setSolenoid0();
		break;
		case 16:
		setSolenoid0();// ON 1
		break;
		case 17:
		setSolenoid0();
		break;
		case 18:
		setSolenoidWait();
		break;
		case 19:
		setSolenoidWait();
		break;
		case 20:
		setSolenoidWait();
		break;
		case 21:
		setSolenoidWait();
		break;
		case 22:
		setSolenoidWait();
		break;
		case 23:
		setSolenoidWait();
		case 24:
		setSolenoid1();// ADDR1 OPEN
		break;
		case 25:
		setSolenoid0();
		break;
		case 26:
		setSolenoid1();// ADDR2 1
		break;
		case 27:
		setSolenoid1();
		break;
		case 28:
		setSolenoid0();// ADDR3 0
		break;
		case 29:
		setSolenoid0();
		break;
		case 30:
		setSolenoid1();// ADDR4 1
		break;
		case 31:
		setSolenoid1();
		break;
		case 32:
		setSolenoid0();// ADDR5 0
		break;
		case 33:
		setSolenoid0();
		break;
		case 34:
		if (previousPin) {
			setSolenoid1(); // D1 1
		} else {
			setSolenoid0(); // D1 0
		}
		break;
		case 35:
		if (previousPin) {
			setSolenoid1(); // D1 1
		} else {
			setSolenoid0(); // D1 0
		}
		break;
		case 36:
		setSolenoid0(); // D2 0
		break;
		case 37:
		setSolenoid0();
		break;
		case 38:
		setSolenoid0();// D3 0
		break;
		case 39:
		setSolenoid0();
		break;
		case 40:
		setSolenoid1();// ON 1
		break;
		case 41:
		setSolenoid1();
		break;
		case 42:
		setSolenoidWait();
		break;
		case 43:
		setSolenoidWait();
		break;
		case 44:
		setSolenoidWait();
		break;
		case 45:
		setSolenoidWait();
		break;
		case 46:
		setSolenoidWait();
		break;
		case 47:
		setSolenoidWait();
		cycle = 0;
		return;
	}
	cycle++;
}
#endif
/*testData1[0] = 1;
 testData1[1] = 0;
 testData1[2] = 1;
 testData1[3] = 1;
 testData1[4] = 0;
 testData1[5] = 0;
 testData1[6] = 1;
 testData1[7] = 1;
 testData1[8] = 0;
 testData1[9] = 0;
 testData1[10] = 0;
 testData1[11] = 0;
 testData1[12] = 0;
 testData1[13] = 0;
 testData1[14] = 0;
 testData1[15] = 0;
 testData1[16] = 1;
 testData1[17] = 1;
 testData1[18] = 2;
 testData1[19] = 2;
 testData1[20] = 2;
 testData1[21] = 2;
 testData1[22] = 2;
 testData1[23] = 2;

 testData1[24] = testData1[0];
 testData1[25] = testData1[1];
 testData1[26] = testData1[2];
 testData1[27] = testData1[3];
 testData1[28] = testData1[4];
 testData1[29] = testData1[5];
 testData1[30] = testData1[6];
 testData1[31] = testData1[7];
 testData1[32] = testData1[8];
 testData1[33] = testData1[9];
 testData1[34] = testData1[10];
 testData1[35] = testData1[11];
 testData1[36] = testData1[12];
 testData1[37] = testData1[13];
 testData1[38] = testData1[14];
 testData1[39] = testData1[15];
 testData1[40] = testData1[16];
 testData1[41] = testData1[17];
 testData1[42] = testData1[18];
 testData1[43] = testData1[19];
 testData1[44] = testData1[20];
 testData1[45] = testData1[21];
 testData1[46] = testData1[22];
 testData1[47] = testData1[23];

 testData1[0 + 48] = 1;
 testData1[1 + 48] = 0;
 testData1[2 + 48] = 1;
 testData1[3 + 48] = 1;
 testData1[4 + 48] = 0;
 testData1[5 + 48] = 0;
 testData1[6 + 48] = 1;
 testData1[7 + 48] = 1;
 testData1[8 + 48] = 0;
 testData1[9 + 48] = 0;
 testData1[10 + 48] = 1;
 testData1[11 + 48] = 1;
 testData1[12 + 48] = 0;
 testData1[13 + 48] = 0;
 testData1[14 + 48] = 0;
 testData1[15 + 48] = 0;
 testData1[16 + 48] = 1;
 testData1[17 + 48] = 1;
 testData1[18 + 48] = 2;
 testData1[19 + 48] = 2;
 testData1[20 + 48] = 2;
 testData1[21 + 48] = 2;
 testData1[22 + 48] = 2;
 testData1[23 + 48] = 2;

 testData1[24 + 48] = testData1[0 + 48];
 testData1[25 + 48] = testData1[1 + 48];
 testData1[26 + 48] = testData1[2 + 48];
 testData1[27 + 48] = testData1[3 + 48];
 testData1[28 + 48] = testData1[4 + 48];
 testData1[29 + 48] = testData1[5 + 48];
 testData1[30 + 48] = testData1[6 + 48];
 testData1[31 + 48] = testData1[7 + 48];
 testData1[32 + 48] = testData1[8 + 48];
 testData1[33 + 48] = testData1[9 + 48];
 testData1[34 + 48] = testData1[10 + 48];
 testData1[35 + 48] = testData1[11 + 48];
 testData1[36 + 48] = testData1[12 + 48];
 testData1[37 + 48] = testData1[13 + 48];
 testData1[38 + 48] = testData1[14 + 48];
 testData1[39 + 48] = testData1[15 + 48];
 testData1[40 + 48] = testData1[16 + 48];
 testData1[41 + 48] = testData1[17 + 48];
 testData1[42 + 48] = testData1[18 + 48];
 testData1[43 + 48] = testData1[19 + 48];
 testData1[44 + 48] = testData1[20 + 48];
 testData1[45 + 48] = testData1[21 + 48];
 testData1[46 + 48] = testData1[22 + 48];
 testData1[47 + 48] = testData1[23 + 48];*/

/*unsigned char data1[13] = { 0, 2, 1, 0, 1, 0, 0, 0, 0, 1, 3, 3, 3 };
 unsigned char data2[13] = { 0, 2, 1, 0, 1, 0, 1, 0, 0, 1, 3, 3, 3 };
 unsigned char data3[13] = { 0, 2, 1, 0, 1, 0, 0, 1, 0, 1, 3, 3, 3 };
 unsigned char data4[13] = { 0, 2, 1, 0, 1, 0, 1, 1, 0, 1, 3, 3, 3 };

 _delay_ms(500);

 offset = 48;
 fillData(data1);
 actualBit = 0;

 _delay_ms(500);

 offset = 48;
 fillData(data2);
 actualBit = 0;

 _delay_ms(500);
 offset = 0;
 fillData(data3);
 actualBit = 0;

 _delay_ms(500);

 offset = 48;
 fillData(data4);
 actualBit = 0;*/

/*_delay_ms(1000);
 unsigned char dataLoco[13] = { 1, 0, 2, 2, 0, 0, 1, 1, 0, 0, 3, 3, 3 };

 offset = 0;
 fillData(dataLoco);

 if (dataLoco[0] == MODE_SOLENOID) {
 //SOLENDOID FREQUENCY
 TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
 ICR1H = 0x00;
 ICR1L = 0xCC; // counting to TOP takes
 pwm_mode = MODE_SOLENOID;
 TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

 actualBit = 0;

 } else {
 //LOCO FREQUENCY
 TCCR1A &= ~(1 << COM1A1);
 ICR1 = 408;
 pwm_mode = MODE_LOCO;
 TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

 actualBit = 0;

 while (actualBit != -1) {
 }
 _delay_ms(2);


 }*/

//TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
/*enqueueData(data1);
 enqueueData(data3);
 actualBit = 0;
 TCCR1A |= (1 << COM1A1); // ACTIVATE PWM


 while (1) {
 }*/

/*void enqueueData(unsigned char* data) {

 for (uint8_t i = 0; i < 12; i++) {
 uint8_t pos = (2 * i);
 uint8_t pos1 = pos + 1;
 uint8_t pos2 = pos + 24;
 uint8_t pos21 = pos1 + 24;

 uint8_t j = i + 1;
 unsigned char d = data[j];
 if (d == 0) {
 // ternary 0
 commandQueue[0][pos] = 0;
 commandQueue[0][pos1] = 0;
 commandQueue[0][pos2] = 0;
 commandQueue[0][pos21] = 0;
 } else if (d == 1) {
 // ternary 1
 commandQueue[0][pos] = 1;
 commandQueue[0][pos1] = 1;
 commandQueue[0][pos2] = 1;
 commandQueue[0][pos21] = 1;
 } else if (d == 2) {
 // ternary open
 commandQueue[0][pos] = 1;
 commandQueue[0][pos1] = 0;
 commandQueue[0][pos2] = 1;
 commandQueue[0][pos21] = 0;
 } else {
 // ternary wait
 commandQueue[0][pos] = 2;
 commandQueue[0][pos1] = 2;
 commandQueue[0][pos2] = 2;
 commandQueue[0][pos21] = 2;
 }
 }

 for (uint8_t i = 48; i < 54; i++) {

 commandQueue[0][i] = 2;
 }
 //cmdQueueIndex = (cmdQueueIndex + 1) % MAX_COMMAND_QUEUE;
 }*/

/*commandQueue[queueIdxLoc][0] = 1;
 commandQueue[queueIdxLoc][1] = 0;
 commandQueue[queueIdxLoc][2] = 1;
 commandQueue[queueIdxLoc][3] = 1;
 commandQueue[queueIdxLoc][4] = 0;
 commandQueue[queueIdxLoc][5] = 0;
 commandQueue[queueIdxLoc][6] = 1;
 commandQueue[queueIdxLoc][7] = 1;
 commandQueue[queueIdxLoc][8] = 0;
 commandQueue[queueIdxLoc][9] = 0;
 commandQueue[queueIdxLoc][10] = 0;
 commandQueue[queueIdxLoc][11] = 0;
 commandQueue[queueIdxLoc][12] = 0;
 commandQueue[queueIdxLoc][13] = 0;
 commandQueue[queueIdxLoc][14] = 0;
 commandQueue[queueIdxLoc][15] = 0;
 commandQueue[queueIdxLoc][16] = 1;
 commandQueue[queueIdxLoc][17] = 1;
 commandQueue[queueIdxLoc][18] = 2;
 commandQueue[queueIdxLoc][19] = 2;
 commandQueue[queueIdxLoc][20] = 2;
 commandQueue[queueIdxLoc][21] = 2;
 commandQueue[queueIdxLoc][22] = 2;
 commandQueue[queueIdxLoc][23] = 2;

 commandQueue[queueIdxLoc][24] = 1;
 commandQueue[queueIdxLoc][25] = 0;
 commandQueue[queueIdxLoc][26] = 1;
 commandQueue[queueIdxLoc][27] = 1;
 commandQueue[queueIdxLoc][28] = 0;
 commandQueue[queueIdxLoc][29] = 0;
 commandQueue[queueIdxLoc][30] = 1;
 commandQueue[queueIdxLoc][31] = 1;
 commandQueue[queueIdxLoc][32] = 0;
 commandQueue[queueIdxLoc][33] = 0;
 commandQueue[queueIdxLoc][34] = 0;
 commandQueue[queueIdxLoc][35] = 0;
 commandQueue[queueIdxLoc][36] = 0;
 commandQueue[queueIdxLoc][37] = 0;
 commandQueue[queueIdxLoc][38] = 0;
 commandQueue[queueIdxLoc][39] = 0;
 commandQueue[queueIdxLoc][40] = 1;
 commandQueue[queueIdxLoc][41] = 1;
 commandQueue[queueIdxLoc][42] = 2;
 commandQueue[queueIdxLoc][43] = 2;
 commandQueue[queueIdxLoc][44] = 2;
 commandQueue[queueIdxLoc][45] = 2;
 commandQueue[queueIdxLoc][46] = 2;
 commandQueue[queueIdxLoc][47] = 2;
 commandQueue[queueIdxLoc][48] = 2;
 commandQueue[queueIdxLoc][49] = 2;
 commandQueue[queueIdxLoc][50] = 2;
 commandQueue[queueIdxLoc][51] = 2;
 commandQueue[queueIdxLoc][52] = 2;
 commandQueue[queueIdxLoc][53] = 2;*/
