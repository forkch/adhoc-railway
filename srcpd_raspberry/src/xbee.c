/***************************************************************************
                          xbee.h  -  description
                             -------------------
    begin                : Wed Aug 1 2013
    copyright            : (C) 2013 by Daniel Sigg
    email                : daniel@siggsoftware.ch
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
#include <fcntl.h>
#include <sys/ioctl.h>
#include <unistd.h>

#define DBG_PRINT 1

#ifdef __CYGWIN__
#include <sys/socket.h>         /*for FIONREAD */
#endif

#ifdef __sun__
#include <sys/filio.h>
#endif

#include "config-srcpd.h"
#include "io.h"
#include "xbee.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-power.h"
#include "srcp-info.h"
#include "srcp-server.h"
#include "srcp-error.h"
#include "syslogmessage.h"
#include "ttycygwin.h"

#define __xbee ((XBEE_DATA*)buses[busnumber].driverdata)

#define XAPI_START  0x7E
#define XAPI_ESCAPE 0x7D
#define XAPI_XON    0x11
#define XAPI_XOFF   0x13
//Exor für Escape Sequenz
#define XAPI_ESCAPE_XOR 0x20
//Grösse Frame Overhead (Start Delimiter, 2 Byte Length, Checksum)
#define XAPI_FRAME_SIZE 4
//Max. Länge NI (Name)
#define NI_MAX_LEN 20

//XBee Frame Typs
#define FTYP_AT_COMMAND            0x09
#define FTYP_TRANSMIT_REQUEST      0x10
#define FTYP_EXPLICIT_ADDRESSING   0x11
#define FTYP_REMOTE_AT_COMMAND     0x17
#define FTYP_CREATE_SOURCE_ROUTE   0x21
#define FTYP_AT_COMMAND_RESPONSE   0x88
#define FTYP_MODEM_STATUS          0x8A
#define FTYP_TRANSMIT_STATUS       0x8B
#define FTYP_RX_PAKET              0x90
#define FTYP_RX_IND                0x91
#define FTYP_IO_DATA_RX_IND        0x92
#define FTYP_SENSOR_READ_IND       0x94
#define FTYP_NODE_IDENT_IND        0x95
#define FTYP_REMOTE_CMD_RESPONSE   0x97
#define FTYP_FW_UPDATE_STATUS      0xA0
#define FTYP_ROUTE_REC_IND         0xA1
#define FTYP_MANY_ROUTE_REC_IND    0xA3

#define CMD_OPTIONS_APPLY_CHANGES  0x02

#define DIO_OUTPUT_LOW  4
#define DIO_OUTPUT_HIGH 5

#define ND_BLOCK_TIMEOUT 30

typedef struct _XBEE_API_FRAME {
  //Totale Länge mit frameTyp
  uint16_t length;
  uint8_t frameTyp;
  union {
    uint8_t frameData[254]; //Mit Typ max 255 Byte langes Frame, mehr wird hier nicht unterstützt
    uint8_t frameID;
  };
} XBEE_API_FRAME;

/**
 * Mapping IO Port auf AT Kommando.
 * NULL: Port nicht vorhanden / nicht verwendbar.
 */
static const char* IO_AT_CMD[] = {
  "D0", //0
  "D1", //1
  "D2", //2
  "D3", //3
  "D4", //4
  "D5", //5
  NULL, //6
  NULL, //7
  NULL, //8
  NULL, //9
  "P0", //10
  "P1", //11
  "P2", //12
  NULL, //13
  NULL, //14
  NULL  //15
};

void sendNodeDiscovery(bool force, bus_t busnumber);

/**
 * readconfig_XBEE: Liest den Teilbaum der xml Configuration und
 * parametriert den busspezifischen Datenteil, wird von register_bus()
 * aufgerufen.
 **/

int readconfig_XBEE(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    #ifdef DBG_PRINT
    printf("readconfig_XBEE\n");
    #endif
    buses[busnumber].driverdata = malloc(sizeof(struct _XBEE_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_XBEE;
    buses[busnumber].init_func = &init_bus_XBEE;
    buses[busnumber].thr_func = &thr_sendrec_XBEE;
    buses[busnumber].init_gl_func = NULL;
    buses[busnumber].init_ga_func = &init_ga_XBEE;
    buses[busnumber].flags |= FB_16_PORTS;
    buses[busnumber].flags |= FB_ORDER_0;
    
    /* Driver spezifiische Daten init */
    __xbee->ga_min_active_time = 100;

    strcpy(buses[busnumber].description, "GA FB LOCK DESCRIPTION");

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }
        else if (xmlStrcmp(child->name, BAD_CAST "ga_min_activetime") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __xbee -> ga_min_active_time = atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else {
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;
        }
        child = child->next;
    }

    if (init_GA(busnumber, MAX_XBEE_MODULES * MAX_XBEE_GA)) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessories");
    }

    if (init_FB(busnumber, MAX_XBEE_MODULES * MAX_XBEE_INPUTS)) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }
    
    //Alle XBee Modul Verwaltungsdaten auf 0 setzen (alles frei, kein Modul, kein Output)
    memset (__xbee->xBeeModules, 0, sizeof(__xbee->xBeeModules));

    return 1;
}


/*******************************************************
 *     configure serial line
 *******************************************************/
static int init_lineXBEE(bus_t busnumber)
{
    #ifdef DBG_PRINT
    printf("init_lineXBEE %s\n", buses[busnumber].device.file.path);
    #endif

    int fd;
    struct termios interface;

    if (buses[busnumber].debuglevel > 0) {
        syslog_bus(busnumber, DBG_INFO, "Opening XBee: %s",
                   buses[busnumber].device.file.path);
    }

    fd = open(buses[busnumber].device.file.path, O_RDWR | O_NONBLOCK);
    if (fd == -1) {
        syslog_bus(busnumber, DBG_ERROR, "Open serial device '%s' failed: %s "
                   "(errno = %d).\n", buses[busnumber].device.file.path,
                   strerror(errno), errno);
        #ifdef DBG_PRINT
        printf("Open serial device '%s' failed: %s "
                   "(errno = %d).\n", buses[busnumber].device.file.path,
                   strerror(errno), errno);
        #endif
        return -1;
    }
    tcgetattr(fd, &interface);
#ifdef linux
    interface.c_cflag = CS8 | CREAD | CLOCAL | CRTSCTS;
    interface.c_oflag = 0;
    interface.c_iflag = IGNBRK;
    interface.c_iflag &= ~(ISTRIP | IXON | IXOFF | IXANY);
    interface.c_lflag = NOFLSH | IEXTEN;
    interface.c_lflag &= ~(ISIG | ICANON | ECHO | ECHOE | TOSTOP | PENDIN);
#else
    cfmakeraw(&interface);

    interface.c_cflag = CREAD | HUPCL | CS8;
#endif
    cfsetospeed(&interface, B115200);
    cfsetispeed(&interface, B115200);

    tcsetattr(fd, TCSANOW, &interface);
    syslog_bus(busnumber, DBG_INFO, "Opening XBee succeeded (fd = %d).", fd);
    #ifdef DBG_PRINT
    printf("Opening XBee succeeded (fd = %d).\n", fd);
    #endif
    return fd;
}

