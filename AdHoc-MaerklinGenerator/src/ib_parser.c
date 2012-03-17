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

#include "ib_parser.h"
#include "global.h"
#include "debug.h"
#include "uart_interrupt.h"
#include "booster.h"

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

//#ifdef DEBUG_EXTREME
//	uart_puts("DEBUG: Token ");
//	send_number_dec(j);
//	uart_puts(": ");
//	if (tokens[j] == NULL)
//		uart_puts("EOL");
//	else
//		uart_puts(tokens[j]);
//	uart_puts("\n");
//#endif

	while (tokens[j] != NULL) {

		j++;
		tokens[j] = strtok(NULL, delimiter);

		//#ifdef DEBUG_EXTREME
//		uart_puts("Token ");
//		send_number_dec(j);
//		uart_puts(": ");
//		if (tokens[j] == NULL)
//		uart_puts("EOL");
//		else
//		uart_puts(tokens[j]);
//		uart_puts("\n");
//#endif
	}

	uint8_t ret;
	if (strcasecmp(tokens[0], "XGO") == 0 || strcasecmp(tokens[0], "X!") == 0) {
		ret = ib_go_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XSTOP") == 0
			|| strcasecmp(tokens[0], "X.") == 0) {
		ret = ib_stop_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XT") == 0) {
		ret = ib_solenoid_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XL") == 0) {
		ret = ib_loco_set_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XLS") == 0) {
		ret = ib_loco_config_cmd(tokens, j);
	} else if (strcasecmp(tokens[0], "XDB") == 0) {
		ret = ib_debug_level_cmd(tokens, j);
	} else {
		ret = 0;
	}

	free(tokens);
	return ret;
}

uint8_t ib_go_cmd(char** tokens, uint8_t nTokens) {

	uint8_t number = 0;

	if (nTokens != 1 && nTokens != 2) {
		log_error("Command format: GO [boosternumber]");
		return 0;
	}
#ifdef DEBUG
	log_debug("New Go Command");
#endif

	if (nTokens == 1) {
		//turn on ALL boosters

#ifdef DEBUG
		log_debug("turn on ALL boosters");
#endif
		go_all_boosters();

	} else {
#ifdef DEBUG
		log_debug3("turn on booster ", number);
#endif
		number = atoi(tokens[1]);
		go_booster(number);
	}

	return 1;

}

uint8_t ib_stop_cmd(char** tokens, uint8_t nTokens) {

	uint8_t address = 0;
	uint8_t port = 0;
	uint8_t number = 0;

	if (nTokens != 1 && nTokens != 2) {
		log_error("Command format: STOP [boosternumber]");
		return 0;
	}
#ifdef DEBUG
	log_debug("New Stop Command");
#endif

	if (nTokens == 1) {
		//turn off ALL boosters

#ifdef DEBUG
		log_debug("turn off ALL boosters");
#endif
		stop_all_boosters();

	} else {
#ifdef DEBUG
		log_debug3("turn off booster ", number);
#endif
		number = atoi(tokens[1]);
		stop_booster(number);
	}
	return 1;
}

uint8_t ib_solenoid_cmd(char** tokens, uint8_t nTokens) {

	uint8_t address = 0;
	uint8_t port = 0;
	uint8_t number = 0;

	if (nTokens != 3) {
		log_error("Command format: XT turnoutnumber r|g|0|1");
		return 0;
	}
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

	solenoidQueue[solenoidQueueIdxEnter].address =
			locoData[address - 1].address;
	solenoidQueue[solenoidQueueIdxEnter].port = portData[port];
	solenoidQueue[solenoidQueueIdxEnter].active = 0;
	solenoidQueue[solenoidQueueIdxEnter].timerDetected = 0;
	solenoidQueue[solenoidQueueIdxEnter].deactivate = 0;

	enqueue_solenoid();
	return 1;

}

