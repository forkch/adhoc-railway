/*
 * uart_interrupt.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef UART_INTERRUPT_H_
#define UART_INTERRUPT_H_



#include <avr/io.h>


unsigned char checkForNewCommand();

extern void uart_init (void);
extern int uart_putc (const uint8_t);
extern int uart_puts(const char*);
extern uint8_t uart_getc_wait (void);
extern int uart_getc_nowait (void);
extern uint8_t uart_get_inbuf_size(void);
extern uint8_t uart_peek_inbuf(void);
extern void uart_clear_inbuf(void);

static inline void uart_flush (void)
{

	while (UCSR0B & (1 << UDRIE0));
}

#endif /* UART_INTERRUPT_H_ */
