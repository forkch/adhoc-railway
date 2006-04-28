#-------------------------------------------------------------------------
#
# <java.mk>  -  <desc>
#
# begin     : Mon Apr 25 22:20:52 CEST 2005
# copyright : (C)  by Benjamin Mueller 
# email     : akula@akula.ch
# language  : make
# version   : $Id$
#
#-------------------------------------------------------------------------

#-------------------------------------------------------------------------
#
# This program is free software; you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# Free Software Foundation; either version 2 of the License, or
# (at your option) any later version.
#
#-------------------------------------------------------------------------

include ~/.make/common.mk

EXTRALIBS=../jsrcpc/classes
CLEAN_LIST+=./lib/RailControl.jar

build:: java-build lib/RailControl.jar

test: build
	$(JAVA) $(JAVA_FLAGS) ch.fork.RailControl.ui.RailControlGUI


# " vim:ts=4
