package ch.fork.AdHocRailway.domain.locomotives;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import de.dermoba.srcp.client.SRCPSession;

public interface LocomotiveControlface {

	/**
	 * Sets the SRCPSession on this Control.
	 * 
	 * @param session
	 */
	public abstract void setSession(SRCPSession session);

	/**
	 * Toggles the direction of the Locomotive
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public abstract void toggleDirection(Locomotive locomotive)
			throws LocomotiveException;

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

	public abstract void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l);

	public abstract void removeAllLocomotiveChangeListener();

	public abstract void undoLastChange() throws ControlException;

	public abstract void previousDeviceToDefault() throws ControlException;

}