int init_bus_XBEE(bus_t busnumber)
{
    static char *protocols = "MN"; //M=Märklin, N=NMRADDC, egal was der Anwendern dann wählt.
    buses[busnumber].protocols = protocols;
    syslog_bus(busnumber, DBG_INFO, "XBee  init: debug %d",
               buses[busnumber].debuglevel);
    if (buses[busnumber].debuglevel <= DBG_DEBUG) {
        buses[busnumber].device.file.fd = init_lineXBEE(busnumber);
    }
    else {
        buses[busnumber].device.file.fd = -1;
    }
    syslog_bus(busnumber, DBG_INFO, "XBee init done, fd=%d",
               buses[busnumber].device.file.fd);
    syslog_bus(busnumber, DBG_INFO, "XBee: %s", buses[busnumber].description);
    syslog_bus(busnumber, DBG_INFO, "XBee flags: %d",
               buses[busnumber].flags & AUTO_POWER_ON);
    return 0;
}

/**
 * initGA: modifies the ga data used to initialize the device
 **/
int init_ga_XBEE(ga_state_t * ga)
{
  if ((ga->protocol == 'M') || (ga->protocol == 'N') || (ga->protocol == 'P')) {
    return SRCP_OK;
  }
  return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    #ifdef DBG_PRINT
    printf("XBee bus terminated\n");
    #endif
    syslog_bus(btd->bus, DBG_INFO, "XBee bus terminated.");
    if (buses[btd->bus].device.file.fd != -1) {
      close(buses[btd->bus].device.file.fd);
    }

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

/**
 * Fügt ein Zeichen in den XBee aPI Ausgabebuffer hinzu.
 * Es wird berücksichtigt, wenn eine Escape Sequenz notwendig ist.
 * @param buffer Der Ausgabebuffer
 * @param c Zeichen, das ergänzt werden soll
 * @param freePos Die nächste freie Position. Wird erhöht.
 */
static void addCharToXBeeAPIBuffer(uint8_t *buffer, uint8_t c, unsigned int *freePos) {
  if ((c == XAPI_ESCAPE) || (c == XAPI_START) || (c == XAPI_XOFF) || (c == XAPI_XON)) {
    buffer[*freePos] = XAPI_ESCAPE;
    (*freePos)++;
    c ^= XAPI_ESCAPE_XOR;
  }
  buffer[*freePos] = c;
  (*freePos)++;
}

/**
 * Sendet ein XBee API Frame.
 * Rahmen (Start, Checksumme) und Byte-Escape Character Ersatz werden automatische erzeugt.
 * @param bus Bus, für den gesendet werden soll
 * @param apiFrame Das zu sendende Frame
 */
static void sendXBeeAPIFrame(bus_t busnumber, const XBEE_API_FRAME *frame) {
  uint8_t buffer[(sizeof(XBEE_API_FRAME) + XAPI_FRAME_SIZE) * 2]; //So dass es auch reicht, wenn alles mit Escape ersetzt werden muss
  buffer[0] = XAPI_START;
  int freePos = 1;
  //MSB Länge
  addCharToXBeeAPIBuffer(buffer, (frame->length >> 8) & 0xFF, &freePos);
  //LSB Länge
  addCharToXBeeAPIBuffer(buffer, frame->length & 0xFF, &freePos);
  //Frame Typ
  addCharToXBeeAPIBuffer(buffer, frame->frameTyp, &freePos);
  uint8_t checksum = frame->frameTyp; //Checksumme beginnt mit Frame Typ
  int i;
  for (i=0; i < (frame->length - 1); i++) { //Framelänge ist mit Frame Typ
    addCharToXBeeAPIBuffer(buffer, frame -> frameData[i], &freePos);
    checksum += frame -> frameData[i];
  }
  addCharToXBeeAPIBuffer(buffer, 0xFF - checksum, &freePos);

  /*#ifdef DBG_PRINT
  printf("sendXBeeAPIFrame, len=%d, Frame=[", freePos);
  {
    int ii;
    for (ii=0; ii<freePos; ii++) {
      printf("0x%02x,", buffer[ii]);
    }
  }
  printf("]\n");
  #endif*/

  i = write(buses[busnumber].device.file.fd, buffer, freePos);
  if (i != freePos) {
    #ifdef DBG_PRINT
    printf("(FD: %d) XBee Frame write failed: %s (errno = %d)\n",
                       buses[busnumber].device.file.fd, strerror(errno), errno);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "(FD: %d) XBee Frime write failed: %s (errno = %d)\n",
                       buses[busnumber].device.file.fd, strerror(errno), errno);
  }
  else {
    #ifdef DBG_PRINT
    printf("(FD: %d) %i Bytes XBee Frame sent: ID=0x%02x, len=%d\n",
               buses[busnumber].device.file.fd, i, frame->frameData[0], frame->length);
    #endif
    syslog_bus(busnumber, DBG_DEBUG, "(FD: %d) %i Bytes XBee Frame sent: ID=0x%02x, len=%d\n",
               buses[busnumber].device.file.fd, i, frame->frameData[0], frame->length);
  }
}

/**
 * Sendet ein XBee API AT Kommando Frame.
 * @param busnumber Bus, für den gesendet werden soll
 * @param atCommand Das zu sendene AT Kommando
 * @param frameID Die zu verwendende Frame ID, bei 0 erfolgt kein "AT Response"
 */
