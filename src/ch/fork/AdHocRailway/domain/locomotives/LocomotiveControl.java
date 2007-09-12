/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/LocomotiveControl.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:01 BST 2006
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

import java.util.ArrayList;
import java.util.List;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive.Direction;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveException;
import ch.fork.AdHocRailway.domain.locomotives.exception.LocomotiveLockedException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GL;
import de.dermoba.srcp.devices.GLInfoListener;

/**
 * Controls all actions which can be performed on Locomotives.
 * 
 * @author fork
 * 
 */
public class LocomotiveControl extends Control implements GLInfoListener,
		Constants {

	private static LocomotiveControl instance;
	private LocomotivePersistence persistence = LocomotivePersistence.getInstance();
	
	private List<LocomotiveChangeListener> listeners;

	private LocomotiveControl() {
		listeners = new ArrayList<LocomotiveChangeListener>();
	}

	/**
	 * Gets an instance of a LocomotiveControl.
	 * 
	 * @return an instance of LocomotiveControl
	 */
	public static LocomotiveControl getInstance() {
		if (instance == null) {
			instance = new LocomotiveControl();
		}
		return instance;
	}

	/**
	 * Sets the SRCPSession on this Control.
	 * 
	 * @param session
	 */
	public void setSession(SRCPSession session) {
		this.session = session;
		for (Locomotive l : persistence.getAllLocomotives()) {
			l.setSession(session);
		}
		session.getInfoChannel().addGLInfoListener(this);
	}

	/**
	 * Toggles the direction of the Locomotive
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public void toggleDirection(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		switch (locomotive.direction) {
		case FORWARD:
			locomotive.setDirection(Direction.REVERSE);
			break;
		case REVERSE:
			locomotive.setDirection(Direction.FORWARD);
			break;
		}
	}

	public int getCurrentSpeed(Locomotive locomotive) {
		return locomotive.getCurrentSpeed();
	}

	/**
	 * Sets the speed of the Locomotive
	 * 
	 * @param locomotive
	 * @param speed
	 * @throws LocomotiveException
	 */
	public void setSpeed(Locomotive locomotive, int speed, boolean[] functions)
			throws LocomotiveException {

		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		try {
			if (functions == null) {
				functions = locomotive.getFunctions();
			}
			LocomotiveType lt = locomotive.getLocomotiveType();
			int drivingSteps = lt.getDrivingSteps();
			if (speed < 0 || speed > drivingSteps) {
				return;
			}
			GL gl = locomotive.getGL();
			switch (locomotive.direction) {
			case FORWARD:
				gl.set(Locomotive.FORWARD_DIRECTION, speed, drivingSteps,
						functions);
				break;
			case REVERSE:
				gl.set(Locomotive.REVERSE_DIRECTION, speed, drivingSteps,
						functions);
				break;
			case UNDEF:
				gl.set(Locomotive.FORWARD_DIRECTION, speed, drivingSteps,
						functions);
				locomotive.setDirection(Direction.FORWARD);
				break;
			}
			locomotive.setCurrentSpeed(speed);
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new LocomotiveLockedException(ERR_LOCKED);
			} else {
				throw new LocomotiveException(ERR_FAILED, x);
			}
		}
	}

	/**
	 * Increases the speed of the Locomotive.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed() + 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/**
	 * Decreases the speed of the Locomotive.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed() - 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/**
	 * Increases the speed of the Locomotive by a step.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public void increaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed()
				+ locomotive.getLocomotiveType().getStepping();
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/**
	 * Decreases the speed of the Locomotive by a step.
	 * 
	 * @param locomotive
	 * @throws LocomotiveException
	 */
	public void decreaseSpeedStep(Locomotive locomotive)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed()
				- locomotive.getLocomotiveType().getStepping();
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/**
	 * Sets the functions of the Locomotive on or off.
	 * 
	 * @param locomotive
	 * @param functions
	 * @throws LocomotiveException
	 */
	public void setFunctions(Locomotive locomotive, boolean[] functions)
			throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		setSpeed(locomotive, locomotive.getCurrentSpeed(), functions);
	}

	public void GLinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		Locomotive locomotive = persistence.getLocomotiveByAddress(address);
		if (locomotive != null) {
			GL gl = new GL(session);
			gl.setBus(locomotive.getBus());
			gl.setAddress(locomotive.getAddress());
			locomotive.setGL(gl);
			locomotive.setInitialized(true);
			informListeners(locomotive);
		}
	}

	public void GLset(double timestamp, int bus, int address, String drivemode,
			int v, int vMax, boolean[] functions) {
		Locomotive locomotive = persistence.getLocomotiveByAddress(address);
		if (locomotive != null) {
			if (drivemode.equals(Locomotive.FORWARD_DIRECTION)) {
				locomotive.setDirection(Direction.FORWARD);
			} else if (drivemode.equals(Locomotive.REVERSE_DIRECTION)) {
				locomotive.setDirection(Direction.REVERSE);
			}
			locomotive.setCurrentSpeed(v);
			locomotive.setFunctions(functions);
		}
	}

	public void GLterm(double timestamp, int bus, int address) {
		Locomotive locomotive = persistence.getLocomotiveByAddress(address);
		if (locomotive != null) {
			locomotive.setGL(null);
			locomotive.setInitialized(false);
		}
	}

	public void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l) {
		listeners.add(l);
	}

	public void removeAllLocomotiveChangeListener() {
		listeners.clear();
	}

	private void informListeners(Locomotive changedLocomotive) {
		for (LocomotiveChangeListener l : listeners)
			l.locomotiveChanged(changedLocomotive);

	}

	private void checkLocomotive(Locomotive locomotive)
			throws LocomotiveException {
		if (locomotive == null)
			return;
		try {
			if (locomotive.getSession() == null) {
				throw new NoSessionException();
			}
			if (locomotive.getAddress() == 0) {
				throw new InvalidAddressException();
			}
		} catch (NoSessionException e) {
			throw new LocomotiveException(Constants.ERR_NOT_CONNECTED, e);
		} catch (InvalidAddressException e) {
			throw new LocomotiveException(Constants.ERR_FAILED, e);
		}
	}

	private void initLocomotive(Locomotive locomotive)
			throws LocomotiveException {
		if (!locomotive.isInitialized()) {
			try {
				GL gl = new GL(session);
				LocomotiveType lt = locomotive.getLocomotiveType();
				String[] params = new String[3];
				params[0] = Integer.toString(LocomotiveType.PROTOCOL_VERSION);
				params[1] = Integer.toString(lt.getDrivingSteps());
				params[2] = Integer.toString(lt.getFunctionCount());
				gl.init(locomotive.getBus(), locomotive.getAddress(),
						LocomotiveType.PROTOCOL, params);
				locomotive.setInitialized(true);
				locomotive.setGL(gl);
			} catch (SRCPDeviceLockedException x1) {
				throw new LocomotiveLockedException(ERR_LOCKED, x1);
			} catch (SRCPException x) {
				throw new LocomotiveException(ERR_INIT_FAILED, x);
			}
		}
	}

	@Override
	public void undoLastChange() throws ControlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void previousDeviceToDefault() throws ControlException {
		// TODO Auto-generated method stub
	}
}
