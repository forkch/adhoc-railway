/*------------------------------------------------------------------------
 * 
 * <DefaultSwitch.java>  -  <A standard switch>
 * 
 * begin     : Tue Jan  3 21:26:08 CET 2006
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

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.switches.exception.SwitchException;
import ch.fork.RailControl.domain.switches.exception.SwitchLockedException;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class DefaultSwitch extends Switch {
	private GA ga;

	private int STRAIGHT_PORT = 0;

	private int CURVED_PORT = 1;

	public DefaultSwitch(int pNumber, String pDesc) {
		this(pNumber, pDesc, 0, new Address(0, 0));
	}

	public DefaultSwitch(int pNumber, String pDesc, int pBus, Address pAddress) {
		super(pNumber, pDesc, pBus, pAddress);

	}

	public void init() throws SwitchException {
		super.init();
		try {
			ga = new GA(session);
			ga.init(bus, address.getAddress1(), "M");
			initialized = true;
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_LOCKED);
			} else {
				throw new SwitchException(ERR_INIT_FAILED, x);
			}
		}
	}

	public void term() throws SwitchException {
		try {
			super.term();
			ga.term();
			initialized = false;
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_LOCKED);
			} else {
				throw new SwitchException(ERR_TERM_FAILED, x);
			}
		}
	}

	@Override
	protected void reinit() throws SwitchException {
		try {
			if (ga != null) {
				ga.term();
			}
		} catch (SRCPException e) {
			throw new SwitchException(ERR_REINIT_FAILED, e);
		}
		if (session != null) {
			init();
		}
	}

	protected void toggle() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		try {
			switch (switchState) {
			case STRAIGHT:
				ga.set(CURVED_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				switchState = SwitchState.LEFT;
				break;
			case RIGHT:
			case LEFT:
				ga.set(STRAIGHT_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				switchState = SwitchState.STRAIGHT;
				break;
			case UNDEF:
				if (defaultState == SwitchState.STRAIGHT) {
					ga.set(STRAIGHT_PORT, SWITCH_PORT_ACTIVATE, Preferences
							.getInstance().getDefaultActivationTime());
					switchState = SwitchState.STRAIGHT;
				} else if (defaultState == SwitchState.RIGHT
						|| defaultState == SwitchState.LEFT) {
					ga.set(CURVED_PORT, SWITCH_PORT_ACTIVATE, Preferences
							.getInstance().getDefaultActivationTime());
					switchState = SwitchState.LEFT;
				}
			}
		} catch (SRCPException x) {
			if (x instanceof SRCPDeviceLockedException) {
				throw new SwitchLockedException(ERR_LOCKED);
			} else {
				throw new SwitchException(ERR_TOGGLE_FAILED, x);
			}
		}
	}

	protected void switchPortChanged(int pAddress, int pChangedPort, int value) {
		if (value == 0) {

		} else {
			// a port has been ACTIVATED
			if (pChangedPort == STRAIGHT_PORT) {
				switchState = SwitchState.STRAIGHT;
			} else if (pChangedPort == CURVED_PORT) {
				switchState = SwitchState.LEFT;
			}
		}

	}

	@Override
	protected void switchInitialized(int pBus, int pAddress) {
		ga = new GA(session);
		address.setAddress1(pAddress);
		this.bus = pBus;
		ga.setBus(bus);
		ga.setAddress(address.getAddress1());
		initialized = true;
	}

	@Override
	protected void switchTerminated(int pAddress) {
		ga = null;
		initialized = false;
	}

	@Override
	protected void setStraight() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		try {
			if (defaultState == SwitchState.STRAIGHT) {
				ga.set(STRAIGHT_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				ga.set(CURVED_PORT, SWITCH_PORT_DEACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				// TODO: resolve get
				switchState = SwitchState.STRAIGHT;
			} else if (defaultState == SwitchState.LEFT
					|| defaultState == SwitchState.RIGHT) {
				ga.set(CURVED_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				ga.set(STRAIGHT_PORT, SWITCH_PORT_DEACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				// TODO: resolve get
				switchState = SwitchState.LEFT;
			}
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}

	@Override
	protected void setCurvedLeft() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		try {
			if (defaultState == SwitchState.LEFT
					|| defaultState == SwitchState.RIGHT) {
				ga.set(STRAIGHT_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				ga.set(CURVED_PORT, SWITCH_PORT_DEACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				// TODO: resolve get
				switchState = SwitchState.STRAIGHT;
			} else if (defaultState == SwitchState.STRAIGHT) {
				ga.set(CURVED_PORT, SWITCH_PORT_ACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				ga.set(STRAIGHT_PORT, SWITCH_PORT_DEACTIVATE, Preferences
						.getInstance().getDefaultActivationTime());
				// TODO: resolve get
				switchState = SwitchState.LEFT;
			}
		} catch (SRCPException e) {
			throw new SwitchException(ERR_TOGGLE_FAILED, e);
		}
	}

	@Override
	protected void setCurvedRight() throws SwitchException {
		setCurvedLeft();
	}

}
