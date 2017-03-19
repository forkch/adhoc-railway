/* $Id: ddl.c 1543 2012-01-09 18:54:14Z tvogt $ */

/*
 * DDL: Bus driver connected with a booster only without any special hardware.
 */

/* +----------------------------------------------------------------------+ */
/* | DDL - Digital Direct for Linux                                       | */
/* +----------------------------------------------------------------------+ */
/* | Copyright (c) 2002 - 2003 Vogt IT                                    | */
/* +----------------------------------------------------------------------+ */
/* | This source file is subject of the GNU general public license 2,     | */
/* | that is bundled with this package in the file COPYING, and is        | */
/* | available at through the world-wide-web at                           | */
/* | http://www.gnu.org/licenses/gpl.txt                                  | */
/* | If you did not receive a copy of the PHP license and are unable to   | */
/* | obtain it through the world-wide-web, please send a note to          | */
/* | gpl-license@vogt-it.com so we can mail you a copy immediately.       | */
/* +----------------------------------------------------------------------+ */

/***************************************************************/
/* erddcd - Electric Railroad Direct Digital Command Daemon    */
/*    generates without any other hardware digital commands    */
/*    to control electric model railroads                      */
/*                                                             */
/* Authors of the old erddcd part:                             */
/*                                                             */
/* 1999 - 2002 Torsten Vogt <vogt@vogt-it.com>                 */
/*                                                             */
/* Thanks to:                                                  */
/*                                                             */
/* Kurt Harders: i8255 implementation.                         */
/*                                                             */
/* Dieter Schaefer: s88 implementation                         */
/*                  additional code for marklin acc. decoders  */
/*                                                             */
/* Olaf Schlachter: debugging                                  */
/*                                                             */
/* Michael Peschel: new nmra dcc translation routine           */
/*                                                             */
/* Markus Gietzen: debugging and corrections                   */
/*                                                             */
/* Martin Wolf: re-implementation and enhancements of the s88  */
/*              support and the implementation of ga_manager   */
/*                                                             */
/* Sim IJskes: patch for a better port handling                */
/*                                                             */
/* Martin Schönbeck: third version of nmra dcc translation rtn.*/
/*                                                             */
/* Harald Barth: debugging and corrections                     */
/*                                                             */
/* Berthold Benning: usleep patch for SuSE kernels             */
/***************************************************************/

#include <sys/utsname.h>
#include <stdbool.h>

#include "config-srcpd.h"
#include "ddl.h"
#include "ddl_maerklin.h"
#include "ddl_nmra.h"
#include "io.h"
#include "srcp-error.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-sm.h"
#include "srcp-info.h"
#include "srcp-power.h"
#include "srcp-server.h"
#include "syslogmessage.h"

#define __DDL ((DDL_DATA*)buses[busnumber].driverdata)
#define __DDLt ((DDL_DATA*)buses[btd->bus].driverdata)

#ifdef __CYGWIN__
#define TIOCOUTQ 0x5411
#endif

void (*waitUARTempty_MM) (bus_t busnumber);

static int (*nanosleep_DDL) (const struct timespec * req,
                             struct timespec * rem);
static void *thr_sendrec_DDL(void *);

/********* Q U E U E *****************/

static void queue_init(bus_t busnumber)
{
    int result, i;

    result = pthread_mutex_init(&__DDL->queue_mutex, NULL);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_init() failed: %s (errno = %d).",
                   strerror(result), result);
        exit(1);
    }

    result = pthread_mutex_lock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    for (i = 0; i < QSIZE; i++) {
        __DDL->QData[i].packet_type = QNOVALIDPKT;
        __DDL->QData[i].addr = 0;
        memset(__DDL->QData[i].packet, 0, PKTSIZE);
    }
    __DDL->queue_in = 0;
    __DDL->queue_out = 0;
    __DDL->queue_initialized = true;

    result = pthread_mutex_unlock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

}

static int queue_empty(bus_t busnumber)
{
    return (__DDL->queue_in == __DDL->queue_out);
}

void queue_add(bus_t busnumber, int addr, char *const packet,
               int packet_type, int packet_size)
{
    int result;

    result = pthread_mutex_lock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    memset(__DDL->QData[__DDL->queue_in].packet, 0, PKTSIZE);
    memcpy(__DDL->QData[__DDL->queue_in].packet, packet, packet_size);
    __DDL->QData[__DDL->queue_in].packet_type = packet_type;
    __DDL->QData[__DDL->queue_in].packet_size = packet_size;
    __DDL->QData[__DDL->queue_in].addr = addr;
    __DDL->queue_in++;
    if (__DDL->queue_in == QSIZE)
        __DDL->queue_in = 0;

    result = pthread_mutex_unlock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }
}

static int queue_get(bus_t busnumber, int *addr, char *packet,
                     int *packet_size)
{
    int rtc;
    int result;

    if (!__DDL->queue_initialized || queue_empty(busnumber))
        return QEMPTY;

    result = pthread_mutex_lock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    memcpy(packet, __DDL->QData[__DDL->queue_out].packet, PKTSIZE);
    rtc = __DDL->QData[__DDL->queue_out].packet_type;
    *packet_size = __DDL->QData[__DDL->queue_out].packet_size;
    *addr = __DDL->QData[__DDL->queue_out].addr;
    __DDL->QData[__DDL->queue_out].packet_type = QNOVALIDPKT;
    __DDL->queue_out++;
    if (__DDL->queue_out == QSIZE)
        __DDL->queue_out = 0;

    result = pthread_mutex_unlock(&__DDL->queue_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    return rtc;
}


/* functions to open, initialize and close comport */

#if linux
static int init_serinfo(int fd, int divisor,
                        struct serial_struct **serinfo)
{
    if (*serinfo == NULL) {
        *serinfo = malloc(sizeof(struct serial_struct));
        if (!*serinfo)
            return -1;
    }

    if (ioctl(fd, TIOCGSERIAL, *serinfo) < 0)
        return -1;
    /* check baud_base - for other baud_base values change the divisor */
    if ((*serinfo)->baud_base != 115200)
        return -1;

    (*serinfo)->custom_divisor = divisor;
    (*serinfo)->flags = ASYNC_SPD_CUST | ASYNC_SKIP_TEST;

    return 0;
}

static int set_customdivisor(int fd, struct serial_struct *serinfo)
{
    if (ioctl(fd, TIOCSSERIAL, serinfo) < 0)
        return -1;
    return 0;
}

static int reset_customdivisor(int fd)
{
    struct serial_struct serinfo;

    if (ioctl(fd, TIOCGSERIAL, &serinfo) < 0)
        return -2;
    serinfo.custom_divisor = 0;
    serinfo.flags = 0;
    if (ioctl(fd, TIOCSSERIAL, &serinfo) < 0)
        return -3;
    return 0;
}
#endif

int setSerialMode(bus_t busnumber, int mode)
{
    switch (mode) {
        case SDM_MAERKLIN:
            if (__DDL->SERIAL_DEVICE_MODE != SDM_MAERKLIN) {
                if (tcsetattr
                    (buses[busnumber].device.file.fd, TCSANOW,
                     &__DDL->maerklin_dev_termios) != 0) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "Error setting serial device mode to Maerklin!");
                    return -1;
                }
#if linux
                if (__DDL->IMPROVE_NMRADCC_TIMING) {
                    if (set_customdivisor
                        (buses[busnumber].device.file.fd,
                         __DDL->serinfo_marklin) != 0) {
                        syslog_bus(busnumber, DBG_ERROR,
                                   "Cannot set custom divisor for maerklin of serial device!");
                        return -1;
                    }
                }
#endif
                __DDL->SERIAL_DEVICE_MODE = SDM_MAERKLIN;
            }
            break;
        case SDM_NMRA:
            if (__DDL->SERIAL_DEVICE_MODE != SDM_NMRA) {
                if (tcsetattr
                    (buses[busnumber].device.file.fd, TCSANOW,
                     &__DDL->nmra_dev_termios) != 0) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "Error setting serial device mode to NMRA!");
                    return -1;
                }
#if linux
                if (__DDL->IMPROVE_NMRADCC_TIMING) {
                    if (set_customdivisor
                        (buses[busnumber].device.file.fd,
                         __DDL->serinfo_nmradcc) != 0) {
                        syslog_bus(busnumber, DBG_ERROR,
                                   "Cannot set custom divisor for nmra dcc of serial device!");
                        return -1;
                    }
                }
