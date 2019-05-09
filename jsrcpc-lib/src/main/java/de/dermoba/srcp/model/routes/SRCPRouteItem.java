/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: SRCPRouteItem.java,v 1.1 2008-04-24 07:29:53 fork_ch Exp $
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

package de.dermoba.srcp.model.routes;

import de.dermoba.srcp.model.turnouts.SRCPTurnout;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class SRCPRouteItem {

	private SRCPTurnout			turnout;

	private SRCPRoute			route;

	private SRCPRouteItemState	routedState;

	@Override
	public int hashCode() {
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public boolean equals(Object obj) {
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	/** default constructor */
	public SRCPRouteItem() {
	}

	/** full constructor */
	public SRCPRouteItem(SRCPTurnout turnout, SRCPRoute route,
			SRCPRouteItemState routedState) {
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

	public SRCPRouteItemState getRoutedState() {
		return routedState;
	}

	public void setRoutedState(SRCPRouteItemState routedState) {
		this.routedState = routedState;
	}
}
