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

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.InvalidAddressException;
import ch.fork.AdHocRailway.domain.NoSessionException;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.common.exception.SRCPDeviceLockedException;
import de.dermoba.srcp.common.exception.SRCPException;
import de.dermoba.srcp.devices.GA;
import de.dermoba.srcp.devices.GAInfoListener;

public class SRCPTurnoutControl implements TurnoutControlIface, GAInfoListener {
	private static Logger						logger	=
																Logger
																		.getLogger(SRCPTurnoutControl.class);
	private static SRCPTurnoutControl			instance;
	
	TurnoutPersistenceIface						persistence;
	
	Map<Turnout, List<TurnoutChangeListener>>	listeners;
	
	Map<Turnout, SRCPTurnout>					srcpTurnouts;
	Turnout										lastChangedTurnout;
	
	TurnoutState								previousState;
	private SRCPSession	session;
	
	private SRCPTurnoutControl() {
		logger.info("SRCPTurnoutControl loaded");
		listeners = new HashMap<Turnout, List<TurnoutChangeListener>>();
		srcpTurnouts = new HashMap<Turnout, SRCPTurnout>();
	}
	
	public static SRCPTurnoutControl getInstance() {
		if (instance == null) {
			instance = new SRCPTurnoutControl();
		}
		return instance;
	}
	
	public void update() {
		srcpTurnouts.clear();
		for (Turnout t : persistence.getAllTurnouts()) {
			SRCPTurnout sTurnout = new SRCPTurnout(t);
			srcpTurnouts.put(t, sTurnout);
			sTurnout.setSession(session);
		}
	}
	
