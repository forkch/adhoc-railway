/*
 * ib_parser.c
 *
 *  Created on: 18.02.2012
 *      Author: fork
 */

#include <avr/io.h>
#include <string.h>
#include <stdlib.h>
#include <math.h>

#include "global.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "ib_parser.h"

uint8_t parse_ib_cmd(char* cmd) {
	char cmdCopy[64];
	//char* token;
	char delimiter[] = " ,";
	char **tokens;
	int count = 0;
	strcpy(cmdCopy, cmd);

	for (int i = 0; i < strlen(cmd); i++) {
		if (cmd[i] == ',' || cmd[i] == ' ')
			count++;
	}

	tokens = malloc(count * sizeof(char*));
	int j = 0;
	tokens[j] = strtok(cmd, delimiter);

#ifdef DEBUG
	uart_puts("Token ");
	send_number_dec(j);
	uart_puts(": ");
	if (tokens[j] == NULL)
		uart_puts("EOL");
	else
		uart_puts(tokens[j]);
	uart_puts("\n");
#endif

	while (tokens[j] != NULL) {

		j++;
		tokens[j] = strtok(NULL, delimiter);

#ifdef DEBUG
		uart_puts("Token ");
		send_number_dec(j);
		uart_puts(": ");
		if (tokens[j] == NULL)
			uart_puts("EOL");
		else
			uart_puts(tokens[j]);
		uart_puts("\n");
#endif
	}

	uint8_t ret;
	if (strcasecmp(tokens[0], "XT") == 0) {
		ret = ib_solenoid_cmd(tokens);
	} else if (strcasecmp(tokens[0], "XL") == 0) {
		ret = ib_loco_set_cmd(tokens);
	} else if (strcasecmp(tokens[0], "XLS") == 0) {
		ret = ib_loco_config_cmd(tokens);
	} else {
		ret = 0;
	}

	free(tokens);
	return ret;
}

uint8_t ib_solenoid_cmd(char** tokens) {

	uint8_t address = 0;
	uint8_t port = 0;
	uint8_t number = 0;

#ifdef DEBUG
	log_debug("New Solenoid Command");
#endif

	number = atoi(tokens[1]);
	address = (unsigned char) ceilf(number / 4.f);

	uint8_t color = 0;
	if (strcasecmp(tokens[2], "g") == 0 || strcasecmp(tokens[2], "1") == 0) {
		color = 1;
	} else if (strcasecmp(tokens[2], "r") == 0
			|| strcasecmp(tokens[2], "0") == 0) {
		color = 0;
	} else {
		return 0;
	}

	port = (unsigned char) (number - 1) % 4;
	port *= 2;
	port += color;

#ifdef DEBUG
	uart_flush();
	log_debug3("Decoder-Address: ", address);
	log_debug3("Decoder-Port: ", port);

#endif

	solenoidData[solenoidDataIdxInsert].address = locoData[address - 1].address;
	solenoidData[solenoidDataIdxInsert].port = portData[port];
	solenoidData[solenoidDataIdxInsert].active = 0;
	solenoidData[solenoidDataIdxInsert].timerDetected = 0;
	solenoidData[solenoidDataIdxInsert].deactivate = 0;

	enqueue_solenoid();
	return 1;

}

uint8_t ib_loco_config_cmd(char** tokens) {

	uint8_t number = 0;
	char protocol[4];

#ifdef DEBUG
	log_debug("New Loco Config Command");
#endif
	number = atoi(tokens[1]);
	strcpy(protocol, tokens[2]);

	if (strcasecmp(protocol, "MM2") == 0) {
		locoData[number - 1].isNewProtocol = 1;
	} else if (strcasecmp(protocol, "MM") == 0) {
		locoData[number - 1].isNewProtocol = 0;
	} else {
		return 0;
	}

#ifdef DEBUG
	log_debug3("Loco Number: ", number);
	log_debug2("Protocol: ", protocol);

#endif

	return 1;

}

uint8_t ib_loco_set_cmd(char** tokens) {

	uint8_t number = 0;
	uint8_t t = 0;
	uint8_t speed = 0;
	uint8_t direction = 0;
	uint8_t functions = 0;

#ifdef DEBUG
	log_debug("New Loco Set Command");
#endif
	number = atoi(tokens[1]);
	speed = atoi(tokens[2]);
	direction = atoi(tokens[3]);
	t = number - 1;

	locoData[t].active = 1;
	if (locoData[t].isNewProtocol) {
		// NEW protocol
	} else {
		// OLD protocol (DELTA)
		if (direction != locoData[t].direction) {
			locoData[t].speed = deltaSpeedData[1];
			locoData[t].direction = direction;
		} else if (speed == 0 || speed == 1) {
			locoData[t].speed = deltaSpeedData[0];
		} else {
			locoData[t].speed = deltaSpeedData[speed + 2];
		}
	}

#ifdef DEBUG
	log_debug3("Loco Number: ", number);
	log_debug3("Loco Speed: ", speed);
	log_debug3("Loco Direction: ", direction);
	log_debug3("Loco Functions: ", functions);
	log_debug3("Loco isNewProtocol: ", locoData[t].isNewProtocol);

#endif
	enqueue_loco(t);

	return 1;

}

