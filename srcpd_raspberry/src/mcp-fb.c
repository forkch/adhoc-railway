/***************************************************************************
                          mcp-fb.c  -  description
                             -------------------
    begin                : Wed Mar 21 2018
    copyright            : (C) 2018-2019 by Rüdiger Seidel
    email                : ruediger.seidel@web.de

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
    This software runs operating 8 of these chips adressed 0-7 on srcpd bus 2
    on my own model railway.
    last change 12th oct 2019
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#include <errno.h>
#include <string.h>
#include <unistd.h>

#include <sys/io.h>

#include "config.h"
#include "config-srcpd.h"
#include "srcp-fb.h"
#include "io.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "syslogmessage.h"
#include "mcp-fb.h"

#include <stdint.h>
#include <fcntl.h>
#include <sys/ioctl.h>

// seems some waiting is neccesary for MCP23S17 chips
static int wait = 50;

// because we don not know if MCP chips are powered all time (in case of separate power supply
// we are reinitializing after this number of read cycles
// 12th oct 2019: changed from 1000 to 100 to minimize waiting time for next reinitialization
static const int reinitAfterCycles = 100;

/* values of the bits in a byte */
static const unsigned char BIT_VALUES[] =
    { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };

#define __ddl_MCP ((DDL_MCP_DATA *) buses[busnumber].driverdata)
#define __ddl_MCPt ((DDL_MCP_DATA *) buses[btd->bus].driverdata)