	public void setSession(SRCPSession session) {
		this.session = session;
		session.getInfoChannel().addGAInfoListener(this);
		for (SRCPTurnout st : srcpTurnouts.values()) {
			st.setSession(session);
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
	int getPort(TurnoutAddress address, int wantedPort) {
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
		// initTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		if (turnout.isThreeWay()) {
			toggleThreeWay(turnout);
			return;
		}
		previousState = sTurnout.getTurnoutState();
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
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		switch (sTurnout.getTurnoutState()) {
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
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		previousState = sTurnout.getTurnoutState();
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
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		sTurnout.setTurnoutState(TurnoutState.STRAIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}
	
	public void setNonDefaultState(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		previousState = sTurnout.getTurnoutState();
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
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		previousState = sTurnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setStraightThreeWay(turnout);
			return;
		}
		GA ga = sTurnout.getGA();
		TurnoutAddress address = sTurnout.getTurnoutAddresses()[0];
		try {
			int defaultActivationTime =
					Preferences.getInstance().getIntValue(
							PreferencesKeys.ACTIVATION_TIME);
			ga.set(getPort(address, Constants.TURNOUT_STRAIGHT_PORT),
					Constants.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(address, Constants.TURNOUT_CURVED_PORT),
					Constants.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			sTurnout.setTurnoutState(TurnoutState.STRAIGHT);
			informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new SwitchLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			logger.error(e);
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}
	
	private void setStraightThreeWay(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		setStraight(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		sTurnout.setTurnoutState(TurnoutState.STRAIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}
	
	public void setCurvedLeft(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		previousState = sTurnout.getTurnoutState();
		if (turnout.isThreeWay()) {
			setCurvedLeftThreeWay(turnout);
			return;
		}
		GA ga = sTurnout.getGA();
		TurnoutAddress address = sTurnout.getTurnoutAddresses()[0];
		try {
			int defaultActivationTime =
					Preferences.getInstance().getIntValue(
							PreferencesKeys.ACTIVATION_TIME);
			ga.set(getPort(address, Constants.TURNOUT_CURVED_PORT),
					Constants.TURNOUT_PORT_ACTIVATE, defaultActivationTime);
			ga.set(getPort(address, Constants.TURNOUT_STRAIGHT_PORT),
					Constants.TURNOUT_PORT_DEACTIVATE, defaultActivationTime);
			sTurnout.setTurnoutState(TurnoutState.LEFT);
			informListeners(turnout);
			lastChangedTurnout = turnout;
		} catch (SRCPDeviceLockedException x1) {
			throw new SwitchLockedException(Constants.ERR_LOCKED, x1);
		} catch (SRCPException e) {
			logger.error(e);
			throw new TurnoutException(Constants.ERR_TOGGLE_FAILED, e);
		}
	}
	
	private void setCurvedLeftThreeWay(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		
		setCurvedLeft(subTurnouts[0]);
		setStraight(subTurnouts[1]);
		sTurnout.setTurnoutState(TurnoutState.LEFT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}
	
	public void setCurvedRight(Turnout turnout) throws TurnoutException {
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		previousState = sTurnout.getTurnoutState();
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
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		for (Turnout t : subTurnouts) {
			checkTurnout(t);
			initTurnout(t);
		}
		
		setStraight(subTurnouts[0]);
		setCurvedLeft(subTurnouts[1]);
		sTurnout.setTurnoutState(TurnoutState.RIGHT);
		informListeners(turnout);
		lastChangedTurnout = turnout;
	}
	
	public TurnoutState getTurnoutState(Turnout turnout) {
		try {
			checkTurnout(turnout);
			SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
			return sTurnout.getTurnoutState();
		} catch (TurnoutException e) {
		}
		return TurnoutState.UNDEF;
	}
	
	public void GAset(double timestamp, int bus, int address, int port,
			int value) {
		logger.debug("GAset("
				+ bus
				+ " , "
				+ address
				+ " , "
				+ port
				+ " , "
				+ value
				+ " )");
		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		if (turnout == null) {
			// TODO
			return;
		}
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
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
		if (sTurnout != null) {
			informListeners(turnout);
		}
	}
	
	private void portChanged(Turnout turnout, int port) {
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		if (port == getPort(sTurnout.getTurnoutAddress(0),
				Constants.TURNOUT_STRAIGHT_PORT)) {
			sTurnout.setTurnoutState(TurnoutState.STRAIGHT);
		} else if (port == getPort(sTurnout.getTurnoutAddress(0),
				Constants.TURNOUT_CURVED_PORT)) {
			sTurnout.setTurnoutState(TurnoutState.LEFT);
		}
	}
	
	private void portChangedThreeway(Turnout turnout, int address, int port) {
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout[] subTurnouts = sTurnout.getSubTurnouts();
		SRCPTurnout[] subSRCPTurnouts = new SRCPTurnout[2];
		for (Turnout subTurnout : subTurnouts) {
			if (subTurnout.getAddress1() == address) {
				portChanged(subTurnout, port);
			}
		}
		subSRCPTurnouts[0] = srcpTurnouts.get(subTurnouts[0]);
		subSRCPTurnouts[1] = srcpTurnouts.get(subTurnouts[1]);
		if (subSRCPTurnouts[0].getTurnoutState() == TurnoutState.STRAIGHT
				&& subSRCPTurnouts[1].getTurnoutState() == TurnoutState.STRAIGHT) {
			sTurnout.setTurnoutState(TurnoutState.STRAIGHT);
		} else if (subSRCPTurnouts[0].getTurnoutState() == TurnoutState.LEFT
				&& subSRCPTurnouts[1].getTurnoutState() == TurnoutState.STRAIGHT) {
			sTurnout.setTurnoutState(TurnoutState.LEFT);
		} else if (subSRCPTurnouts[0].getTurnoutState() == TurnoutState.STRAIGHT
				&& subSRCPTurnouts[1].getTurnoutState() == TurnoutState.LEFT) {
			sTurnout.setTurnoutState(TurnoutState.RIGHT);
		} else if (subSRCPTurnouts[0].getTurnoutState() == TurnoutState.LEFT
				&& subSRCPTurnouts[1].getTurnoutState() == TurnoutState.LEFT) {
			sTurnout.setTurnoutState(TurnoutState.UNDEF);
		}
	}
	
	public void GAinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		logger.debug("GAinit("
				+ bus
				+ " , "
				+ address
				+ " , "
				+ protocol
				+ " , "
				+ params
				+ " )");
		
		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		if (sTurnout != null) {
			try {
				sTurnout.getGA().get(0);
			} catch (SRCPException e) {
				
			}
			informListeners(turnout);
		}
	}
	
	public void GAterm(double timestamp, int bus, int address) {
		logger.debug("GAterm( " + bus + " , " + address + " )");
		Turnout turnout = persistence.getTurnoutByAddressBus(bus, address);
		checkTurnout(turnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		if (sTurnout != null) {
			sTurnout.setGA(null);
			sTurnout.setInitialized(false);
			informListeners(turnout);
		}
	}
	
	public void addTurnoutChangeListener(Turnout turnout,
			TurnoutChangeListener listener) {
		if (listeners.get(turnout) == null) {
			listeners.put(turnout, new ArrayList<TurnoutChangeListener>());
		}
		listeners.get(turnout).add(listener);
	}
	
	public void removeTurnoutChangeListener(Turnout turnout) {
		listeners.remove(turnout);
	}
	
	public void removeTurnoutChangeListener(TurnoutChangeListener listener) {
		listeners.keySet().remove(listener);
	}
	
	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}
	
	private void informListeners(Turnout changedTurnout) {
		List<TurnoutChangeListener> ll = listeners.get(changedTurnout);
		SRCPTurnout sTurnout = srcpTurnouts.get(changedTurnout);
		if (ll == null)
			// its a sub-turnout of a threeway turnout
			return;
		for (TurnoutChangeListener scl : ll)
			scl.turnoutChanged(changedTurnout, sTurnout.getTurnoutState());
		logger.debug("turnoutChanged("
				+ changedTurnout.getNumber()
				+ ", "
				+ sTurnout.getTurnoutState()
				+ ")");
		
	}
	
	private void checkTurnout(Turnout turnout) throws TurnoutException {
		if (turnout == null) {
			return;
		}
		if (turnout.getBus1() == 0 || turnout.getAddress1() == 0)
			throw new TurnoutException(Constants.ERR_FAILED,
					new InvalidAddressException("Turnout "
							+ turnout.getNumber()
							+ " has an invalid address or bus"));
		if (turnout.isThreeWay()) {
			if (turnout.getBus2() == 0 || turnout.getAddress2() == 0)
				throw new TurnoutException(Constants.ERR_FAILED,
						new InvalidAddressException("Turnout "
								+ turnout.getNumber()
								+ " has an invalid address or bus"));
		}
		
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		if (sTurnout == null) {
			srcpTurnouts.put(turnout, new SRCPTurnout(turnout));
			sTurnout = srcpTurnouts.get(turnout);
		}
		
		if (sTurnout.getSession() == null && session == null) {
			throw new TurnoutException(Constants.ERR_NOT_CONNECTED,
					new NoSessionException());
		}
		if (sTurnout.getSession() == null && session != null) {
			sTurnout.setSession(session);
		}
		
		initTurnout(turnout);
	}
	
	private void initTurnout(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		
		if (!sTurnout.isInitialized()) {
			if (turnout.isThreeWay())
				initTurnoutThreeWay(turnout);
			try {
				GA ga = new GA(session);
				if (Preferences.getInstance().getBooleanValue(
						PreferencesKeys.INTERFACE_6051)) {
					ga.init(turnout.getBus1(), turnout.getAddress1(),
							SRCPTurnout.PROTOCOL);
				} else {
					ga.setBus(turnout.getBus1());
					ga.setAddress(turnout.getAddress1());
				}
				ga.get(0);
				
				sTurnout.setGA(ga);
				sTurnout.setInitialized(true);
			} catch (SRCPException e) {
				logger.error(e);
				throw new TurnoutException(Constants.ERR_INIT_FAILED, e);
			}
		}
	}
	
	private void initTurnoutThreeWay(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = srcpTurnouts.get(turnout);
		Turnout turnout1 = new Turnout();
		Turnout turnout2 = new Turnout();
		SRCPTurnout sTurnout1 = new SRCPTurnout(turnout1);
		SRCPTurnout sTurnout2 = new SRCPTurnout(turnout2);
		turnout1.setNumber(turnout.getNumber());
		turnout1.setBus1(turnout.getBus1());
		turnout1.setAddress1(turnout.getAddress1());
		turnout1.setAddress1Switched(turnout.isAddress1Switched());
		turnout1.setTurnoutType(persistence
				.getTurnoutType(TurnoutTypes.DEFAULT));
		turnout1.setTurnoutGroup(turnout.getTurnoutGroup());
		sTurnout1.setSession(sTurnout.getSession());
		
		turnout2.setNumber(turnout.getNumber());
		turnout2.setBus1(turnout.getBus2());
		turnout2.setAddress1(turnout.getAddress2());
		turnout2.setAddress1Switched(turnout.isAddress2Switched());
		turnout2.setTurnoutType(persistence
				.getTurnoutType(TurnoutTypes.DEFAULT));
		turnout2.setTurnoutGroup(turnout.getTurnoutGroup());
		sTurnout2.setSession(sTurnout.getSession());
		
		srcpTurnouts.put(turnout1, sTurnout1);
		srcpTurnouts.put(turnout2, sTurnout2);
		initTurnout(turnout1);
		initTurnout(turnout2);
		
		Turnout[] turnouts = new Turnout[] { turnout1, turnout2 };
		sTurnout.setSubTurnouts(turnouts);
		
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
		informListeners(lastChangedTurnout);
		
		lastChangedTurnout = null;
		previousState = null;
	}
	
	public void previousDeviceToDefault() throws TurnoutException {
		if (lastChangedTurnout == null) {
			return;
		}
		setDefaultState(lastChangedTurnout);
	}
	
	public void setTurnoutPersistence(TurnoutPersistenceIface persistence) {
		this.persistence = persistence;
		
	}
}