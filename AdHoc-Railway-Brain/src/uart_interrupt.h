/*
 * uart_interrupt.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef UART_INTERRUPT_H_
#define UART_INTERRUPT_H_

#include <avr/io.h>




#define UART_CTS_OUTPUT_PIN PD3
#define UART_RTS_INPUT_PIN PD2
#define UART_OUTPUT_PORT PORTD
#define UART_INPUT_PINS PIND
#define UART_DDR DDRD

typedef enum IB_CMD {
	BINARY_MODE, ASCII_MODE, INVALID
} IB_CMD;

unsigned char checkForNewCommand();

extern void replys(const char*);
extern void uart_init (void);
extern int uart_putc (const uint8_t);
extern int uart_puts(const char*);
extern uint8_t uart_getc(void);		// old uart_getc_nowait


#endif /* UART_INTERRUPT_H_ */


