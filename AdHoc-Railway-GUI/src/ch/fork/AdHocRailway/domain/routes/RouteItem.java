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

package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.beans.Model;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class RouteItem extends Model implements java.io.Serializable,
		Comparable<RouteItem> {

	private int id;

	private Turnout turnout;

	private Route route;

	private String routedState;

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TURNOUT = "turnout";
	public static final String PROPERTYNAME_ROUTE = "route";
	public static final String PROPERTYNAME_ROUTED_STATE = "routedState";

	private int turnoutId;

	@Override
	public int compareTo(RouteItem o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		return turnout.compareTo(o.getTurnout());
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final RouteItem l = (RouteItem) o;
		if (id != l.getId())
			return false;
		if (!turnout.equals(l.getTurnout()))
			return false;
		if (!routedState.equals(l.getRoutedState()))
			return false;
		return true;
	}

	@Override
	public int hashCode() {
		return turnout.hashCode() + routedState.hashCode();
	}

	@Override
	public String toString() {
		return turnout.toString() + ":" + routedState;
	}

	public SRCPTurnoutState getRoutedStateEnum() {
		if (routedState.toUpperCase().equals("STRAIGHT")) {
			return SRCPTurnoutState.STRAIGHT;
		} else if (routedState.toUpperCase().equals("LEFT")) {
			return SRCPTurnoutState.LEFT;
		} else if (routedState.toUpperCase().equals("RIGHT")) {
			return SRCPTurnoutState.RIGHT;
		}
		return SRCPTurnoutState.UNDEF;
	}

	public void setRoutedStateEnum(SRCPTurnoutState state) {
		switch (state) {
		case STRAIGHT:
			setRoutedState("STRAIGHT");
			break;
		case LEFT:
			setRoutedState("LEFT");
			break;
		case RIGHT:
			setRoutedState("RIGHT");
			break;
		default:
			setRoutedState("UNDEF");
		}
	}

	public RouteItem() {
	}

	public RouteItem(int id, Turnout turnout, Route route, String routedState) {
		this.id = id;
		this.turnout = turnout;
		this.route = route;
		this.routedState = routedState;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Turnout getTurnout() {
		return this.turnout;
	}

	public void setTurnout(Turnout turnout) {
		Turnout old = this.turnout;
		this.turnout = turnout;
		firePropertyChange(PROPERTYNAME_TURNOUT, old, turnout);
	}

	public Route getRoute() {
		return this.route;
	}

	public void setRoute(Route route) {
		Route old = this.route;
		this.route = route;
		firePropertyChange(PROPERTYNAME_ROUTE, old, route);
	}

	public String getRoutedState() {
		return this.routedState;
	}

	public void setRoutedState(String routedState) {
		String old = this.routedState;
		this.routedState = routedState;
		firePropertyChange(PROPERTYNAME_ROUTED_STATE, old, routedState);
	}

	public void setTurnoutId(int turnoutId) {
		this.turnoutId = turnoutId;

	}

	public int getTurnoutId() {
		return turnoutId;
	}

}
