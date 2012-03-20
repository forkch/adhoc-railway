/*
 * debug.c
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#include "debug.h"
#include "uart_interrupt.h"
#include "global.h"

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
	_delay_ms(200);
	RED_GREEN_PORT &= ~(1 << GREEN_LED);
	_delay_ms(200);
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
	_delay_ms(200);
	RED_GREEN_PORT &= ~(1 << RED_LED);
	_delay_ms(200);
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

void logit(char* msg) {
	uart_puts(msg);
	send_nl();
}
void logit2(char* msg, char* msg2) {
	uart_puts(msg);
	uart_puts(msg2);
	send_nl();
}
void logit3(char* msg, uint8_t number) {
	uart_puts(msg);
	send_number(number);
	send_nl();
}

void logit_binary(char* msg, unsigned char number) {
	uart_puts(msg);

	for (uint8_t i = 0; i < 8; i++) {
		if (((number >> (7 - i)) & 1) == 0) {
			uart_putc('0');
		} else {
			uart_putc('1');

		}
	}

	send_nl();
}

void log_error(char* msg) {
	if (debugLevel >= DEBUG_ERROR) {
		uart_flush();
		uart_puts("ERROR: ");
		logit(msg);
	}
}

void log_error2(char* msg, char* msg2) {
	if (debugLevel >= DEBUG_ERROR) {
		uart_flush();
		uart_puts("ERROR: ");
		logit2(msg, msg2);
	}
}
void log_error3(char* msg, uint8_t number) {
	if (debugLevel >= DEBUG_ERROR) {
		uart_flush();
		uart_puts("ERROR: ");
		logit3(msg, number);
	}
}

void log_warn(char* msg) {
	if (debugLevel >= DEBUG_WARN) {
		uart_flush();
		uart_puts("WARN: ");
		logit(msg);
	}
}

void log_warn2(char* msg, char* msg2) {
	if (debugLevel >= DEBUG_WARN) {
		uart_flush();
		uart_puts("WARN: ");
		logit2(msg, msg2);
	}
}
void log_warn3(char* msg, uint8_t number) {
	if (debugLevel >= DEBUG_WARN) {
		uart_flush();
		uart_puts("WARN: ");
		logit3(msg, number);
	}
}

void log_info(char* msg) {
	if (debugLevel >= DEBUG_INFO) {
		uart_flush();
		uart_puts("INFO: ");
		logit(msg);
	}
}

void log_info2(char* msg, char* msg2) {
	if (debugLevel >= DEBUG_INFO) {
		uart_flush();
		uart_puts("INFO: ");
		logit2(msg, msg2);
	}
}
void log_info3(char* msg, uint8_t number) {
	if (debugLevel >= DEBUG_INFO) {
		uart_flush();
		uart_puts("INFO: ");
		logit3(msg, number);
	}
}

void log_debug(char* msg) {
	if (debugLevel >= DEBUG_DEBUG) {
		uart_flush();
		uart_puts("DEBUG: ");
		logit(msg);
	}
}

void log_debug2(char* msg, char* msg2) {
	if (debugLevel >= DEBUG_DEBUG) {
		uart_flush();
		uart_puts("DEBUG: ");
		logit2(msg, msg2);
	}
}

void log_debug3(char* msg, uint8_t number) {
	if (debugLevel >= DEBUG_DEBUG) {
		uart_flush();
		uart_puts("DEBUG: ");
		logit3(msg, number);
	}
}

void send_nl() {
	uart_putc('\n');
	uart_flush();
}

void send_number(unsigned int my_val) {
	send_number_dec(my_val);
	uart_puts(" [");
	send_number_hex(my_val);
	uart_puts("]");
	uart_flush();
}
void send_number_dec(unsigned int my_val) {

	char buffer[4];
	buffer[0] = ' ';
	buffer[1] = ' ';
	buffer[2] = ' ';
	buffer[3] = ' ';

	itoa(my_val, buffer, 10);
	uart_puts(buffer);
}

void send_number_hex(unsigned int my_val) {

	unsigned char digit;
	uart_puts("0x");
	for (signed int i = 4; i >= 0; i -= 4) {
		digit = (my_val >> i) & 0x0f; // eine hex-Stelle extrahieren
		digit = (digit > 9) ? (digit + 'A' - 10) : (digit + '0'); // in ASCII - Zeichen	konvertieren
		uart_putc(digit); // oder was immer die UART-Funktion in Deinem compiler ist
	}
	uart_flush();
}