#endif
                __DDL->SERIAL_DEVICE_MODE = SDM_NMRA;
            }
            break;
        default:
            syslog_bus(busnumber, DBG_ERROR,
                       "Error setting serial device to unknown mode!");
            return -1;
    }
    return 0;
}

int init_lineDDL(bus_t busnumber)
{
    /* opens and initializes the selected comport */
    /* returns a file handle                      */

    int dev;
    int rc;
    int result;

    /* open comport */
    dev = open(buses[busnumber].device.file.path, O_WRONLY);
    if (dev < 0) {
        syslog_bus(busnumber, DBG_FATAL,
                   "Device '%s' open failed: %s (errno = %d). "
                   "Terminating...\n", buses[busnumber].device.file.path,
                   strerror(errno), errno);
        /* there is no chance to continue */
        exit(1);
    }
#if linux
    if ((rc = reset_customdivisor(dev)) != 0) {
        syslog_bus(busnumber, DBG_FATAL,
                   "Error initializing device %s (reset custom "
                   "divisor %d). Abort!",
                   buses[busnumber].device.file.path, rc);
        exit(1);
    }
#endif

    result = tcflush(dev, TCOFLUSH);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflush() failed: %s (errno = %d).",
                   strerror(result), result);
        exit(1);
    }

    result = tcflow(dev, TCOOFF);       /* suspend output */
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        exit(1);
    }

    result = tcgetattr(dev, &__DDL->maerklin_dev_termios);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcgetattr() failed: %s (errno = %d, device = %s).",
                   strerror(result), result,
                   buses[busnumber].device.file.path);
        exit(1);
    }

    result = tcgetattr(dev, &__DDL->nmra_dev_termios);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcgetattr() failed: %s (errno = %d, device = %s).",
                   strerror(result), result,
                   buses[busnumber].device.file.path);
        exit(1);
    }

    /* init termios structure for Maerklin mode */
    __DDL->maerklin_dev_termios.c_lflag &=
        ~(ECHO | ICANON | IEXTEN | ISIG);
    __DDL->maerklin_dev_termios.c_oflag &= ~(OPOST);
    __DDL->maerklin_dev_termios.c_iflag &=
        ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
    __DDL->maerklin_dev_termios.c_cflag &= ~(CSIZE | PARENB);
    __DDL->maerklin_dev_termios.c_cflag |= CS6; /* 6 data bits      */
    cfsetospeed(&__DDL->maerklin_dev_termios, B38400);  /* baud rate: 38400 */
    cfsetispeed(&__DDL->maerklin_dev_termios, B38400);  /* baud rate: 38400 */

    /* init termios structure for NMRA mode */
    __DDL->nmra_dev_termios.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
    __DDL->nmra_dev_termios.c_oflag &= ~(OPOST);
    __DDL->nmra_dev_termios.c_iflag &=
        ~(BRKINT | ICRNL | INPCK | ISTRIP | IXON);
    __DDL->nmra_dev_termios.c_cflag &= ~(CSIZE | PARENB);
    __DDL->nmra_dev_termios.c_cflag |= CS8;     /* 8 data bits      */
    if (__DDL->IMPROVE_NMRADCC_TIMING) {
        /* IMPROVE_NMRADCC_TIMING
           With 19200 baud we are already outside of the specification of NMRA.
           19200 baud means we have pulses of 52 microseconds - the specification
           for generating DCC signals is in the range between 55 and 61 
           microseconds.
           Decoders are expected to accept signals with a length between 52 
           and 66 microseconds (all this timigs are for one half of a logical 1).
           Actually most decoders accept an even wider range than specified by
           NMRA, thus 19200 baud normaly works.
           If you have communication problems here is the method to reduce the
           speed of your serial line to 16457 baud (i.e. 60.8 microseconds),
           which is inside the NMRA specification.

           On linux this is done in an odd way:
           You have to set your baudrate to 38400 and set the flag 
           ASYNC_SPD_CUST (see init_serinfo), this actualy sets the speed of
           the line to Baud_base / custom_devisor
           Baud_base is 115200 on "normal" serial lines (using the chip 16550A)
           therefore a custom_devisor of 7 is needed for 16457 baud
           and a custom_devisor of 3 will give you 38400 baud (maerklin)
         */
        cfsetospeed(&__DDL->nmra_dev_termios, B38400);  /* baud rate: 38400 */
        cfsetispeed(&__DDL->nmra_dev_termios, B38400);  /* baud rate: 38400 */
    }
    else {
        cfsetospeed(&__DDL->nmra_dev_termios, B19200);  /* baud rate: 19200 */
        cfsetispeed(&__DDL->nmra_dev_termios, B19200);  /* baud rate: 19200 */
    }

#if linux
    /* if IMPROVE_NMRADCC_TIMING is set, we have to initialize some */
    /* structures */
    if (__DDL->IMPROVE_NMRADCC_TIMING) {
        if (init_serinfo(dev, 3, &__DDL->serinfo_marklin) != 0) {
            syslog_bus(busnumber, DBG_FATAL,
                       "Error initializing device %s (init_serinfo mm). Abort!",
                       buses[busnumber].device.file.path);
            exit(1);
        }
        if (init_serinfo(dev, 7, &__DDL->serinfo_nmradcc) != 0) {
            syslog_bus(busnumber, DBG_FATAL,
                       "Error initializing device %s (init_serinfo dcc). Abort!",
                       buses[busnumber].device.file.path);
            exit(1);
        }
    }
#endif
    buses[busnumber].device.file.fd = dev;      /* we need that value at the next step */
    /* setting serial device to default mode */
    if (!setSerialMode(busnumber, SDM_DEFAULT) == 0) {
        syslog_bus(busnumber, DBG_FATAL,
                   "Error initializing device %s. Abort!",
                   buses[busnumber].device.file.path);
        exit(1);
    }

    return dev;
}


/****** routines for Maerklin packet pool *********************/

static void init_MaerklinPacketPool(bus_t busnumber)
{
    int i, j;
    int result;

    result = pthread_mutex_init(&__DDL->maerklin_pktpool_mutex, NULL);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_init() failed: %s (errno = %d). Abort!",
                   strerror(result), result);
        exit(1);
    }

    result = pthread_mutex_lock(&__DDL->maerklin_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    for (i = 0; i <= MAX_MARKLIN_ADDRESS; i++)
        __DDL->MaerklinPacketPool.knownAddresses[i] = 0;

    __DDL->MaerklinPacketPool.NrOfKnownAddresses = 1;
    __DDL->MaerklinPacketPool.knownAddresses[__DDL->MaerklinPacketPool.
                                             NrOfKnownAddresses - 1] = 81;
    /* generate idle packet */
    for (i = 0; i < 4; i++) {
        __DDL->MaerklinPacketPool.packets[81].packet[2 * i] = HI;
        __DDL->MaerklinPacketPool.packets[81].packet[2 * i + 1] = LO;
        for (j = 0; j < 4; j++) {
            __DDL->MaerklinPacketPool.packets[81].f_packets[j][2 * i] = HI;
            __DDL->MaerklinPacketPool.packets[81].f_packets[j][2 * i + 1] =
                LO;
        }
    }
    for (i = 4; i < 9; i++) {
        __DDL->MaerklinPacketPool.packets[81].packet[2 * i] = LO;
        __DDL->MaerklinPacketPool.packets[81].packet[2 * i + 1] = LO;
        for (j = 0; j < 4; j++) {
            __DDL->MaerklinPacketPool.packets[81].f_packets[j][2 * i] = LO;
            __DDL->MaerklinPacketPool.packets[81].f_packets[j][2 * i + 1] =
                LO;
        }
    }

    result = pthread_mutex_unlock(&__DDL->maerklin_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }
}

char *get_maerklin_packet(bus_t busnumber, int adr, int fx)
{
    return __DDL->MaerklinPacketPool.packets[adr].f_packets[fx];
}

