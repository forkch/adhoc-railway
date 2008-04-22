/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteItem.java 153 2008-03-27 17:44:48Z fork_ch $
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

package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutState;

public class SRCPRouteItem {

	private SRCPTurnout			turnout;

	private SRCPRoute			route;

	private SRCPTurnoutState	routedState;

	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final SRCPRouteItem l = (SRCPRouteItem) o;
		
		if (!turnout.equals(l.getTurnout()))
			return false;
		if (!routedState.equals(l.getRoutedState()))
			return false;
		return true;
	}

	public int hashCode() {
		return turnout.hashCode() + routedState.hashCode();
	}

	/** default constructor */
	public SRCPRouteItem() {
	}

	/** full constructor */
	public SRCPRouteItem(SRCPTurnout turnout, SRCPRoute route,
			SRCPTurnoutState routedState) {
		this.turnout = turnout;
		this.route = route;
		this.routedState = routedState;
	}

	public SRCPTurnout getTurnout() {
		return turnout;
	}

	public void setTurnout(SRCPTurnout turnout) {
		this.turnout = turnout;
	}

	public SRCPRoute getRoute() {
		return route;
	}

	public void setRoute(SRCPRoute route) {
		this.route = route;
	}

	public SRCPTurnoutState getRoutedState() {
		return routedState;
	}

	public void setRoutedState(SRCPTurnoutState routedState) {
		this.routedState = routedState;
	}
}
