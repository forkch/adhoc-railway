/* $Id: loconet.c 1547 2012-01-10 19:42:34Z gscholz $ */

/*
 * loconet: loconet/srcp gateway
 */

#include <errno.h>
#include <fcntl.h>
#include <netdb.h>
#include <stdbool.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <sys/ioctl.h>
#include <netinet/in.h>

#include "config.h"
#ifdef HAVE_LINUX_SERIAL_H
#include <linux/serial.h>
#else
#warning "MS100 support for Linux only!"
#endif

#include "config-srcpd.h"
#include "io.h"
#include "loconet.h"
#include "srcp-fb.h"
#include "srcp-ga.h"
#include "srcp-gl.h"
#include "srcp-sm.h"
#include "srcp-power.h"
#include "srcp-server.h"
#include "srcp-time.h"
#include "srcp-info.h"
#include "srcp-session.h"
#include "srcp-error.h"
#include "syslogmessage.h"

#define __loconet ((LOCONET_DATA*)buses[busnumber].driverdata)
#define __loconett ((LOCONET_DATA*)buses[btd->bus].driverdata)

static int init_gl_LOCONET(bus_t bus,gl_state_t *);
static int init_ga_LOCONET(ga_state_t *);
/**
 * Read and analyze the XML subtree for the <loconet> configuration.
 *
 */
int readConfig_LOCONET(xmlDocPtr doc, xmlNodePtr node, bus_t busnumber)
{
    xmlNodePtr child = node->children;
    xmlChar *txt;

    buses[busnumber].driverdata = malloc(sizeof(struct _LOCONET_DATA));

    if (buses[busnumber].driverdata == NULL) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Memory allocation error in module '%s'.", node->name);
        return 0;
    }

    buses[busnumber].type = SERVER_LOCONET;
    buses[busnumber].init_func = &init_bus_LOCONET;
    buses[busnumber].thr_func = &thr_sendrec_LOCONET;
    buses[busnumber].init_gl_func = &init_gl_LOCONET;
    buses[busnumber].init_ga_func = &init_ga_LOCONET;

    __loconet->number_fb = 2048;        /* max address for OPC_INPUT_REP (10+1 bit) */
    __loconet->number_ga = 2048;        /* max address for OPC_SW_REQ */
    __loconet->number_gl = 9999;        /* DCC address range */
    __loconet->loconetID = 0x50;        /* Loconet ID */
    buses[busnumber].device.file.baudrate = B57600;
    memset(__loconet->slotmap, 0, sizeof(__loconet->slotmap));

    strcpy(buses[busnumber].description, "GA GL FB POWER DESCRIPTION");

    while (child != NULL) {

        if ((xmlStrncmp(child->name, BAD_CAST "text", 4) == 0) ||
            (xmlStrncmp(child->name, BAD_CAST "comment", 7) == 0)) {
            /* just do nothing, it is only formatting text or a comment */
        }

        else if (xmlStrcmp(child->name, BAD_CAST "loconet-id") == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                __loconet->loconetID = (unsigned char) atoi((char *) txt);
                xmlFree(txt);
            }
        }
        else if (xmlStrcmp(child->name, BAD_CAST "sync-time-from-loconet")
                 == 0) {
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "yes") == 0) {
                    __loconet->flags |= LN_FLAG_GETTIME;
                }
                else {
                    __loconet->flags &= ~LN_FLAG_GETTIME;
                }
                xmlFree(txt);
            }
        }

        else if (xmlStrcmp(child->name, BAD_CAST "ms100") == 0) {
#ifdef HAVE_LINUX_SERIAL_H
            txt = xmlNodeListGetString(doc, child->xmlChildrenNode, 1);
            if (txt != NULL) {
                if (xmlStrcmp(txt, BAD_CAST "yes") == 0) {
                    __loconet->flags |= LN_FLAG_MS100;
                }
                else {
                    __loconet->flags &= ~LN_FLAG_MS100;
                }
                xmlFree(txt);
            }
#endif
        }
        else
            syslog_bus(busnumber, DBG_WARN,
                       "WARNING, unknown tag found: \"%s\"!\n",
                       child->name);;

        child = child->next;
    }                           /* while */

    if (init_FB(busnumber, __loconet->number_fb)) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Can't create array for feedback");
    }

    if (init_GA(busnumber, __loconet->number_ga)) {
        syslog_bus(busnumber, DBG_ERROR, "Can't create array for GA");
    }

    if (init_GL(busnumber, __loconet->number_gl)) {
        syslog_bus(busnumber, DBG_ERROR, "Can't create array for GL");
    }

    return (1);
}

