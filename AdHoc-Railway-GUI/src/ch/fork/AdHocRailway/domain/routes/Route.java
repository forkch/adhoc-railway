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

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.jgoodies.binding.beans.Model;

public class Route extends Model implements java.io.Serializable,
		Comparable<Route> {

	private int id;

	private RouteGroup routeGroup;

	private int number;

	private String name;

	private SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_ROUTE_GROUP = "routeGroup";
	public static final String PROPERTYNAME_NUMBER = "number";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";

	private int routeGroupId;

	private Set<Integer> routeItemIds;

	@Override
	public int compareTo(Route o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		if (number > o.getNumber())
			return 1;
		else if (number == o.getNumber())
			return 0;
		else
			return -1;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		final Route l = (Route) o;
		if (!name.equals(l.getName()))
			return false;
		if (number != l.getNumber())
			return false;
		if (!routeGroup.equals(l.getRouteGroup()))
			return false;
		if (!routeItems.equals(l.getRouteItems()))
			return false;

		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode() + routeGroup.hashCode() + routeItems.hashCode()
				+ number;
	}

	@Override
	public String toString() {
		return name;
	}

	public Route() {
	}

	public Route(int id, RouteGroup routeGroup, int number, String name) {
		this.id = id;
		this.routeGroup = routeGroup;
		this.number = number;
		this.name = name;
	}

	public Route(int id, RouteGroup routeGroup, int number, String name,
			SortedSet<RouteItem> routeItems) {
		this.id = id;
		this.routeGroup = routeGroup;
		this.number = number;
		this.name = name;
		this.routeItems = routeItems;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public RouteGroup getRouteGroup() {
		return this.routeGroup;
	}

	public void setRouteGroup(RouteGroup routeGroup) {
		this.routeGroup = routeGroup;
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		int old = this.number;
		this.number = number;
		firePropertyChange(PROPERTYNAME_NUMBER, old, number);
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	public SortedSet<RouteItem> getRouteItems() {
		return this.routeItems;
	}

	public void setRouteItems(SortedSet<RouteItem> routeItems) {
		this.routeItems = routeItems;
	}

	public void setRouteGroupId(int routeGroupId) {
		this.routeGroupId = routeGroupId;

	}

	public int getRouteGroupId() {
		return routeGroupId;
	}

	public void addRouteItemId(int routeItemId) {
		routeItemIds = new HashSet<Integer>();
		routeItemIds.add(routeItemId);

	}

	public Set<Integer> getRouteItemIds() {
		return routeItemIds;
	}
}
