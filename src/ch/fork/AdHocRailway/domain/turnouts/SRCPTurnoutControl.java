/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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
import java.util.Set;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.InvalidAddressException;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.NoSessionException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;
import de.dermoba.srcp.devices.GAInfoListener;

public class SRCPTurnoutControl implements GAInfoListener {
	private static Logger					logger	= Logger
															.getLogger(SRCPTurnoutControl.class);
	private static SRCPTurnoutControl		instance;

	private List<SRCPTurnoutChangeListener>	listeners;

	private List<SRCPTurnout>				srcpTurnouts;

	private Map<LookupAddress, SRCPTurnout>	addressTurnoutCache;
	private Map<LookupAddress, SRCPTurnout>	addressThreewayCache;
	private SRCPTurnout						lastChangedTurnout;

	private SRCPTurnoutState				previousState;
	private SRCPSession						session;

	private SRCPTurnoutControl() {
		logger.info("SRCPTurnoutControl loaded");
		listeners = new ArrayList<SRCPTurnoutChangeListener>();
		srcpTurnouts = new ArrayList<SRCPTurnout>();

		this.addressTurnoutCache = new HashMap<LookupAddress, SRCPTurnout>();
		this.addressThreewayCache = new HashMap<LookupAddress, SRCPTurnout>();
	}

	public static SRCPTurnoutControl getInstance() {
		if (instance == null) {
			instance = new SRCPTurnoutControl();
		}
		return instance;
	}

	public void update(Set<SRCPTurnout> turnouts) {

		this.addressTurnoutCache.clear();
		this.addressThreewayCache.clear();
		srcpTurnouts.clear();
		for (SRCPTurnout turnout : turnouts) {
			srcpTurnouts.add(turnout);
			turnout.setSession(session);

			addressTurnoutCache.put(new LookupAddress(turnout.getBus1(),
					turnout.getAddress1(), turnout.getBus2(), turnout
							.getAddress2()), turnout);
			if (turnout.isThreeWay()) {
				addressThreewayCache.put(new LookupAddress(turnout.getBus1(),
						turnout.getAddress1(), 0, 0), turnout);
				addressThreewayCache.put(new LookupAddress(0, 0, turnout
						.getBus2(), turnout.getAddress2()), turnout);
			}
		}
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		if (session != null) {
			session.getInfoChannel().addGAInfoListener(this);
		}
		for (SRCPTurnout st : srcpTurnouts) {
			st.setSession(session);
		}
	}

	/**
	 * Returns the port to activate according to the addressSwitched flag.
	 * 
	 * @param wantedPort
	 *            The port to 'convert'
	 * @return The 'converted' port
	 */
	int getPort(SRCPTurnout turnout, int wantedPort) {
		if (!turnout.isAddress1Switched()) {
			return wantedPort;
		} else {
			if (wantedPort == SRCPTurnout.TURNOUT_STRAIGHT_PORT) {
				return SRCPTurnout.TURNOUT_CURVED_PORT;
			} else {
				return SRCPTurnout.TURNOUT_STRAIGHT_PORT;
			}
		}
	}

	public void refresh(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		if (turnout.isThreeWay()) {
			refreshThreeWay(turnout);
			return;
		}

		switch (turnout.getTurnoutState()) {
		case STRAIGHT:
			setStraight(turnout);
			break;
		case LEFT:
			setCurvedLeft(turnout);
			break;
		case RIGHT:
			setCurvedRight(turnout);
			break;
		default:
		}
		// informListeners(turnout);
	}

