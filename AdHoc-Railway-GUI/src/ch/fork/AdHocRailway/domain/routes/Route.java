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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.jgoodies.binding.beans.Model;

public class Route extends Model implements java.io.Serializable,
		Comparable<Route> {

	private int id;

	private RouteGroup routeGroup;

	private int number;

	private String name;

	private SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>();

	private int routeGroupId;

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
		this.number = number;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
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

	public void addRouteItem(RouteItem routeItem) {
		routeItems.add(routeItem);
	}

	@Override
	public int compareTo(Route o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return -1;
		}
		if (number > o.getNumber()) {
			return 1;
		} else if (number == o.getNumber()) {
			return 0;
		} else {
			return -1;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Route other = (Route) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
