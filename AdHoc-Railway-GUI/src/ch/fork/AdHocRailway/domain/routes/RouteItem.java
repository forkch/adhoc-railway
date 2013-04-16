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

import org.apache.commons.lang3.builder.ToStringBuilder;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;

import com.jgoodies.binding.beans.Model;

public class RouteItem extends Model implements java.io.Serializable,
		Comparable<RouteItem> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7776737980695074105L;

	private int id;

	private Turnout turnout;

	private Route route;

	private TurnoutState routedState;

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TURNOUT = "turnout";
	public static final String PROPERTYNAME_ROUTE = "route";
	public static final String PROPERTYNAME_ROUTED_STATE = "routedState";

	@Override
	public int compareTo(final RouteItem o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return -1;
		}
		if (turnout == null) {
			return -1;
		}
		if (o.getTurnout() == null) {
			return 1;
		}
		return turnout.compareTo(o.getTurnout());
	}

	@Override
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final RouteItem l = (RouteItem) o;
		if (id != l.getId()) {
			return false;
		}
		if (!turnout.equals(l.getTurnout())) {
			return false;
		}
		if (!routedState.equals(l.getRoutedState())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return turnout.hashCode() + routedState.hashCode();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	public RouteItem() {
	}

	public RouteItem(final int id, final Turnout turnout, final Route route,
			final TurnoutState routedState) {
		this.id = id;
		this.turnout = turnout;
		this.route = route;
		this.routedState = routedState;
	}

	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public Turnout getTurnout() {
		return this.turnout;
	}

	public void setTurnout(final Turnout turnout) {
		final Turnout old = this.turnout;
		this.turnout = turnout;
		firePropertyChange(PROPERTYNAME_TURNOUT, old, turnout);
	}

	public Route getRoute() {
		return this.route;
	}

	public void setRoute(final Route route) {
		final Route old = this.route;
		this.route = route;
		firePropertyChange(PROPERTYNAME_ROUTE, old, route);
	}

	public TurnoutState getRoutedState() {
		return this.routedState;
	}

	public void setRoutedState(final TurnoutState routedState) {
		final TurnoutState old = this.routedState;
		this.routedState = routedState;
		firePropertyChange(PROPERTYNAME_ROUTED_STATE, old, routedState);
	}

}
