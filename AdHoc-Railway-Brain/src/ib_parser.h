/*
 * ib_parser.h
 *
 *  Created on: 18.02.2012
 *      Author: fork
 */

#ifndef IB_PARSER_H_
#define IB_PARSER_H_


uint8_t parse_ib_cmd(char* receivedCmdString);
uint8_t ib_go_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_stop_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_booster_state_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_solenoid_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_loco_config_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_loco_set_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_debug_level_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_get_uid_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_remove_loc_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_loc_halt_cmd(char** tokens, uint8_t nTokens);
uint8_t ib_buffer_info_cmd(char** tokens, uint8_t nTokens);
uint8_t encodeMM2Function(uint8_t functionNr, unsigned char functionState, unsigned char deltaSpeed,
		unsigned char speed);
uint8_t encodeMFXCmd(uint8_t mfxIdx, uint8_t direction);
uint8_t encodeMFXSIDCmd(uint8_t mfxIdx);

typedef enum LOCO_PROTOCOL {
	MM, MM2, MFX, DCC
} LP;


#endif /* IB_PARSER_H_ */