static int cacheGetSlotNumberforAddr(bus_t busnumber, unsigned int addr)
{
    int i;
    syslog_bus(busnumber, DBG_DEBUG,
               "looking up slot number for address %d", addr);
    for (i = 1; i < 128; i++) {
        if (__loconet->slotmap[i] == addr) {
            syslog_bus(busnumber, DBG_DEBUG,
                       "found slot %d for address %d", i, addr);
            return i;
        }
    }
    return -1;
}

static int init_lineLOCONET_serial(bus_t busnumber)
{
    int fd;
    int result;
    struct termios interface;

    fd = open(buses[busnumber].device.file.path,
              O_RDWR | O_NDELAY | O_NOCTTY);
    if (fd == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Device open failed: %s (errno = %d). "
                   "Terminating...\n", strerror(errno), errno);
        return 1;
    }
    buses[busnumber].device.file.fd = fd;
#ifdef HAVE_LINUX_SERIAL_H
    if ((__loconet->flags & LN_FLAG_MS100) == LN_FLAG_MS100) {
        struct serial_struct serial;
        struct termios tios;
        unsigned int cm;

        result = ioctl(fd, TIOCGSERIAL, &serial);
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        serial.custom_divisor = 7;
        serial.flags &= ~ASYNC_USR_MASK;
        serial.flags |= ASYNC_SPD_CUST | ASYNC_LOW_LATENCY;

        result = ioctl(fd, TIOCSSERIAL, &serial);
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        tcgetattr(fd, &tios);
        tios.c_iflag = IGNBRK | IGNPAR;
        tios.c_oflag = 0;
        tios.c_cflag = CS8 | CREAD | CLOCAL;
        tios.c_lflag = 0;
        cfsetospeed(&tios, buses[busnumber].device.file.baudrate);
        tcsetattr(fd, TCSANOW, &tios);

        tcflow(fd, TCOON);
        tcflow(fd, TCION);

        result = ioctl(fd, TIOCMGET, &cm);
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        cm &= ~TIOCM_DTR;
        cm |= TIOCM_RTS | TIOCM_CTS;
        result = ioctl(fd, TIOCMSET, &cm);
        if (result == -1) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ioctl() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }

        tcflush(fd, TCOFLUSH);
        tcflush(fd, TCIFLUSH);
    }
    else {
#endif
        tcgetattr(fd, &interface);
        interface.c_oflag = ONOCR;
        interface.c_cflag = CS8 | CLOCAL | CREAD | HUPCL;
        if ((buses[busnumber].device.file.settings & SER_FC_HARD) == SER_FC_HARD) {
          interface.c_cflag |=  CRTSCTS;
            syslog_bus(busnumber, DBG_DEBUG,
                       "using hardware flow control (CRTRTS)");
        }
        if ((buses[busnumber].device.file.settings & SER_FC_SOFT) == SER_FC_SOFT) {
          interface.c_oflag |=  IXON;
            syslog_bus(busnumber, DBG_DEBUG,
                       "using soft flow control (XON/XOFF)");
        }
        interface.c_iflag = IGNBRK;
        interface.c_lflag = IEXTEN;
        interface.c_lflag &= ~(ECHO | ICANON | IEXTEN | ISIG);
        cfsetispeed(&interface, buses[busnumber].device.file.baudrate);
        cfsetospeed(&interface, buses[busnumber].device.file.baudrate);
        interface.c_cc[VMIN] = 0;
        interface.c_cc[VTIME] = 0;

        tcsetattr(fd, TCSANOW, &interface);
#ifdef HAVE_LINUX_SERIAL_H
    }
#endif
    return 1;

}

