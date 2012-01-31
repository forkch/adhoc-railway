#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define FOSC 16000000
#define BAUD 115200
#define MYUBRR FOSC/16/BAUD-1

#define MODE_SOLENOID 0
#define MODE_LOCO 1

#define GREEN_LED PC0
#define RED_LED PC1
unsigned char pwm_mode = 0;
unsigned char isLocoCommand = 1;

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

uint8_t currentLocoIdx = 79;
/*typedef struct LocoRefresh {
 struct LocoData* locoData;
 struct LocoRefresh* next;
 struct LocoRefresh* previous;
 };

 struct LocoRefresh locoRefreshStart;
 struct LocoRefresh locoRefreshEnd;
 */

struct LocoData locoData[80];
struct LocoData* newLoco = 0;

typedef struct SolenoidData {
	unsigned char address;
	unsigned char port;
	char active :1;
	char timerDetected :1;
	char deactivate :1;
};

uint8_t debugCounter = 0;

struct SolenoidData solenoidData[MAX_SOLENOID_QUEUE];

uint8_t solenoidDataIdxInsert = 0;
uint8_t solenoidDataIdxPop = 0;

unsigned char portData[8];
unsigned char deltaSpeedData[16];

volatile unsigned char pwmQueueIdx = 0;
unsigned char commandQueue[2][54];

volatile uint8_t actualBit = 0;

unsigned char cmd[5];
unsigned char prepareNextData = 1;
uint8_t stop = 0;
uint8_t newSolenoid = 0;
uint8_t maetthusEnpreller = 0;

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

	//Loco FREQUENCY
	pwm_mode = MODE_LOCO;
	TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
	ICR1 = 0x192; // counting to TOP takes
	TCCR1A |= (1 << COM1A1); // ACTIVATE PWM

	sei();

	//Do this forever
	while (1) {

		/*unsigned char cmdAvail = 0;
		if (PINC & (1 << PC3) && stop == 0) {
			cmdAvail = 1;
			cmd[0] = 'w';
			cmd[1] = 0x20;
			cmd[2] = 0x0;
			stop = 1;
			_delay_ms(10);
		} else if ((PINC & (1 << PC3)) == 0 && stop == 1) {
			stop = 0;
			_delay_ms(10);
		}*/

		debugCounter++;

		unsigned char cmdAvail = checkForNewCommand();

		if (cmdAvail == 1) {
			processData(cmd);
		}

		/*transmitUSART('m');
		 transmitUSART(' ');
		 transmitUSART(debugCounter);
		 transmitUSART(' ');
		 transmitUSART(' ');
		 transmitUSART(prepareNextData + 48);
		 transmitUSART('\n');*/

		if (prepareNextData == 1)
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

		newSolenoid = 1;
		/*transmitUSART('w');
		 transmitUSART('\n');*/
	} else if (cmd[0] == 'l') {
		// loco
		//unsigned char functions = 0;
		//unsigned char config = cmd[4];

		unsigned char address = cmd[1];
		int8_t speed = cmd[2];
		uint8_t t = address - 1;
		//locoData[t].active = (config >> 7) & 1;
		//locoData[t].isNewProtocol = (config >> 6) & 1;
		locoData[t].active = 1;
		locoData[t].isNewProtocol = 0;
		if (locoData[t].isNewProtocol) {
			// NEW protocol
		} else {
			// OLD protocol (DELTA)

			if (speed < 0) {
				locoData[t].speed = deltaSpeedData[1];
			} else if (speed == 0) {
				locoData[t].speed = deltaSpeedData[0];
			} else {
				locoData[t].speed = deltaSpeedData[speed + 2];
			}
		}
		newLoco = &locoData[t];

		transmitUSART('l');
		transmitUSART('\n');
	}
}

