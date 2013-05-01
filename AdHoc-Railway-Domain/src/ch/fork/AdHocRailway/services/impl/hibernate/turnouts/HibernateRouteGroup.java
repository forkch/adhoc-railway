/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteGroup.java 199 2012-01-14 23:46:24Z fork_ch $
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

package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

/**
 * RouteGroup generated by hbm2java
 */
@Entity
@Table(name = "route_group", uniqueConstraints = {})
public class HibernateRouteGroup implements java.io.Serializable,
		Comparable<HibernateRouteGroup> {

	// Fields

	/**
	 * 
	 */
	private static final long serialVersionUID = -1258997643222460823L;

	@Id
	@GeneratedValue
	private int id;

	private String name;
	private int routeNumberOffset;

	private int routeNumberAmount;

	@Sort(type = SortType.NATURAL)
	private SortedSet<HibernateRoute> routes = new TreeSet<HibernateRoute>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_ROUTE_NUMBER_OFFSET = "routeNumberOffset";
	public static final String PROPERTYNAME_ROUTE_NUMBER_AMOUNT = "routeNumberAmount";
	public static final String PROPERTYNAME_ROUTES = "routes";

	@Override
	public int compareTo(final HibernateRouteGroup o) {
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
	public boolean equals(final Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final HibernateRouteGroup l = (HibernateRouteGroup) o;
		if (id != l.getId()) {
			return false;
		}
		if (!name.equals(l.getName())) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return name.hashCode();
	}

	@Override
	public String toString() {
		return name;
	}

	// Constructors

	/** default constructor */
	public HibernateRouteGroup() {
	}

	/** minimal constructor */
	public HibernateRouteGroup(final int id, final String name,
			final int routeNumberOffset, final int routeNumberAmount) {
		this.id = id;
		this.name = name;
		this.routeNumberOffset = routeNumberOffset;
		this.routeNumberAmount = routeNumberAmount;
	}

	/** full constructor */
	public HibernateRouteGroup(final int id, final String name,
			final int routeNumberOffset, final int routeNumberAmount,
			final SortedSet<HibernateRoute> routes) {
		this.id = id;
		this.name = name;
		this.routeNumberOffset = routeNumberOffset;
		this.routeNumberAmount = routeNumberAmount;
		this.routes = routes;
	}

	// Property accessors

	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = true, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	@Column(name = "name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getName() {
		return this.name;
	}

	@Column(name = "route_number_offset", unique = false, nullable = false, insertable = true, updatable = true)
	public int getRouteNumberOffset() {
		return this.routeNumberOffset;
	}

	public void setRouteNumberOffset(final int turnoutNumberOffset) {
		this.routeNumberOffset = turnoutNumberOffset;
	}

	@Column(name = "route_number_amount", unique = false, nullable = false, insertable = true, updatable = true)
	public int getRouteNumberAmount() {
		return this.routeNumberAmount;
	}

	public void setRouteNumberAmount(final int turnoutNumberAmount) {
		this.routeNumberAmount = turnoutNumberAmount;
	}

	public void setName(final String name) {
		this.name = name;
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "routeGroup")
	public SortedSet<HibernateRoute> getRoutes() {
		if (routes == null) {
			routes = new TreeSet<HibernateRoute>();
		}
		return this.routes;
	}

	@Sort(type = SortType.NATURAL)
	public void setRoutes(final SortedSet<HibernateRoute> routes) {
		this.routes = routes;
	}
}
