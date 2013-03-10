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

import java.util.SortedSet;
import java.util.TreeSet;

import com.jgoodies.binding.beans.Model;

public class RouteGroup extends Model implements java.io.Serializable,
		Comparable<RouteGroup> {

	private int id;

	private String name;
	private int routeNumberOffset;

	private int routeNumberAmount;

	private SortedSet<Route> routes = new TreeSet<Route>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_ROUTE_NUMBER_OFFSET = "routeNumberOffset";
	public static final String PROPERTYNAME_ROUTE_NUMBER_AMOUNT = "routeNumberAmount";
	public static final String PROPERTYNAME_ROUTES = "routes";

	@Override
	public int compareTo(RouteGroup o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return -1;
		}
		if (id > o.getId()) {
			return 1;
		} else if (id < o.getId()) {
			return -1;
		} else {
			return 0;
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final RouteGroup l = (RouteGroup) o;
		if (id != l.getId()) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(id).hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	public RouteGroup() {
	}

	public RouteGroup(int id, String name, int routeNumberOffset,
			int routeNumberAmount) {
		this.id = id;
		this.name = name;
		this.routeNumberOffset = routeNumberOffset;
		this.routeNumberAmount = routeNumberAmount;
	}

	public RouteGroup(int id, String name, int routeNumberOffset,
			int routeNumberAmount, SortedSet<Route> routes) {
		this.id = id;
		this.name = name;
		this.routeNumberOffset = routeNumberOffset;
		this.routeNumberAmount = routeNumberAmount;
		this.routes = routes;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public int getRouteNumberOffset() {
		return this.routeNumberOffset;
	}

	public void setRouteNumberOffset(int turnoutNumberOffset) {
		int old = this.routeNumberOffset;
		this.routeNumberOffset = turnoutNumberOffset;
		firePropertyChange(PROPERTYNAME_ID, old, turnoutNumberOffset);
	}

	public int getRouteNumberAmount() {
		return this.routeNumberAmount;
	}

	public void setRouteNumberAmount(int turnoutNumberAmount) {
		int old = this.routeNumberAmount;
		this.routeNumberAmount = turnoutNumberAmount;
		firePropertyChange(PROPERTYNAME_ID, old, turnoutNumberAmount);
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	public SortedSet<Route> getRoutes() {
		return this.routes;
	}

	public void setRoutes(SortedSet<Route> routes) {
		this.routes = routes;
	}

	public void addRoute(Route route) {
		this.routes.add(route);

	}

	public void removeRoute(Route route) {
		this.routes.remove(route);

	}
}
