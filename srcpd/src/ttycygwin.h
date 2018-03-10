/***************************************************************************
 *   copyright           : (C) 2002 by Guido Scholz                        *
 *   mail                : guido.scholz@bayernline.de                      *
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/

#ifndef TTYCYGWIN_H
#define TTYCYGWIN_H

#include "config.h"

#ifdef NO_CFMAKERAW
int cfmakeraw(struct termios *termios_p);
#endif

#endif