static void sendXBeeAPI_AT_CMD(bus_t busnumber, const unsigned char *atCMD, uint8_t frameID) {
  XBEE_API_FRAME xBeeFrame;
  #ifdef DBG_PRINT
  printf("sendXBeeAPI_AT_CMD(): %s\n", atCMD);
  #endif
  int len = strlen(atCMD);
  if (len > (sizeof(xBeeFrame.frameData) - 1)) { //-1 wegen ID
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_AT_CMD(): Command to long: %s\n", atCMD);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_AT_CMD(): Command to long: %s\n", atCMD);
    return;
  }
  xBeeFrame.length = 2 + len;
  xBeeFrame.frameTyp = FTYP_AT_COMMAND;
  xBeeFrame.frameID = frameID;
  strncpy(&(xBeeFrame.frameData[1]), atCMD, len);
  sendXBeeAPIFrame(busnumber, &xBeeFrame);
}

/**
 * Sendet ein XBee API AT Remote Kommando Frame.
 * @param bus Bus, für den gesendet werden soll
 * @param srcpAdr Die srcp Adresse 1-99, an die das Remote AT Command gesendet werden soll.
 *                Wenn für diese Adresse kein Modul registriert ist, wird nichts gesendet.
 * @param atCommand Das zu sendene AT Kommando, max 2 Zeichen
 * @param atParameter Alle Parameter, die zum AT Kommando gesendet werden sollen 
 *                    oder NULL falls keine.
 * @param paramCount Anzahl Parameter Bytes, die gesendet werden (zu atParameter).
 * @param frameID Die zu verwendende Frame ID, bei 0 erfolgt kein "AT Response"
 * @param applyChanges Wenn true werden Änderungen auf dem Modul sofort wirksam,
 *                     bei false erst nach Kommando AC.
 */
static void sendXBeeAPI_remote_AT_CMD(bus_t busnumber, unsigned int srcpAdr, 
                                      const unsigned char *atCMD, uint8_t atParameter[], unsigned int paramCount, 
                                      uint8_t frameID, bool applyChanges) {
  XBEE_API_FRAME xBeeFrame;
  int i;
  #ifdef DBG_PRINT
  printf("sendXBeeAPI_remote_AT_CMD(): to SRCP Adr %d, CMD: %s\n", srcpAdr, atCMD);
  #endif
  //XBee Adressen aus der srcpAdr ermitteln
  if (srcpAdr >= MAX_XBEE_MODULES) {
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_remote_AT_CMD(): SRCP Adr %d not valid!\n", srcpAdr);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_remote_AT_CMD(): SRCP Adr %d not valid!\n", srcpAdr);
    return;
  }
  uint64_t adr64 = __xbee -> xBeeModules[srcpAdr].adr64;
  uint16_t adr16 = __xbee -> xBeeModules[srcpAdr].adr16;
  if (adr64 == 0) {
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_remote_AT_CMD(): no XBee Modul found for SRCP Adr %d!\n", srcpAdr);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_remote_AT_CMD(): no XBee Modul found for SRCP Adr %d!\n", srcpAdr);
    //Falls nicht alle vorhandenen Module erkannt wurden wird nun nochmals alles abgefragt.
    sendNodeDiscovery(true, busnumber);
    return;
  }
  if (adr16 == 0) {
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_remote_AT_CMD(): no XBee Modul 16 Bit Adress found, SRCP Adr %d. Using 0xFFFE!\n", srcpAdr);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_remote_AT_CMD(): no XBee Modul 16 Bit Adress found, SRCP Adr %d. Using 0xFFFE!\n", srcpAdr);
    adr16 = 0xFFFE;
  }
  int len = strlen(atCMD);
  if (len > 2) {
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_remote_AT_CMD(): AT Command to long %d, max len 2!\n", len);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_remote_AT_CMD(): AT Command to long %d, max len 2!\n", len);
    return;
  }
  len += (atParameter != NULL) ? paramCount : 0; //Wenn keine Paramater angegeben sind wird paramCount ignoriert
  if (len > (sizeof(xBeeFrame.frameData) - 12)) { //-12 wegen ID, 64 & 16 Bit Adr, CMD Options
    #ifdef DBG_PRINT
    printf("sendXBeeAPI_remote_AT_CMD(): Command to long: %s\n", atCMD);
    #endif
    syslog_bus(busnumber, DBG_ERROR, "sendXBeeAPI_remote_AT_CMD(): Command to long: %s\n", atCMD);
    return;
  }
  xBeeFrame.length = 13 + len;
  xBeeFrame.frameTyp = FTYP_REMOTE_AT_COMMAND;
  xBeeFrame.frameID = frameID;
  //Nach ID kommen Adr64, Adr 16, CMD Options
  for (i=8; i>0; i--) {
    xBeeFrame.frameData[i] = adr64 & 0xFF;
    adr64 >>= 8;
  } 
  for (i=10; i>8; i--) {
    xBeeFrame.frameData[i] = adr16 & 0xFF;
    adr16 >>= 8;
  }
  xBeeFrame.frameData[11] = applyChanges ? CMD_OPTIONS_APPLY_CHANGES : 0;
   
  strncpy(&(xBeeFrame.frameData[12]), atCMD, 2);
  if (atParameter != NULL) {
    for (i=0; i<paramCount; i++) {
      xBeeFrame.frameData[12 + strlen(atCMD) + i] = atParameter[i];
    }
  }
  
  sendXBeeAPIFrame(busnumber, &xBeeFrame);
}

/**
 * Liefert die 64 Bit Adresse aus Frame an angegebener Position.
 * @param xBeeFrame Das empfange Frame
 * @param posAdr64 Die Position der 64 Bit Adresse in "frameData".
 * @return Die ermittelte 64 Bit Adresse.
 */
static uint64_t get64BitAdr(const XBEE_API_FRAME *xBeeFrame, unsigned int posAdr64) {
  uint64_t adr64 = 0;
  int i;
  for (i=0; i<8; i++) {
    adr64 <<= 8;
    adr64 |= xBeeFrame->frameData[posAdr64 + i];
  }
  return adr64;
}

/**
 * Liefert die 16 Bit Adresse aus Frame an angegebener Position.
 * @param xBeeFrame Das empfange Frame
 * @param posAdr16 Die Position der 16 Bit Adresse in "frameData".
 * @return Die ermittelte 16 Bit Adresse.
 */
