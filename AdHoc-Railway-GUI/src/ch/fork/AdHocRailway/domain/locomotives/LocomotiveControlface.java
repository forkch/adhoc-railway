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
import de.dermoba.srcp.model.locomotives.SRCPLocomotiveDirection;

public interface LocomotiveControlface extends LockControlIface<Locomotive> {

	/**
	 * Toggles the direction of the Locomotive
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void toggleDirection(Locomotive locomotive)
			throws LocomotiveException;

	public abstract SRCPLocomotiveDirection getDirection(Locomotive locomotive);

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

	public abstract void setFunction(Locomotive locomotive, int functionNumber,
			boolean state, int deactivationDelay) throws LocomotiveException;

	public boolean[] getFunctions(Locomotive locomotive);

	public abstract void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l);

	public void removeLocomotiveChangeListener(final Locomotive locomotive,
			final LocomotiveChangeListener listener);

	public abstract void removeAllLocomotiveChangeListener();

	public abstract void emergencyStop(Locomotive myLocomotive)
			throws LocomotiveException;

	public abstract void addOrUpdateLocomotive(Locomotive locomotive);

	public void activateLoco(final Locomotive locomotive,
			final boolean[] functions) throws LocomotiveException;

	public void deactivateLoco(final Locomotive locomotive)
			throws LocomotiveException;

	public abstract void emergencyStopActiveLocos() throws LocomotiveException;

}