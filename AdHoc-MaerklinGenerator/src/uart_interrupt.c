/*
 * uart_interrupt.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */


#include <avr/io.h>
#include <avr/interrupt.h>
#include "uart.h"
#include "fifo.h" // erklärt im Artikel "FIFO mit avr-gcc"

#define BAUDRATE 115200

// FIFO-Objekte und Puffer für die Ein- und Ausgabe

#define BUFSIZE_IN  0x40
uint8_t inbuf[BUFSIZE_IN];
fifo_t infifo;

#define BUFSIZE_OUT 0x40
uint8_t outbuf[BUFSIZE_OUT];
fifo_t outfifo;

void uart_init (void)
{
    uint8_t sreg = SREG;
    uint16_t ubrr = (uint16_t) ((uint32_t) F_CPU/(16UL*BAUDRATE) - 1);

    UBRRH = (uint8_t) (ubrr>>8);
    UBRRL = (uint8_t) (ubrr);

    // Interrupts kurz deaktivieren
    cli();

    // UART Receiver und Transmitter anschalten, Receive-Interrupt aktivieren
    // Data mode 8N1, asynchron
    UCSRB = (1 << RXEN) | (1 << TXEN) | (1 << RXCIE);
    UCSRC = (1 << URSEL) | (1 << UCSZ1) | (1 << UCSZ0);

    // Flush Receive-Buffer (entfernen evtl. vorhandener ungültiger Werte)
    do
    {
        // UDR auslesen (Wert wird nicht verwendet)
        UDR;
    }
    while (UCSRA & (1 << RXC));

    // Rücksetzen von Receive und Transmit Complete-Flags
    UCSRA = (1 << RXC) | (1 << TXC);

    // Global Interrupt-Flag wieder herstellen
    SREG = sreg;

    // FIFOs für Ein- und Ausgabe initialisieren
    fifo_init (&infifo,   inbuf, BUFSIZE_IN);
    fifo_init (&outfifo, outbuf, BUFSIZE_OUT);
}

// Empfangene Zeichen werden in die Eingabgs-FIFO gespeichert und warten dort
ISR (USART_RXC_vect)
{
    _inline_fifo_put (&infifo, UDR);
}

// Ein Zeichen aus der Ausgabe-FIFO lesen und ausgeben
// Ist das Zeichen fertig ausgegeben, wird ein neuer SIG_UART_DATA-IRQ getriggert
// Ist die FIFO leer, deaktiviert die ISR ihren eigenen IRQ.
ISR (USART_UDRE_vect)
{
    if (outfifo.count > 0)
       UDR = _inline_fifo_get (&outfifo);
    else
        UCSRB &= ~(1 << UDRIE);
}

int uart_putc (const uint8_t c)
{
    int ret = fifo_put (&outfifo, c);

    UCSRB |= (1 << UDRIE);

    return ret;
}

int uart_getc_nowait (void)
{
    return fifo_get_nowait (&infifo);
}

uint8_t uart_getc_wait (void)
{
    return fifo_get_wait (&infifo);
}

uint8_t uart_get_inbuf_size(void) {
	return fifo_get_size(&infifo);
}