static uint16_t get16BitAdr(const XBEE_API_FRAME *xBeeFrame, unsigned int posAdr16) {
  uint16_t adr16 = 0;
  int i;
  for (i=0; i<2; i++) {
    adr16 <<= 8;
    adr16 |= xBeeFrame->frameData[posAdr16 + i];
  }
  return adr16;
}

/**
 * Setzen eines Outputs auf einem Modul.
 * Achtung: Es erfolgt keine Kontrolle, dass der Output auf dem Modul
 * auch als Output konfiguriert ist. Es kann also ein Input damit auf
 * Outout konfiguriert werden! Es liegt in der Verantwortung des 
 * Anwenders, dies NICHT zu tun. Er muss wissen, was auf dem Modul
 * konfiguriert ist.
 * @param busnumber Der SRCP Bus, auf dem gearbeitet wird.
 * @param srcpAdr Die SRCP Adresse des XBee Moduls.
 * @param output Der Output, der gesetzt werden soll.
 *               Prinzipiell erlaubt, da vorhanden, sind (Mapping analog IC CMD): 
 *               0 : DIO0, PIN20, D0
 *               1 : DIO1, PIN19, D1
 *               2 : DIO2, PIN18, D2
 *               3 : DIO3, PIN17, D3
 *               4 : DIO4, PIN11, D4
 *               5 : DIO5, PIN15, D5
 *              10 : DIO10, PIN6, P0
 *              11 : DIO11, PIN7, P1
 *              12 : DIO12, PIN4, P2
 * @param value true : Ausgangs auf 1 setzen, false auf 0
 */
static void setXBeeOutput(bus_t busnumber, unsigned int srcpAdr, unsigned int output, bool value) {
  if ((output > 15) || (IO_AT_CMD[output] == NULL)) {
    #ifdef DBG_PRINT
    printf("setXBeeOutput(): Output %d not vaild, ignored.\n", output);
    #endif
    syslog_bus(busnumber, DBG_WARN, "setXBeeOutput(): Output %d not vaild, ignored.\n", output);
    return;
  }
  if ((__xbee -> xBeeModules[srcpAdr].outputs & (1 << output)) == 0) {
    #ifdef DBG_PRINT
    printf("setXBeeOutput(): XBee Modul SRCP Adr. %d: DIO %d is not a output -> start new Node Discovery.\n", srcpAdr, output);
    #endif
    syslog_bus(busnumber, DBG_WARN, "setXBeeOutput(): XBee Modul SRCP Adr. %d: %d is not a output -> start new Node Discovery.\n", srcpAdr, output);
    __xbee -> xBeeModules[srcpAdr].outputMissing = true;
    sendNodeDiscovery(true, busnumber);
    return;
  }
  uint8_t atParam = value ? DIO_OUTPUT_HIGH : DIO_OUTPUT_LOW;
  sendXBeeAPI_remote_AT_CMD(busnumber, srcpAdr, IO_AT_CMD[output], &atParam, 1, 0, true);
  //Output Spiegel aktualisieren
  if (value) {
    __xbee -> xBeeModules[srcpAdr].outputState |= (1 << output);
  }
  else {
    __xbee -> xBeeModules[srcpAdr].outputState &= ~(1 << output);
  }
}

/**
 * Abfrage der gesamten IO Konfiguration (zur Ermittlung der Outputs) eines XBee Moduls.
 * @param bus Bus, mit dem gearbeitet wird.
 * @param srcpAdr SRCP Adr. des Moduls das abgefragt werden soll.
 */
void requestIOConfig(bus_t busnumber, unsigned int srcpAdr) {
  int i;
  for (i=0; i<16; i++) {
     if (IO_AT_CMD[i] != NULL) {
       sendXBeeAPI_remote_AT_CMD(busnumber, srcpAdr, IO_AT_CMD[i], NULL, 0, 0x01, false);
     }
   }
 }

/**
 * Aufnahme eines über ein API Frame gemeldeten SRCP Nodes.
 * Wenn noch nicht vorhanden, wird der Node aufgenommen.
 * Wenn identische 64 Bit Adresse bereits vorhanden ist, wird die 16 Bit Adresse aktualisiert.
 * Wenn ein weiterer Node mit selben Namen schon vorhanden ist:
 * -> Fehlermeldung, Node wird ignoriert.
 * Wenn der Name des Node ungültig ist (!= SRCP_xx):
 * -> Fehlermeldung, Node wird ignoriert.
 * Wenn Adresse des Nodes (xx) ungültig ist (erlaubt sind 01 bis 99)
 * -> Fehlermeldung, Node wird ignoriert.
 * Wenn der Node schon vorhanden war: Wiederherstellung der zuletzt aktiven Outputs.
 * 
 * @param bus Bus, mit dem gearbeitet wird.
 * @param xBeeFrame Das empfange Frame
 * @param posAdr16 Position der 16 Bit Adresse des Module in frameData
 * @param posAdr64 Position der 64 Bit Adresse des Module in frameData
 * @param posNI Position Node Identification (Name) in frameData
 */
