/*
 * spi.h
 *
 *  Created on: 03.02.2012
 *      Author: fork
 *
 *  Multiprotcol-Version (MM/MM2/MFX/DCC)
 *    Added on: 06.06.2016
 *      Author: m2
 *
 */

#ifndef SPI_H_
#define SPI_H_


#define DDR_SPI DDRB
#define DD_MOSI PB5
#define DD_MISO PB6
#define DD_SCK PB7
#define DD_SS PB4

#define SS_PORT PORTC
#define SS_PORT_DDR DDRC
#define SS_GO PC1
#define SS_SHORT PC2
#define SS_DBG_LED PC3


void SPI_MasterInitOutput();
void SPI_MasterInitInput();
void SPI_MasterTransmitDebug(unsigned char);
void SPI_MasterTransmitGO(unsigned char);
unsigned char SPI_MasterReceiveShort();

#endif /* SPI_H_ */