static int init_lineLOCONET_lbserver(bus_t busnumber)
{
    int sockfd = -1;
    struct addrinfo *ai;
    struct addrinfo hi;
    int result;
    char msg[256];

    memset(&hi, '\0', sizeof(hi));

    /* Set preferred network connection options, for Cygwin use IPv4-only
     * as IPv6 is not supported yet */
#if defined(__CYGWIN__) || defined(__OpenBSD__)
    hi.ai_family = AF_INET;
    hi.ai_protocol = IPPROTO_TCP;
#else                                            
    hi.ai_flags = AI_ADDRCONFIG;
#endif
    hi.ai_socktype = SOCK_STREAM;
    result = getaddrinfo(buses[busnumber].device.net.hostname,
                         buses[busnumber].device.net.port, &hi, &ai);
    if (result != 0) {
        syslog_bus(busnumber, DBG_ERROR, "getaddrinfo %s",
                   gai_strerror(result));
        return 0;
    }
    struct addrinfo *runp = ai;
    if (runp != NULL) {
        sockfd =
            socket(runp->ai_family, runp->ai_socktype, runp->ai_protocol);
        if (sockfd == -1) {
            syslog_bus(busnumber, DBG_FATAL,
                       "Socket creation failed: %s (errno = %d).\n",
                       strerror(errno), errno);
            return 0;
        }
        alarm(30);
        if (connect(sockfd, runp->ai_addr, runp->ai_addrlen) != 0) {
            syslog_bus(busnumber, DBG_ERROR,
                       "ERROR connecting to %s:%d %d",
                       buses[busnumber].device.net.hostname,
                       buses[busnumber].device.net.port, errno);
            close(sockfd);
            return 0;
        }
        alarm(0);

        result = socket_readline(sockfd, msg, sizeof(msg) - 1);

        /* client terminated connection */
        if (0 == result) {
            freeaddrinfo(ai);
            shutdown(sockfd, SHUT_RDWR);
            return 0;
        }
    }
    /* read errror */
    if (-1 == result) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Socket read failed: %s (errno = %d)\n",
                   strerror(errno), errno);
        return (-1);
    }
    freeaddrinfo(ai);
    syslog_bus(busnumber, DBG_INFO, "connected to %s", msg);
    buses[busnumber].device.net.sockfd = sockfd;
    return 1;

}

static int init_lineLOCONET(bus_t busnumber)
{
    DeviceState rc = devNONE;

    switch (buses[busnumber].devicetype) {
        case HW_FILENAME:
            rc = init_lineLOCONET_serial(busnumber);
            break;
        case HW_NETWORK:
            rc = init_lineLOCONET_lbserver(busnumber);
            break;
    }
    buses[busnumber].devicestate = rc;
    return rc;
}

/**
 *
 */
static int init_gl_LOCONET(bus_t bus, gl_state_t * gl)
{
    /* this is a complex transaction 
       1. send out a BF<hi><lo> Message. 
       2. wait for the E7 response, if a B4 comes, the slot table is full, exit
       3. check the slot status. If it is IN_USE or CONSISTED, mark the address
       as (SRCP-)locked and exit
       4. mark the slot as IN_USE with a BA<slot><slot> message
     */
    return SRCP_OK;
}

/**
 * GA don't need initialization
 */
static int init_ga_LOCONET(ga_state_t * ga)
{
    return SRCP_OK;
}

/*
 * @param srcp busnumber
 * @return 0 if OK, -1 on error
 */
int init_bus_LOCONET(bus_t busnumber)
{
    int result = 0;
    static char *protocols = "LPMN";

    buses[busnumber].protocols = protocols;
    __loconet->sent_packets = __loconet->recv_packets = 0;
    __loconet->ibufferin = 0;
    syslog_bus(busnumber, DBG_INFO, "Loconet init: bus #%d, debug %d",
               busnumber, buses[busnumber].debuglevel);

    if (buses[busnumber].debuglevel <= 5)
        result = (init_lineLOCONET(busnumber) == 0) ? -1 : 0;

    syslog_bus(busnumber, DBG_INFO, "Loconet bus %ld init done",
               busnumber);
    return result;
}

static unsigned char ln_checksum(const unsigned char *cmd, int len)
{
    unsigned char chksum = 0xff;
    int i;
    for (i = 0; i < len; i++) {
        chksum ^= cmd[i];
    }
    return chksum;
}