int readconfig_MCP_FB(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    int i;

    buses[busnumber].driverdata = malloc(sizeof(struct _DDL_MCP_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_MCP;
    buses[busnumber].init_func = &init_bus_MCP;
    buses[busnumber].thr_func = &thr_sendrec_MCP;
    buses[busnumber].flags |= FB_ORDER_0;
    //buses[busnumber].flags |= FB_16_PORTS;

    __ddl_MCP->refresh = 100;


    strcpy(buses[busnumber].description, "FB POWER");
    __ddl_MCP->number_fb[0] = 0;
    __ddl_MCP->number_fb[1] = 0;
    __ddl_MCP->number_fb[2] = 0;
    __ddl_MCP->number_fb[3] = 0;
    __ddl_MCP->max_number_fb = 0;
    __ddl_MCP->MISO = 23; // Storing Default values before evaluating srcpd.conf
    __ddl_MCP->MOSI = 24; // see bcm2835.h for your pins
    __ddl_MCP->SCLK = 18;
    __ddl_MCP->CS   = 25;

    for (i = 1; i < 4; i++) {
        strcpy(buses[busnumber + i].description, "FB");
        buses[busnumber + i].type = SERVER_MCP;
        buses[busnumber + i].debuglevel = buses[busnumber].debuglevel;
        buses[busnumber + i].init_func = NULL;
        buses[busnumber + i].thr_func = &thr_sendrec_NoMCP;
        buses[busnumber + i].driverdata = NULL;
    }

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "waitus") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                /* better than atoi(3) */
                wait = strtol((char *) txt, (char **) NULL, 0);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "mosi") == 0) {
			txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
			if (txt != NULL) {
				/* better than atoi(3) */
				__ddl_MCP->MOSI = strtol((char *) txt, (char **) NULL, 0);
				xmlFree(txt);
			}
		}

        else if (xmlStrcmp(child->name, BAD_CAST "miso") == 0) {
			txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
			if (txt != NULL) {
				/* better than atoi(3) */
				__ddl_MCP->MISO = strtol((char *) txt, (char **) NULL, 0);
				xmlFree(txt);
			}
		}

        else if (xmlStrcmp(child->name, BAD_CAST "sclk") == 0) {
			txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
			if (txt != NULL) {
				/* better than atoi(3) */
				__ddl_MCP->SCLK = strtol((char *) txt, (char **) NULL, 0);
				xmlFree(txt);
			}
		}

        else if (xmlStrcmp(child->name, BAD_CAST "cs_1") == 0) {
			txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
			if (txt != NULL) {
				/* better than atoi(3) */
				__ddl_MCP->CS = strtol((char *) txt, (char **) NULL, 0);
				xmlFree(txt);
			}
		}

        else if (xmlStrcmp(child->name, BAD_CAST "refresh") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_MCP->refresh = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "fb_delay_time_0") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                set_min_time(busnumber, atoi((char *) txt));
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_fb_1") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __ddl_MCP->number_fb[0] = atoi((char *) txt);

                if (__ddl_MCP->number_fb[0] > __ddl_MCP->max_number_fb) {
                    __ddl_MCP->max_number_fb = __ddl_MCP->number_fb[0];
                }
                xmlFree(txt);
            }
        }

        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }

    if ((__ddl_MCP->max_number_fb * 2) > MCP_MAXPORTSB) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Zu viele MCP FB's konfiguriert!");
        return 0;
    }

    if ((__ddl_MCP->waitus > 0)) {
        syslog_bus(busnumber, DBG_ERROR,
                   "MCP23S17 wait activated %d [micro sec]!", __ddl_MCP->waitus);
        return 0;
    }
    
    if ( (__ddl_MCP->MISO < 0) | (__ddl_MCP->MISO > 27)) { // RPI_BPLUS_GPIO_J8_13 = 27
		syslog_bus(busnumber, DBG_ERROR,
				   "MISO user defined out of range :%d must be [0..27]", __ddl_MCP->MISO);
		return 1;
	}

    if ( (__ddl_MCP->MOSI < 0) | (__ddl_MCP->MOSI > 27)) { // RPI_BPLUS_GPIO_J8_13 = 27
		syslog_bus(busnumber, DBG_ERROR,
				   "MOSI user defined out of range :%d must be [0..27]", __ddl_MCP->MOSI);
		return 1;
	}

    if ( (__ddl_MCP->SCLK < 0) | (__ddl_MCP->SCLK > 27)) { // RPI_BPLUS_GPIO_J8_13 = 27
		syslog_bus(busnumber, DBG_ERROR,
				   "SCLK user defined out of range :%d must be [0..27]", __ddl_MCP->SCLK);
		return 1;
	}

    if ( (__ddl_MCP->CS < 0) | (__ddl_MCP->CS > 27)) { // RPI_BPLUS_GPIO_J8_13 = 27
		syslog_bus(busnumber, DBG_ERROR,
				   "CS user defined out of range :%d must be [0..27]", __ddl_MCP->CS);
		return 1;
	}

    for (i = 0; i < MCP_MAXBUSSES; i++)
    {   // init_FB is function in srcp_fb. Serves to
        if (init_FB(busnumber + i, __ddl_MCP->number_fb[i] * 16))
        {
            __ddl_MCP->number_fb[i] = 0;
            syslog_bus(busnumber + i, DBG_ERROR,
                       "Can't create array for mcp-feedback "
                       "channel %d", i + 1);
        }
        else {
            syslog_bus(busnumber + i, DBG_INFO,
                       "%d feeback contacts for channel %d successfully "
                       "initialized.", __ddl_MCP->number_fb[i] * 16,
                       i + 1);

        }
    }


    return (MCP_MAXBUSSES);
}
/****************************************************************
* MCP helperfunctions                                           *
* sendValue transfering a singe bit to MCP23S17 by operating the*
* connected RasPi GPIOs in a suitable manner                    *
* in: 8bit value to write                                                               *
****************************************************************/
void sendValue(bus_t busnumber, uint8_t value)
{
	int i = 0;
	//write value bitwise to MCP23S17 chip
	for (i = 0; i < 8; i++)
	{
		if (value & 0x80)
		{
			bcm2835_gpio_write(__ddl_MCP->MOSI, HIGH);
		}
		else
		{
			bcm2835_gpio_write(__ddl_MCP->MOSI, LOW);
		}
		bcm2835_gpio_write(__ddl_MCP->SCLK, HIGH);
		if (wait > 0){bcm2835_delayMicroseconds(wait);} // minimum Delay needed here
		bcm2835_gpio_write(__ddl_MCP->SCLK, LOW);
		if (wait > 0){bcm2835_delayMicroseconds(wait);} // minimum Delay needed here
		value <<= 1;
	}
}
/****************************************************************
* MCP helperfunctions                                           *
* sendSPI sends data to registerbyte of adress(ed) MCP23S17 chip*
* connected RasPi GPIOs in a suitable manner                    *
* uses call to sendValue
* in: busnumber needed for Macro __ddl_MCP                      *
* in: 8bit adress (range is [0..7]                              *
* because MCP23S17 supports 3 bit adressing)                    *
* in: 8 Bit register, see manual of MCP23S17                    *
* in: 8 bit data to be written into selected registerbyte       *
* ***************************************************************/
void sendSPI(bus_t busnumber, uint8_t adress, uint8_t registerbyte, uint8_t data)
{
	// Set value of an MCP (selected by adress [0..7] register to data
	// use sendValue
	if (adress < MAX_MCP_ADRESS)
	{
		//Read/WriteBit (Read = 1)
		//Adress  of this Chip   |
		//BaseAdress0x40______||||
		//                ||||||||
		//                0100AAAR
		uint8_t opcode = adress << 1;
		opcode = opcode | SPI_BASE_ADRESS;
		//MCP23S17 CS -> LOW is activ
		bcm2835_gpio_write(__ddl_MCP->CS, LOW);

		sendValue(busnumber, opcode); //Write
		sendValue(busnumber, registerbyte);
		sendValue(busnumber, data);

		bcm2835_gpio_write(__ddl_MCP->CS, HIGH);
	}
	else
	{
		syslog_bus(busnumber, DBG_INFO,"sendSPI range ERROR adress:%d registerbyte:%d data:%d", adress, registerbyte, data);
	}
}


