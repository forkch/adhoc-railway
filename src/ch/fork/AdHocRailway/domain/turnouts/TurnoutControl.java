/*------------------------------------------------------------------------
 * 
 * <./domain/switches/SwitchControl.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:54:48 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
 * language  : java
 * version   : $Id: TurnoutControl.java 123 2007-09-12 22:51:44Z fork_ch $
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

package ch.fork.AdHocRailway.domain.turnouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.exception.InvalidAddressException;
import ch.fork.AdHocRailway.domain.exception.NoSessionException;
import ch.fork.AdHocRailway.domain.turnouts.Turnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.exception.SwitchLockedException;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;

public class TurnoutControl extends Control {
	private static TurnoutControl instance;

	private TurnoutPersistenceIface persistence = HibernateTurnoutPersistence
			.getInstance();

	private Map<Turnout, List<TurnoutChangeListener>> listeners;

	private Turnout lastChangedTurnout;

	private TurnoutState previousState;

	private TurnoutControl() {
		listeners = new HashMap<Turnout, List<TurnoutChangeListener>>();

	}

	public static TurnoutControl getInstance() {
		if (instance == null) {
			instance = new TurnoutControl();
		}
		return instance;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		for (Turnout t : persistence.getAllTurnouts()) {
			t.setSession(session);
		}
		// session.getInfoChannel().addGAInfoListener(this);
	}

	/**
	 * Returns the port to activate according to the addressSwitched flag.
	 * 
	 * @param wantedPort
	 *            The port to 'convert'
	 * @return The 'converted' port
	 */
	private int getPort(TurnoutAddress address, int wantedPort) {
		if (!address.isSwitched()) {
			return wantedPort;
		} else {
			if (wantedPort == Constants.TURNOUT_STRAIGHT_PORT) {
				return Constants.TURNOUT_CURVED_PORT;
			} else {
				return Constants.TURNOUT_STRAIGHT_PORT;
			}
		}
	}

	public void toggle(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		if (turnout.isThreeWay()) {
			toggleThreeWay(turnout);
			return;
		}
		previousState = turnout.getTurnoutState();
		switch (previousState) {
		case STRAIGHT:
			setCurvedLeft(turnout);
			break;
		case RIGHT:
		case LEFT:
			setStraight(turnout);
			break;
		case UNDEF:
			setDefaultState(turnout);
		}
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void toggleThreeWay(Turnout turnout) throws TurnoutException {
		Turnout[] subTurnouts = turnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		for (Turnout t : subTurnouts) {
			System.out.println(t);
		}
		switch (turnout.getTurnoutState()) {
		case LEFT:
			setStraightThreeWay(turnout);
			break;
		case STRAIGHT:
			setCurvedRightThreeWay(turnout);
			break;
		case RIGHT:
			setCurvedLeftThreeWay(turnout);
			break;
		case UNDEF:
			setStraightThreeWay(turnout);
			break;
		}
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setDefaultState(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setDefaultStateTheeWay(turnout);
			return;
		}
		switch (turnout.getDefaultStateEnum()) {
		case STRAIGHT:
			setStraight(turnout);
			break;
		case LEFT:
		case RIGHT:
			setCurvedLeft(turnout);
			break;
		}
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setDefaultStateTheeWay(Turnout turnout)
			throws TurnoutException {
		Turnout[] subTurnouts = turnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(TurnoutState.STRAIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setNonDefaultState(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setNonDefaultStateTheeWay(turnout);
			return;
		}
		switch (turnout.getDefaultStateEnum()) {
		case STRAIGHT:
			setCurvedLeft(turnout);
			break;
		case LEFT:
		case RIGHT:
			setStraight(turnout);
			break;
		}
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setNonDefaultStateTheeWay(Turnout turnout) {
		// do nothing
	}

	public void setStraight(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setStraightThreeWay(turnout);
			return;
		}
		GA ga = turnout.getGA();
		TurnoutAddress address = turnout.getTurnoutAddresses()[0];
		try {
			int defaultActivationTime = Preferences.getInstance().getIntValue(
					PreferencesKeys.ACTIVATION_TIME);

			ga.set(getPort(address, Constants.TURNOUT_STRAIGHT_PORT),
					Constants.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(address, Constants.TURNOUT_CURVED_PORT),
					Constants.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			turnout.setTurnoutState(TurnoutState.STRAIGHT);
			informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new SwitchLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}

	private void setStraightThreeWay(Turnout turnout) throws TurnoutException {
		Turnout[] subTurnouts = turnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(TurnoutState.STRAIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setCurvedLeft(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setCurvedLeftThreeWay(turnout);
			return;
		}
		GA ga = turnout.getGA();
		TurnoutAddress address = turnout.getTurnoutAddresses()[0];
		try {
			int defaultActivationTime = Preferences.getInstance().getIntValue(
					PreferencesKeys.ACTIVATION_TIME);
			ga.set(getPort(address, Constants.TURNOUT_CURVED_PORT),
					Constants.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(address, Constants.TURNOUT_STRAIGHT_PORT),
					Constants.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			turnout.setTurnoutState(TurnoutState.LEFT);
			informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new SwitchLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}

	private void setCurvedLeftThreeWay(Turnout turnout) throws TurnoutException {
		Turnout[] subTurnouts = turnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}

		setCurvedLeft(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(TurnoutState.LEFT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setCurvedRight(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		initTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setCurvedRightThreeWay(turnout);
			return;
		}
		setCurvedLeft(turnout);

		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setCurvedRightThreeWay(Turnout turnout)
			throws TurnoutException {
		Turnout[] subTurnouts = turnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}

		setStraight(subTurnouts[1]);
		setCurvedRight(subTurnouts[0]);
		turnout.setTurnoutState(TurnoutState.RIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void GAset(double timestamp, int bus, int address, int port,
			int value) {
		/*
		 * System.out.println("GAset(" + bus + " , " + address + " , " + port + " , " +
		 * value + " )");
		 */
		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		if (turnout != null) {
			// TODO
			informListeners(turnout);
		}
	}

	public void GAinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		/*
		 * System.out.println("GAinit(" + bus + " , " + address + " , " +
		 * protocol + " , " + params + " )");
		 */

		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		if (turnout != null) {
			// TODO
			// GA[] gas = new GA[turnout.getTurnoutAddresses().length];
			// int i = 0;
			// for (TurnoutAddress addr : turnout.getTurnoutAddresses()) {
			// GA ga = new GA(session);
			// ga.setBus(addr.getBus());
			// ga.setAddress(addr.getAddress());
			// gas[i] = ga;
			// i++;
			// }
			// turnout.setGA(gas);
			turnout.setInitialized(true);
			informListeners(turnout);
		}
	}

	public void GAterm(double timestamp, int bus, int address) {
		/*
		 * System.out.println("GAterm( " + bus + " , " + address + " )");
		 */
		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		if (turnout != null) {
			// TODO

			turnout.setGA(null);
			turnout.setInitialized(false);
			informListeners(turnout);
		}
	}

	public void addSwitchChangeListener(Turnout turnout,
			TurnoutChangeListener listener) {
		if (listeners.get(turnout) == null) {
			listeners.put(turnout, new ArrayList<TurnoutChangeListener>());
		}
		listeners.get(turnout).add(listener);
	}

	public void removeSwitchChangeListener(Turnout turnout) {
		listeners.remove(turnout);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	private void informListeners(Turnout changedTurnout) {
		List<TurnoutChangeListener> ll = listeners.get(changedTurnout);
		if (ll == null)
			// its a sub-turnout of a threeway turnout
			return;
		for (TurnoutChangeListener scl : ll)
			scl.turnoutChanged(changedTurnout);
	}

	private void checkTurnout(Turnout turnout) throws TurnoutException {
		if (turnout == null)
			return;
		try {
			if (turnout.getSession() == null) {
				throw new NoSessionException();
			}

			if (turnout.getBus1() == 0 || turnout.getAddress1() == 0)
				throw new InvalidAddressException();

		} catch (NoSessionException e) {
			throw new TurnoutException(Constants.ERR_NOT_CONNECTED, e);
		} catch (InvalidAddressException e) {
			throw new TurnoutException(Constants.ERR_FAILED, e);
		}
	}

	private void initTurnout(Turnout turnout) throws TurnoutException {
		if (!turnout.isInitialized()) {
			if (turnout.isThreeWay())
				initSwitchThreeWay(turnout);
			try {
				GA ga = new GA(session);
				if (Preferences.getInstance().getBooleanValue(
						PreferencesKeys.INTERFACE_6051)) {
					ga.init(turnout.getBus1(), turnout.getAddress1(),
							Turnout.PROTOCOL);
				} else {
					ga.setBus(turnout.getBus1());
					ga.setAddress(turnout.getAddress1());
				}

				turnout.setGA(ga);
				turnout.setInitialized(true);
			} catch (SRCPException e) {
				throw new TurnoutException(Constants.ERR_INIT_FAILED, e);
			}
		}
	}

	private void initSwitchThreeWay(Turnout turnout) throws TurnoutException {
		Turnout turnout1 = new Turnout();
		Turnout turnout2 = new Turnout();
		turnout1.setBus1(turnout.getBus1());
		turnout1.setAddress1(turnout.getAddress1());
		turnout1.setAddress1Switched(turnout.isAddress1Switched());
		turnout1.setTurnoutType(persistence
				.getTurnoutType(TurnoutTypes.DEFAULT));
		turnout1.setTurnoutGroup(turnout.getTurnoutGroup());
		turnout1.setSession(turnout.getSession());

		turnout2.setBus1(turnout.getBus1());
		turnout2.setAddress1(turnout.getAddress2());
		turnout2.setAddress1Switched(turnout.isAddress2Switched());
		turnout2.setTurnoutType(persistence
				.getTurnoutType(TurnoutTypes.DEFAULT));
		turnout2.setTurnoutGroup(turnout.getTurnoutGroup());
		turnout2.setSession(turnout.getSession());

		initTurnout(turnout1);
		initTurnout(turnout2);

		Turnout turnouts[] = new Turnout[] { turnout1, turnout2 };
		turnout.setSubTurnouts(turnouts);

	}

	@Override
	public void undoLastChange() throws ControlException {
		if (lastChangedTurnout == null) {
			return;
		}
		switch (previousState) {

		case STRAIGHT:
			setStraight(lastChangedTurnout);
			break;
		case LEFT:
			setCurvedLeft(lastChangedTurnout);
			break;
		case RIGHT:
			setCurvedRight(lastChangedTurnout);
			break;
		case UNDEF:
			setStraight(lastChangedTurnout);
			break;
		}
		informListeners(lastChangedTurnout);

		lastChangedTurnout = null;
		previousState = null;
	}

	@Override
	public void previousDeviceToDefault() throws ControlException {
		if (lastChangedTurnout == null) {
			return;
		}
		setDefaultState(lastChangedTurnout);
	}
}
