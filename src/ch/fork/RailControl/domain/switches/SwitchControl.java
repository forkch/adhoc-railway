/*------------------------------------------------------------------------
 * 
 * <SwitchControl.java>  -  <Provides control over a switch>
 * 
 * begin     : j Tue Jan  3 21:25:16 CET 2006
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.RailControl.domain.switches.exception.SwitchException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GAInfoListener;

public class SwitchControl implements GAInfoListener {

	private static SwitchControl instance;

	private SRCPSession session;

	private List<SwitchChangeListener> listeners;

	private Map<Integer, Switch> addressToSwitch;

	private SwitchControl() {
		listeners = new ArrayList<SwitchChangeListener>();
		addressToSwitch = new HashMap<Integer, Switch>();
	}

	public static SwitchControl getInstance() {
		if (instance == null) {
			instance = new SwitchControl();
			return instance;
		} else {
			return instance;
		}
	}

	public void registerSwitches(Collection<Switch> switches) {
		for (Switch aSwitch : switches) {
			Address address = aSwitch.getAddress();
			addressToSwitch.put(address.getAddress1(), aSwitch);
			if (address.getAddress2() != 0) {
				addressToSwitch.put(address.getAddress2(), aSwitch);
			}
			System.out.println(aSwitch);
		}
	}

	public void unregisterSwitches(List<Switch> switches)  {
		for (Switch aSwitch : switches) {
			//aSwitch.term();
			addressToSwitch.remove(aSwitch.getAddress());
		}
	}

	public void unregisterAllSwitches()  {
		for (Switch aSwitch : addressToSwitch.values()) {
			//aSwitch.term();
		}
		addressToSwitch.clear();
	}

	public void setSessionOnSwitches(SRCPSession session) {
		for (Switch aSwitch : addressToSwitch.values()) {
			aSwitch.setSession(session);

			System.out.println("Setting session on : " + aSwitch);
		}
	}
	public void toggle(Switch aSwitch) throws SwitchException {
		aSwitch.toggle();

		for (SwitchChangeListener l : listeners) {
			l.switchChanged(aSwitch);
		}
	}

	public void setStraight(Switch aSwitch) throws SwitchException {
		aSwitch.setStraight();
	}

	public void setCurvedRight(Switch aSwitch) throws SwitchException {
		aSwitch.setCurvedRight();
	}

	public void setCurvedLeft(Switch aSwitch) throws SwitchException {
		aSwitch.setCurvedLeft();
	}

	public void addSwitchChangeListener(SwitchChangeListener listener) {
		listeners.add(listener);
	}

	public void GAset(double timestamp, int bus, int address, int port,
			int value) {
		/*
		 * System.out.println("GAset(" + bus + " , " + address + " , " + port + " , " +
		 * value + " )");
		 */
		Switch s = addressToSwitch.get(address);
		s.switchPortChanged(address, port, value);
		if (value != 0) {
			informListeners(s);
		}
	}

	public void GAinit(double timestamp, int bus, int address, String protocol,
			String[] params) {
		/*
		 * System.out.println("GAinit(" + bus + " , " + address + " , " +
		 * protocol + " , " + params + " )");
		 */
		Switch s = addressToSwitch.get(Integer.valueOf(address));
		if (s != null) {
			s.switchInitialized(bus, address);
			informListeners(s);
		}
	}

	public void GAterm(double timestamp, int bus, int address) {
		/*
		 * System.out.println("GAterm( " + bus + " , " + address + " )");
		 */
		Switch s = addressToSwitch.get(address);
		s.switchTerminated(address);
		informListeners(s);
	}

	private void informListeners(Switch changedSwitch) {
		for (SwitchChangeListener l : listeners) {
			l.switchChanged(changedSwitch);
		}
	}

	public SRCPSession getSession() {
		return session;
	}

	public void setSession(SRCPSession session) {
		this.session = session;
		session.getInfoChannel().addGAInfoListener(this);
	}
}
