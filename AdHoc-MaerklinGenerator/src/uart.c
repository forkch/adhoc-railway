/*
 * uart.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "uart.h"


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
