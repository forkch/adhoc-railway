/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Route.java 199 2012-01-14 23:46:24Z fork_ch $
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
@Table(name = "route", uniqueConstraints = { @UniqueConstraint(columnNames = { "number" }) })
public class HibernateRoute extends Model implements java.io.Serializable,
		Comparable<HibernateRoute> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3923095019284054584L;

	// Fields
	@Id
	@GeneratedValue
	private int id;

	private HibernateRouteGroup routeGroup;

	private int number;

	private String name;

	@Sort(type = SortType.NATURAL)
	private SortedSet<HibernateRouteItem> routeItems = new TreeSet<HibernateRouteItem>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_ROUTE_GROUP = "routeGroup";
	public static final String PROPERTYNAME_NUMBER = "number";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";

	@Override
	public int compareTo(HibernateRoute o) {
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
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}
		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		final HibernateRoute l = (HibernateRoute) o;
		if (!name.equals(l.getName())) {
			return false;
		}
		if (number != l.getNumber()) {
			return false;
		}
		if (!routeGroup.equals(l.getRouteGroup())) {
			return false;
		}
		if (!routeItems.equals(l.getRouteItems())) {
			return false;
		}

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

	// Constructors

	/** default constructor */
	public HibernateRoute() {
	}

	/** minimal constructor */
	public HibernateRoute(int id, HibernateRouteGroup routeGroup, int number,
			String name) {
		this.id = id;
		this.routeGroup = routeGroup;
		this.number = number;
		this.name = name;
	}

	/** full constructor */
	public HibernateRoute(int id, HibernateRouteGroup routeGroup, int number,
			String name, SortedSet<HibernateRouteItem> routeItems) {
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
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "route_group_id", unique = false, nullable = false, insertable = true, updatable = true)
	public HibernateRouteGroup getRouteGroup() {
		return this.routeGroup;
	}

	public void setRouteGroup(HibernateRouteGroup routeGroup) {
		this.routeGroup = routeGroup;
	}

	@Column(name = "number", unique = false, nullable = false, insertable = true, updatable = true)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Column(name = "name", unique = false, nullable = false, insertable = true, updatable = true)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Sort(type = SortType.NATURAL)
	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "route")
	public SortedSet<HibernateRouteItem> getRouteItems() {
		if (routeItems == null) {
			routeItems = new TreeSet<HibernateRouteItem>();
		}
		return this.routeItems;
	}

	public void setRouteItems(SortedSet<HibernateRouteItem> routeItems) {
		this.routeItems = routeItems;
	}
}
