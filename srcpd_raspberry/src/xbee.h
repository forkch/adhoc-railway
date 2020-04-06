/***************************************************************************
                          xbee.h  -  description
                             -------------------
    copyright            : (C) 2013 by Daniel Sigg
    email                : daniel@siggsoftware.ch
    
    30.05.13 Neuerstellung. Funktionen für GA und FB. 
    28.08.13 Wiederherstellung eingeschalteter Ausgänge nach Restart 
             (Power Off - On) eines Moduls.
    07.09.13 RTS/CTS
    08.09.13 Nach einer "Join Notification" Meldung wird ein ND ausgelöst.
    28.10.13 Verbesserte Node Erkennung.
    
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/
 
 /**
  * Interface zu Digi XBee Modulen.
  * Diese können sowohl als Input (FB, analog S88) als auch als Output (GA)
  * für Weichen und Signale verwendet werden.
  * Es werden alle erreichbaren XBee Module deren Name mit "SRCP_" beginnt berücksichtigt.
  * Alle Konfigurierten Inputs werden als FB genommen, alle Outpts können
  * über GA angesprochen werden.
  * Die XBee Module definieren über ihren Namen die Adresse der FB und GA:
  * SRCP_xx
  * Dabei ist xx die Adresse, beginnend bei 01 bis max. 99. (0 ist nicht erlaubt)
  * Für FB wird analog S88 pro XBee Modul mit 16 Inputs gerechnet,
  * auch wenn ein Modul nicht soviele Inputs hat.
  * Für GA wird pro Modul mit 8 Elementen (8 Outputs für Magnetartikel) gerechnet.
  * 
  * Die Module müssen wie folgt konfiguriert sein:
  * - IC (Digital IO Change Detection): für jeden Input gesetzt
  *      Wird dies auch für Outputs (siehe D0-5, P0-2) gesetzt, wird der entsprechende
  *      Output automatisch als Feedback gemeldet. Man kann so einfach
  *      eine Rückmeldung machen und hat die Kontrolle, ob ein Outputbefehl
  *      auch umgesetzt wurde :-)
  * - D0-5, P0-2: Auf 3 für Inputs, muss zu IC passen, auf 4 (Out Low)
  *               für Outputs (nur Output Low wird als Output aufgenommen!)
  * - NI (Node Identifier): SRCP_xx, xx muss eindeutig sein von 00 bis 99, immer 2 Stellen!
  * - AP = 2 -> API Mode Escape Character enabled
  * - JN = 1 -> Join Notification eingeschaltet
  */
  
#ifndef _XBEE_H
#define _XBEE_H

#include <libxml/tree.h> /*xmlDocPtr, xmlNodePtr*/
#include <stdint.h>
#include <time.h>

//Max. unterstützte Anzahl XBee Module
#define MAX_XBEE_MODULES 100
//Max. Anzahl Input eines Moduls. Damit Kompatibel zu S88 wird 16 genommen, 
//auch wenn das Modul gar nicht soviel Inputs hat
#define MAX_XBEE_INPUTS 16
//Max. Anzahl GA Outputs eines Modul. Da In- und Outputs auf 16 Ausgelegt sind
//werden, auch wenn nicht 16 tatsächlich verwendet werden können, 16 GA's
//pro XBee Modul angenommen.
//DIO0 ist GA1 bis DIO12 GA13 für GA Port 0. Mit GA Port 1 wird je der nächste
//Output adressiert. GA1 Port 1 ist also auch GA2 Port 0!
//Dies deshalb, damit auch eine Weiche an z.B.  DIO3 und 4 als ein Element GA4 angesprochen werden kann.
#define MAX_XBEE_GA 16

/**
 * Die Daten eines XBee Moduls.
 * */
typedef struct _XBEE_MODUL {
  //64 Bit Adresse, 0 = Nicht belegt.
  uint64_t adr64;
  //16 Bit Adresse, 0 = Nicht belegt
  uint16_t adr16;
  //Welche DIO sind als Output konfiguriert (auf diese darf ein Outputbefehl abgesetzt werden)
  uint16_t outputs;
  //Spiegel des aktuellen Output Zustandes
  uint16_t outputState;
  //Nicht erkannter Output wurde verlangt -> nach erneutem "Node Discovery"
  //werden nochmals alle IO Konfigurationen des XBee Moduls abgefragt.
  bool outputMissing;
} XBEE_MODUL;

typedef struct _XBEE_DATA {
  //Zeit in ms, die ein Schaltkommando aktiv ist.
  unsigned int ga_min_active_time;
  //Letzter Zeitpunkt an dem ein ND gesendet wurde
  time_t lastTimeND;
  //Verwaltung aller gefundenen XBee Module. 
  //Index ist die logische Adresse, definiert durch den Namen des Moduls ("SRCP_xx").
  XBEE_MODUL xBeeModules[MAX_XBEE_MODULES];
} XBEE_DATA;

int readconfig_XBEE(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber);

int init_bus_XBEE(bus_t);
int init_ga_XBEE(ga_state_t *ga);
int getDescription_XBEE(char *reply);
void *thr_sendrec_XBEE(void *);

#endif
