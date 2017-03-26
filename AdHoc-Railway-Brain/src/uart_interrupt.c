/*
 * uart_interrupt.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include <avr/io.h>
#include <avr/interrupt.h>
#include "uart_interrupt.h"
#include "fifo.h" // erklärt im Artikel "FIFO mit avr-gcc"




//#define BAUDRATE 57600
//#define BAUDRATE 115200
//#define BAUDRATE 230400

// FIFO-Objekte und Puffer für die Ein- und Ausgabe
#define BUFSIZE_IN  100
uint8_t inbuf[BUFSIZE_IN];
fifo_t infifo;

#define BUFSIZE_OUT 200
uint8_t outbuf[BUFSIZE_OUT];
fifo_t outfifo;


volatile unsigned char cmdReceived = 0;
volatile unsigned char infifoNoOverflow = 1;
volatile unsigned char waitforRTS = 0;

extern unsigned char receivedCmdString[128];



unsigned char checkForNewCommand() {

	// Testen: infifoBufferOverflow
	if (infifoNoOverflow == 0){
		uart_puts("rxBuf OF");
		uart_putc('\r');
		infifoNoOverflow = 1;
	}

	// Testen: RTS
	if ((waitforRTS == 1) && !(UART_INPUT_PINS & (1 << UART_RTS_INPUT_PIN))){
		waitforRTS = 0;
	}

	if (cmdReceived == 0)
		return 0;

	unsigned char c = uart_getc();
	uint8_t counter = 0;

	while (c != 0x0d) {
		receivedCmdString[counter] = c;
		counter++;

		//nötig? richtig?
		if (counter >= BUFSIZE_IN){
			fifo_init(&infifo, inbuf, BUFSIZE_IN);
			break;
		}

		c = uart_getc();
	}

	receivedCmdString[counter] = 0x0;

	cmdReceived--;

	if (receivedCmdString[0] != 'X')
		return INVALID;

	if (receivedCmdString[1] > 0x80) {
		return BINARY_MODE;
	}

	return ASCII_MODE;

}




void uart_init(void) {
	uint8_t sreg = SREG;  //AVR-Status Register

	//Berechung der Baudrate für UBR, Rechnung nicht korrekt, fehlt richtiges Runden am Schluss
	// ACHTUNG: wenn U2X0 = 1 wird Baudrate verdoppelt
	//uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU / (16UL * BAUDRATE) - 1);
	//uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU / (16UL * BAUDRATE) );

	//m2
	//UART CTS Output-Config
	UART_DDR |= (1 << UART_CTS_OUTPUT_PIN);
	//UART RTS Input-Config
	UART_DDR &= ~(1 << UART_RTS_INPUT_PIN);
//	UART_OUTPUT_PORT |= (1 << UART_RTS_INPUT_PIN); // Pull-Up on


	//FIXME hardcoded
	uint16_t ubrr = 10; //230.4 kBaud @ 20 MHz und U2X0 = 1

	// UART Baud Rate Registers
	UBRR0H = (uint8_t) (ubrr >> 8);
	UBRR0L = (uint8_t) (ubrr);

	// Interrupts kurz deaktivieren
	cli();

	// UART Receive-Interrupt aktivieren, Receiver und Transmitter einschalten
	UCSR0B = (1 << RXCIE0) | (1 << RXEN0) | (1 << TXEN0);

	// UART Transmit-Interrupt deaktivieren
	UCSR0B &= ~(1 << TXCIE0);

	//USART Data Register Empty Interrupt aktivieren
	UCSR0B |= (1 << UDRIE0);

	// UART Mode: asynchronous
	UCSR0C &= ~(1 << UMSEL00);
	UCSR0C &= ~(1 << UMSEL01);

	// UART Parity Mode: None
	UCSR0C &= ~(1 << UPM00);
	UCSR0C &= ~(1 << UPM01);

	// UART Stop Bit: 1
	UCSR0C &= ~(1 << USBS0);

	// UART Character Size: 8 Bit
	UCSR0B &= ~(1 << UCSZ02);
	UCSR0C = (1 << UCSZ01) | (1 << UCSZ00);

	// UART Clock Polarity: only for synchronous mode
	UCSR0C &= ~(1 << UCPOL0);

	// UART Clearing Transmit Complete Flag, Double USART Transmission Speed mit U2X0
	UCSR0A = (1 << TXC0) | (1 << U2X0);

	//Data Register Empty Flag zurücksetzen
	UCSR0A &= ~(1 << UDRE0);

	// UART Reset Frame Error Flag
	UCSR0A &= ~(1 << FE0);

	// UART Reset Data OverRun Flag
	UCSR0A &= ~(1 << DOR0);

	// UART Reset Parity Error Flag
	UCSR0A &= ~(1 << UPE0);

	// UART Multi-processor Communication Mode deaktivieren
	UCSR0A &= ~(1 << MPCM0);


	// Global Interrupt-Flag wieder herstellen
	SREG = sreg;

	// FIFOs für Ein- und Ausgabe initialisieren
	fifo_init(&infifo, inbuf, BUFSIZE_IN);
	fifo_init(&outfifo, outbuf, BUFSIZE_OUT);


}




// Empfangene Zeichen werden in die Eingabgs-FIFO gespeichert und warten dort
// wenn CR (0x0D) empfangen, Kommando komplett
ISR (USART0_RX_vect) {
	unsigned char c = UDR0;

	infifoNoOverflow = _inline_infifo_put(&infifo, c);

	if ((c == 0x0D) && (infifoNoOverflow == 1)) { //complete command received => CR => 0x0D
		cmdReceived++;
	}
}




// Transmit Data-Register Empty Interrupt
// Ein Zeichen aus der Ausgabe-FIFO lesen und ausgeben
// Ist das Zeichen fertig ausgegeben, wird ein neuer SIG_UART_DATA-IRQ getriggert
// Ist die FIFO leer, deaktiviert die ISR ihren eigenen IRQ.
ISR (USART0_UDRE_vect){
	if (outfifo.count > 0){
		UDR0 = _inline_outfifo_get(&outfifo);
	} else {
		UCSR0B &= ~(1 << UDRIE0);
	}
}




void replys(const char* s) {
	uart_puts(s);
	uart_putc('\r');
}




int uart_puts(const char* str) {
	uint8_t i = 0;
	int ret = 0;
	while (waitforRTS);
	while (*(str + i) != 0x0) {
		while (ret == 0){
			ret = outfifo_put(&outfifo, *(str + i));
		}
		ret = 0;
		UCSR0B |= (1 << UDRIE0);
		i++;
	}

	return ret;
}


int uart_putc(const uint8_t c) {
	int ret = 0;
	while (waitforRTS);
	while (ret == 0){
		ret = outfifo_put(&outfifo, c);
	}
	UCSR0B |= (1 << UDRIE0);

	return ret;
}


uint8_t uart_getc(void) {
	return infifo_get(&infifo);
}


uint8_t uart_get_inbuf_size(void) {
	return fifo_get_size(&infifo);
}