static void addSRCPNode(bus_t busnumber, const XBEE_API_FRAME *xBeeFrame,
                        unsigned int posAdr16, unsigned int posAdr64, unsigned int posNI) {
  uint64_t adr64;
  uint16_t adr16;
  unsigned int srcpAdr;
  char ni[NI_MAX_LEN + 1];
  char *check;
  int i;

  //64 Bit Adresse, MSB zuerst
  adr64 = get64BitAdr(xBeeFrame, posAdr64);
  //16 Bit Adresse
  adr16 = get16BitAdr(xBeeFrame, posAdr16);
  //Nun noch den NI String lesen, dieser ist max. 20 Zeichen lang
  strncpy(ni, &(xBeeFrame->frameData[posNI]), NI_MAX_LEN);
  ni[NI_MAX_LEN] = 0; //Falls max Länge erreicht wurde kein 0 ergänzt.
  //Der Name muss nun SRCP_xx sein
  if ((strncmp(ni, "SRCP_", 5) != 0) || (strlen(ni) < 7)) {
    #ifdef DBG_PRINT
    printf("xBeeFrameRx(): no SRCP_xx Node found '%s', ignored\n", ni);
    #endif
    syslog_bus(busnumber, DBG_WARN, "xBeeFrameRx(): no SRCP_xx Node found '%s', ignored\n", ni);
    return;
  }
  //Die logische SRCP Adresse lesen
  srcpAdr = strtol(&(ni[5]), &check, 10);
  //Kontrolle, ob xx auch zwei Ziffern waren und im Range 1 bis 99 ist
  if (((check - &(ni[5])) != 2) || (srcpAdr < 1) || (srcpAdr > 99)) {
    #ifdef DBG_PRINT
    printf("xBeeFrameRx(): SRCP Adr must be 01 to 99, found %s\n", &(ni[5]));
    #endif
    syslog_bus(busnumber, DBG_ERROR, "xBeeFrameRx(): SRCP Adr must be 01 to 99, found %s\n", &(ni[5]));
    return;
  }
  //XBee Node eintragen wenn noch nicht vorhanden
  if (__xbee -> xBeeModules[srcpAdr].adr64 == 0) {
    __xbee -> xBeeModules[srcpAdr].adr64 = adr64;
    __xbee -> xBeeModules[srcpAdr].adr16 = adr16;
    #ifdef DBG_PRINT
    printf("xBeeFrameRx(): new SRCP XBee Modul %d added, 16 Bit Adr=%d\n", srcpAdr, adr16);
    #endif
    syslog_bus(busnumber, DBG_INFO, "xBeeFrameRx(): new SRCP XBee Modul %d added, 16 Bit Adr=%d\n", srcpAdr, adr16);
    //Vom soeben neu eingetragenem Node die gesamte IO Konfiguration abrufen 
    //um festzustellen, welche DIO als Output verwenden werden dürfen.
    requestIOConfig(busnumber, srcpAdr);
  }
  else {
    //Modul schon eingetragen. Wenn 64 Bit Adresse noch identisch ist
    //-> 16 Bit Adresse nochmals übernehmen
    if (__xbee -> xBeeModules[srcpAdr].adr64 == adr64) {
      __xbee -> xBeeModules[srcpAdr].adr16 = adr16;
      #ifdef DBG_PRINT
      printf("xBeeFrameRx(): SRCP XBee Modul %d update, 16 Bit Adr=%d\n", srcpAdr, adr16);
      #endif
      syslog_bus(busnumber, DBG_INFO, "xBeeFrameRx(): SRCP XBee Modul %d update, 16 Bit Adr=%d\n", srcpAdr, adr16);
      if (__xbee -> xBeeModules[srcpAdr].outputMissing) {
        //Output wurde verlangt, war aber nicht als solcher vorhanden.
        //Es wird nochmals abgefragt.
        requestIOConfig(busnumber, srcpAdr);
        __xbee -> xBeeModules[srcpAdr].outputMissing = false;
      }
      else {
        //Wiederherstellung der zuletzt aktiven Outputs
        for (i=0; i<MAX_XBEE_GA; i++) {
          if ((__xbee -> xBeeModules[srcpAdr].outputState & (1 << i)) != 0) {
            setXBeeOutput(busnumber, srcpAdr, i, true);
          }
        }
      }
    }
    else {
      //Es dürfen nicht zwei XBee Module mit selber logischer Adresse vorhanden sein
      #ifdef DBG_PRINT
      printf("xBeeFrameRx(): SRCP Adr %d ist doppelt vergeben!\n", srcpAdr);
      #endif
      syslog_bus(busnumber, DBG_ERROR, "xBeeFrameRx(): SRCP Adr %d ist doppelt vergeben!\n", srcpAdr);
    }
  }
}

/**
 * Suche in den registrierten XBee Modulen anhand der 16 Bit Adr die SRCP Adresse.
 * @param bus Bus, mit dem gearbeitet wird.
 * @param adr16 Die 16 Bit Adresse des XBee Moduls.
 * @return -1 Wenn nicht gefunden, sonst die ermittelte SRCP Adresse.
 */
static int searchSRCPAdr(bus_t busnumber, uint16_t adr16) {
  int srcpAdr;
  for (srcpAdr=0; srcpAdr<MAX_XBEE_MODULES; srcpAdr++) {
    if (__xbee -> xBeeModules[srcpAdr].adr16 == adr16) {
      //Passendes Modul gefunden
      break;
    }
  }
  if (srcpAdr >= MAX_XBEE_MODULES) {
    //Modul nicht gefunden.
    //Dürfte nicht vorkommen, alle Module wurden am Anfang mit ND ermittelt, 
    //alle müssen so konfiguriert sein, dass sie sich bei Anmledung selber melden (JN=1).
    #ifdef DBG_PRINT
    printf("processXBeeIOData(): Modul with 16 Bit Adr %d not found!\n", adr16);
    #endif
    syslog_bus(busnumber, DBG_WARN, "processXBeeIOData(): Modul with 16 Bit Adr %d not found!\n", adr16);
    return -1;
  }
  return srcpAdr;
}

/**
 * Verarbeite ein empfangenes XBee IO Frame.
 * Digitale Inputs werden als Rückmeldungen weitergemeldet.
 * Mapping: wie S88 Modul 16 Inputs pro Modul, DIO0 bis 12
 */
static void processXBeeIOData(bus_t busnumber, const XBEE_API_FRAME *xBeeFrame) {
  uint16_t adr16 = get16BitAdr(xBeeFrame, 8);
  //Zuerst anhand der 16 Bit Adresse feststellen, welches Modul, d.h. welche SRCP (S88) Adressbereich
  int srcpAdr = searchSRCPAdr(busnumber, adr16);
  if (srcpAdr < 0) {
    //Modul nicht vorhanden, kein SRCP Modul.
    return;
  }
  //Die tatsächlich als Input verwendeten DIO's, dazu wird DI (Digital Channel Mask)
  //verwendet -> nur Inputs, bei denen auch DI gesetzt ist werden als Inputs behandelt.
  uint16_t inputMask = (xBeeFrame -> frameData[12] << 8) | xBeeFrame -> frameData[13];
  uint16_t inputs = (xBeeFrame -> frameData[15] << 8) | xBeeFrame -> frameData[16];
  //Alles weg, was nicht als Input (DI) konfiguriert ist und invertieren (geschlossen auf Masse ist hier 1).
  inputs = (~inputs) & inputMask;
  setFBmodul(busnumber, srcpAdr, inputs);
  #ifdef DBG_PRINT
  printf("processXBeeIOData(): Modul Adr %d new FB 0x%x\n", srcpAdr, inputs);
  #endif
}