void update_MaerklinPacketPool(bus_t busnumber, int adr,
                               char const *const sd_packet,
                               char const *const f1, char const *const f2,
                               char const *const f3, char const *const f4)
{
    int i, found;
    int result;

    syslog_bus(busnumber, DBG_INFO, "Update MM packet pool: %d", adr);
    found = 0;
    for (i = 0; i < __DDL->MaerklinPacketPool.NrOfKnownAddresses && !found;
         i++)
        if (__DDL->MaerklinPacketPool.knownAddresses[i] == adr)
            found = true;

    result = pthread_mutex_lock(&__DDL->maerklin_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    memcpy(__DDL->MaerklinPacketPool.packets[adr].packet, sd_packet, 18);
    memcpy(__DDL->MaerklinPacketPool.packets[adr].f_packets[0], f1, 18);
    memcpy(__DDL->MaerklinPacketPool.packets[adr].f_packets[1], f2, 18);
    memcpy(__DDL->MaerklinPacketPool.packets[adr].f_packets[2], f3, 18);
    memcpy(__DDL->MaerklinPacketPool.packets[adr].f_packets[3], f4, 18);

    result = pthread_mutex_unlock(&__DDL->maerklin_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    if (__DDL->MaerklinPacketPool.NrOfKnownAddresses == 1
        && __DDL->MaerklinPacketPool.knownAddresses[0] == 81)
        __DDL->MaerklinPacketPool.NrOfKnownAddresses = 0;

    if (!found) {
        __DDL->MaerklinPacketPool.knownAddresses[__DDL->MaerklinPacketPool.
                                                 NrOfKnownAddresses] = adr;
        __DDL->MaerklinPacketPool.NrOfKnownAddresses++;
    }
}

/**********************************************************/

/****** routines for NMRA packet pool *********************/
static void reset_NMRAPacketPool(bus_t busnumber)
{
    int i;
    int result;
    result = pthread_mutex_lock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    for (i = 0; i < __DDL->NMRAPacketPool.NrOfKnownAddresses; i++) {
        int nr = __DDL->NMRAPacketPool.knownAddresses[i];
        free(__DDL->NMRAPacketPool.packets[nr]);
        __DDL->NMRAPacketPool.packets[nr] = 0;
    }

    /* free idle package - this is needed because the idle packet is removed
       from the knownAdresses in the PacketPool after the first Loco is
       refreshed -> TODO: a better place for this free would be in 
       update_NMRAPacketPool */
    free(__DDL->NMRAPacketPool.packets[128]);

    result = pthread_mutex_unlock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

}

static void init_NMRAPacketPool(bus_t busnumber)
{
    int i, j;
    char idle_packet[] = "1111111111111110111111110000000000111111111";
    char idle_pktstr[PKTSIZE];
    int result;

    result = pthread_mutex_init(&__DDL->nmra_pktpool_mutex, NULL);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_init() failed: %s (error = %d). Terminating!\n",
                   strerror(result), result);
        exit(1);
    }

    result = pthread_mutex_lock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    for (i = 0; i <= MAX_NMRA_ADDRESS; i++) {
        __DDL->NMRAPacketPool.knownAddresses[i] = 0;
        __DDL->NMRAPacketPool.packets[i] = 0;
    }

    __DDL->NMRAPacketPool.NrOfKnownAddresses = 0;

    result = pthread_mutex_unlock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    /* put idle packet in packet pool */
    j = translateBitstream2Packetstream(busnumber, idle_packet,
                                        idle_pktstr, false);
    update_NMRAPacketPool(busnumber, 128, idle_pktstr, j, idle_pktstr, j);
    /* generate and override idle_data */
    /* insert the NMRA idle packetstream (the standard idle stream was all
       '1' which is OK for NMRA, so keep the rest of the idle string) */
    for (i = 0; i < (MAXDATA / j) * j; i++)
        __DDL->idle_data[i] = idle_pktstr[i % j];
    memcpy(__DDL->NMRA_idle_data, idle_pktstr, j);
    __DDL->NMRA_idle_data_size = j;
}

void update_NMRAPacketPool(bus_t busnumber, int adr,
                           char const *const packet, int packet_size,
                           char const *const fx_packet, int fx_packet_size)
{
    int i, found;
    int result;

    found = 0;
    for (i = 0; i <= __DDL->NMRAPacketPool.NrOfKnownAddresses && !found;
         i++)
        if (__DDL->NMRAPacketPool.knownAddresses[i] == adr)
            found = true;

    result = pthread_mutex_lock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_lock() failed: %s (errno = %d).",
                   strerror(result), result);
    }
    if (!__DDL->NMRAPacketPool.packets[adr]) {
        __DDL->NMRAPacketPool.packets[adr] = malloc(sizeof(tNMRAPacket));
        if (__DDL->NMRAPacketPool.packets[adr] == NULL) {
            syslog_bus(busnumber, DBG_ERROR,
                       "Memory allocation error in update_NMRAPacketPool");
            return;
        }
    }
    memcpy(__DDL->NMRAPacketPool.packets[adr]->packet, packet,
           packet_size);
    __DDL->NMRAPacketPool.packets[adr]->packet_size = packet_size;
    memcpy(__DDL->NMRAPacketPool.packets[adr]->fx_packet, fx_packet,
           fx_packet_size);
    __DDL->NMRAPacketPool.packets[adr]->fx_packet_size = fx_packet_size;


    if (__DDL->NMRAPacketPool.NrOfKnownAddresses == 1
        && __DDL->NMRAPacketPool.knownAddresses[0] == 128)
        __DDL->NMRAPacketPool.NrOfKnownAddresses = 0;

    if (!found) {
        __DDL->NMRAPacketPool.knownAddresses[__DDL->NMRAPacketPool.
                                             NrOfKnownAddresses] = adr;
        __DDL->NMRAPacketPool.NrOfKnownAddresses++;
    }
    result = pthread_mutex_unlock(&__DDL->nmra_pktpool_mutex);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR,
                   "pthread_mutex_unlock() failed: %s (errno = %d).",
                   strerror(result), result);
    }
}


/* busy wait until UART is empty, without delay */
static void waitUARTempty_COMMON(bus_t busnumber)
{
    int value = 0;
    int result;

    do {
#if linux
        result =
            ioctl(buses[busnumber].device.file.fd, TIOCSERGETLSR, &value);
#else
        result = ioctl(buses[busnumber].device.file.fd, TCSADRAIN, &value);
#endif
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    } while (!value);
}

