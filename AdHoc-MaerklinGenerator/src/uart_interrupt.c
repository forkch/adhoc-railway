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
#define BAUDRATE 57600

// FIFO-Objekte und Puffer für die Ein- und Ausgabe

#define BUFSIZE_IN  0x40
uint8_t inbuf[BUFSIZE_IN];
fifo_t infifo;

#define BUFSIZE_OUT 0x40
uint8_t outbuf[BUFSIZE_OUT];
fifo_t outfifo;

unsigned char cmdReceived = 0;

extern unsigned char cmd[64];

unsigned char checkForNewCommand() {

	if (!cmdReceived)
		return 0;

	/*if(uart_peek_inbuf() != 'w' && uart_peek_inbuf() != 'l') {
	 uart_clear_inbuf();
	 return 0;
	 }

	 if (uart_get_inbuf_size() == 3 || uart_peek_inbuf() == 'w') {

	 cmd[0] = uart_getc_wait();
	 cmd[1] = uart_getc_wait();
	 cmd[2] = uart_getc_wait();
	 flash_once_red();
	 return 1;
	 }

	 if (uart_get_inbuf_size() == 6 && uart_peek_inbuf() == 'l') {
	 cmd[0] = uart_getc_nowait();
	 cmd[1] = uart_getc_nowait();
	 cmd[2] = uart_getc_nowait();
	 cmd[3] = uart_getc_nowait();
	 cmd[4] = uart_getc_nowait();
	 cmd[5] = uart_getc_nowait();
	 flash_once_green();
	 return 1;
	 }*/

//	flash_twice_green();
//	flash_twice_red();
	unsigned char c;
	uint8_t counter = 0;
	while (uart_get_inbuf_size() > 0) {
		c = uart_getc_wait();
		cmd[counter] = c;
	}
	//cmd[counter+1] = 0;
	cmdReceived = 0;

	return 1;

}

void uart_init(void) {
	uint8_t sreg = SREG;
	uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU / (16UL * BAUDRATE) - 1);

	UBRRH = (uint8_t) (ubrr >> 8);
	UBRRL = (uint8_t) (ubrr);

	// Interrupts kurz deaktivieren
	cli();

	// UART Receiver und Transmitter anschalten, Receive-Interrupt aktivieren
	// Data mode 8N1, asynchron
	UCSRB = (1 << RXEN) | (1 << TXEN) | (1 << RXCIE);
	UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);

	// Flush Receive-Buffer (entfernen evtl. vorhandener ungültiger Werte)
	do {
		// UDR auslesen (Wert wird nicht verwendet)
		UDR;
	} while (UCSRA & (1 << RXC));

	// Rücksetzen von Receive und Transmit Complete-Flags
	UCSRA = (1 << RXC) | (1 << TXC);

	// Global Interrupt-Flag wieder herstellen
	SREG = sreg;

	// FIFOs für Ein- und Ausgabe initialisieren
	fifo_init(&infifo, inbuf, BUFSIZE_IN);
	fifo_init(&outfifo, outbuf, BUFSIZE_OUT);
}

// Empfangene Zeichen werden in die Eingabgs-FIFO gespeichert und warten dort
ISR (USART_RXC_vect) {
	_inline_fifo_put(&infifo, UDR);

	if (UDR == 0x0D) //complete command received
		cmdReceived = 1;

	//TODO: XOff when fifo is nearly full!!
}

// Ein Zeichen aus der Ausgabe-FIFO lesen und ausgeben
// Ist das Zeichen fertig ausgegeben, wird ein neuer SIG_UART_DATA-IRQ getriggert
// Ist die FIFO leer, deaktiviert die ISR ihren eigenen IRQ.
ISR (USART_UDRE_vect) {
	if (outfifo.count > 0)
		UDR = _inline_fifo_get(&outfifo);
	else
		UCSRB &= ~(1 << UDRIE);
}

int uart_putc(const uint8_t c) {
	int ret = fifo_put(&outfifo, c);

	UCSRB |= (1 << UDRIE);

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