/****************************************************************
* function initMCP23S17                                         *
*                                                               *
* purpose: configure all port of each connected MCP23S17        *
*          for input and activates internal pullups             *
* in:      busnumber needed for Macro __ddl_MCP                 *
* in:      adress [0..7] select 1 of max 8 chips connected to this bus*
* in [2]:  Each MCP23S17 has 2 port that are configured         *
*  out:     none                                                *
*                                                               *
* remarks:                                                      *
*                                                               *
****************************************************************/

void initMCP23S17(bus_t busnumber, uint8_t adress, uint8_t portsA, uint8_t portsB)
{
	// MCP23S17 prepare first 8 portbits, see also MCP23S17 data sheet
	sendSPI(busnumber, adress, SPI_PULLUP_A, portsA); //activate MCP23S17 pullups
	sendSPI(busnumber, adress, SPI_IODIRA, portsA);   //set In/Out
	sendSPI(busnumber, adress, SPI_POL_A, portsA);    //invertlogic
	// MCP23S17 prepare second 8 portbits
	sendSPI(busnumber, adress, SPI_PULLUP_B, portsB); //activate MCP23S17 pullups
	sendSPI(busnumber, adress, SPI_IODIRB, portsB);   //set In/Out
	sendSPI(busnumber, adress, SPI_POL_B, portsB);    //invertlogic

	//syslog_bus(busnumber, DBG_INFO,"initMCP23S17 adress:%d portsA:%d portsB:%d", adress, portsA, portsB);
}

/****************************************************************
* function activate adressing                                   *
*                                                               *
* purpose: copied from erik bartman python script,              *
*          did not try if it can be omitted                     *
* in:      busnumber needed for Macro __ddl_MCP                 *
* in:      adress [0..7] select 1 of max 8 chips connected to this bus*
* out:     none                                                *
*                                                               *
* remarks:                                                       *
*                                                               *
****************************************************************/
void activateAdressing(bus_t busnumber, uint8_t mcp)
{
	sendSPI(busnumber, mcp, SPI_CONFIG_A, SPI_HW_ADDR);
	sendSPI(busnumber, mcp, SPI_CONFIG_B, SPI_HW_ADDR);
}