/* busy wait until UART is empty, with delay */
static void waitUARTempty_COMMON_USLEEPPATCH(bus_t busnumber)
{
    int value = 0;
    int result;

    do {
#if linux
        result =
            ioctl(buses[busnumber].device.file.fd, TIOCSERGETLSR, &value);
#else
        result = ioctl(buses[busnumber].device.file.fd, TCSADRAIN, &value);
#endif
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        if (usleep(__DDL->WAITUART_USLEEP_USEC) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    } while (!value);
}

/* new Version of waitUARTempty() for a clean NMRA-DCC signal */
/* from Harald Barth */
#define SLEEPFACTOR 48000l      /* used in waitUARTempty() */
#define NUMBUFFERBYTES 1024     /* used in waitUARTempty() */

static void waitUARTempty_CLEANNMRADCC(bus_t busnumber)
{
    int outbytes;
    int result;

    /* look how many bytes are in UART's out buffer */
    result = ioctl(buses[busnumber].device.file.fd, TIOCOUTQ, &outbytes);
    if (result == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    if (outbytes > NUMBUFFERBYTES) {
        struct timespec sleeptime;
        /* calculate sleep time */
        sleeptime.tv_sec = outbytes / SLEEPFACTOR;
        sleeptime.tv_nsec =
            (outbytes % SLEEPFACTOR) * (1000000000l / SLEEPFACTOR);

        nanosleep_DDL(&sleeptime, NULL);
    }
}

static int checkRingIndicator(bus_t busnumber)
{
    int result, arg;

    result = ioctl(buses[busnumber].device.file.fd, TIOCMGET, &arg);
    if (result >= 0) {
        if (arg & TIOCM_RI) {
            syslog_bus(busnumber, DBG_INFO,
                       "Ring indicator alert. Power on tracks stopped!");
            return 1;
        }
        return 0;
    }
    else {
        syslog_bus(busnumber, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d). Power on tracks stopped!",
                   strerror(errno), errno);
        return 1;
    }
}

static int checkShortcut(bus_t busnumber)
{
    int result, arg;
    time_t short_now = 0;
    struct timeval tv_shortcut = { 0, 0 };

    result = ioctl(buses[busnumber].device.file.fd, TIOCMGET, &arg);
    if (result >= 0) {
        if (((arg & TIOCM_DSR) && (!__DDL->DSR_INVERSE))
            || ((~arg & TIOCM_DSR) && (__DDL->DSR_INVERSE))) {
            if (__DDL->short_detected == 0) {
                gettimeofday(&tv_shortcut, NULL);
                __DDL->short_detected =
                    tv_shortcut.tv_sec * 1000000 + tv_shortcut.tv_usec;
            }
            gettimeofday(&tv_shortcut, NULL);
            short_now = tv_shortcut.tv_sec * 1000000 + tv_shortcut.tv_usec;
            if (__DDL->SHORTCUTDELAY <=
                (short_now - __DDL->short_detected)) {
                syslog_bus(busnumber, DBG_INFO,
                           "Shortcut detected. Power on tracks stopped!");
                return 1;
            }
        }
        else {
            __DDL->short_detected = 0;
            return 0;
        }
    }
    else {
        syslog_bus(busnumber, DBG_INFO,
                   "ioctl() failed: %s (errno = %d). Power on tracks stopped!",
                   strerror(errno), errno);
        return 1;
    }
    return 0;
}


static void send_packet(bus_t busnumber, char *packet,
                        int packet_size, int packet_type, int refresh)
{
    ssize_t result;
    int i, laps;
    /* arguments for nanosleep and Maerklin loco decoders (19KHz) */
    /* all using busy waiting */
    static struct timespec rqtp_btw19K = { 0, 1250000 };
    static struct timespec rqtp_end19K = { 0, 1700000 };
    /* arguments for nanosleep and Maerklin solenoids/function decoders (38KHz) */
    static struct timespec rqtp_btw38K = { 0, 625000 };
    static struct timespec rqtp_end38K = { 0, 850000 };

    waitUARTempty(busnumber);

    switch (packet_type) {
        case QM1LOCOPKT:
        case QM2LOCOPKT:
            if (setSerialMode(busnumber, SDM_MAERKLIN) < 0)
                return;
            if (refresh)
                laps = 2;
            else
                laps = 4;       /* YYTV 9 */
            for (i = 0; i < laps; i++) {
                nanosleep_DDL(&rqtp_end19K, &__DDL->rmtp);
                result =
                    write(buses[busnumber].device.file.fd, packet, 18);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_MM(busnumber);
                nanosleep_DDL(&rqtp_btw19K, &__DDL->rmtp);
                result =
                    write(buses[busnumber].device.file.fd, packet, 18);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_MM(busnumber);
            }
            break;
        case QM2FXPKT:
            if (setSerialMode(busnumber, SDM_MAERKLIN) < 0)
                return;
            if (refresh)
                laps = 2;
            else
                laps = 3;       /* YYTV 6 */
            for (i = 0; i < laps; i++) {
                nanosleep_DDL(&rqtp_end19K, &__DDL->rmtp);
                result =
                    write(buses[busnumber].device.file.fd, packet, 18);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_MM(busnumber);
                nanosleep_DDL(&rqtp_btw19K, &__DDL->rmtp);
                result =
                    write(buses[busnumber].device.file.fd, packet, 18);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_MM(busnumber);
            }
            break;
        case QM1FUNCPKT:
        case QM1SOLEPKT:
            if (setSerialMode(busnumber, SDM_MAERKLIN) < 0)
                return;
            for (i = 0; i < 2; i++) {
                nanosleep_DDL(&rqtp_end38K, &__DDL->rmtp);
                result = write(buses[busnumber].device.file.fd, packet, 9);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_COMMON(busnumber);
                nanosleep_DDL(&rqtp_btw38K, &__DDL->rmtp);
                result = write(buses[busnumber].device.file.fd, packet, 9);
                if (result == -1) {
                    syslog_bus(busnumber, DBG_ERROR,
                               "write() failed: %s (errno = %d)\n",
                               strerror(errno), errno);
                }
                waitUARTempty_COMMON(busnumber);
            }
            break;
        case QNBLOCOPKT:
        case QNBACCPKT:
            if (setSerialMode(busnumber, SDM_NMRA) < 0)
                return;
            result = write(buses[busnumber].device.file.fd, packet,
                           packet_size);
            if (result == -1) {
                syslog_bus(busnumber, DBG_ERROR,
                           "write() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            waitUARTempty_CLEANNMRADCC(busnumber);
            result = write(buses[busnumber].device.file.fd,
                           __DDL->NMRA_idle_data,
                           __DDL->NMRA_idle_data_size);
            waitUARTempty_CLEANNMRADCC(busnumber);
            result = write(buses[busnumber].device.file.fd,
                           packet, packet_size);
            if (result == -1) {
                syslog_bus(busnumber, DBG_ERROR,
                           "write() failed: %s (errno = %d)\n",
                           strerror(errno), errno);
            }
            break;
    }
    if (__DDL->ENABLED_PROTOCOLS & EP_MAERKLIN)
        nanosleep_DDL(&rqtp_end38K, &__DDL->rmtp);
    if (setSerialMode(busnumber, SDM_DEFAULT) < 0)
        return;
}

static void refresh_loco(bus_t busnumber)
{
    int adr;
    int result;

    if (__DDL->maerklin_refresh) {
        adr =
            __DDL->MaerklinPacketPool.knownAddresses[__DDL->
                                                     last_refreshed_maerklin_loco];
        result = tcflush(buses[busnumber].device.file.fd, TCOFLUSH);
        if (result == -1) {
            syslog_bus(busnumber, DBG_FATAL,
                       "tcflush() failed: %s (errno = %d).",
                       strerror(result), result);
            /*What to do now? */
        }

        if (__DDL->last_refreshed_maerklin_fx < 0)
            send_packet(busnumber,
                        __DDL->MaerklinPacketPool.packets[adr].packet, 18,
                        QM2LOCOPKT, true);
        else
            send_packet(busnumber,
                        __DDL->MaerklinPacketPool.packets[adr].
                        f_packets[__DDL->last_refreshed_maerklin_fx], 18,
                        QM2FXPKT, true);
        __DDL->last_refreshed_maerklin_fx++;
        if (__DDL->last_refreshed_maerklin_fx == 4) {
            __DDL->last_refreshed_maerklin_fx = -1;
            __DDL->last_refreshed_maerklin_loco++;
            if (__DDL->last_refreshed_maerklin_loco >=
                __DDL->MaerklinPacketPool.NrOfKnownAddresses)
                __DDL->last_refreshed_maerklin_loco = 0;
        }
    }
    else {
        adr =
            __DDL->NMRAPacketPool.knownAddresses[__DDL->
                                                 last_refreshed_nmra_loco];
        if (adr >= 0) {
            if (__DDL->last_refreshed_nmra_fx < 0) {
                send_packet(busnumber,
                            __DDL->NMRAPacketPool.packets[adr]->packet,
                            __DDL->NMRAPacketPool.packets[adr]->
                            packet_size, QNBLOCOPKT, true);
                __DDL->last_refreshed_nmra_fx = 0;
            }
            else {
                send_packet(busnumber,
                            __DDL->NMRAPacketPool.packets[adr]->fx_packet,
                            __DDL->NMRAPacketPool.packets[adr]->
                            fx_packet_size, QNBLOCOPKT, true);
                __DDL->last_refreshed_nmra_fx = 1;
            }
        }
        if (__DDL->last_refreshed_nmra_fx == 1) {
            __DDL->last_refreshed_nmra_loco++;
            __DDL->last_refreshed_nmra_fx = -1;
            if (__DDL->last_refreshed_nmra_loco >=
                __DDL->NMRAPacketPool.NrOfKnownAddresses)
                __DDL->last_refreshed_nmra_loco = 0;
        }
    }
    if (__DDL->ENABLED_PROTOCOLS == (EP_MAERKLIN | EP_NMRADCC))
        __DDL->maerklin_refresh = !__DDL->maerklin_refresh;
}

/* calculate difference between two time values and return the
 * difference in microseconds */
static long int compute_delta(struct timeval tv1, struct timeval tv2)
{
    long int delta_sec;
    long int delta_usec;

    delta_sec = tv2.tv_sec - tv1.tv_sec;
    if (delta_sec == 0)
        delta_usec = tv2.tv_usec - tv1.tv_usec;
    else {
        if (delta_sec == 1)
            delta_usec = tv2.tv_usec + (1000000 - tv1.tv_usec);
        else
            delta_usec =
                1000000 * (delta_sec - 1) + tv2.tv_usec + (1000000 -
                                                           tv1.tv_usec);
    }
    return delta_usec;
}

static void set_SerialLine(bus_t busnumber, int line, int mode)
{
    int result, arg;

    result = ioctl(buses[busnumber].device.file.fd, TIOCMGET, &arg);
    if (result == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "ioctl() failed: %s (errno = %d). "
                   "Serial line not set! (%d: %d)",
                   strerror(errno), errno, line, mode);
    }
    else {
        switch (line) {
            case SL_DTR:
                if (mode == OFF)
                    arg &= ~TIOCM_DTR;
                if (mode == ON)
                    arg |= TIOCM_DTR;
                break;
            case SL_DSR:
                if (mode == OFF)
                    arg &= ~TIOCM_DSR;
                if (mode == ON)
                    arg |= TIOCM_DSR;
                break;
            case SL_RI:
                if (mode == OFF)
                    arg &= ~TIOCM_RI;
                if (mode == ON)
                    arg |= TIOCM_RI;
                break;
            case SL_RTS:
                if (mode == OFF)
                    arg &= ~TIOCM_RTS;
                if (mode == ON)
                    arg |= TIOCM_RTS;
                break;
            case SL_CTS:
                if (mode == OFF)
                    arg &= ~TIOCM_CTS;
                if (mode == ON)
                    arg |= TIOCM_CTS;
                break;
        }
        result = ioctl(buses[busnumber].device.file.fd, TIOCMSET, &arg);
        if (result == -1) {
            syslog_bus(busnumber, DBG_FATAL,
                       "ioctl() failed: %s (errno = %d).",
                       strerror(result), result);
            /*What to do now */
        }
    }
}

/* ************************************************ */

static void set_lines_on(bus_t busnumber)
{
    int result;

    set_SerialLine(busnumber, SL_DTR, ON);
    result = tcflow(buses[busnumber].device.file.fd, TCOON);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }
}

static void set_lines_off(bus_t busnumber)
{
    int result;

    /* set interface lines to the off state */
    result = tcflush(buses[busnumber].device.file.fd, TCOFLUSH);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflush() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }

    result = tcflow(buses[busnumber].device.file.fd, TCOOFF);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }

    set_SerialLine(busnumber, SL_DTR, OFF);
}

