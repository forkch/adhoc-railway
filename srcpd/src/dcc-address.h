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


#ifndef DCC_ADDRESS_H
#define DCC_ADDRESS_H


/* Map lenz address to nmra/subaddress values and backward.
 * Valid ranges
 *   lenz = 0..2044
 *   nmra = 0..511
 *   sub = 0..3
 */
void lenz_to_nmra(unsigned int lenz, unsigned int* nmra,
        unsigned char* sub);

void nmra_to_lenz(unsigned int nmra, unsigned char sub,
        unsigned int *lenz);


/* Map nmra/port to lenz/port values and backward.
 * Valid ranges
 *   lenz = 0..2044
 *   port = 0..1
 *   nmra_a = 0..511
 *   nmra_p = 0..7
 */
void lenz2_to_nmra2(unsigned int lenz, unsigned char port,
        unsigned int* nmra_a, unsigned char* nmra_p);

void nmra2_to_lenz2(unsigned int nmra_a, unsigned char nmra_p,
        unsigned int *lenz, unsigned char* port);

#endif
