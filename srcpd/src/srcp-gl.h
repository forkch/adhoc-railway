/* $Id: srcp-gl.h 1508 2010-12-11 18:56:51Z gscholz $ */

#ifndef _SRCP_GL_H
#define _SRCP_GL_H

#include <stdbool.h>
#include <sys/time.h>

#include "config-srcpd.h"
#include "srcp-session.h"


/* locomotive decoder */
typedef struct _GLSTATE {
    int state;                  /* 0==dead, 1==living, 2==terminating */
    char protocol;
    int protocolversion;
    int n_func;
    int n_fs;
    int id;                     /* address  */
    int speed;                  /* Sollgeschwindigkeit skal. auf 0..14 */
    int direction;              /* 0/1/2                               */
    unsigned int funcs;         /* Fx..F1, F                           */
    unsigned long int  uuid;
    struct timeval tv;          /* Last time of change                 */
    struct timeval inittime;
    struct timeval locktime;
    long int lockduration;
    sessionid_t locked_by;
} gl_state_t;

typedef struct _GL {
    int numberOfGl;
    gl_state_t *glstate;
} GL;

int startup_GL(void);
int init_GL(bus_t busnumber, int number);
int getMaxAddrGL(bus_t busnumber);
bool isInitializedGL(bus_t busnumber, int addr);
int isValidGL(bus_t busnumber, int addr);
int enqueueGL(bus_t busnumber, int addr, int dir, int speed,
        int maxspeed, int f);
int queue_GL_isempty(bus_t busnumber);
int dequeueNextGL(bus_t busnumber, gl_state_t * l);
int cacheGetGL(bus_t busnumber, int addr, gl_state_t * l);
int cacheSetGL(bus_t busnumber, int addr, gl_state_t l);
int cacheInfoGL(bus_t busnumber, int addr, char *info);
int cacheDescribeGL(bus_t busnumber, int addr, char *msg);
int cacheInitGL(bus_t busnumber, int addr, const char protocol,
                int protoversion, int n_fs, int n_func, unsigned long int uuid);
int cacheTermGL(bus_t busnumber, int addr);
int cacheLockGL(bus_t busnumber, int addr, long int duration,
                sessionid_t sessionid);
int cacheGetLockGL(bus_t busnumber, int addr, sessionid_t * sessionid);
int cacheUnlockGL(bus_t busnumber, int addr, sessionid_t sessionid);
void unlock_gl_bysessionid(sessionid_t sessionid);
void unlock_gl_bytime(void);
int describeLOCKGL(bus_t bus, int addr, char *reply);
void debugGL(bus_t busnumber, int start, int end);

#endif
