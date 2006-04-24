/*------------------------------------------------------------------------
 * 
 * <DefaultSwitch.java>  -  <A standard switch>
 * 
 * begin     : j Tue Jan  3 21:26:08 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
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

package ch.fork.RailControl.domain.switches;

import static ch.fork.RailControl.ui.ImageTools.createImageIcon;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;

import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.domain.switches.exception.SwitchLockedException;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class ThreeWaySwitch extends Switch {

	private GA ga1;

	private GA ga2;

	private int[] LEFT_PORTS = { 1, 0 };

	private int[] STRAIGHT_PORTS = { 0, 0 };

	private int[] RIGHT_PORTS = { 0, 1 };

	private int[] UNDEF_PORTS = { 1, 1 };

	public ThreeWaySwitch(int pNumber, String pDesc, int pBus, Address address) {
		super(pNumber, pDesc, pBus, address);
	}

	public void init() throws SwitchException {
		super.init();
		try {
			ga1 = new GA(session);
			ga2 = new GA(session);
			ga1.init(bus, address.getAddress1(), "M");
			ga2.init(bus, address.getAddress2(), "M");
			// TODO: immediately a get to determine state !!!!
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_SWITCH_LOCKED);
			} else {
				throw new SwitchException(ERR_TOGGLE_FAILED, x);
			}
		}
		initialized = true;
	}

	@Override
	protected void reinit() throws SwitchException {
		try {
			if (ga1 != null) {
				ga1.term();
			}
			if (ga2 != null) {
				ga2.term();
			}
		} catch (SRCPException e) {
			throw new SwitchException(ERR_REINIT_FAILED, e);
		}
		if(session != null) {
			init();
		}
	}

	protected void toggle() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		try {
			switch (switchState) {
			case LEFT:
				ga1.set(STRAIGHT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
				ga2.set(STRAIGHT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);

				ga1.set(UNDEF_PORTS[0], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				ga2.set(UNDEF_PORTS[1], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				switchState = SwitchState.STRAIGHT;
				break;
			case STRAIGHT:
				ga1.set(RIGHT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
				ga2.set(RIGHT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
				ga1.set(LEFT_PORTS[0], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				ga2.set(LEFT_PORTS[1], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				switchState = SwitchState.RIGHT;
				break;
			case RIGHT:
				ga1.set(LEFT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
				ga2.set(LEFT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
				ga1.set(RIGHT_PORTS[0], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				ga2.set(RIGHT_PORTS[1], SWITCH_PORT_DEACTIVATE, SWITCH_DELAY);
				switchState = SwitchState.LEFT;
				break;
			case UNDEF:
			}
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_SWITCH_LOCKED);
			} else {
				throw new SwitchException(ERR_TOGGLE_FAILED, x);
			}
		}
	}

	protected void switchPortChanged(int pAddress, int pChangedPort, int value) {
		int[] actualPorts = UNDEF_PORTS;
		switch (switchState) {
		case LEFT:
			actualPorts = LEFT_PORTS;
		case STRAIGHT:
			actualPorts = STRAIGHT_PORTS;
		case RIGHT:
			actualPorts = RIGHT_PORTS;
		case UNDEF:
			actualPorts = UNDEF_PORTS;
		}
		if (address.getAddress1() == pAddress) {
			actualPorts[0] = pChangedPort;
		} else if (address.getAddress2() == pAddress) {
			actualPorts[1] = pChangedPort;
		}
		if (actualPorts == LEFT_PORTS) {
			switchState = SwitchState.LEFT;
		} else if (actualPorts == STRAIGHT_PORTS) {
			switchState = SwitchState.STRAIGHT;
		} else if (actualPorts == RIGHT_PORTS) {
			switchState = SwitchState.RIGHT;
		} else {
			switchState = SwitchState.UNDEF;
		}
	}

	@Override
	protected void switchInitialized(int pBus, int pAddress) {
		ga1 = new GA(session);
		ga2 = new GA(session);
		address.setAddress1(pAddress);
		this.bus = pBus;
		ga1.setBus(bus);
		ga1.setAddress(address.getAddress1());
		initialized = true;
	}

	@Override
	protected void switchTerminated(int pAddress) {

	}

	@Override
	protected void setStraight() throws SwitchException {
		try {
			ga1.set(STRAIGHT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			ga2.set(STRAIGHT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			// TODO: resolve get
			switchState = SwitchState.STRAIGHT;
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}

	@Override
	protected void setCurvedLeft() throws SwitchException {
		try {
			ga1.set(LEFT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			ga2.set(LEFT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			// TODO: resolve get
			switchState = SwitchState.LEFT;
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}

	@Override
	protected void setCurvedRight() throws SwitchException {
		try {
			ga1.set(RIGHT_PORTS[0], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			ga2.set(RIGHT_PORTS[1], SWITCH_PORT_ACTIVATE, SWITCH_DELAY);
			// TODO: resolve get
			switchState = SwitchState.RIGHT;
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}
}