	private void refreshThreeWay(SRCPTurnout turnout) {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			// initTurnout(t);
			refresh(t);
		}
	}

	public void toggle(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		
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
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void toggleThreeWay(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
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
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setDefaultState(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setDefaultStateTheeWay(turnout);
			return;
		}
		switch (turnout.getDefaultState()) {
		case STRAIGHT:
			setStraight(turnout);
			break;
		case LEFT:
		case RIGHT:
			setCurvedLeft(turnout);
			break;
		}
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setDefaultStateTheeWay(SRCPTurnout turnout)
			throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setNonDefaultState(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setNonDefaultStateTheeWay(turnout);
			return;
		}
		switch (turnout.getDefaultState()) {
		case STRAIGHT:
			setCurvedLeft(turnout);
			break;
		case LEFT:
		case RIGHT:
			setStraight(turnout);
			break;
		}
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setNonDefaultStateTheeWay(SRCPTurnout turnout) {
		// do nothing
	}

	public void setStraight(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setStraightThreeWay(turnout);
			return;
		}
		GA ga = turnout.getGA();
		try {
			int defaultActivationTime = Preferences.getInstance().getIntValue(
					PreferencesKeys.ACTIVATION_TIME);
			ga.set(getPort(turnout, SRCPTurnout.TURNOUT_STRAIGHT_PORT),
					SRCPTurnout.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(turnout, SRCPTurnout.TURNOUT_CURVED_PORT),
					SRCPTurnout.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
			// informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new TurnoutLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			logger.error(e);
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}

	private void setStraightThreeWay(SRCPTurnout turnout)
			throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setCurvedLeft(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setCurvedLeftThreeWay(turnout);
			return;
		}
		GA ga = turnout.getGA();
		try {
			int defaultActivationTime = Preferences.getInstance().getIntValue(
					PreferencesKeys.ACTIVATION_TIME);
			ga.set(getPort(turnout, SRCPTurnout.TURNOUT_CURVED_PORT),
					SRCPTurnout.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(turnout, SRCPTurnout.TURNOUT_STRAIGHT_PORT),
					SRCPTurnout.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			turnout.setTurnoutState(SRCPTurnoutState.LEFT);
			// informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new TurnoutLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			logger.error(e);
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}

	private void setCurvedLeftThreeWay(SRCPTurnout turnout)
			throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}

		setCurvedLeft(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		turnout.setTurnoutState(SRCPTurnoutState.LEFT);
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public void setCurvedRight(SRCPTurnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		previousState = turnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setCurvedRightThreeWay(turnout);
			return;
		}
		setCurvedLeft(turnout);

		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	private void setCurvedRightThreeWay(SRCPTurnout turnout)
			throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}

		setStraight(subTurnouts[0]);
		setCurvedLeft(subTurnouts[1]);
		turnout.setTurnoutState(SRCPTurnoutState.RIGHT);
		// informListeners(turnout);
		lastChangedTurnout = turnout;
	}

	public SRCPTurnoutState getTurnoutState(SRCPTurnout turnout) {
		try {
			checkTurnout(turnout);
			return turnout.getTurnoutState();
		} catch (TurnoutException e) {
		}
		return SRCPTurnoutState.UNDEF;
	}

	public void GAset(double timestamp, int bus, int address, int port,
			int value) {
		logger.debug("GAset(" + bus + " , " + address + " , " + port + " , "
				+ value + " )");
		SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
		if (turnout == null) {
			// a turnout which the srcp-server knows but not me
			return;
		}
		checkTurnout(turnout);
		if (value == 0) {
			// ignore deactivation
			return;
		}
		// a port has been activated
		if (turnout.isThreeWay()) {
			portChangedThreeway(turnout, address, port);
		} else {
			portChanged(turnout, port);
		}

		informListeners(turnout);
	}

	private void portChanged(SRCPTurnout turnout, int port) {
		if (port == getPort(turnout, SRCPTurnout.TURNOUT_STRAIGHT_PORT)) {
			turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
		} else if (port == getPort(turnout, SRCPTurnout.TURNOUT_CURVED_PORT)) {
			turnout.setTurnoutState(SRCPTurnoutState.LEFT);
		}
	}

	private void portChangedThreeway(SRCPTurnout turnout, int address, int port) {
		SRCPTurnout[] subTurnouts = turnout.getSubTurnouts();
		for (SRCPTurnout subTurnout : subTurnouts) {
			if (subTurnout.getAddress1() == address) {
				portChanged(subTurnout, port);
			}
		}
		if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.STRAIGHT
				&& subTurnouts[1].getTurnoutState() == SRCPTurnoutState.STRAIGHT) {
			turnout.setTurnoutState(SRCPTurnoutState.STRAIGHT);
		} else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.LEFT
				&& subTurnouts[1].getTurnoutState() == SRCPTurnoutState.STRAIGHT) {
			turnout.setTurnoutState(SRCPTurnoutState.LEFT);
		} else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.STRAIGHT
				&& subTurnouts[1].getTurnoutState() == SRCPTurnoutState.LEFT) {
			turnout.setTurnoutState(SRCPTurnoutState.RIGHT);
		} else if (subTurnouts[0].getTurnoutState() == SRCPTurnoutState.LEFT
				&& subTurnouts[1].getTurnoutState() == SRCPTurnoutState.LEFT) {
			turnout.setTurnoutState(SRCPTurnoutState.UNDEF);
		}
	}

	public void GAinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		logger.debug("GAinit(" + bus + " , " + address + " , " + protocol
				+ " , " + params + " )");

		SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
		checkTurnout(turnout);
		if (turnout != null) {
			informListeners(turnout);
		}
	}

	public void GAterm(double timestamp, int bus, int address) {
		logger.debug("GAterm( " + bus + " , " + address + " )");
		SRCPTurnout turnout = getTurnoutByAddressBus(bus, address);
		checkTurnout(turnout);
		if (turnout != null) {
			turnout.setGA(null);
			turnout.setInitialized(false);
			informListeners(turnout);
		}
	}

	public void addTurnoutChangeListener(SRCPTurnoutChangeListener listener) {
		listeners.add(listener);
	}

	public void removeTurnoutChangeListener(SRCPTurnoutChangeListener listener) {
		listeners.remove(listener);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	void informListeners(SRCPTurnout changedTurnout) {
		
		for (SRCPTurnoutChangeListener scl : listeners)
			scl
					.turnoutChanged(changedTurnout, changedTurnout
							.getTurnoutState());
		logger.debug("turnoutChanged(" + changedTurnout + ")");

	}

	void checkTurnout(SRCPTurnout turnout) throws TurnoutException {
		logger.debug("checkTurnout(" + turnout + ")");
		if (turnout == null) {
			return;
			//throw new TurnoutException("Unknown Turnout (null)");
		}
		if (!turnout.checkBusAddress())
			throw new TurnoutException(Constants.ERR_FAILED,
					new InvalidAddressException(
							"Turnout has an invalid address or bus"));

		if (!srcpTurnouts.contains(turnout)) {
			srcpTurnouts.add(turnout);
			addressTurnoutCache.put(new LookupAddress(turnout.getBus1(),
					turnout.getAddress1(), turnout.getBus2(), turnout
							.getAddress2()), turnout);
			if (turnout.isThreeWay()) {
				addressThreewayCache.put(new LookupAddress(turnout.getBus1(),
						turnout.getAddress1(), 0, 0), turnout);
				addressThreewayCache.put(new LookupAddress(0, 0, turnout
						.getBus2(), turnout.getAddress2()), turnout);
			}
		}

		if (turnout.getSession() == null && session == null) {
			throw new TurnoutException(Constants.ERR_NOT_CONNECTED,
					new NoSessionException());
		}
		if (turnout.getSession() == null && session != null) {
			turnout.setSession(session);
		}

		initTurnout(turnout);
	}

	private void initTurnout(SRCPTurnout turnout) throws TurnoutException {

		if (!turnout.isInitialized()) {
			if (turnout.isThreeWay())
				initTurnoutThreeWay(turnout);
			try {
				GA ga = new GA(session);
				if (Preferences.getInstance().getBooleanValue(
						PreferencesKeys.INTERFACE_6051)) {
					ga.init(turnout.getBus1(), turnout.getAddress1(), turnout
							.getProtocol());
				} else {
					ga.setBus(turnout.getBus1());
					ga.setAddress(turnout.getAddress1());
				}

				turnout.setGA(ga);
				turnout.setInitialized(true);
			} catch (SRCPException e) {
				logger.error(e);
				throw new TurnoutException(Constants.ERR_INIT_FAILED, e);
			}
		}
	}

	private void initTurnoutThreeWay(SRCPTurnout turnout)
			throws TurnoutException {
		SRCPTurnout turnout1 = (SRCPTurnout) turnout.clone();
		SRCPTurnout turnout2 = (SRCPTurnout) turnout.clone();

		turnout1.setBus1(turnout.getBus1());
		turnout1.setAddress1(turnout.getAddress1());
		turnout1.setAddress1Switched(turnout.isAddress1Switched());
		turnout1.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
		turnout1.setSession(turnout.getSession());

		turnout2.setBus1(turnout.getBus2());
		turnout2.setAddress1(turnout.getAddress2());
		turnout2.setAddress1Switched(turnout.isAddress2Switched());
		turnout2.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
		turnout2.setSession(turnout.getSession());

		srcpTurnouts.add(turnout1);
		srcpTurnouts.add(turnout2);
		initTurnout(turnout1);
		initTurnout(turnout2);

		SRCPTurnout[] turnouts = new SRCPTurnout[] { turnout1, turnout2 };
		turnout.setSubTurnouts(turnouts);

	}

	public void undoLastChange() throws TurnoutException {
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
		// informListeners(lastChangedTurnout);

		lastChangedTurnout = null;
		previousState = null;
	}

	public void previousDeviceToDefault() throws TurnoutException {
		if (lastChangedTurnout == null) {
			return;
		}
		setDefaultState(lastChangedTurnout);
	}

	private SRCPTurnout getTurnoutByAddressBus(int bus, int address) {
		logger.debug("getTurnoutByAddressBus()");
		LookupAddress key1 = new LookupAddress(bus, address, 0, 0);
		SRCPTurnout lookup1 = addressTurnoutCache.get(key1);
		if (lookup1 != null)
			return lookup1;
		LookupAddress key2 = new LookupAddress(0, 0, bus, address);
		SRCPTurnout lookup2 = addressTurnoutCache.get(key2);
		if (lookup2 != null)
			return lookup2;
		SRCPTurnout threewayLookup1 = addressThreewayCache.get(key1);
		if (threewayLookup1 != null)
			return threewayLookup1;

		SRCPTurnout threewayLookup2 = addressThreewayCache.get(key2);
		if (threewayLookup2 != null)
			return threewayLookup2;

		return null;
	}
}