uint8_t ib_loco_config_cmd(char** tokens, uint8_t nTokens) {

	uint8_t number = 0;
	char protocol[4];
	if (nTokens != 3) {
		log_error("Command format: XLS [loconumber] [mm|mm2]");
		return 0;
	}
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

uint8_t ib_loco_set_cmd(char** tokens, uint8_t nTokens) {

	uint8_t number = 0;
	uint8_t t = 0;
	uint8_t speed = 0;
	uint8_t direction = 0;
	uint8_t fl;
	uint8_t f1;
	uint8_t f2;
	uint8_t f3;
	uint8_t f4;

	if (nTokens != 9) {
		log_error(
				"Command format: XL loconumber speed fl direction F1 F2 F3 F4");
		return 0;
	}
#ifdef DEBUG
	log_debug("New Loco Set Command");
#endif

	number = atoi(tokens[1]);
	speed = atoi(tokens[2]);
	fl = atoi(tokens[3]);
	direction = atoi(tokens[4]);
	f1 = atoi(tokens[5]);
	f2 = atoi(tokens[6]);
	f3 = atoi(tokens[7]);
	f4 = atoi(tokens[8]);

	t = number - 1;

	locoData[t].active = 1;
	locoData[t].refreshState = 0;
	locoData[t].speed = speed;

	if (direction != locoData[t].direction) {
		locoData[t].encodedSpeed = mmChangeDirection;
		locoData[t].direction = direction;
	} else {
		locoData[t].encodedSpeed = deltaSpeedData[speed];
	}

	if (locoData[t].isNewProtocol) {
		// NEW MM protocol change bits E F G H
		unsigned char efgh = 0xFF;
		unsigned char mask = 0b01010101;
		if (direction == 0) {
			if (speed <= 14 && speed >= 7) {
				efgh = 0b11001100;
			} else if (speed <= 6 && speed >= 0) {
				efgh = 0b11001111;
			}
		} else {
			if (speed <= 14 && speed >= 7) {
				efgh = 0b00110000;
			} else if (speed <= 6 && speed >= 0) {
				efgh = 0b00110011;
			}
		}

		locoData[t].fl = fl != 0;
		locoData[t].f1 = f1 != 0;
		locoData[t].f2 = f2 != 0;
		locoData[t].f3 = f3 != 0;
		locoData[t].f4 = f4 != 0;

		// merge new E F G H values
		unsigned char abcd = locoData[t].encodedSpeed;
		locoData[t].encodedSpeed = abcd ^ ((abcd ^ efgh) & mask);

#ifdef DEBUG_EXTREME
		log_debug("MASK");
		for (uint8_t i = 0; i < 8; i++) {
			if ((mask >> (7 - i)) & 1)
			uart_putc('1');
			else
			uart_putc('0');
		}
		send_nl();

		log_debug("ABCD");
		for (uint8_t i = 0; i < 8; i++) {
			if ((abcd >> (7 - i)) & 1)
			uart_putc('1');
			else
			uart_putc('0');
		}
		send_nl();

		log_debug("EFGH");
		for (uint8_t i = 0; i < 8; i++) {
			if ((efgh >> (7 - i)) & 1)
			uart_putc('1');
			else
			uart_putc('0');
		}
		send_nl();
#endif
	}

#ifdef DEBUG_EXTREME
	log_debug("SPEED");
	for (uint8_t i = 0; i < 8; i++) {
		if ((locoData[t].encodedSpeed >> (7 - i)) & 1)
		uart_putc('1');
		else
		uart_putc('0');
	}
	send_nl();
#endif

#ifdef DEBUG
	log_debug3("Loco Number: ", number);
	locoData[t].isNewProtocol == 1 ?
			log_debug("Protocol: MM2") : log_debug("Protocol: MM");
	log_debug3("Loco Speed: ", speed);
	log_debug3("Loco FL: ", fl);
	log_debug3("Loco Direction: ", direction);
	log_debug3("Loco F1: ", f1);
	log_debug3("Loco F2: ", f2);
	log_debug3("Loco F3: ", f3);
	log_debug3("Loco F4: ", f4);
	log_debug3("Loco isNewProtocol: ", locoData[t].isNewProtocol);

#endif
	enqueue_loco(t);

	return 1;

}

uint8_t ib_debug_level_cmd(char** tokens, uint8_t nTokens) {

	if (nTokens != 2) {
		log_error("Command format: XDB 0-4");
		return 0;
	}

	debugLevel = atoi(tokens[1]);
#ifdef DEBUG
	log_debug("New Debug Level Command");
#endif
	return 1;
}