/****************************************************************
* function read SPI                                             *
*                                                               *
* purpose: reads bitwise one byte from MCP23S17                 *
* in:      busnumber needed for Macro __ddl_MCP                 *
* in:      adress [0..7] select 1 of max 8 chips connected to this bus*
* in [2]:  registerbyte to read                                 *
* out:     read byte                                            *
*                                                               *
* remarks:                                                      *
*                                                               *
****************************************************************/
uint8_t readSPI(bus_t busnumber, uint8_t adress, uint8_t registerbyte)
{
	uint8_t retvalue = 0;
	uint8_t opcode = adress << 1 | SPI_BASE_ADRESS;

	bcm2835_gpio_write(__ddl_MCP->CS, LOW);         // CS low activ
	if (wait > 0){bcm2835_delayMicroseconds(wait);} // slowing down seems necessary
	sendValue(busnumber, opcode | SPI_SLAVE_READ);  // send opcode
	sendValue(busnumber, registerbyte);

	int i = 0;
	for (i = 0; i < 8; i++)
	{
		retvalue <<= 1; // shift 1 bit to left
		if ( HIGH == bcm2835_gpio_lev(__ddl_MCP->MISO) )
		{
			retvalue |= 0x01;
		}
		// generate falling
		bcm2835_gpio_write(__ddl_MCP->SCLK, HIGH);
		if (wait > 0){bcm2835_delayMicroseconds(wait);} // slowing down seems necessary
		bcm2835_gpio_write(__ddl_MCP->SCLK, LOW);
		if (wait > 0){bcm2835_delayMicroseconds(wait);} // slowing down seems necessary
	}
	bcm2835_gpio_write(__ddl_MCP->CS, HIGH);         // CS high inactiv
	if (wait > 0){bcm2835_delayMicroseconds(wait);} // slowing down seems necessary
	return retvalue;
}
/*******************************************************************/
/* will be called from srcpd.c during startup in init_all_buses() */
/*******************************************************************/
int init_bus_MCP(bus_t busnumber)
{
	// calling bcm_init must be done before this operation bcm2835_init() was already called before init_all buses in srcpd.c

    //RaspberryPI MCP23S17 init
    // we support only one feedback bus of MCP23S17 chips for now

	//syslog_bus(busnumber, DBG_INFO, "DDL_MCP init Pins MISO:%d MOSI:%d SCLK:%d. CS:%d",
	//             __ddl_MCP->MISO, __ddl_MCP->MOSI, __ddl_MCP->SCLK, __ddl_MCP->CS);

	//set used pins as input or output
	bcm2835_gpio_fsel(__ddl_MCP->MISO, BCM2835_GPIO_FSEL_INPT);
	bcm2835_gpio_fsel(__ddl_MCP->MOSI, BCM2835_GPIO_FSEL_OUTP);
	bcm2835_gpio_fsel(__ddl_MCP->SCLK, BCM2835_GPIO_FSEL_OUTP);
	bcm2835_gpio_fsel(__ddl_MCP->CS, BCM2835_GPIO_FSEL_OUTP);

	//initialize pin level
	bcm2835_gpio_write(__ddl_MCP->SCLK, LOW);
	bcm2835_gpio_write(__ddl_MCP->CS, HIGH);

	int busNo = 0;
	for (busNo=0;busNo<MCP_MAXBUSSES;busNo++) // currently we have one bus only
	{
		uint8_t mcp_adr = 0;

		for (mcp_adr = 0; mcp_adr < (__ddl_MCP->number_fb[busNo]); mcp_adr++)
		{
			activateAdressing(busnumber, mcp_adr);
			// configure both ports of each mcp for reading (0xff)
			initMCP23S17(busnumber, mcp_adr, 0xff, 0xff);
		}
		syslog_bus(busnumber, DBG_INFO, "initMCP23S17 bus:%d. max No MCPS:%d",busNo, __ddl_MCP->number_fb[busNo]);
	}

    //Am Anfang von alles 1 (MCP offener Eingang) ausgehen
    memset(__ddl_MCP->MCPdataBuffer, 1, sizeof(__ddl_MCP->MCPdataBuffer));
    __ddl_MCP->aktMCPDataBufferIndex = 0;

    return 0;
}

