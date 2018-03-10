/*
 * fifo.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 *
 * Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#include "fifo.h"


void fifo_init (fifo_t *f, uint8_t *buffer, const uint8_t size)
{
	f->count = 0;
	f->pread = f->pwrite = buffer;
	f->read2end = f->write2end = f->size = size;
	f->comstate = 1;
}



uint8_t outfifo_put (fifo_t *f, const uint8_t data)
{
	return _inline_outfifo_put (f, data);
}



uint8_t infifo_get (fifo_t *f)	{
	if (!f->count)		return 0x16; //Char NACK

	return _inline_infifo_get (f);
}



uint8_t fifo_get_size(fifo_t *f) {
	return f->count;
}




