/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/DeltaLocomotive.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:10 BST 2006
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


package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;

public class DeltaLocomotive extends Locomotive {
    private static final int DRIVING_STEPS = 14;
    private static final int STEPPING      = 2;
    private static final int FUNCTIONCOUNT = 1;

    public DeltaLocomotive(String name, Address address, String desc) {
        super(name, address, DRIVING_STEPS, desc, FUNCTIONCOUNT);
    }

    protected void increaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() + STEPPING);
    }

    protected void decreaseSpeedStep() throws LocomotiveException {
        super.setSpeed(getCurrentSpeed() - STEPPING);
    }

    @Override
    public Locomotive clone() {
        DeltaLocomotive clone = new DeltaLocomotive(name, address, desc);
        clone.functions = functions;
        clone.initialized = initialized;
        clone.currentSpeed = currentSpeed;
        clone.direction = direction;
        clone.params = params;
        return clone;
    }
}
