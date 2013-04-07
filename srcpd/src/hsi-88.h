/***************************************************************************
                          hsi-88.h  -  description
                             -------------------
    begin                : Mon Oct 29 2001
    copyright            : (C) 2001 by Dipl.-Ing. Frank Schmischke
    email                : frank.schmischke@t-online.de
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
#ifndef _HSI_88_H
#define _HSI_88_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/


typedef struct _HSI_88_DATA
{
  int number_fb[3];
  int refresh;
  char v_text[50];
} HSI_88_DATA;

int readConfig_HSI_88(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

int init_bus_HSI_88(bus_t busnumber);
int get_bus_config_HSI_88(bus_t busnummer, int *num_gl, int *num_ga,
                int *num_fb);
void* thr_sendrec_HSI_88(void*);

#endif
