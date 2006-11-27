/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/NoneLocomotive.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:03 BST 2006
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

/** Dummy Locomotive.
 * 
 * @author fork
 *
 */
public class NoneLocomotive extends Locomotive {
    private static final int DRIVING_STEPS = 0;
    private static final int FUNCTIONCOUNT = 0;

    public NoneLocomotive() {
        super("NONE", new Address(DEFAULT_BUS, 0), DRIVING_STEPS, "Dummy",
            FUNCTIONCOUNT);
    }

    @Override
    public void decreaseSpeed() throws LocomotiveException {
    }

    @Override
    public int getCurrentSpeed() {
        return 0;
    }

    @Override
    public void increaseSpeed() throws LocomotiveException {
    }

    protected @Override
    void init() throws LocomotiveException {
    }

    @Override
    public void setSpeed(int speed) throws LocomotiveException {
    }

    @Override
    protected void increaseSpeedStep() throws LocomotiveException {
    }

    @Override
    protected void decreaseSpeedStep() throws LocomotiveException {
    }

    @Override
    public Locomotive clone() {
        return this;
    }
}
