/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

import ch.fork.AdHocRailway.domain.locking.LockControlIface;
import ch.fork.AdHocRailway.domain.locomotives.SRCPLocomotive.Direction;

public interface LocomotiveControlface extends LockControlIface<Locomotive> {

	public abstract void setLocomotivePersistence(
			LocomotivePersistenceIface persistence);

	/**
	 * Toggles the direction of the Locomotive
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void toggleDirection(Locomotive locomotive)
			throws LocomotiveException;

	public abstract Direction getDirection(Locomotive locomotive);

	public abstract int getCurrentSpeed(Locomotive locomotive);

	/**
	 * Sets the speed of the Locomotive
	 * 
	 * @param locomotive
	 * @param speed
	 * @throws LocomotiveException
	 */
	public abstract void setSpeed(Locomotive locomotive, int speed,
			boolean[] functions) throws LocomotiveException;

	/**
	 * Increases the speed of the Locomotive.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void increaseSpeed(Locomotive locomotive)
			throws LocomotiveException;

	/**
	 * Decreases the speed of the Locomotive.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void decreaseSpeed(Locomotive locomotive)
			throws LocomotiveException;

	/**
	 * Increases the speed of the Locomotive by a step.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException;

	/**
	 * Decreases the speed of the Locomotive by a step.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException;

	/**
	 * Sets the functions of the Locomotive on or off.
	 * 
	 * @param locomotive
	 * @param functions
	 * @throws LocomotiveException
	 */
	public abstract void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException;

	public boolean[] getFunctions(Locomotive locomotive);

	public abstract void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l);

	public abstract void removeAllLocomotiveChangeListener();

	public void update();

}