/* check if shortcut or emergengy break happened
   return "true" if power is off
   return "false" if power is on
 */
static bool power_is_off(bus_t busnumber)
{
    char msg[110];

    if (__DDL->CHECKSHORT)
        if (checkShortcut(busnumber) == 1) {
            buses[busnumber].power_state = 0;
            buses[busnumber].power_changed = 1;
            strcpy(buses[busnumber].power_msg, "SHORTCUT DETECTED");
            infoPower(busnumber, msg);
            enqueueInfoMessage(msg);
        }

    if (__DDL->RI_CHECK)
        if (checkRingIndicator(busnumber) == 1) {
            buses[busnumber].power_state = 0;
            buses[busnumber].power_changed = 1;
            strcpy(buses[busnumber].power_msg, "RINGINDICATOR DETECTED");
            infoPower(busnumber, msg);
            enqueueInfoMessage(msg);
        }

    if (buses[busnumber].power_changed == 1) {
        if (buses[busnumber].power_state == 0) {
            set_lines_off(busnumber);
            syslog_bus(busnumber, DBG_INFO, "Refresh cycle stopped.");
        }
        if (buses[busnumber].power_state == 1) {
            set_lines_on(busnumber);
            syslog_bus(busnumber, DBG_INFO, "Refresh cycle started.");
        }
        buses[busnumber].power_changed = 0;
        infoPower(busnumber, msg);
        enqueueInfoMessage(msg);
    }

    if (buses[busnumber].power_state == 0) {
        if (usleep(1000) == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
        return true;
    }
    return false;
}

/* tvo 2005-12-03 */
static int krnl26_nanosleep(const struct timespec *req,
                            struct timespec *rem)
{
    struct timeval start_tv, stop_tv;
    struct timezone start_tz, stop_tz;
    long int sleep_usec;
    double slept;

    /* Falls "Schlafwerte" zu groß, soll ein normales nanosleep gemacht werden */
    if ((*req).tv_sec > 0 || (*req).tv_nsec > 2000000) {
        return nanosleep(req, rem);     /* non-busy waiting */
    }

    /* here begins the busy waiting section */
    /* Genauigkeit nur im usec-Bereich!!! */
    sleep_usec = (*req).tv_nsec / 1000;
    gettimeofday(&start_tv, &start_tz);
    do {
        gettimeofday(&stop_tv, &stop_tz);
        slept = ((stop_tv.tv_sec + (stop_tv.tv_usec / 1000000.) -
                  (start_tv.tv_sec +
                   (start_tv.tv_usec / 1000000.))) * 1000000.);
    } while (slept < sleep_usec);
    return 0;
}


static void *thr_refresh_cycle(void *v)
{
    ssize_t wresult;
    int result;
    struct sched_param sparam;
    int policy;
    int packet_size;
    int packet_type;
    char packet[PKTSIZE];
    int addr;
    struct _thr_param *tp = v;
    bus_t busnumber = tp->busnumber;
    struct timeval tv1, tv2;
    struct timezone tz;
    /* argument for nanosleep to do non-busy waiting */
    static struct timespec rqtp_sleep = { 0, 2500000 };

    /* set the best waitUARTempty-Routine */
    if (__DDL->WAITUART_USLEEP_PATCH) {
        waitUARTempty = waitUARTempty_COMMON_USLEEPPATCH;
        waitUARTempty_MM = waitUARTempty_COMMON_USLEEPPATCH;
    }
    else {
        waitUARTempty = waitUARTempty_COMMON;
        waitUARTempty_MM = waitUARTempty_COMMON;
    }

    nanosleep_DDL = nanosleep;
    if (__DDL->oslevel == 1) {
        nanosleep_DDL = krnl26_nanosleep;

        result = pthread_getschedparam(pthread_self(), &policy, &sparam);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "pthread_getschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
        sparam.sched_priority = 10;
        result =
            pthread_setschedparam(pthread_self(), SCHED_FIFO, &sparam);
        if (result != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "pthread_setschedparam() failed: %s (errno = %d).",
                       strerror(result), result);
            /*TODO: Add an expressive error message */
            pthread_exit((void *) 1);
        }
    }

    /* some boosters like the Maerklin 6017 must be initialized */
    result = tcflow(buses[busnumber].device.file.fd, TCOON);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }

    set_SerialLine(busnumber, SL_DTR, ON);

    wresult = write(buses[busnumber].device.file.fd, "SRCP-DAEMON", 11);
    if (wresult == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "write() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }

    result = tcflush(buses[busnumber].device.file.fd, TCOFLUSH);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflush() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }

    /* now set some serial lines */
    result = tcflow(buses[busnumber].device.file.fd, TCOOFF);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }
    set_SerialLine(busnumber, SL_RTS, ON);      /* +12V for ever on RTS   */
    set_SerialLine(busnumber, SL_CTS, OFF);     /* -12V for ever on CTS   */
    set_SerialLine(busnumber, SL_DTR, OFF);     /* disable booster output */

    result = tcflow(buses[busnumber].device.file.fd, TCOON);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "tcflow() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now? */
    }
    set_SerialLine(busnumber, SL_DTR, ON);

    gettimeofday(&tv1, &tz);
    for (;;) {
        if (power_is_off(busnumber))
            continue;
        wresult = write(buses[busnumber].device.file.fd,
                        __DDL->idle_data, MAXDATA);
        if (wresult == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "write() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        /* Check if there are new commands and send them. */
        packet_type = queue_get(busnumber, &addr, packet, &packet_size);
        if (packet_type > QNOVALIDPKT) {
            result = tcflush(buses[busnumber].device.file.fd, TCOFLUSH);
            if (result == -1) {
                syslog_bus(busnumber, DBG_FATAL,
                           "tcflush() failed: %s (errno = %d).",
                           strerror(result), result);
                /*What to do now? */
            }

            while (packet_type > QNOVALIDPKT) {

                /* if power is off, wait here until power is turned on
                 * again */
                if (power_is_off(busnumber))
                    continue;

                send_packet(busnumber, packet, packet_size,
                            packet_type, false);
                if (__DDL->ENABLED_PROTOCOLS == (EP_MAERKLIN | EP_NMRADCC)) {
                    wresult = write(buses[busnumber].device.file.fd,
                                    __DDL->NMRA_idle_data,
                                    __DDL->NMRA_idle_data_size);
                    if (wresult == -1) {
                        syslog_bus(busnumber, DBG_ERROR,
                                   "write() failed: %s (errno = %d)\n",
                                   strerror(errno), errno);
                    }
                }
                packet_type =
                    queue_get(busnumber, &addr, packet, &packet_size);
            }
        }

        /* If there are no new commands, send a loco state refresch; but
         * only if the last refresh was applied more than 100 ms ago. */
        else {
            if (power_is_off(busnumber))
                continue;

            gettimeofday(&tv2, &tz);

            if (compute_delta(tv1, tv2) > 100000) {
                refresh_loco(busnumber);
                gettimeofday(&tv1, &tz);
            }
            else
                nanosleep_DDL(&rqtp_sleep, &__DDL->rmtp);
        }
    }

    return NULL;
}