/**
 * Interpretiere und verarbeite eine empfangenee Remote AT Command Antwort.
 * Verarbeitet werden:
 * - Alle AT DIO Kommandos gemäss IO_AT_CMD -> Alle DIO_OUTPUT_LOW werden als Outputs registriert
 * @param bus Der SRCP Bus, auf dem gearbeitet wird.
 * @param xBeeFrame Das empfange Frame mit dem Remote Command Response
 */
static void frameRxRemoteATResponse(bus_t busnumber, const XBEE_API_FRAME *xBeeFrame) {
  int i;
  //XBee Adressen des Moduls und damit die SRCP Adresse ermitteln.
  uint16_t adr16 = get16BitAdr(xBeeFrame, 9);
  int srcpAdr = searchSRCPAdr(busnumber, adr16);
  if (srcpAdr < 0) {
    //Modul nicht vorhanden, kein SRCP Modul.
    #ifdef DBG_PRINT
    printf("frameRxRemoteATResponse(): Response from No SRCP Modul, 16 Bit Adr: %d\n", adr16);
    #endif
    syslog_bus(busnumber, DBG_WARN, "frameRxRemoteATResponse(): Response from No SRCP Modul, 16 Bit Adr: %d\n", adr16);
    return;
  }
  if (xBeeFrame -> frameData[13] != 0) {
    #ifdef DBG_PRINT
    printf("frameRxRemoteATResponse(): Modul 16 Bit Adr %d, SRCP Adr %d Command Status not OK: %d\n", adr16, srcpAdr, xBeeFrame -> frameData[13]);
    #endif
    syslog_bus(busnumber, DBG_WARN, "frameRxRemoteATResponse(): Modul 16 Bit Adr %d, SRCP Adr %d Command Status not OK: %d\n", adr16, srcpAdr, xBeeFrame -> frameData[13]);
    return;
  }
  //Feststellen, welches AT Kommando beantwortet / quittiert wurde.
  //Das 0 des Command State OK an frameData[13] benutzen wir gleich als String Terminierung....
  //IO_AT_CMD durchsuchen, 1 Byte Antwort Paramater -> Länge muss 16 Byte Sein
  if ((xBeeFrame -> length == 16) && 
      ((xBeeFrame -> frameData[14] == DIO_OUTPUT_LOW) || (xBeeFrame -> frameData[14] == DIO_OUTPUT_HIGH))) {
    for (i=0; i<16; i++) {
      if ((IO_AT_CMD[i] != NULL) && (strcmp(IO_AT_CMD[i], &(xBeeFrame -> frameData[11])) == 0)) {
        __xbee -> xBeeModules[srcpAdr].outputs |= (1 << i);
        #ifdef DBG_PRINT
        printf("frameRxRemoteATResponse(): Modul SRCP Adr %d Output %d found\n", srcpAdr, i);
        #endif
        break;
      }
    }
  }
  else {
    #ifdef DBG_PRINT
    printf("frameRxRemoteATResponse(): xBeeFrame -> length = %d, xBeeFrame -> frameData[14] = %d\n", xBeeFrame -> length, xBeeFrame -> frameData[14]);
    #endif
  }
}

/**
 * Sendet ein Node Discovery (ND) wenn seit dem letzten mal mindestens
 * ND_BLOCK_TIMEOUT Sekunden vergangen sind.
 * @param force Wenn true wird ND unabängig von ND_BLOCK_TIMEOUT gesendet.
 * @param busnumber
 */
void sendNodeDiscovery(bool force, bus_t busnumber) {
  if (force || ((time(NULL) - __xbee->lastTimeND) > ND_BLOCK_TIMEOUT)) {
    sendXBeeAPI_AT_CMD(busnumber, "ND", 1);
    //Wenn "force" ist der nächste "normale" gleich erlaubt (Start)
    __xbee->lastTimeND = time(NULL) - (force ? ND_BLOCK_TIMEOUT : 0);
  }
}

/**
 * Interpretiere und verarbeite ein vollständig empfangenes und korrektes
 * XBee Frame.
 * @param bus Der SRCP Bus, auf dem gearbeitet wird.
 * @param xBeeFrame Das empfange Frame
 */
static void xBeeFrameRx(bus_t busnumber, const XBEE_API_FRAME *xBeeFrame) {
  switch (xBeeFrame -> frameTyp) {
    case FTYP_IO_DATA_RX_IND:
      //IO Data empfangen
      //Länge muss sicher mindestens 18 sein, damit ist sicher, dass "Digital Sample" dabei ist (native Pos. 20)
      if (xBeeFrame -> length < 18) {
        #ifdef DBG_PRINT
        printf("xBeeFrameRx(): FTYP_IO_DATA_RX_IND len to short: %d\n", xBeeFrame -> length);
        #endif
        syslog_bus(busnumber, DBG_WARN, "xBeeFrameRx(): FTYP_IO_DATA_RX_IND len to short: %d\n", xBeeFrame -> length);
        return;
      }
      processXBeeIOData(busnumber, xBeeFrame);
      break;
    case FTYP_AT_COMMAND_RESPONSE:
      //Behandlung je nach AT Commando, auf das geantwortet wurde.
      //Es ist eine Frame mit FrameID. Nach dieser kommen 2 Byte mit dem AT Kommando, dann der Command State
      //Command State prüfen
      if (xBeeFrame -> frameData[3] != 0) {
        #ifdef DBG_PRINT
        printf("xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE command state not OK: %d\n", xBeeFrame -> frameData[3]);
        #endif
        syslog_bus(busnumber, DBG_WARN, "xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE command state not OK: %d\n", xBeeFrame -> frameData[3]);
        return;
      }
      //ND: Node discover
      if ((xBeeFrame -> frameData[1] == 'N') && (xBeeFrame -> frameData[2] == 'D')) {
        //Als Daten nach dem command state (frameDataID[3]) folgen:
        //16 Bit Adresse
        //64 Bit Adresse
        //NI
        //Min. Länge der Nutzdaten bei leerem NI (Name) muss sicher vorhanden sein 5 AT Response + 2 + 8 + 1 = 16
        if (xBeeFrame->length < 16) {
          syslog_bus(busnumber, DBG_ERROR, "xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE ND to short %d\n", xBeeFrame -> length);
          #ifdef DBG_PRINT
          printf("xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE ND to short %d\n", xBeeFrame -> length);
          #endif
          return;
        }
        addSRCPNode(busnumber, xBeeFrame, 4, 6, 14);
      }
      else {
        #ifdef DBG_PRINT
        printf("xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE unknown AT Command %c%c, ignored\n", xBeeFrame -> frameData[1], xBeeFrame -> frameData[2]);
        #endif
        syslog_bus(busnumber, DBG_WARN, "xBeeFrameRx(): FTYP_AT_COMMAND_RESPONSE unknown AT Command %c%c, ignored\n", xBeeFrame -> frameData[1], xBeeFrame -> frameData[2]);
        return;
      }
      break;
    case FTYP_NODE_IDENT_IND: //Join notification
      //Min. Länge der Nutzdaten bei leerem NI (Name) ist 0x19
      if (xBeeFrame->length < 0x19) {
        syslog_bus(busnumber, DBG_ERROR, "xBeeFrameRx(): FTYP_NODE_IDENT_IND to short %d\n", xBeeFrame -> length);
        #ifdef DBG_PRINT
        printf("xBeeFrameRx(): FTYP_NODE_IDENT_IND to short %d\n", xBeeFrame -> length);
        #endif
        return;
      }
      addSRCPNode(busnumber, xBeeFrame, 8, 0, 21);
      //Zur Sicherheit wird nun ein Node Discovery ausgelöst (mit Blocking Timeout).
      //Grund: beim geichzeigen einschalten von mehreren XBee's kann es vorkommen,
      //dass nicht alle "Join notification" Pakete ankommen.
      sendNodeDiscovery(false, busnumber);
      break;
    case FTYP_REMOTE_CMD_RESPONSE: //Antwort auf Remote AT Command
      frameRxRemoteATResponse(busnumber, xBeeFrame);
      break;
    default:
      #ifdef DBG_PRINT
      printf("xBeeFrameRx(): unknow frametyp %d\n", xBeeFrame -> frameTyp);
      #endif
      syslog_bus(busnumber, DBG_ERROR, "xBeeFrameRx(): unknow frametyp %d\n", xBeeFrame -> frameTyp);
  }
}

