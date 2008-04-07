/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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

package ch.fork.AdHocRailway.domain.turnouts;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.devices.GA;

public class SRCPTurnout {

	private Turnout	turnout;

	protected boolean	initialized	= false;
	public enum TurnoutState {
		LEFT, STRAIGHT, RIGHT, UNDEF
	}

	protected TurnoutState		turnoutState	= TurnoutState.UNDEF;

	public static final String	PROTOCOL		= "M";
	private Turnout[]			subTurnouts;
	private GA					ga;
	private SRCPSession			session;

	public SRCPTurnout(Turnout turnout) {
		this.turnout = turnout;
	}

	public GA getGA() {
		return this.ga;
	}

	protected void setGA(GA ga) {
		this.ga = ga;
	}

	public SRCPSession getSession() {
		return this.session;
	}

	protected void setSession(SRCPSession session) {
		this.session = session;
	}

	public TurnoutState getTurnoutState() {
		return turnoutState;
	}

	protected void setTurnoutState(TurnoutState switchState) {
		this.turnoutState = switchState;
	}

	public TurnoutAddress[] getTurnoutAddresses() {
		return new TurnoutAddress[] {
				new TurnoutAddress(turnout.getAddress1(), turnout.getBus1(),
						turnout.isAddress1Switched()),
				new TurnoutAddress(turnout.getAddress2(), turnout.getBus2(),
						turnout.isAddress2Switched()) };
	}

	public TurnoutAddress getTurnoutAddress(int index) {
		return getTurnoutAddresses()[index];
	}

	protected Turnout[] getSubTurnouts() {
		return subTurnouts;
	}

	protected void setSubTurnouts(Turnout[] subTurnouts) {
		this.subTurnouts = subTurnouts;
	}

	public String toString() {

		String buf = "\"" + turnout.getNumber() + ": "
				+ turnout.getTurnoutType().getTypeName() + " @";

		buf += " " + getTurnoutAddress(0);
		if (turnout.getTurnoutType().getTypeName().equals("ThreeWay"))
			buf += " " + getTurnoutAddress(1);
		buf += " Group:" + turnout.getTurnoutGroup().toString();
		return buf;
	}

	public boolean isInitialized() {
		return initialized;
	}
	
	public void setInitialized(boolean init) {
		initialized = init;
	}
}
