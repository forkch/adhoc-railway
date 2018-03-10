/*
 * fifo.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef FIFO_H_
#define FIFO_H_


#include <avr/io.h>
#include <avr/interrupt.h>
#include "uart_interrupt.h"


typedef struct
{
	uint8_t volatile count;       // # Zeichen im Puffer
	uint8_t size;                 // Puffer-Gršsse
	uint8_t *pread;               // Lesezeiger
	uint8_t *pwrite;              // Schreibzeiger
	uint8_t read2end, write2end;  // # Zeichen bis zum †berlauf Lese-/Schreibzeiger
	uint8_t comstate;			  // UART-Komm. / Hardware-Handshaking 1 => Com freigegeben
} fifo_t;

extern void fifo_init (fifo_t*, uint8_t* buf, const uint8_t size);
extern uint8_t outfifo_put (fifo_t*, const uint8_t data);
extern uint8_t infifo_get (fifo_t*);
extern uint8_t fifo_get_size(fifo_t*);



static inline uint8_t
_inline_infifo_put (fifo_t *f, const uint8_t data)
{

	//infifo Overflow?
	if (f->count >= f->size){
		f->count = 0;
		f->read2end = f->write2end = f->size;
		UART_OUTPUT_PORT &= ~(1 << UART_CTS_OUTPUT_PIN);
		return 0;
	}

	if (f->count > (f->size / 2))
		UART_OUTPUT_PORT |= (1 << UART_CTS_OUTPUT_PIN);

	uint8_t * pwrite = f->pwrite;

	*(pwrite++) = data;

	uint8_t write2end = f->write2end;

	if (--write2end == 0)
	{
		write2end = f->size;
		pwrite -= write2end;
	}

	f->write2end = write2end;
	f->pwrite = pwrite;

	f->count++;

	return 1;
}




static inline uint8_t
_inline_infifo_get (fifo_t *f)
{
	uint8_t *pread = f->pread;
	uint8_t data = *(pread++);
	uint8_t read2end = f->read2end;


	if (f->count < (f->size / 4)){
		UART_OUTPUT_PORT &= ~(1 << UART_CTS_OUTPUT_PIN);
	}

	uint8_t sreg = SREG;
	cli();
	if (--read2end == 0)
	{
		read2end = f->size;
		pread -= read2end;
	}

	f->pread = pread;
	f->read2end = read2end;

	f->count--;
	SREG = sreg;

	return data;
}



static inline uint8_t
_inline_outfifo_put (fifo_t *f, const uint8_t data)
{
	if (f->count >= (f->size - 10)){
		return 0;
	}

	uint8_t sreg = SREG;
	cli();
	uint8_t * pwrite = f->pwrite;

	*(pwrite++) = data;

	uint8_t write2end = f->write2end;

	if (--write2end == 0)
	{
		write2end = f->size;
		pwrite -= write2end;
	}

	f->write2end = write2end;
	f->pwrite = pwrite;

	f->count++;
	SREG = sreg;

	return 1;
}



static inline uint8_t
_inline_outfifo_get (fifo_t *f)
{
	uint8_t *pread = f->pread;
	uint8_t data = *(pread++);
	uint8_t read2end = f->read2end;


	if (--read2end == 0)
	{
		read2end = f->size;
		pread -= read2end;
	}

	f->pread = pread;
	f->read2end = read2end;

	f->count--;

	return data;
}




#endif /* FIFO_H_ */