/**
 * Schalten eines Zubehörs (GA).
 * Anhand der GA Adresse wird die XBee SRCP Adresse und Outputport ermittelt.
 * Dazu wird mit MAX_XBEE_GA pro Modul gerechnet.
 * @param busnumber Der SRCP Bus, auf dem gearbeitet wird.
 * @param gaAdr Die GA Adresse
 * @param port Der Port zu Adresse. Nur 0 und 1 erlaubt, pro GA Adresse zwei 
 *             Ausgänge vorhanden, analog Märklin oder DCC GA-Dekoder.
 *             Achtung: GA x Port 1 enstricht GA x+1 Port 0!
 * @param vlaue Wenn true wird der Ausgang eingeschaltet, sonst aus.
 */
static void setXBeeGA(bus_t busnumber, unsigned int gaAdr, unsigned int port, bool value) {
  unsigned int srcpAdr = ((gaAdr - 1) / MAX_XBEE_GA) + 1;
  unsigned int output = ((gaAdr - 1) % MAX_XBEE_GA) + port;
  if ((srcpAdr >= MAX_XBEE_MODULES) || (port > 1)) {
    #ifdef DBG_PRINT
    printf("setXBeeGA(): GA Adr %d or Port %d not valid.\n", gaAdr, port);
    #endif
    syslog_bus(busnumber, DBG_WARN, "setXBeeGA(): GA Adr %d or Port %d not valid.\n", gaAdr, port);
    return;
  }
  #ifdef DBG_PRINT
  printf("setXBeeGA(): GA Adr %d, GA Port %d, SRCP Adr %d, XBee Port %d, Value %d.\n", gaAdr, port, srcpAdr, output, value);
  #endif
  setXBeeOutput(busnumber, srcpAdr, output, value);
}

/**
 * Alle Meldungen der ZBee Module einlesen und verarbeiten.
 * Da IC (Digital IO Change Detection) für alle vorhandenen Inputs gesetzt sein müssen,
 * werden Veränderungen der Inputs spontan von den Modulen gesendet.
 * Da auch JN (Join Notification) gesetzt ist, sollten sich neue Module selbst anmelden.
 * @param bus Der SRCP Bus, auf dem gearbeitet wird.
 * @return 0 = OK, -1 = Error
 */