void prepareDataForPWM() {

	unsigned char queueIdxLoc = 0; // = (pwmQueueIdx + 1) % 2;

	//if (solenoidDataIdxInsert != solenoidDataIdxPop) {
	if (newSolenoid == 1) {

		// there is a new solenoid to handle!!
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

		//solenoidData[solenoidDataIdxPop].active = 1;
		//solenoidDataIdxPop++;
		//solenoidDataIdxPop = solenoidDataIdxPop % MAX_SOLENOID_QUEUE;
		pwm_mode = MODE_SOLENOID;
		isLocoCommand = 0;
		newSolenoid = 0;
	} else {
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
				commandQueue[queueIdxLoc][24 + i] =
						commandQueue[queueIdxLoc][i];

			commandQueue[queueIdxLoc][48] = 2;
			commandQueue[queueIdxLoc][49] = 2;
			commandQueue[queueIdxLoc][50] = 2;
			commandQueue[queueIdxLoc][51] = 2;
			commandQueue[queueIdxLoc][52] = 2;
			commandQueue[queueIdxLoc][53] = 2;
			pwm_mode = MODE_LOCO;
			isLocoCommand = 1;
		}
	}

	prepareNextData = 0;

}

void flash_twice_green() {
	PORTC |= (1 << GREEN_LED);
	_delay_ms(100);
	PORTC &= ~(1 << GREEN_LED);
	_delay_ms(100);
	PORTC |= (1 << GREEN_LED);
	_delay_ms(100);
	PORTC &= ~(1 << GREEN_LED);
}
void flash_once_green() {
	PORTC |= (1 << GREEN_LED);
	_delay_ms(200);
	PORTC &= ~(1 << GREEN_LED);
	_delay_ms(200);
}
void flash_once_green_quick() {
	PORTC |= (1 << GREEN_LED);
	PORTC &= ~(1 << GREEN_LED);
}
void flash_once_red_quick() {
	PORTC |= (1 << RED_LED);
	PORTC &= ~(1 << RED_LED);
}
void flash_twice_red() {
	PORTC |= (1 << RED_LED);
	_delay_ms(100);
	PORTC &= ~(1 << RED_LED);
	_delay_ms(100);
	PORTC |= (1 << RED_LED);
	_delay_ms(100);
	PORTC &= ~(1 << RED_LED);
	_delay_ms(100);
}
void flash_once_red() {
	PORTC |= (1 << RED_LED);
	_delay_ms(100);
	PORTC &= ~(1 << RED_LED);
}

void red_led_on() {
	PORTC |= (1 << RED_LED);
}
void red_led_off() {
	PORTC &= ~(1 << RED_LED);
}
void green_led_on() {
	PORTC |= (1 << GREEN_LED);
}
void green_led_off() {
	PORTC &= ~(1 << GREEN_LED);
}
/********* UART CODE **************/

void initUSART(unsigned int ubrr) {
	/* Set baud rate */
	UBRRH = (unsigned char) (ubrr >> 8);
	UBRRL = (unsigned char) ubrr;

	/* Enable receiver and transmitter */UCSRB = (1 << RXEN) | (1 << TXEN);
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

	unsigned char b = receiveUSART();
	cmd[0] = b;
	if (b == 'w') {
		for (uint8_t i = 1; i < 3; i++) {
			b = receiveUSART();
			cmd[i] = b;
		}
	} else if (b == 'l') {
		for (uint8_t i = 1; i < 5; i++) {
			b = receiveUSART();
			cmd[i] = b;
		}
	}

	return 1;
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
			//SOLENDOID FREQUENCY
			//TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
			ICR1H = 0x00;
			ICR1L = 0xCC; // counting to TOP takes
			//TCCR1A |= (1 << COM1A1); // ACTIVATE PWM
		} else {
			//LOCO FREQUENCY
			//TCCR1A &= ~(1 << COM1A1); // DEACTIVATE PWM
			ICR1 = 0x198;
			//TCCR1A |= (1 << COM1A1); // ACTIVATE PWM
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

	/*locoRefreshStart.locoData = 0;
	 locoRefreshStart.next = &locoRefreshEnd;
	 locoRefreshEnd.previous = &locoRefreshStart;*/

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
	}
	locoData[0].active = 1;
}

