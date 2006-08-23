/*------------------------------------------------------------------------
 * 
 * <./domain/switches/DoubleCrossSwitch.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:54:42 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/


package ch.fork.AdHocRailway.domain.switches;

import ch.fork.AdHocRailway.domain.Address;

public class DoubleCrossSwitch extends DefaultSwitch {
    public DoubleCrossSwitch(int pNumber, String pDesc) {
        super(pNumber, pDesc);
    }

    public DoubleCrossSwitch(int pNumber, String pDesc, Address address) {
        super(pNumber, pDesc, address);
    }

    @Override
    public Switch clone() {
        DoubleCrossSwitch newSwitch = new DoubleCrossSwitch(number, desc,
            addresses[0]);
        newSwitch.setSession(session);
        newSwitch.setSwitchOrientation(switchOrientation);
        newSwitch.setDefaultState(defaultState);
        return newSwitch;
    }
}