static int readXBee(bus_t bus) {
  //Wurde ein Frame Start Delimiter erkannt? -> Frame wird zusammengesetzt
  enum FrameState {
    waitStartDelimiter,
    waitLenMSB,
    waitLenLSB,
    waitFrameType,
    waitChecksum
  };
  static enum FrameState frameState = waitStartDelimiter;
  //Sind wir gerade in einer Escape Sequenz?
  static bool escapeSeq = false;
  //Das empfange Frame wird hier zusammengesetzt
  static XBEE_API_FRAME xBeeFrame;
  //Wieviele Byte des Frames ab und inkl. Frame-Type wurden schon empfangen
  static int xBeeFrameRxLen = 0;
  //Berechnung Checksumme des empfangen Frames zur Prüfung
  static uint8_t checksum = 0;
  //read Buffer
  uint8_t buffer[256];
  int i;
  //Solange arbeiten, bis alle vorhandenen Daten gelesen sind
  while (1) {
    int readCount;
    int status = ioctl(buses[bus].device.file.fd, FIONREAD, &readCount);
    if (status == -1) {
      #ifdef DBG_PRINT
      printf("readXBee(): ioctl() failed: %s (errno = %d)\n", strerror(errno), errno);
      #endif
      syslog_bus(bus, DBG_ERROR, "readXBee(): ioctl() failed: %s (errno = %d)\n", strerror(errno), errno);
      return -1;
    }
    if (readCount == 0) {
      //Nichts mehr zu lesen da
      break;
    }
    /*#ifdef DBG_PRINT
    printf("readXBee(): (fd = %d), there are %d bytes to read.\n", buses[bus].device.file.fd, readCount);
    #endif
    syslog_bus(bus, DBG_DEBUG, "readXBee(): (fd = %d), there are %d bytes to read.", buses[bus].device.file.fd, readCount);*/
    if (readCount > sizeof(buffer)) {
      readCount = sizeof(buffer);
    }
    int readReturn = read(buses[bus].device.file.fd, buffer, readCount);
    if (readReturn < 0) {
      #ifdef DBG_PRINT
      printf("readXBee(): read() failed: %s (errno = %d)\n", strerror(errno), errno);
      #endif
      syslog_bus(bus, DBG_ERROR, "readXBee(): read() failed: %s (errno = %d)\n", strerror(errno), errno);
      return -1;
    } 
    for (i=0; i<readReturn; i++) {
      if (buffer[i] == XAPI_START) {
        //Ganz egal wo wir waren, wenn ein Start Delimter empfangen wurde
        //fangen wir von vorne an. Da mit Escape Sequenzen gearbeitet wird,
        //sollte der tatsächlich nur beim Start vorkommen.
        frameState = waitLenMSB; //-> das nächste Zeichen ist das MSB der Framelänge
      }
      else if (buffer[i] == XAPI_ESCAPE) {
        escapeSeq = true;
      }
      else {
        unsigned char c = buffer[i];
        if (escapeSeq) {
          c ^= XAPI_ESCAPE_XOR;
          escapeSeq = false;
        }
        switch (frameState) {
          case waitStartDelimiter:
            //Nichts machen, bis jetzt kein Start Delemiter erkannt
            break;
          case waitLenMSB:
            xBeeFrame.length = c << 8;
            frameState = waitLenLSB;
            break;
          case waitLenLSB:
            xBeeFrame.length |= c;
            if ((xBeeFrame.length == 0) || (xBeeFrame.length > (sizeof(xBeeFrame.frameData)+1))) {
              #ifdef DBG_PRINT
              printf("readXBee(): illegal frame length %d\n", xBeeFrame.length);
              #endif
              syslog_bus(bus, DBG_ERROR, "readXBee(): illegal frame length %d\n", xBeeFrame.length);
              frameState = waitStartDelimiter;
            }
            else {
              xBeeFrameRxLen = 0;
              frameState = waitFrameType;
            }
            break;
          case waitFrameType:
            xBeeFrame.frameTyp = c;
            checksum = c;
            xBeeFrameRxLen++;
            frameState = waitChecksum;
            break;
          case waitChecksum:
            checksum += c;
            //Solange Daten ergänzen, bis Framelänge erreicht ist.
            if (xBeeFrameRxLen == xBeeFrame.length) {
              //Frame Ende erreicht, aktuelles Zeichen ist die Checksumme -> prüfen
              if (checksum == 0xFF) {
                //Korrektes Frame empfangen -> verarbeiten
                xBeeFrameRx(bus, &xBeeFrame);
              }
              else {
                #ifdef DBG_PRINT
                printf("readXBee(): wrong checksum %d, drop frame\n", checksum);
                #endif
                syslog_bus(bus, DBG_ERROR, "readXBee(): wrong checksum %d, drop frame\n", checksum);
              }
              //Und es beginnt wieder von vorne
              frameState = waitStartDelimiter;
            }
            else {
              //Zeichen ergänzen (xBeeFrameRxLen ist inkl. Frame-Typ)
              xBeeFrame.frameData[xBeeFrameRxLen-1] = c;
              xBeeFrameRxLen++;
            }
            break;
          default:
            #ifdef DBG_PRINT
            printf("readXBee(): illegal frameState\n");
            #endif
            syslog_bus(bus, DBG_ERROR, "readXBee(): illegal frameState\n");
            frameState = waitStartDelimiter;
        } 
      }
    }
  }
  return 0;
}

void *thr_sendrec_XBEE(void *v)
{
    int result;
    ga_state_t gatmp;
    int addr;
    int last_cancel_state, last_cancel_type;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));
    if (btd == NULL) {
      pthread_exit((void *) 1);
    }
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);

    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    #ifdef DBG_PRINT
    printf("XBee bus started (device = %s).\n",
               buses[btd->bus].device.file.path);
    #endif
    syslog_bus(btd->bus, DBG_INFO, "XBee bus started (device = %s).",
               buses[btd->bus].device.file.path);

    int ga_min_active_time =
        ((XBEE_DATA *) buses[btd->bus].driverdata)->ga_min_active_time;

    buses[btd->bus].watchdog = 1;

    //Alle vorhandenen XBee Module mal Abfragen
    sendNodeDiscovery(true, btd->bus);
    
    while (1) {
        pthread_testcancel();
        buses[btd->bus].watchdog = 2;

        /* Magnetantriebe, die muessen irgendwann sehr bald
           abgeschaltet werden */
        if (!queue_GA_isempty(btd->bus)) {
          dequeueNextGA(btd->bus, &gatmp);
          addr = gatmp.id;
          if (gatmp.action == 1) {
            gettimeofday(&gatmp.tv[gatmp.port], NULL);
            setGA(btd->bus, addr, gatmp);
            //Kommando senden, Ausgang einschalten
            setXBeeGA(btd->bus, addr, gatmp.port, true);
            //Wenn verlangt automatisch wieder ausschalten
            if (gatmp.activetime >= 0) {
              gatmp.activetime =
                  (gatmp.activetime < ga_min_active_time) ? ga_min_active_time : gatmp.activetime;
              /* next action is auto switch off */
              gatmp.action = 0;
              if (usleep((unsigned long) gatmp.activetime * 1000) == -1) {
                syslog_bus(btd->bus, DBG_ERROR, "thr_sendrec_XBEE()::usleep() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
              }
            }
          }
          if (gatmp.action == 0) {
            setGA(btd->bus, addr, gatmp);
            //Kommando senden, Ausgang einschalten
            setXBeeGA(btd->bus, addr, gatmp.port, false);
          }

          buses[btd->bus].watchdog = 6;
        }
        buses[btd->bus].watchdog = 7;

        //Alle Meldungen der ZBee Module einlesen und verarbeiten.
        if (readXBee(btd->bus) == -1) {
          #ifdef DBG_PRINT
          printf("thr_sendrec_XBEE(): readXBee() failed: Thread termination\n");
          #endif
          syslog_bus(btd->bus, DBG_ERROR, "thr_sendrec_XBEE(): readXBee() failed: Thread termination\n");
          break;
        }

        buses[btd->bus].watchdog = 10;
        check_reset_fb(btd->bus);
        
        //Kurze Pause 10ms
        if (usleep(10000) == -1) {
          syslog_bus(btd->bus, DBG_ERROR, "thr_sendrec_XBEE::usleep() failed: %s (errno = %d)\n", strerror(errno), errno);
        }
        /* fprintf(stderr, " ende\n"); */
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
