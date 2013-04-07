# Makefile.am for srcpd
# version: $Revision: 1417 $
# last update: $Date: 2009-12-09 21:38:43 +0100 (Wed, 09. Dec 2009) $

sbin_PROGRAMS = srcpd

srcpd_SOURCES = \
	clientservice.c clientservice.h \
	config-srcpd.c config-srcpd.h \
	dcc-address.c dcc-address.h \
	io.c io.h \
	toolbox.c toolbox.h \
	netservice.c netservice.h \
	portio.c portio.h \
	srcp-command.c srcp-command.h \
	srcpd.c \
	srcp-descr.c srcp-descr.h \
	srcp-error.c srcp-error.h \
	srcp-fb.c srcp-fb.h \
	srcp-ga.c srcp-ga.h \
	srcp-gl.c srcp-gl.h \
	srcp-gm.c srcp-gm.h \
	srcp-info.c srcp-info.h \
	srcp-lock.c srcp-lock.h \
	srcp-power.c srcp-power.h \
	srcp-server.c srcp-server.h \
	srcp-session.c srcp-session.h \
	srcp-sm.c srcp-sm.h \
	srcp-time.c srcp-time.h \
	syslogmessage.c syslogmessage.h \
	ttycygwin.c ttycygwin.h


if USE_DDL
srcpd_SOURCES += \
	ddl.c ddl.h \
	ddl_maerklin.c ddl_maerklin.h \
	ddl_nmra.c ddl_nmra.h
endif

if USE_DDLS88
srcpd_SOURCES += ddl-s88.c ddl-s88.h
endif

if USE_I2C
srcpd_SOURCES += i2c-dev.c i2c-dev.h
endif

if USE_IB
srcpd_SOURCES += ib.c ib.h
endif

if USE_HSI88
srcpd_SOURCES += hsi-88.c hsi-88.h
endif

# tricky: li100-main includes li100.c twice for both USB and RS232 support
if USE_LI100
srcpd_SOURCES += li100.h \
		 li100-main.c
endif

if USE_LOCONET
srcpd_SOURCES += loconet.c loconet.h
endif

if USE_LOCONET
srcpd_SOURCES += loopback.c loopback.h
endif

if USE_M605X
srcpd_SOURCES += m605x.c m605x.h
endif

if USE_SELECTRIX
srcpd_SOURCES += selectrix.c selectrix.h
endif

if USE_ZIMO
srcpd_SOURCES += zimo.c zimo.h
endif

if USE_ADHOCMM
srcpd_SOURCES += adhocmm.c adhocmm.h
endif

EXTRA_DIST = li100.c

DEFS = -D_REENTRANT -DSYSCONFDIR=\"$(sysconfdir)\" @DEFS@

