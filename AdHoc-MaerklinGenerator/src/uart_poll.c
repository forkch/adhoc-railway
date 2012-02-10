/*
 * uart.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "uart_poll.h"
extern char cmd[64];

unsigned char checkForNewCommandPoll() {

	if (!(UCSRA & (1 << RXC))) {
		return 0;
	}

	unsigned char b = uart_receive_poll();
	cmd[0] = b;
	if (b == 'w') {
		for (uint8_t i = 1; i < 3; i++) {
			b = uart_receive_poll();
			cmd[i] = b;
		}
	} else if (b == 'l') {
		for (uint8_t i = 1; i < 6; i++) {
			b = uart_receive_poll();
			cmd[i] = b;
		}
	} else {
		return 0;
	}

	flash_once_red();

	return 1;
}
void uart_init_poll(unsigned int ubrr) {
	/* Set baud rate */
	UBRRH = (unsigned char) (ubrr >> 8);
	UBRRL = (unsigned char) ubrr;

	/* Enable receiver and transmitter */UCSRB = (1 << RXEN) | (1 << TXEN);
	/* Set frame format: 8data, 2stop bit */
	UCSRC = (1 << URSEL) | (1 << USBS) | (1 << UCSZ0) | (1 << UCSZ1);
}

void uart_transmit_poll(unsigned char data) {
	/* Wait for empty transmit buffer */
	while (!(UCSRA & (1 << UDRE)))
		;
	/* Put data into buffer, sends the data */
	UDR = data;
}

unsigned char uart_receive_poll(void) {
	/* Wait for data to be received */
	while (!(UCSRA & (1 << RXC)))
		;

	/* Get and return received data from buffer */

	return UDR;
}