/****************************************************************
* function MCPload                                              *
*                                                               *
* purpose: Loads the data from the MCP bus in MCPdata if the    *
*          valid-                                               *
*          time space MCPREFRESH is over. Then also the new     *
*          validity-timeout is set to MCPvalid.                 *
*          If port is disabled or data is valid does nothing.   *
*                                                               *
* in:      bus number                                           *
* out:     ---                                                  *
*                                                               *
* remarks:                                                      *
*                                                               *
****************************************************************/
void MCPload(bus_t busnumber)
{
    int i, j, k, w, inbyteA, inbyteB;
    struct timeval nowtime;
    unsigned int MCPdata[MCP_MAXBUSSES][MAX_MCP_ADRESS*2];        /* valid bus-data */
    int MCPREFRESH = 1000 * __ddl_MCP->refresh;
    static int cycleCounter = 0;

    gettimeofday(&nowtime, NULL);
    if ((nowtime.tv_sec > __ddl_MCP->MCPvalid.tv_sec) ||
        ((nowtime.tv_sec == __ddl_MCP->MCPvalid.tv_sec) &&
         (nowtime.tv_usec > __ddl_MCP->MCPvalid.tv_usec)))
    {
        /* data is out of date - get new data from the bus */
        /* initialize the MCPdata array */
        memset(MCPdata, 0, sizeof(MCPdata));

		/* reading the data */
		int busNo = 0;
		uint8_t mcp_adr = 0;
		for (busNo=0;busNo<MCP_MAXBUSSES;busNo++) // currently we have one bus only
		{
			for (mcp_adr = 0; mcp_adr < (__ddl_MCP->number_fb[busNo]); mcp_adr++)
			{
			  /* reading 2 bytes from each of the supposed 8 MCP devices */
			  inbyteA = readSPI(busnumber, mcp_adr, SPI_GPIOA);
			  __ddl_MCP->MCPdataBuffer[busNo][__ddl_MCP->aktMCPDataBufferIndex][(mcp_adr * 2) + 1] = inbyteA;

			  inbyteB = readSPI(busnumber, mcp_adr, SPI_GPIOB);
			  __ddl_MCP->MCPdataBuffer[busNo][__ddl_MCP->aktMCPDataBufferIndex][(mcp_adr * 2)] = inbyteB;
			}
		}

		// fill for each FB module ANZ_MCP_WIEDERHOLUNGEN values in order to debounce data
		__ddl_MCP->aktMCPDataBufferIndex++;
		if (__ddl_MCP->aktMCPDataBufferIndex >= ANZ_MCP_WIEDERHOLUNGEN)
		{
		  __ddl_MCP->aktMCPDataBufferIndex = 0;

		}

		cycleCounter++;
		if (cycleCounter >= reinitAfterCycles)
		{
			cycleCounter = 0;
			for (busNo=0;busNo<MCP_MAXBUSSES;busNo++) // currently we have one bus only
			{
			  uint8_t mcp_adr = 0; // Don know if MCP23S17 are powered all time, therefore reiniatializing chips cyclically

			  for (mcp_adr = 0; mcp_adr < (__ddl_MCP->number_fb[busNo]); mcp_adr++)
			  {
				activateAdressing(busnumber, mcp_adr);
				// configure both ports of each mcp for reading (0xff)
				initMCP23S17(busnumber, mcp_adr, 0xff, 0xff);
			  }
			  // with reinitAfterCycles = 1000 my pi does this roughly every 2 minutes
			  // syslog_bus(busnumber, DBG_INFO, "MCP23S17 port maxAdress %d initialized", __ddl_MCP->number_fb[busNo]);
			}
		}
		// debouncing by voting
		for (busNo=0;busNo<MCP_MAXBUSSES;busNo++) // currently we have one bus only
		{
		  for (j=0;j<(__ddl_MCP->number_fb[busNo] * 2);j++)
		  {
			for (k=0;k<8;k++)
			{
			  int count = 0;
			  for (w=0; w<ANZ_MCP_WIEDERHOLUNGEN;w++)
			  {
				if (__ddl_MCP->MCPdataBuffer[busNo][w][j] & BIT_VALUES[k])
				{
				  count++;
				}
			  }
			  if (count > (ANZ_MCP_WIEDERHOLUNGEN / 2))
			  {
				MCPdata[busNo][j]+=BIT_VALUES[k];
			  }
			}
		  }
		}
		for (busNo=0; busNo<MCP_MAXBUSSES; busNo++) // currently MCP_MAXBUSSES is set to 1
		{
		  for (j=0;j<(__ddl_MCP->number_fb[busNo] * 2);j++)
		  {
			setFBmodul(busnumber, j + 1, MCPdata[busNo][j]);
			//syslog_bus(busnumber, DBG_ERROR, "modul j:%d 'MCPdata(j) %x'.", j, MCPdata[busNo][j]);
		  }
		}

		nowtime.tv_usec += MCPREFRESH;
		__ddl_MCP->MCPvalid.tv_usec = nowtime.tv_usec % 1000000;
		__ddl_MCP->MCPvalid.tv_sec =
		nowtime.tv_sec + nowtime.tv_usec / 1000000;
    }
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    #ifdef MCP_DEBUG
    printf("MCPend_bus_thread\n");
    #endif
    int result;

    syslog_bus(btd->bus, DBG_INFO, "DDL-MCP bus terminated.");
    result = pthread_mutex_destroy(&buses[btd->bus].transmit_mutex);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_WARN,
                   "pthread_mutex_destroy() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    result = pthread_cond_destroy(&buses[btd->bus].transmit_cond);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_WARN,
                   "pthread_mutex_init() failed: %s (errno = %d).",
                   strerror(result), result);
    }
    
    free(buses[btd->bus].driverdata);
    free(btd);
}

