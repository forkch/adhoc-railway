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

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.jgoodies.binding.beans.Model;

/**
 * Route generated by hbm2java
 */
@Entity
@Table(name = "route", catalog = "adhocrailway", uniqueConstraints = { @UniqueConstraint(columnNames = { "number" }) })
public class Route extends Model implements java.io.Serializable,
		Comparable<Route> {

	// Fields
	@Id
	@GeneratedValue
	private int						id;

	private RouteGroup				routeGroup;

	private int						number;

	private String					name;

	@Sort(type = SortType.NATURAL)
	private SortedSet<RouteItem>	routeItems					= new TreeSet<RouteItem>();

	public static final String		PROPERTYNAME_ID				= "id";
	public static final String		PROPERTYNAME_ROUTE_GROUP	= "routeGroup";
	public static final String		PROPERTYNAME_NUMBER			= "number";
	public static final String		PROPERTYNAME_NAME			= "name";
	public static final String		PROPERTYNAME_ROUTE_ITEMS	= "routeItems";


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

	public int hashCode() {
		return name.hashCode() + routeGroup.hashCode() + routeItems.hashCode()
				+ number;
	}

	public String toString() {
		return name;
	}

	// Constructors

	/** default constructor */
	public Route() {
	}

	/** minimal constructor */
	public Route(int id, RouteGroup routeGroup, int number, String name) {
		this.id = id;
		this.routeGroup = routeGroup;
		this.number = number;
		this.name = name;
	}

	/** full constructor */
	public Route(int id, RouteGroup routeGroup, int number, String name,
			SortedSet<RouteItem> routeItems) {
		this.id = id;
		this.routeGroup = routeGroup;
		this.number = number;
		this.name = name;
		this.routeItems = routeItems;
	}

	// Property accessors

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = true, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		//firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "route_group_id", unique = false, nullable = false, insertable = true, updatable = true)
	public RouteGroup getRouteGroup() {
		return this.routeGroup;
	}

	public void setRouteGroup(RouteGroup routeGroup) {
		this.routeGroup = routeGroup;
	}

	@Column(name = "number", unique = false, nullable = false, insertable = true, updatable = true)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		int old = this.number;
		this.number = number;
		firePropertyChange(PROPERTYNAME_NUMBER, old, number);
	}

	@Column(name = "name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "route")
	public SortedSet<RouteItem> getRouteItems() {
		return this.routeItems;
	}

	public void setRouteItems(SortedSet<RouteItem> routeItems) {
		SortedSet<RouteItem> old = this.routeItems;
		this.routeItems = routeItems;
		//firePropertyChange(PROPERTYNAME_ROUTE_ITEMS, old, routeItems);
	}
}