static int ln_read_serial(bus_t busnumber, unsigned char *cmd, int len)
{
    /* two tasks: first check the serial line for a character, append it to
       the buffer. Second: check the buffer for a complete loconet
       packet, remove it from the buffer and transfer it to the caller
       if complete. */
    int fd = buses[busnumber].device.file.fd;
    fd_set fds;
    struct timeval t = { 0, 0 };
    int retval;
    unsigned char c;
    ssize_t pktlen;

    FD_ZERO(&fds);
    FD_SET(fd, &fds);
    retval = select(fd + 1, &fds, NULL, NULL, &t);

    /* read data from locobuffer,
       we skip everthing before the first ln-packet later */
    if (retval > 0 && FD_ISSET(fd, &fds)) {
        pktlen = read(fd, &c, 1);
        __loconet->ibuffer[__loconet->ibufferin++] = c;
    }

    /* now examine the buffer */
    __loconet->ibufferout = 0;
    if (__loconet->ibufferin < 2)
        return (0);

    /* first skip everything up to the next loconet packet start */
    while ((__loconet->ibuffer[__loconet->ibufferout] & 0x80) != 0x80)
        __loconet->ibufferout++;
    switch (__loconet->ibuffer[__loconet->ibufferout] & 0xe0) {
        case 0x80:
            pktlen = 2;
            break;
        case 0xa0:
            pktlen = 4;
            break;
        case 0xc0:
            pktlen = 6;
            break;
        case 0xe0:
            pktlen = __loconet->ibuffer[__loconet->ibufferout + 1];
            break;
    }

    /* complete packet ? */
    if (__loconet->ibufferout + pktlen > __loconet->ibufferin) {
        return (0);
    }

    syslog_bus(busnumber, DBG_DEBUG,
               "got a packet size %d, first 2 byte: 0x%02x%02x ", pktlen,
               __loconet->ibuffer[__loconet->ibufferout],
               __loconet->ibuffer[__loconet->ibufferout + 1]);
    /* copy the packet to the transfer buffer and mark the input buffer empty */
    memcpy(cmd, &__loconet->ibuffer[__loconet->ibufferout], pktlen);
    __loconet->ibufferin = 0;
    __loconet->recv_packets++;
    return pktlen;
}


static int ln_read_lbserver(bus_t busnumber, unsigned char *cmd, int len)
{
    int fd = buses[busnumber].device.net.sockfd;
    fd_set fds;
    struct timeval t = { 0, 0 };
    int retval = 0;
    char line[256];
    ssize_t result;

    FD_ZERO(&fds);
    FD_SET(fd, &fds);
    retval = select(fd + 1, &fds, NULL, NULL, &t);

    if (retval > 0 && FD_ISSET(fd, &fds)) {
        result = socket_readline(fd, line, sizeof(line) - 1);

        /* client terminated connection */
        if (0 == result) {
            shutdown(fd, SHUT_RDWR);
            close(fd);
            buses[busnumber].devicestate = devFAIL;
            return 0;
        }

        /* read errror */
        else if (-1 == result) {
            buses[busnumber].devicestate = devFAIL;
            shutdown(fd, SHUT_RDWR);
            close(fd);
            syslog_bus(busnumber, DBG_WARN,
                       "Socket read failed: %s (errno = %d)\n",
                       strerror(errno), errno);
            return 0;
        }

        /* line may begin with
           SENT message: last command was sent (or not)
           RECEIVE message: new message from Loconet
           VERSION text: VERSION information about the server */

        if (strstr(line, "RECEIVE ")) {
            /* we have a fixed format */
            size_t len = strlen(line) - 7;
            int pktlen = len / 3;
            int i;
            char *d;
            for (i = 0; i < pktlen; i++) {
                cmd[i] = strtol(line + 7 + 3 * i, &d, 16);
                /* syslog_bus(busnumber, DBG_DEBUG, " * %d %d ", i, cmd[i]); */
            }
            retval = pktlen;
        }
        __loconet->recv_packets++;
    }
    else if (retval == -1) {
        syslog_bus(busnumber, DBG_ERROR,
                   "Select failed: %s (errno = %d)\n", strerror(errno),
                   errno);
    }
    return retval;
}

static int ln_read(bus_t busnumber, unsigned char *cmd, int len)
{
    int rc = 0;
    /* re-establish a lost connection, nothing else can be done at this point */
    while (buses[busnumber].devicestate != devOK) {
        sleep(1);
        init_lineLOCONET(busnumber);
    }

    switch (buses[busnumber].devicetype) {
        case HW_FILENAME:
            rc = ln_read_serial(busnumber, cmd, len);
            break;
        case HW_NETWORK:
            rc = ln_read_lbserver(busnumber, cmd, len);
            break;
    }

    if (rc > 0) {
        syslog_bus(busnumber, DBG_DEBUG,
                   "received Loconet packet with OPC 0x%02X. %s to "
                   "send commands to loconet",
                   cmd[0], cmd[0] & 0x08 ? "block" : "ok");
    }
    return rc;
}


