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
public class SRCPLocomotiveControl extends Control implements GLInfoListener,
		Constants, LocomotiveControlface {

	private static LocomotiveControlface instance;
	private LocomotivePersistenceIface persistence = HibernateLocomotivePersistence.getInstance();
	
	private List<LocomotiveChangeListener> listeners;

	private SRCPLocomotiveControl() {
		listeners = new ArrayList<LocomotiveChangeListener>();
	}

	/**
	 * Gets an instance of a LocomotiveControl.
	 * 
	 * @return an instance of LocomotiveControl
	 */
	public static LocomotiveControlface getInstance() {
		if (instance == null) {
			instance = new SRCPLocomotiveControl();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setSession(de.dermoba.srcp.client.SRCPSession)
	 */
	public void setSession(SRCPSession session) {
		this.session = session;
		for (Locomotive l : persistence.getAllLocomotives()) {
			l.setSession(session);
		}
		session.getInfoChannel().addGLInfoListener(this);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#toggleDirection(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#getCurrentSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public int getCurrentSpeed(Locomotive locomotive) {
		return locomotive.getCurrentSpeed();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive, int, boolean[])
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#increaseSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void increaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed() + 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#decreaseSpeed(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void decreaseSpeed(Locomotive locomotive) throws LocomotiveException {
		checkLocomotive(locomotive);
		initLocomotive(locomotive);
		int newSpeed = locomotive.getCurrentSpeed() - 1;
		if (newSpeed <= locomotive.getLocomotiveType().getDrivingSteps()) {
			setSpeed(locomotive, newSpeed, locomotive.getFunctions());
		}
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#increaseSpeedStep(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#decreaseSpeedStep(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#setFunctions(ch.fork.AdHocRailway.domain.locomotives.Locomotive, boolean[])
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#addLocomotiveChangeListener(ch.fork.AdHocRailway.domain.locomotives.Locomotive, ch.fork.AdHocRailway.domain.locomotives.LocomotiveChangeListener)
	 */
	public void addLocomotiveChangeListener(Locomotive loco,
			LocomotiveChangeListener l) {
		listeners.add(l);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#removeAllLocomotiveChangeListener()
	 */
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#undoLastChange()
	 */
	@Override
	public void undoLastChange() throws ControlException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface#previousDeviceToDefault()
	 */
	@Override
	public void previousDeviceToDefault() throws ControlException {
		// TODO Auto-generated method stub
	}
}