void *thr_sendrec_MCP(void *v)
{
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));
    if (btd == NULL)
        pthread_exit((void *) 1);
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);

    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    {
        //Thread als Realtime setzen, Prio aber tiefer als bei DDL-Thread.
        int policy;
        struct sched_param sparam;
        int result = pthread_getschedparam(pthread_self(), &policy, &sparam);
        if (result != 0) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "MCP pthread_getschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
        sparam.sched_priority = 5;
        result =
            pthread_setschedparam(pthread_self(), SCHED_FIFO, &sparam);
        if (result != 0) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "MCP pthread_setschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
    }

    unsigned long int sleepusec = 10000;

    int MCPREFRESH =
        ((DDL_MCP_DATA *) buses[btd->bus].driverdata)->refresh;
    /* set refresh-cycle */
    if (sleepusec < MCPREFRESH * 1000)
        sleepusec = MCPREFRESH * 1000;

    syslog_bus(btd->bus, DBG_INFO, "DDL_MCP bus startet (wait %d [micro sec]).",
               __ddl_MCPt->waitus);

    while (1) {

        if (buses[btd->bus].power_changed == 1) {
            char msg[110];
            buses[btd->bus].power_changed = 0;
            infoPower(btd->bus, msg);
            enqueueInfoMessage(msg);
        }

        check_reset_fb(btd->bus);

        if (usleep(sleepusec) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed in MCP thread: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        MCPload(btd->bus);
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    #ifdef MCP_DEBUG
    printf("MCP Thread pthread_cleanup_pop\n");
    #endif
    return NULL;
}

void *thr_sendrec_NoMCP(void *v)
{
    int result;

    while (true) {
        result = sleep(1);
        if (result != 0) {
            syslog_bus(0, DBG_ERROR,
                       "sleep() interrupted, %d seconds left\n", result);
        }
    }
}

/*---------------------------------------------------------------------------
 * End of Linux Code
 *---------------------------------------------------------------------------*/