static int ln_write_lbserver(long int busnumber, const unsigned char *cmd,
                             unsigned char len)
{
    unsigned char i;
    ssize_t result;
    char msg[256], tmp[10];

    snprintf(msg, sizeof(msg), "SEND");
    for (i = 0; i < len; i++) {
        snprintf(tmp, sizeof(tmp), " %02X", cmd[i]);
        strcat(msg, tmp);
    }
    strcat(msg, "\r\n");
    result = writen(buses[busnumber].device.net.sockfd, msg, strlen(msg));
    if (result == -1) {
        buses[busnumber].devicestate = devFAIL;
        shutdown(buses[busnumber].device.net.sockfd, SHUT_RDWR);
        close(buses[busnumber].device.net.sockfd);
        syslog_bus(busnumber, DBG_WARN,
                   "Socket write failed: %s (errno = %d)\n",
                   strerror(errno), errno);
    }
    __loconet->sent_packets++;
    return 0;
}


static int ln_write_serial(bus_t busnumber, const unsigned char *cmd,
                           unsigned char len)
{
    unsigned char i;
    for (i = 0; i < len; i++) {
        writeByte(busnumber, cmd[i], 0);
    }
    __loconet->sent_packets++;
    return 0;
}

static int ln_write(bus_t busnumber, const unsigned char *cmd,
                    unsigned char len)
{
    syslog_bus(busnumber, DBG_DEBUG,
               "sent Loconet packet with OPC 0x%02X, %d bytes", cmd[0],
               len);
    switch (buses[busnumber].devicetype) {
        case HW_FILENAME:
            return ln_write_serial(busnumber, cmd, len);
            break;
        case HW_NETWORK:
            return ln_write_lbserver(busnumber, cmd, len);
            break;
    }
    return 0;
}