static int init_gl_DDL(gl_state_t * gl)
{
    switch (gl->protocol) {
        case 'M':              /* Motorola Codes */
            if (gl->n_func < 0 || gl->n_func > 5) return SRCP_WRONGVALUE;
            switch (gl->protocolversion) {
               case 1: return (gl->id >= 0 && gl->id < 80 &&
                               gl->n_fs == 14) ? 
                               SRCP_OK : SRCP_WRONGVALUE;
               case 2: return (gl->id >= 0 && gl->id <= 80 &&
                               gl->n_fs == 14) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 3: return (gl->id >= 0 && gl->id <= 255 &&
                               gl->n_fs == 28) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 4: return (gl->id >= 0 && gl->id <= 255 &&
                               gl->n_fs == 14) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 5: return (gl->id >= 0 && gl->id <= 255 &&
                               gl->n_fs == 28) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               default: return SRCP_WRONGVALUE;
            }
            /*
            return (gl->protocolversion > 0
                    && gl->protocolversion <=
                    5) ? SRCP_OK : SRCP_WRONGVALUE;
            */
            break;
        case 'N':
            if (gl->n_func < 0 || gl->n_func > 28) return SRCP_WRONGVALUE;
            switch (gl->protocolversion) {
               case 1: return (gl->id >= 0 && gl->id < 128 &&
                               gl->n_fs == 28) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 2: return (gl->id >= 0 && gl->id < 128 &&
                               gl->n_fs == 128) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 3: return (gl->id >= 0 && gl->id < 10240 &&
                               gl->n_fs == 28) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 4: return (gl->id >= 0 && gl->id < 10240 &&
                               gl->n_fs == 28) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               case 5: return (gl->id >= 0 && gl->id < 128 &&
                               gl->n_fs == 14) ?
                               SRCP_OK : SRCP_WRONGVALUE;
               default: return SRCP_WRONGVALUE;
            }
            /*
            return (gl->protocolversion > 0
                    && gl->protocolversion <=
                    5) ? SRCP_OK : SRCP_WRONGVALUE;
            */
            break;
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}

static int init_ga_DDL(ga_state_t * ga)
{
    switch (ga->protocol) {
        case 'M':              /* Motorola Codes */
            return SRCP_OK;
            break;
        case 'N':
            return SRCP_OK;
            break;
    }
    return SRCP_UNSUPPORTEDDEVICEPROTOCOL;
}


int readconfig_DDL(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    struct utsname utsBuffer;
    char buf[3];
    int result;

    buses[busnumber].driverdata = malloc(sizeof(struct _DDL_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_DDL;
    buses[busnumber].init_func = &init_bus_DDL;
    buses[busnumber].init_gl_func = &init_gl_DDL;
    buses[busnumber].init_ga_func = &init_ga_DDL;

    buses[busnumber].thr_func = &thr_sendrec_DDL;

    strcpy(buses[busnumber].description,
           "GA GL SM POWER LOCK DESCRIPTION");
    __DDL->oslevel = 1;         /* kernel 2.6 */

    /* we need to check for kernel version below 2.6 or below */
    /* the following code breaks if a kernel version 2.10 will ever occur */
    result = uname(&utsBuffer);
    if (result == -1) {
        syslog_bus(busnumber, DBG_FATAL,
                   "uname() failed: %s (errno = %d).",
                   strerror(result), result);
        /*What to do now */
    }
    snprintf(buf, sizeof(buf), "%c%c", utsBuffer.release[0],
             utsBuffer.release[2]);

    if (atoi(buf) > 25) {
        __DDL->oslevel = 1;     /* kernel 2.6 or later */
    }
    else {
        __DDL->oslevel = 0;     /* kernel 2.5 or earlier */
    }

    __DDL->number_gl = 81;
    __DDL->number_ga = 324;

    __DDL->RI_CHECK = false;    /* ring indicator checking      */
    __DDL->CHECKSHORT = false;  /* default no shortcut checking */
    __DDL->DSR_INVERSE = false; /* controls how DSR is used to  */
    /*                             check short-circuits         */
    __DDL->SHORTCUTDELAY = 0;   /* usecs shortcut delay         */
    __DDL->NMRADCC_TR_V = 3;    /* version of the NMRA dcc      */
    /*                             translation routine(1, 2 or 3) */
    __DDL->ENABLED_PROTOCOLS = (EP_MAERKLIN | EP_NMRADCC);      /* enabled p's */
    __DDL->IMPROVE_NMRADCC_TIMING = 0;  /* NMRA DCC: improve timing    */

    __DDL->WAITUART_USLEEP_PATCH = true;        /* enable/disable usleep patch */
    __DDL->WAITUART_USLEEP_USEC = 100;  /* usecs for usleep patch      */
    __DDL->NMRA_GA_OFFSET = 0;  /* offset for ga base address 0 or 1  */
    __DDL->PROGRAM_TRACK = 1;   /* 0: suppress SM commands to PT address */

    __DDL->SERIAL_DEVICE_MODE = SDM_NOTINITIALIZED;

    xmlNodePtr child = node->children;
    xmlChar *txt = NULL;

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_gl") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->number_gl = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "number_ga") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->number_ga = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp
                 (child->name,
                  BAD_CAST "enable_ringindicator_checking") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->RI_CHECK =
                    (xmlStrcmp(txt, BAD_CAST "yes") == 0) ? true : false;
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp
                 (child->name, BAD_CAST "enable_checkshort_checking")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->CHECKSHORT =
                    (xmlStrcmp(txt, BAD_CAST "yes") == 0) ? true : false;
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "inverse_dsr_handling") ==
                 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            __DDL->DSR_INVERSE =
                (xmlStrcmp(txt, BAD_CAST "yes") == 0) ? true : false;
            xmlFree(txt);
        }

        else if (xmlStrcmp(child->name, BAD_CAST "enable_maerklin") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "yes") == 0)
                    __DDL->ENABLED_PROTOCOLS |= EP_MAERKLIN;
                else
                    __DDL->ENABLED_PROTOCOLS &= ~EP_MAERKLIN;

                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "enable_nmradcc") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "yes") == 0)
                    __DDL->ENABLED_PROTOCOLS |= EP_NMRADCC;
                else
                    __DDL->ENABLED_PROTOCOLS &= ~EP_NMRADCC;

                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "improve_nmradcc_timing")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->IMPROVE_NMRADCC_TIMING = (xmlStrcmp(txt,
                                                           BAD_CAST "yes")
                                                 == 0) ? true : false;
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "shortcut_failure_delay")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->SHORTCUTDELAY = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp
                 (child->name, BAD_CAST "nmradcc_translation_routine")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->NMRADCC_TR_V = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "enable_usleep_patch") ==
                 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->WAITUART_USLEEP_PATCH = (xmlStrcmp(txt,
                                                          BAD_CAST "yes")
                                                == 0) ? true : false;
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "usleep_usec") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->WAITUART_USLEEP_USEC = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "nmra_ga_offset") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->NMRA_GA_OFFSET = atoi((char *) txt);
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "program_track") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __DDL->PROGRAM_TRACK = (xmlStrcmp(txt, BAD_CAST "yes")
                                        == 0) ? true : false;
                xmlFree(txt);
            }
        }
        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }                           /* while */

    if (init_GA(busnumber, __DDL->number_ga)) {
        __DDL->number_ga = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for accessories");
    }

    if (init_GL(busnumber, __DDL->number_gl)) {
        __DDL->number_gl = 0;
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for locomotives");
    }
    if (__DDL->IMPROVE_NMRADCC_TIMING && __DDL->oslevel == 1) {
        syslog_bus(busnumber, DBG_NONE,
                   "Improve nmra dcc timing causes changes on serial "
                   "lines custom divisor."
                   "This is deprecated on kernel 2.6 or later. You "
                   "better disable this feature in srcpd.conf");
    }

    return (1);
}

