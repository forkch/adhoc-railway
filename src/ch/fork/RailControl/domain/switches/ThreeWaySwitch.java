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

import java.util.HashMap;
import java.util.Map;

import ch.fork.RailControl.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;

public class ThreeWaySwitch extends Switch {

	private DefaultSwitch switch1;
	private DefaultSwitch switch2;

	private Map<Integer, Switch> addressToSwitch;
	
	public ThreeWaySwitch(int pNumber, String pDesc, int pBus, Address address) {
		super(pNumber, pDesc, pBus, address);
		switch1 = new DefaultSwitch(number, desc, bus, new Address(address
				.getAddress1()));
		switch2 = new DefaultSwitch(number, desc, bus, new Address(address
				.getAddress2()));
		addressToSwitch = new HashMap<Integer, Switch>();
		addressToSwitch.put(address.getAddress1(), switch1);
		addressToSwitch.put(address.getAddress2(), switch2);
	}

	public void init() throws SwitchException {
		super.init();
		switch1.setSession(session);
		switch2.setSession(session);
		switch1.init();
		switch2.init();
		initialized = true;
	}
	
	public void term() throws SwitchException{
		super.term();
		switch1.term();
		switch2.term();
		initialized = false;
	}

	@Override
	protected void reinit() throws SwitchException {
		if (switch1 != null) {
			switch1.reinit();
		}
		if (switch2 != null) {
			switch2.reinit();
		}
		if (session != null) {
			init();
		}
	}

	protected void toggle() throws SwitchException {
		if (session == null) {
			throw new SwitchException(ERR_NO_SESSION);
		}
		switch (switchState) {
		case LEFT:
			switch1.setStraight();
			switch2.setStraight();
			switchState = SwitchState.STRAIGHT;
			break;
		case STRAIGHT:
			switch1.setStraight();
			switch2.setCurvedRight();
			switchState = SwitchState.RIGHT;
			break;
		case RIGHT:
			switch1.setCurvedRight();
			switch2.setStraight();
			switchState = SwitchState.LEFT;
			break;
		case UNDEF:
			switch1.setStraight();
			switch2.setStraight();
			switchState = SwitchState.STRAIGHT;
			break;
		}
	}

	protected void switchPortChanged(int pAddress, int pChangedPort, int value) {
		
	}

	@Override
	protected void switchInitialized(int pBus, int pAddress) {
		addressToSwitch.get(pAddress).switchInitialized(pBus, pAddress);
	}

	@Override
	protected void switchTerminated(int pAddress) {
		addressToSwitch.get(pAddress).switchTerminated(pAddress);
	}

	@Override
	protected void setStraight() throws SwitchException {
		switch1.setStraight();
		switch2.setStraight();
		switchState = SwitchState.STRAIGHT;
	}

	@Override
	protected void setCurvedLeft() throws SwitchException {
		switch1.setCurvedRight();
		switch2.setStraight();
		switchState = SwitchState.LEFT;
	}

	@Override
	protected void setCurvedRight() throws SwitchException {
		switch1.setStraight();
		switch2.setCurvedRight();
		switchState = SwitchState.RIGHT;
	}
	
	public void setSession(SRCPSession session) {
		this.session = session;
		switch1.setSession(session);
		switch2.setSession(session);
	}

	@Override
	public Switch clone() {
		ThreeWaySwitch newSwitch = new ThreeWaySwitch(number, desc, bus, address);
		newSwitch.setSession(session);
		newSwitch.setSwitchOrientation(switchOrientation);
		newSwitch.setDefaultState(defaultState);
		return newSwitch;
	}
}