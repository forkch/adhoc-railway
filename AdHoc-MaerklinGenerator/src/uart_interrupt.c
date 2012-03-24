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

#define BUFSIZE_IN  64
uint8_t inbuf[BUFSIZE_IN];
fifo_t infifo;

#define BUFSIZE_OUT 128
uint8_t outbuf[BUFSIZE_OUT];
fifo_t outfifo;

typedef enum IB_CMD {
	BINARY_MODE, ASCII_MODE, INVALID
};

volatile unsigned char cmdReceived = 0;

extern unsigned char cmd[128];

unsigned char checkForNewCommand() {

	if (!cmdReceived)
		return 0;

	unsigned char c = uart_getc_wait();
	;
	uint8_t counter = 0;
	while (c != 0x0d) {
		cmd[counter] = c;
		counter++;
		c = uart_getc_wait();
	}
	cmdReceived = 0;

	cmd[counter] = 0x0;
	if (cmd[0] != 'X')
		return INVALID;

	if (cmd[1] > 0x80) {
		return BINARY_MODE;
	}
	return ASCII_MODE;

}

void uart_init(void) {
	uint8_t sreg = SREG;
	//Berechung der Baudrate f�r UBR, Rechnung nicht korrekt, fehlt richtiges Runden am Schluss
	// ACHTUNG: wenn U2X0 = 1 wird Baudrate verdoppelt
	//uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU / (16UL * BAUDRATE) - 1);
//	uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU / (16UL * BAUDRATE) );

	//FIXME hardcoded
	uint16_t ubrr = 8;

	UBRR0H = (uint8_t) (ubrr >> 8);
	UBRR0L = (uint8_t) (ubrr);

	// Interrupts kurz deaktivieren
	cli();

	// UART Receiver und Transmitter anschalten, Receive-Interrupt aktivieren, Transmit-Interrupt aktivieren
	// Data mode 8N1, asynchron
	UCSR0B = (1 << RXEN0) | (1 << TXEN0) | (1 << RXCIE0);

	// for ATMega8
	//UCSR0C = (1 << URSEL);
	UCSR0C = (1 << UCSZ01) | (1 << UCSZ00);

	// Flush Receive-Buffer (entfernen evtl. vorhandener ungültiger Werte)
	do {
		// UDR auslesen (Wert wird nicht verwendet)
		UDR0;
	} while (UCSR0A & (1 << RXC0));

	// Rücksetzen von Receive und Transmit Complete-Flags, Double USART Transmission Speed mit U2X0
	UCSR0A = (1 << RXC0) | (1 << TXC0) | (1 << U2X0);
	UCSR0B |= (1 << UDRIE0);
	// Global Interrupt-Flag wieder herstellen
	SREG = sreg;

	// FIFOs für Ein- und Ausgabe initialisieren
	fifo_init(&infifo, inbuf, BUFSIZE_IN);
	fifo_init(&outfifo, outbuf, BUFSIZE_OUT);
}

// Empfangene Zeichen werden in die Eingabgs-FIFO gespeichert und warten dort
ISR (USART0_RX_vect) {
	unsigned char c = UDR0;

	_inline_fifo_put(&infifo, c);

	if (c == 0x0D) { //complete command received
		cmdReceived = 1;
	}

	//TODO: XOff when fifo is nearly full!!
}

// Ein Zeichen aus der Ausgabe-FIFO lesen und ausgeben
// Ist das Zeichen fertig ausgegeben, wird ein neuer SIG_UART_DATA-IRQ getriggert
// Ist die FIFO leer, deaktiviert die ISR ihren eigenen IRQ.
ISR (USART0_UDRE_vect){
	if (outfifo.count > 0)
		UDR0 = _inline_fifo_get(&outfifo);
	else
		UCSR0B &= ~(1 << UDRIE0);
}

void replys(const char* s) {
	uart_puts(s);
	uart_putc('\r');
	uart_flush();
}
void reply(const uint8_t c) {
	uart_putc(c);
}

int uart_puts(const char* str) {
	uint8_t i = 0;
	int ret = 0;
	while (*(str + i) != 0x0) {
		ret = fifo_put(&outfifo, *(str + i));
		i++;
	}
	UCSR0B |= (1 << UDRIE0);
	return ret;
}

int uart_putc(const uint8_t c) {
	int ret = fifo_put(&outfifo, c);
	UCSR0B |= (1 << UDRIE0);
	return ret;
}

int uart_getc_nowait(void) {
	return fifo_get_nowait(&infifo);
}

uint8_t uart_getc_wait(void) {
	return fifo_get_wait(&infifo);
}

uint8_t uart_get_inbuf_size(void) {
	return fifo_get_size(&infifo);
}

uint8_t uart_peek_inbuf(void) {
	return fifo_peek_nowait(&infifo);
}
void uart_clear_inbuf(void) {
	while (uart_get_inbuf_size() > 0)
		uart_getc_nowait();
}
