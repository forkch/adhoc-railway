/***************************************************************************
                          mcp-fb.h  -  description
                             -------------------
    begin                : wed Mar 21 2018
    copyright            : (C) 2018 by Rüdiger Seidel
    email                : ruediger.seidel@web.de
 ***************************************************************************/
/***************************************************************************
This source based on ddl-s88 code by Frank Schmischke / Daniel Sigg.
    Thanks also to the erddcd developers
    Torsten Vogt, Dieter Schaefer, Martin Wolf, Matthias Trute, Michael Meiszl

    This extension runs on RaspberryPi only,
    using some free GPIOs to connect up to 8 MCP23S17
    16 bit port expander to an srcpd bus.
    Inputs are provided as "FB" information according to srcp by using srcp-fb.
    If sometime 8*16 Inports are not sufficient, spending one more
    available RaspberryPi GPIO for
    CS (chip select) of the next MCP23S17 bus could easily extend this.
    The MCP23S17 and its connection to Raspberrypi GPIOs is described
    here: http://erik-bartmann.de/raspberry-pi-2-auflage-downloads/
    I omitted the reset button and the power LED.
    All MCP23S17 pins are configured as input using internal pullup-resistors.
    So external resistors are not necessary.
    An input is activated by simply connecting GND to this dedicated input pin.
    Be careful not to connect any other potential to MCP23S17 pins, like booster out.
    I use optocouplers for connecting pins to GND.
    I also use and recommend an external power supply 3.3V for the MCP23S17 chips.
    It is connected GND to Raspberrypi-GND.
    A restricted number of MCP23S17 Chips can be supported by Raspberrypi itself,
    I tested successfully with one chip only.
***************************************************************************/


/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
#ifndef _MCP_FB_H
#define _MCP_FB_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/
#include <bcm2835.h>

/* maximal number of bytes read from one mcp-chip */
#define MCP_MAXPORTSB 16
/* maximal number of mcp-busses */
#define MCP_MAXBUSSES 1
/* max MCP23S17 per bus is 8 because 3 bit adressing */
#define MAX_MCP_ADRESS 8
/* maximal number of mcp chips per bus is 8 (3 Bit)*/
#define MCP_MAXPORTS MCP_MAXPORTSB*MAX_MCP_ADRESS*MCP_MAXBUSSES

#define ANZ_MCP_WIEDERHOLUNGEN 3

/* some registers of MCP23S17, see data sheet */
#define SPI_IODIRA     0x00  //IO Direction PortA
#define SPI_IODIRB     0x01  //IO Direction PortB

#define SPI_POL_A      0x02  //Polarity PortA
#define SPI_POL_B      0x03  //Polarity PortB

#define SPI_CONFIG_A   0x0A
#define SPI_CONFIG_B   0x0B

#define SPI_PULLUP_A   0x0C  //internal Pullup resistors PortA
#define SPI_PULLUP_B   0x0D  //internal Pullup resistors PortB

#define SPI_GPIOA      0x12
#define SPI_GPIOB      0x13


// Werte
#define SPI_BASE_ADRESS 0x40
#define SPI_SLAVE_WRITE 0x00
#define SPI_SLAVE_READ  0x01
#define SPI_HW_ADDR     0x08

/* MCP23S17-Pins
#define SCLK  RPI_BPLUS_GPIO_J8_12 //18 // Serial clock war 25
#define MOSI  RPI_BPLUS_GPIO_J8_18 //24 // Master-Out-Slave-In war 18
#define MISO  RPI_BPLUS_GPIO_J8_16 //23 // Master-In-Slave-Out
#define CS    RPI_BPLUS_GPIO_J8_22 //25 // Chip-Select war 24
*/

typedef struct _DDL_MCP_DATA {
    int number_fb[MCP_MAXBUSSES];
    int MOSI;
    int MISO;
    int SCLK;
    int CS; // when at anytime extending to more MCP Busses, we need CS only making a field CS[busnumber]
    int max_number_fb;
    int waitus;  // after ever access to MCP23S17 this time in micro secs is waited
    int refresh;
    /* timestamp, until when the mcpdata are valid */
    struct timeval MCPvalid;
    //buffer for voting/debouncing
    unsigned char MCPdataBuffer[MCP_MAXBUSSES][ANZ_MCP_WIEDERHOLUNGEN][MAX_MCP_ADRESS*2]; //MCP23 S17 delivers 2 Byte
    int aktMCPDataBufferIndex;
} DDL_MCP_DATA;

// some helper functions for MCP communication
void sendValue(bus_t busnumber, uint8_t value);
void sendSPI(bus_t busnumber, uint8_t adress, uint8_t registerbyte, uint8_t data);
void initMCP23S17(bus_t busnumber, uint8_t adress, uint8_t portsA, uint8_t portsB);
void activateAdressing(bus_t busnumber, uint8_t mcp);
uint8_t readSPI(bus_t busnumber, uint8_t adress, uint8_t registerbyte);

int readconfig_MCP_FB(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

int init_bus_MCP(bus_t);
void *thr_sendrec_MCP(void *);
void *thr_sendrec_NoMCP(void *v);



#endif
