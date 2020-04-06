/*
  Copyright (c) 2009 Guido Scholz <gscholz@users.sourceforge.net>

  This file is part of srcpd.

  srcpd is free software: you can redistribute it and/or modify
  it under the terms of the GNU General Public License version 2 as
  published by the Free Software Foundation.

  srcpd is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.

  You should have received a copy of the GNU General Public License
  along with srcpd.  If not, see <http://www.gnu.org/licenses/>.
*/


#include "dcc-address.h"


/* Map lenz address to nmra/subaddress values.
 * Valid ranges
 *   lenz = 0..2044
 *   nmra = 0..511
 *   sub = 0..3
 */
void lenz_to_nmra(unsigned int lenz, unsigned int *nmra,
                  unsigned char *sub)
{
    if (lenz > 0) {
        *sub = (lenz - 1) % 4;
        *nmra = (lenz + 3) >> 2;
    }
    else {
        *sub = 0;
        *nmra = 0;
    }
}

/* Map nmra/subaddress values to lenz address.
 * Valid ranges
 *   lenz = 0..2044
 *   nmra = 0..511
 *   sub = 0..3
 */
void nmra_to_lenz(unsigned int nmra, unsigned char sub, unsigned int *lenz)
{
    if (nmra == 0)
        *lenz = 0;
    else {
        *lenz = (nmra - 1) << 2;
        *lenz += sub + 1;
    }
}

/* Map lenz address/port to nmra/port values.
 * Valid ranges
 *   lenz = 0..2044
 *   port = 0..1
 *   nmra_a = 0..511
 *   nmra_p = 0..7
 */
void lenz2_to_nmra2(unsigned int lenz, unsigned char port,
                    unsigned int *nmra_a, unsigned char *nmra_p)
{
    if (lenz > 0) {
        *nmra_p = ((lenz - 1) % 4) << 1;
        *nmra_p |= port & 0x01;
        *nmra_a = (lenz + 3) >> 2;
    }
    else {
        *nmra_p = 0;
        *nmra_a = 0;
    }
}

/* Map nmra/port to lenz/port values.
 * Valid ranges
 *   nmra_a = 0..511
 *   nmra_p = 0..7
 *   lenz = 0..2044
 *   port = 0..1
 */
void nmra2_to_lenz2(unsigned int nmra_a, unsigned char nmra_p,
                    unsigned int *lenz, unsigned char *port)
{
    if (nmra_a == 0) {
        *lenz = 0;
        *port = 0;
    }
    else {
        *lenz = (nmra_a - 1) << 2;
        *lenz += (nmra_p >> 1) + 1;
        *port = nmra_p & 0x01;
    }
}