/*thread cleanup routine for this bus*/
static void end_bus_thread(bus_thread_t * btd)
{
    int result;

    syslog_bus(btd->bus, DBG_INFO, "Loconet bus terminated.");

    switch (buses[btd->bus].devicetype) {
        case HW_FILENAME:
            close(buses[btd->bus].device.file.fd);
            break;
        case HW_NETWORK:
            shutdown(buses[btd->bus].device.net.sockfd, SHUT_RDWR);
            close(buses[btd->bus].device.net.sockfd);
            break;
    }

    syslog_bus(btd->bus, DBG_INFO,
               "Loconet bus: %u packets sent, %u packets received",
               __loconett->sent_packets, __loconett->recv_packets);

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

/* check if the loconet is available, there are two message transfers,
 * that shall not be disturbed
 */
static int ln_sendmessage(bus_t bus, int ln_packetlen,
                          unsigned char *ln_packet)
{
    ln_packet[ln_packetlen - 1] = ln_checksum(ln_packet, ln_packetlen - 1);
    if (ln_packet[0] != OPC_IDLE) {
        ln_write(bus, ln_packet, ln_packetlen);
        return 0;
    }
    return 1;
}

/* static int speed_list[] = {28,28,14,128,28,0,0,128}; */

void *thr_sendrec_LOCONET(void *v)
{
    unsigned char ln_packet[128];       /* max length is coded with 7 bit */
    unsigned char ln_packetlen = 2;
    unsigned int addr, timeoutcnt, twomessageflag;
    unsigned int startup_slot_index = 1;        /* read the slot numbers upon start up */
    /*int code, src, dst, data[8], i; */
    int value, port, tmp;
    int speed = 0;
    char msg[110];
    ga_state_t gatmp;
    gl_state_t gltmp;
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

    timeoutcnt = 0;

    while (1) {
        pthread_testcancel();
        buses[btd->bus].watchdog = 1;
        memset(ln_packet, 0, sizeof(ln_packet));
        twomessageflag = 0;
        /* first action is always a read _from_ Loconet */
        if ((ln_packetlen = ln_read(btd->bus, ln_packet,
                                    sizeof(ln_packet))) > 1) {
            twomessageflag = (ln_packet[0] & 0x08) ? 0 : 1;     /* block:ok */
            switch (ln_packet[0]) {
                    /* basic operations, 2byte Commands on Loconet */
                case OPC_GPOFF:
                    buses[btd->bus].power_state = 0;
                    strcpy(buses[btd->bus].power_msg, "from Loconet");
                    infoPower(btd->bus, msg);
                    enqueueInfoMessage(msg);
                    break;
                case OPC_GPON:
                    buses[btd->bus].power_state = 1;
                    strcpy(buses[btd->bus].power_msg, "from Loconet");
                    infoPower(btd->bus, msg);
                    enqueueInfoMessage(msg);
                    break;
                    /* 4byte Commands and Reports on Loconet */
                    /* loco data, unfortunatly with slot addresses and not decoder addresses */
                case OPC_LOCO_SPD:     /* A0 */
                    addr = ln_packet[1];
                    speed = ln_packet[2];
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Set loco speed (OPC_LOCO_SPD:  /* A0 */) %d: %d",
                               addr, speed);
                    if (__loconett->slotmap[addr] == 0) {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "slot %d still unknown", addr);
                    }
                    else {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "GL decoder address %d found in slot %d",
                                   __loconett->slotmap[addr], addr);
                        cacheGetGL(btd->bus, __loconett->slotmap[addr],
                                   &gltmp);
                        if (speed == 1) {
                            /* gltmp.direction = 2; */
                            gltmp.speed = 0;
                        }
                        else {
                            gltmp.speed = speed;
                        }
                        cacheSetGL(btd->bus, __loconett->slotmap[addr],
                                   gltmp);
                    }
                    break;
                case OPC_LOCO_DIRF:    /* A1 */
                    addr = ln_packet[1];
                    if (__loconett->slotmap[addr] == 0) {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "slot %d still unknown", addr);
                    }
                    else {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "GL decoder address %d found in slot %d",
                                   __loconett->slotmap[addr], addr);
                        cacheGetGL(btd->bus, __loconett->slotmap[addr],
                                   &gltmp);
                        tmp = gltmp.funcs & 0xffe0;
                        /* bit shuffling */
                        tmp |=
                            (ln_packet[2] & 0x0010) >> 4 | (ln_packet[2] &
                                                            0x000f) << 1;
                        gltmp.funcs = tmp;
                        gltmp.direction =
                            (ln_packet[2] & DIRF_DIR) ? 0 : 1;
                        cacheSetGL(btd->bus, __loconett->slotmap[addr],
                                   gltmp);
                    }
                    break;
                case OPC_LOCO_SND:     /* A2 */
                    addr = ln_packet[1];
                    if (__loconett->slotmap[addr] == 0) {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "slot %d still unknown", addr);
                    }
                    else {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "GL decoder address %d found in slot %d",
                                   __loconett->slotmap[addr], addr);
                        cacheGetGL(btd->bus, __loconett->slotmap[addr],
                                   &gltmp);
                        tmp = gltmp.funcs & 0xfe1f;
                        /* bit shuffling */
                        tmp |= (ln_packet[2] & 0x000f) << 5;
                        gltmp.funcs = tmp;
                        cacheSetGL(btd->bus, __loconett->slotmap[addr],
                                   gltmp);
                    }
                    break;

                case OPC_SW_REQ:       /* B0 */
                case OPC_SW_REP:       /* B1 */
                    addr =
                        (ln_packet[1] | ((ln_packet[2] & 0x0f) << 7)) + 1;
                    value = (ln_packet[2] & 0x10) >> 4;
                    port = (ln_packet[2] & 0x20) >> 5;
                    getGA(btd->bus, addr, &gatmp);
                    gatmp.action = value;
                    gatmp.port = port;
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Infomational: switch request (OPC_SW_REQ: /* B0 */)  #%d:%d -> %d",
                               addr, port, value);

                    setGA(btd->bus, addr, gatmp);
                    break;
                case OPC_INPUT_REP:    /* B2 */
                    addr = ln_packet[1] | ((ln_packet[2] & 0x000f) << 7);
                    addr = 1 + addr * 2 + ((ln_packet[2] & 0x0020) >> 5);
                    value = (ln_packet[2] & 0x10) >> 4;
                    updateFB(btd->bus, addr, value);
                    break;
                case OPC_LONG_ACK:     /* B4 */
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Infomational: LONG ACK for command 0x%0X: 0x%0X",
                               ln_packet[1] ==
                               0 ? ln_packet[1] : ln_packet[1] | 0x0080,
                               ln_packet[2]);
                    break;

                case OPC_SLOT_STAT1:   /* B5 */
                    addr = ln_packet[1];
                    if (__loconett->slotmap[addr] == 0) {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "slot %d still unknown", addr);
                    }
                    else {
                        syslog_bus(btd->bus, DBG_DEBUG,
                                   "GL decoder address %d found in slot %d",
                                   __loconett->slotmap[addr], addr);
                        if (ln_packet[2] == 2) {
                            cacheTermGL(btd->bus,
                                        __loconett->slotmap[addr]);
                        }
                    }

                    break;

                case OPC_RQ_SL_DATA:   /* BB, E7 Message follows */
                    addr = ln_packet[1];
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Infomational: Request SLOT DATA (OPC_RQ_SL_DATA: /* BB */)  #%d",
                               addr);
                    break;
                case OPC_LOCO_ADR:     /* BF, E7 Message follows */
                    addr = (ln_packet[1] << 7) | ln_packet[2];
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Informational: request loco address (OPC_LOCO_ADR:  /* BF */)  #%d",
                               addr);
                    break;
                case OPC_SL_RD_DATA:   /* E7 */
                    switch (ln_packet[1]) {
                        case 0x0e:
                            addr = ln_packet[4] | (ln_packet[9] << 7);
                            syslog_bus(btd->bus, DBG_DEBUG,
                                       "OPC_SL_RD_DATA: /* E7 %0X */ slot #%d: status=0x%0x addr=%d",
                                       ln_packet[1], ln_packet[2],
                                       ln_packet[3], addr);
                            __loconett->slotmap[ln_packet[2]] = addr;
                            if (!isInitializedGL(btd->bus, addr)) {
                                cacheInitGL(btd->bus, addr, 'L', 1, 128,
                                            9,0);
                            }
                            cacheGetGL(btd->bus, addr, &gltmp);
                            gltmp.speed = ln_packet[5];
                            tmp = ln_packet[6];
                            tmp =
                                (tmp & 0x0010) >> 4 | (tmp & 0x000f) << 1;
                            tmp = tmp | ln_packet[7] << 5;
                            gltmp.funcs = tmp;
                            cacheSetGL(btd->bus, addr, gltmp);
                            break;
                        default:
                            syslog_bus(btd->bus, DBG_DEBUG,
                                       "Unknown or not decoded Loconet Message OPC_SL_RD_DATA: /* 0x%0X */",
                                       ln_packet[1]);
                    }
                    break;
                case OPC_WR_SL_DATA:
                    switch (ln_packet[1]) {
                        case 0x0e:
                            if (ln_packet[2] == 0x7b) {
                                int day, hour, minute, clkrate, clkstate;
                                clkrate = ln_packet[3];
                                clkstate = ln_packet[10] & 0x20;
                                if (!clkstate) {
                                    day = ln_packet[9];
                                    minute =
                                        ((256 - ln_packet[6]) & 0x7f) % 60;
                                    hour =
                                        ((256 - ln_packet[8]) & 0x7f) % 24;
                                    hour = (24 - hour) % 24;
                                    minute = (60 - minute) % 60;
                                    syslog_bus(btd->bus, DBG_DEBUG,
                                               "fast clock update: day %d %02d:%02d",
                                               day, hour, minute);
                                    if ((__loconett->
                                         flags & LN_FLAG_GETTIME) ==
                                        LN_FLAG_GETTIME) {
                                        initTIME(clkrate, 1);
                                        setTIME(day, hour, minute, 0);
                                    }
                                }
                                else {
                                    syslog_bus(btd->bus, DBG_DEBUG,
                                               "clock frozen");
                                }
                            }
                            break;
                        default:
                            syslog_bus(btd->bus, DBG_DEBUG,
                                       "Unknown or not decoded Loconet Message OPC_WR_SL_DATA: /* 0x%0X */",
                                       ln_packet[1]);

                            ;
                    }
                    break;
                default:
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Unknown or not decoded Loconet Message (0x%0X), packet length is %d bytes",
                               ln_packet[0], ln_packetlen);
                    /* unknown Loconet packet received, ignored */
                    break;
            }
        }
        if (!twomessageflag && (ln_packetlen == 0)) {
            /* now we process the way back _to_ Loconet */
            ln_packet[0] = OPC_IDLE;
            ln_packetlen = 2;
            if (buses[btd->bus].power_changed == 1) {
                ln_packet[0] = 0x82 + buses[btd->bus].power_state;
                ln_packetlen = 2;
                buses[btd->bus].power_changed = 0;
                infoPower(btd->bus, msg);
                enqueueInfoMessage(msg);
            }
            else if (!queue_GL_isempty(btd->bus)) {
                gl_state_t gltmp, glcur;
                int slot;
                dequeueNextGL(btd->bus, &gltmp);
                addr = gltmp.id;
                slot = cacheGetSlotNumberforAddr(btd->bus, addr);
                cacheGetGL(btd->bus, addr, &glcur);
                /* we may have to send out 3 different messages
                   OPC_LOCO_SPD for speed changes
                   OPC_LOCO_DIRF for direction and function 0-4
                   OPC_LOCO_SND for function 5-8
                 */
                if (gltmp.speed != glcur.speed) {
                    ln_packetlen = 4;
                    ln_packet[0] = OPC_LOCO_SPD;
                    ln_packet[1] = slot;
                    ln_packet[2] = gltmp.speed + (gltmp.speed > 0 ? 1 : 0);     /* speed step in loconet 1 is a emergency stop */
                    ln_sendmessage(btd->bus, ln_packetlen, ln_packet);
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Loconet: GL SET slot %d with addr %d to speed %d.",
                               slot, addr, speed);
                }

                if (gltmp.direction != glcur.direction
                    || ((gltmp.funcs & 0x001f) !=
                        (glcur.funcs & 0x001f))) {
                    ln_packetlen = 4;
                    ln_packet[0] = OPC_LOCO_DIRF;
                    ln_packet[1] = slot;
                    ln_packet[2] = (gltmp.direction ? 0 : DIRF_DIR) +
                        ((gltmp.funcs & 0x0001) ? DIRF_F0 : 0) +
                        ((gltmp.funcs >> 1) & 0x000f);
                    ln_sendmessage(btd->bus, ln_packetlen, ln_packet);
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Loconet: GL SET slot %d with addr %d to direction/funcs 0x%x.",
                               slot, addr, ln_packet[2]);
                }
                if ((gltmp.funcs & 0x01e0) != (glcur.funcs & 0x01e0)) {
                    ln_packetlen = 4;
                    ln_packet[0] = OPC_LOCO_SND;
                    ln_packet[1] = slot;
                    ln_packet[2] = (gltmp.funcs >> 5) & 0x000f;
                    ln_sendmessage(btd->bus, ln_packetlen, ln_packet);
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Loconet: GL SET slot %d with addr %d to sound funcs 0x%x.",
                               slot, addr, ln_packet[2]);
                }

                cacheSetGL(btd->bus, gltmp.id, gltmp);

            }
            else if (!queue_GA_isempty(btd->bus)) {
                ga_state_t gatmp;
                dequeueNextGA(btd->bus, &gatmp);
                addr = gatmp.id - 1;
                ln_packetlen = 4;
                ln_packet[0] = OPC_SW_REQ;

                ln_packet[1] = (unsigned short int) (addr & 0x0007f);
                ln_packet[2] = (unsigned short int) ((addr >> 7) & 0x000f);
                ln_packet[2] |=
                    (unsigned short int) ((gatmp.port & 0x0001) << 5);
                ln_packet[2] |=
                    (unsigned short int) ((gatmp.action & 0x0001) << 4);

                if (gatmp.action == 1) {
                    gettimeofday(&gatmp.tv[gatmp.port], NULL);
                }
                setGA(btd->bus, gatmp.id, gatmp);
                syslog_bus(btd->bus, DBG_DEBUG, "Loconet: GA SET #%d %02X",
                           gatmp.id, gatmp.action);
            }
            else {
                /* send out a slot read message to collect the current state. Do this
                   only once at startup time */
                if (startup_slot_index < 120) {
                    syslog_bus(btd->bus, DBG_DEBUG,
                               "Loconet: requesting startup slot info %d",
                               startup_slot_index);
                    ln_packetlen = 4;
                    ln_packet[0] = OPC_RQ_SL_DATA;
                    ln_packet[1] = startup_slot_index++;
                    ln_packet[2] = 0;
                }
            }
            ln_sendmessage(btd->bus, ln_packetlen, ln_packet);
        }
        /* wait 1 ms */
        if (usleep(1000) == -1) {
            syslog_bus(btd->bus, DBG_ERROR,
                       "usleep() failed: %s (errno = %d)\n",
                       strerror(errno), errno);
        }
    }
    /*run the cleanup routine */
    pthread_cleanup_pop(1);
    return NULL;
}
