/**
 * C++ Interface: portio
 *
 * Description: 
 *
 *
 * Author: Ing. Gerard van der Sel
 *
 * Copyright: See COPYING file that comes with this distribution
 *
 */
#ifndef _PORTIO_H
#define _PORTIO_H

#include "config-srcpd.h"

int open_port(bus_t bus);
void close_port(bus_t bus);
void write_port(bus_t bus, unsigned char b);
int check_port(bus_t bus);
unsigned int read_port(bus_t bus);

#endif

