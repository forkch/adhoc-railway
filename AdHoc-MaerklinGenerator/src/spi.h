/*
 * spi.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 */

#ifndef SPI_H_
#define SPI_H_


#define DDR_SPI DDRB
#define DD_MOSI PB5
#define DD_MISO PB6
#define DD_SCK PB7

#define SS_PORT PORTC
#define SS_PORT_DDR DDRC
#define SS_GO PC1
#define SS_SHORT PC2
#define SS_DBG_LED PC3


void SPI_MasterInit();
void SPI_MasterTransmitDebug(unsigned char);
void SPI_MasterTransmitGO(unsigned char);
void SPI_MasterTransmitShort(unsigned char);

#endif /* SPI_H_ */
