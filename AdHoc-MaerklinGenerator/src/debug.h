/*
 * debug.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef DEBUG_H_
#define DEBUG_H_

#include <avr/io.h>
#include <util/delay.h>
#include <avr/interrupt.h>

#define GREEN_LED PC4
#define RED_LED PC5
#define RED_GREEN_PORT PORTC
#define RED_GREEN_DDR DDRC
#define SWITCH PD7
#define SWITCH_PORT PIND
void debug_init();
void flash_twice_green();
void flash_once_green();
void flash_once_green_quick();
void flash_once_red_quick();
void flash_twice_red();
void flash_once_red();
void red_led_on();
void red_led_off();
void green_led_on();
void green_led_off();
void send_number(unsigned int);

#endif /* DEBUG_H_ */
