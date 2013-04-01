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

void logit(char*);
void logit2(char*,char*);
void logit3(char*,uint8_t);
void logit_binary(char*, unsigned char);

void log_error(char*);
void log_error2(char*,char*);
void log_error3(char*,uint8_t);

void log_warn(char*);
void log_warn2(char*,char*);
void log_warn3(char*,uint8_t);

void log_info(char*);
void log_info2(char*,char*);
void log_info3(char*,uint8_t);

void log_debug(char*);
void log_debug2(char*,char*);
void log_debug3(char*,uint8_t);

void log_debug_binary(char*,uint8_t);
void log_error_binary(char*,uint8_t);
void log_info_binary(char*,uint8_t);

void send_nl();
void send_number(unsigned int);
void send_number_dec(unsigned int);
void send_number_hex(unsigned int);

#endif /* DEBUG_H_ */
