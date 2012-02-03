/*
 * uart.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef UART_H_
#define UART_H_

#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define FOSC 16000000
#define BAUD 115200
#define MYUBRR FOSC/16/BAUD-1

void initUSART(unsigned int ubrr);
void transmitUSART(unsigned char data);


#endif /* UART_H_ */