/* Initialisiere den Bus, signalisiere Fehler */
/* Einmal aufgerufen mit busnummer als einzigem Parameter */
/* return code wird ignoriert (vorerst) */
int init_bus_DDL(bus_t busnumber)
{
    int i;
    static char protocols[3] = { '\0', '\0', '\0' };
    int protocol = 0;

    syslog_bus(busnumber, DBG_INFO, "DDL init with debug level %d",
               buses[busnumber].debuglevel);

    buses[busnumber].device.file.fd = init_lineDDL(busnumber);

    __DDL->short_detected = 0;

    __DDL->queue_initialized = false;
    __DDL->queue_out = 0;
    __DDL->queue_in = 0;
    queue_init(busnumber);

    /* generate idle_data */
    for (i = 0; i < MAXDATA; i++)
        __DDL->idle_data[i] = 0x55;     /* 0x55 = 01010101 */
    for (i = 0; i < PKTSIZE; i++)
        __DDL->NMRA_idle_data[i] = 0x55;
    __DDL->NMRA_idle_data_size = PKTSIZE;

    /*
     * ATTENTION:
     *   If NMRA dcc mode is activated __DDL->idle_data[] and
     *   __DDL->NMRA_idle_data must be overridden from init_NMRAPacketPool().
     *   This means, that init_NMRAPacketPool()
     *   must be called after init_MaerklinPacketPool().
     */

    __DDL->last_refreshed_maerklin_loco = 0;
    __DDL->last_refreshed_maerklin_fx = -1;
    __DDL->last_refreshed_nmra_loco = 0;
    __DDL->last_refreshed_nmra_fx = -1;

    if (__DDL->ENABLED_PROTOCOLS & EP_MAERKLIN) {
        init_MaerklinPacketPool(busnumber);
        __DDL->maerklin_refresh = 1;
        protocols[protocol++] = 'M';
    }
    else {
        __DDL->maerklin_refresh = 0;
        __DDL->MaerklinPacketPool.NrOfKnownAddresses = 0;
    }
    if (__DDL->ENABLED_PROTOCOLS & EP_NMRADCC) {
        init_NMRAPacketPool(busnumber);
        protocols[protocol++] = 'N';
    }
    syslog_bus(busnumber, DBG_INFO, "DDL init done");
    buses[busnumber].protocols = protocols;
    return 0;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;
    /* store thread return value here */
    void *pThreadReturn;
    int busnumber = btd->bus;

    /* send cancel to refresh cycle */
    result = pthread_cancel(((DDL_DATA *) buses[btd->bus].driverdata)->
                            refresh_ptid);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "pthread_cancel() failed: %s (errno = %d).",
                   strerror(result), result);
    }
    /* wait until the refresh cycle has terminated */
    result =
        pthread_join(((DDL_DATA *) buses[btd->bus].driverdata)->
                     refresh_ptid, &pThreadReturn);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "pthread_join() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    set_lines_off(btd->bus);

    /* pthread_cond_destroy(&(__DDL->refresh_cond)); */
    if (buses[btd->bus].device.file.fd != -1)
        close(buses[btd->bus].device.file.fd);

    result = pthread_mutex_destroy(&buses[btd->bus].transmit_mutex);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "pthread_mutex_destroy() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    result = pthread_cond_destroy(&buses[btd->bus].transmit_cond);
    if (result != 0) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "pthread_cond_destroy() failed: %s (errno = %d).",
                   strerror(result), result);
    }

    syslog_bus(btd->bus, DBG_INFO, "DDL bus terminated.");

    if (__DDL->ENABLED_PROTOCOLS & EP_NMRADCC) {
        reset_NMRAPacketPool(btd->bus);
    }
    free(buses[btd->bus].driverdata);
    free(btd);
}


typedef struct _delayedGAResetCmdData {
    int busnumber;
    ga_state_t *gastate;
} delayedGAResetCmdData;


