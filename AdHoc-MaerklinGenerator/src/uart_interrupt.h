/*
 * uart_interrupt.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef UART_INTERRUPT_H_
#define UART_INTERRUPT_H_


#include <avr/io.h>


extern void uart_init (void);
extern int uart_putc (const uint8_t);
extern uint8_t uart_getc_wait (void);
extern int uart_getc_nowait (void);
extern uint8_t uart_get_inbuf_size(void);

static inline void uart_flush (void)
{
	while (UCSRB & (1 << UDRIE));
}

#endif /* UART_INTERRUPT_H_ */
