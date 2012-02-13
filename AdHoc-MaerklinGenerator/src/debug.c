/*
 * debug.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "debug.h"
#include "uart_interrupt.h"

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
	_delay_ms(100);
}
void flash_once_green() {
	RED_GREEN_PORT |= (1 << GREEN_LED);
	_delay_ms(50);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
	_delay_ms(50);
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
	_delay_ms(50);
	RED_GREEN_PORT &= ~(1 << RED_LED);
	_delay_ms(50);
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

void send_number(unsigned int my_val) {

	unsigned char digit;
	uart_puts("LKSDF");
	for(int i = 0; i < 8; i++)
	{
		uart_putc('q');

	}
	for (signed int i = 8; i >= 0; i -= 4) {
uart_putc('q');
		digit = (my_val >> i) & 0x0f; // eine hex-Stelle extrahieren
		digit = (digit > 9) ? (digit + 'A' - 10) : (digit + '0'); // in ASCII - Zeichen	konvertieren
		uart_putc( digit); // oder was immer die UART-Funktion in Deinem compiler ist
	}
}