/* sends a GA reset command after a delay */
void *thr_delayedGAResetCmd(void *v)
{

    delayedGAResetCmdData *delgatmp = (delayedGAResetCmdData *) v;
    ga_state_t *gatmp = delgatmp->gastate;
    int busnumber = delgatmp->busnumber;
    free(v);

    if (usleep((unsigned long) gatmp->activetime * 1000) == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "usleep() failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }
    gatmp->action = 0;
    syslog_bus(busnumber, DBG_DEBUG,
               "Delayed GA command (threaded): %c (%x) %d",
               gatmp->protocol, gatmp->protocol, gatmp->id);
    switch (gatmp->protocol) {
        case 'M':              /* Motorola Codes */
            comp_maerklin_ms(busnumber, gatmp->id, gatmp->port,
                             gatmp->action);
            break;
        case 'N':              /* NMRA DCC */
            comp_nmra_accessory(busnumber, gatmp->id, gatmp->port,
                                gatmp->action, __DDL->NMRA_GA_OFFSET);
            break;
    }
    setGA(busnumber, gatmp->id, *gatmp);
    return NULL;
}


static void *thr_sendrec_DDL(void *v)
{
    struct _SM smakt;
    gl_state_t gltmp;
    ga_state_t gatmp;
    int addr, error;
    int last_cancel_state, last_cancel_type;

    delayedGAResetCmdData *tmpv;
    pthread_t ptid_delacccmd;

    bus_thread_t *btd = (bus_thread_t *) malloc(sizeof(bus_thread_t));
    if (btd == NULL)
        pthread_exit((void *) 1);
    btd->bus = (bus_t) v;
    btd->fd = -1;

    pthread_setcancelstate(PTHREAD_CANCEL_ENABLE, &last_cancel_state);
    pthread_setcanceltype(PTHREAD_CANCEL_DEFERRED, &last_cancel_type);

    /*register cleanup routine */
    pthread_cleanup_push((void *) end_bus_thread, (void *) btd);

    syslog_bus(btd->bus, DBG_INFO, "DDL bus started (device = %s).",
               buses[btd->bus].device.file.path);

    buses[btd->bus].watchdog = 1;
    /*
     * Starting the thread that is responsible for the signals on 
     * serial port.
     */
    ((DDL_DATA *) buses[btd->bus].driverdata)->refresh_param.busnumber =
        btd->bus;
    error =
        pthread_create(&
                       (((DDL_DATA *) buses[btd->bus].driverdata)->
                        refresh_ptid), NULL, thr_refresh_cycle,
                       (void *)
                       &(((DDL_DATA *) buses[btd->bus].driverdata)->
                         refresh_param));

    if (error != 0) {
        syslog_bus(btd->bus, DBG_ERROR,
                   "pthread_create() failed: %s (errno = %d).",
                   strerror(error), error);
    }

    while (1) {
        pthread_testcancel();
        if (!queue_GL_isempty(btd->bus)) {
            char p;
            int pv;
            int speed;
            int direction;

            dequeueNextGL(btd->bus, &gltmp);
            p = gltmp.protocol;
            /* need to compute from the n_fs and n_func parameters */
            pv = gltmp.protocolversion;
            addr = gltmp.id;
            speed = gltmp.speed;
            direction = gltmp.direction;
            syslog_bus(btd->bus, DBG_DEBUG, "Next command: %c (%x) %d %d",
                       p, p, pv, addr);
            if (addr > 127)
                pv = 2;
            switch (p) {
                case 'M':      /* Motorola Codes */
                    if (speed == 1)
                        speed++;        /* Never send FS1 */
                    if (direction == 2)
                        speed = 0;
                    switch (pv) {
                        case 1:
                            comp_maerklin_1(btd->bus, addr,
                                            gltmp.direction, speed,
                                            gltmp.funcs & 0x01);
                            break;
                        case 2:
                            comp_maerklin_2(btd->bus, addr,
                                            gltmp.direction, speed,
                                            gltmp.funcs & 0x01,
                                            ((gltmp.funcs >> 1) & 0x01),
                                            ((gltmp.funcs >> 2) & 0x01),
                                            ((gltmp.funcs >> 3) & 0x01),
                                            ((gltmp.funcs >> 4) & 0x01));
                            break;
                        case 3:
                            comp_maerklin_3(btd->bus, addr,
                                            gltmp.direction, speed,
                                            gltmp.funcs & 0x01,
                                            ((gltmp.funcs >> 1) & 0x01),
                                            ((gltmp.funcs >> 2) & 0x01),
                                            ((gltmp.funcs >> 3) & 0x01),
                                            ((gltmp.funcs >> 4) & 0x01));
                            break;
                        case 4:
                            comp_maerklin_4(btd->bus, addr,
                                            gltmp.direction, speed,
                                            gltmp.funcs & 0x01,
                                            ((gltmp.funcs >> 1) & 0x01),
                                            ((gltmp.funcs >> 2) & 0x01),
                                            ((gltmp.funcs >> 3) & 0x01),
                                            ((gltmp.funcs >> 4) & 0x01));
                            break;
                        case 5:
                            comp_maerklin_5(btd->bus, addr,
                                            gltmp.direction, speed,
                                            gltmp.funcs & 0x01,
                                            ((gltmp.funcs >> 1) & 0x01),
                                            ((gltmp.funcs >> 2) & 0x01),
                                            ((gltmp.funcs >> 3) & 0x01),
                                            ((gltmp.funcs >> 4) & 0x01));
                            break;
                    }
                    break;
                case 'N':      /* NMRA / DCC Codes */
                    if (speed)
                        speed++;
                    if (direction != 2)
                        comp_nmra_multi_func(btd->bus, addr, direction,
                                             speed, gltmp.funcs,
                                             gltmp.n_fs, gltmp.n_func, pv);
                    else
                        /* emergency halt: FS 1 */
                        comp_nmra_multi_func(btd->bus, addr, 0,
                                             1, gltmp.funcs, gltmp.n_fs,
                                             gltmp.n_func, pv);
                    break;
            }
            cacheSetGL(btd->bus, addr, gltmp);
        }
        if (!queue_SM_isempty(btd->bus)) {
            dequeueNextSM(btd->bus, &smakt);
            int rc = -1;
            if (!strncmp(smakt.protocol, "NMRA", 4)) {
                switch (smakt.command) {
                    case SET:
                        /* addr 0 and -1 are considered as programming track */
                        /* larger addresses will by considered as PoM */
                        if (smakt.addr <= 0 && (((DDL_DATA *)
                                                 buses[btd->bus].
                                                 driverdata)->
                                                PROGRAM_TRACK)) {
                            switch (smakt.type) {
                                case REGISTER:
                                    rc = protocol_nmra_sm_write_phregister
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                                case CV:
                                    rc = protocol_nmra_sm_write_cvbyte
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                                case CV_BIT:
                                    rc = protocol_nmra_sm_write_cvbit(btd->
                                                                      bus,
                                                                      smakt.
                                                                      typeaddr,
                                                                      smakt.
                                                                      bit,
                                                                      smakt.
                                                                      value);
                                    break;
                                case PAGE:
                                    rc = protocol_nmra_sm_write_page
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                            }
                        }
                        else {
                            int mode = 1;
                            /* HACK protocolversion is not yet set in SM */
                            if (smakt.addr > 127)
                                mode = 2;
                            switch (smakt.type) {
                                case CV:
                                    rc = protocol_nmra_sm_write_cvbyte_pom
                                        (btd->bus, smakt.addr,
                                         smakt.typeaddr, smakt.value,
                                         mode);
                                    break;
                                case CV_BIT:
                                    rc = protocol_nmra_sm_write_cvbit_pom
                                        (btd->bus, smakt.addr,
                                         smakt.typeaddr, smakt.bit,
                                         smakt.value, mode);
                            }
                        }
                        break;
                    case GET:
                        if (smakt.addr <= 0) {
                            switch (smakt.type) {
                                case REGISTER:
                                    rc = protocol_nmra_sm_get_phregister
                                        (btd->bus, smakt.typeaddr);
                                    break;
                                case CV:
                                    rc = protocol_nmra_sm_get_cvbyte(btd->
                                                                     bus,
                                                                     smakt.
                                                                     typeaddr);
                                    break;
                                case CV_BIT:
                                    rc = protocol_nmra_sm_verify_cvbit
                                        (btd->bus, smakt.typeaddr,
                                         smakt.bit, 1);
                                    break;
                                case PAGE:
                                    rc = protocol_nmra_sm_get_page
                                        (btd->bus, smakt.typeaddr);
                                    break;
                            }
                        }
                        break;
                    case VERIFY:
                        if (smakt.addr <= 0) {
                            int my_rc = 0;
                            switch (smakt.type) {
                                case REGISTER:
                                    my_rc =
                                        protocol_nmra_sm_verify_phregister
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                                case CV:
                                    my_rc =
                                        protocol_nmra_sm_verify_cvbyte
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                                case CV_BIT:
                                    my_rc =
                                        protocol_nmra_sm_verify_cvbit(btd->
                                                                      bus,
                                                                      smakt.
                                                                      typeaddr,
                                                                      smakt.
                                                                      bit,
                                                                      smakt.
                                                                      value);
                                    break;
                                case PAGE:
                                    my_rc = protocol_nmra_sm_verify_page
                                        (btd->bus, smakt.typeaddr,
                                         smakt.value);
                                    break;
                            }
                            if (my_rc == 1) {
                                rc = smakt.value;
                            }
                        }
                        break;
                    case TERM:
                        rc = 0;
                        break;
                    case INIT:
                        rc = 0;
                        break;
                }
            }
            session_endwait(btd->bus, rc);
        }
        buses[btd->bus].watchdog = 4;

        if (!queue_GA_isempty(btd->bus)) {
            char p;
            dequeueNextGA(btd->bus, &gatmp);
            addr = gatmp.id;
            p = gatmp.protocol;
            syslog_bus(btd->bus, DBG_DEBUG, "Next GA command: %c (%x) %d",
                       p, p, addr);
            switch (p) {
                case 'M':      /* Motorola Codes */
                    comp_maerklin_ms(btd->bus, addr, gatmp.port,
                                     gatmp.action);
                    break;
                case 'N':
                    comp_nmra_accessory(btd->bus, addr, gatmp.port,
                                        gatmp.action,
                                        __DDLt->NMRA_GA_OFFSET);
                    break;
            }
            setGA(btd->bus, addr, gatmp);
            buses[btd->bus].watchdog = 5;

            if (gatmp.activetime >= 0) {

                /* the handling of delayed GA commands in this way, can only 
                   be a short term improvement. If srcpd will have better
                   inter-thread communication, it should be replaced. */

                if (gatmp.activetime < 1000) {
                    if (usleep((unsigned long) gatmp.activetime * 1000) ==
                        -1) {
                        syslog_bus(btd->bus, DBG_ERROR,
                                   "usleep() failed: %s (errno = %d)\n",
                                   strerror(errno), errno);
                    }
                    gatmp.action = 0;
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Delayed GA command: %c (%x) %d", p, p,
                               addr);
                    switch (p) {
                        case 'M':      /* Motorola Codes */
                            comp_maerklin_ms(btd->bus, addr, gatmp.port,
                                             gatmp.action);
                            break;
                        case 'N':
                            comp_nmra_accessory(btd->bus, addr, gatmp.port,
                                                gatmp.action,
                                                __DDLt->NMRA_GA_OFFSET);
                            break;
                    }
                    setGA(btd->bus, addr, gatmp);
                }
                else {
                    tmpv =
                        (delayedGAResetCmdData *)
                        malloc(sizeof(delayedGAResetCmdData));
                    if (!tmpv) {
                        syslog_bus(btd->bus, DBG_ERROR,
                                   "malloc() failed!");
                        continue;
                    }
                    tmpv->busnumber = btd->bus;
                    tmpv->gastate = &gatmp;
                    error = pthread_create(&ptid_delacccmd, NULL,
                                           thr_delayedGAResetCmd, tmpv);
                    if (error == 0) {
                        pthread_detach(ptid_delacccmd);
                    }
                    else {
                        syslog_bus(btd->bus, DBG_ERROR,
                                   "pthread_create() failed: %s (errno = %d).",
                                   strerror(error), error);
                    }
                }
            }
        }
        if (usleep(3000) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }

    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
