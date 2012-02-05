/*
 * debug.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "debug.h"

void debug_init() {
	RED_GREEN_DDR |= (1 << GREEN_LED) | (1 << RED_LED);
}

void flash_twice_green() {
	RED_GREEN_PORT |= (1 << GREEN_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
	_delay_ms(100);
	RED_GREEN_PORT |= (1 << GREEN_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
}
void flash_once_green() {
	RED_GREEN_PORT |= (1 << GREEN_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
	_delay_ms(100);
}
void flash_once_green_quick() {
	RED_GREEN_PORT |= (1 << GREEN_LED);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
}
void flash_once_red_quick() {
	RED_GREEN_PORT |= (1 << RED_LED);
	RED_GREEN_PORT &= ~(1 << RED_LED);
}
void flash_twice_red() {
	RED_GREEN_PORT |= (1 << RED_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << RED_LED);
	_delay_ms(100);
	RED_GREEN_PORT |= (1 << RED_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << RED_LED);
	_delay_ms(100);
}
void flash_once_red() {
	RED_GREEN_PORT |= (1 << RED_LED);
	_delay_ms(100);
	RED_GREEN_PORT &= ~(1 << RED_LED);
	_delay_ms(100);
}

void red_led_on() {
	RED_GREEN_PORT |= (1 << RED_LED);
}
void red_led_off() {
	RED_GREEN_PORT &= ~(1 << RED_LED);
}
void green_led_on() {
	RED_GREEN_PORT |= (1 << GREEN_LED);
}
void green_led_off() {
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
}
