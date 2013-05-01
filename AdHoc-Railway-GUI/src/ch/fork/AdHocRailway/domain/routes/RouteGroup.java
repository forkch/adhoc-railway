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

import java.beans.PropertyChangeListener;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.domain.AbstractItem;

public class RouteGroup extends AbstractItem implements java.io.Serializable,
		Comparable<RouteGroup> {

	private static final long serialVersionUID = 1072827983159437081L;

	private int id;

	private String name;

	private SortedSet<Route> routes = new TreeSet<Route>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_ROUTE_NUMBER_OFFSET = "routeNumberOffset";
	public static final String PROPERTYNAME_ROUTE_NUMBER_AMOUNT = "routeNumberAmount";
	public static final String PROPERTYNAME_ROUTES = "routes";

	public RouteGroup() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		final String old = this.name;
		this.name = name;
		changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, this.name);
	}

	public SortedSet<Route> getRoutes() {
		return this.routes;
	}

	public void setRoutes(final SortedSet<Route> routes) {
		this.routes = routes;
	}

	public void addRoute(final Route route) {
		this.routes.add(route);

	}

	public void removeRoute(final Route route) {
		this.routes.remove(route);

	}

	@Override
	public int compareTo(final RouteGroup o) {
		return name.compareTo(o.getName());
	}

	@Override
	public boolean equals(final Object o) {
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

	public void addPropertyChangeListener(final PropertyChangeListener x) {
		changeSupport.addPropertyChangeListener(x);
	}

	public void removePropertyChangeListener(final PropertyChangeListener x) {
		changeSupport.removePropertyChangeListener(x);
	}
}
