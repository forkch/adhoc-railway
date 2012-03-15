/*
 * ib_parser.h
 *
 *  Created on: 18.02.2012
 *      Author: fork
 */

#ifndef IB_PARSER_H_
#define IB_PARSER_H_

uint8_t parse_ib_cmd(char* cmd);
uint8_t ib_go_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_stop_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_solenoid_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_loco_config_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_loco_set_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_debug_level_cmd(char** tokens, uint8_t nTokens);

#endif /* IB_PARSER_H_ */
