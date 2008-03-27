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

package ch.fork.AdHocRailway.domain.turnouts;

// Generated 08-Aug-2007 18:10:44 by Hibernate Tools 3.2.0.beta8

import java.util.HashSet;
import java.util.Set;

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
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

import com.jgoodies.binding.beans.Model;

/**
 * Turnout generated by hbm2java
 */
@Entity
@Table(name = "turnout", catalog = "adhocrailway", uniqueConstraints = { @UniqueConstraint(columnNames = { "number" }) })
public class Turnout extends Model implements java.io.Serializable,
		Comparable<Turnout> {

	// Fields

	@Id
	@GeneratedValue
	private int					id;

	private TurnoutType			turnoutType;

	private TurnoutGroup		turnoutGroup;

	private int					number;

	private String				description;

	private String				defaultState;

	private String				orientation;

	private Set<RouteItem>		routeItems						= new HashSet<RouteItem>(
																		0);

	private int					address1;

	private int					address2;

	private int					bus1;

	private int					bus2;

	private boolean				address1Switched;

	private boolean				address2Switched;

	public static final String	PROPERTYNAME_ID					= "id";
	public static final String	PROPERTYNAME_TURNOUT_TYPE		= "turnoutType";
	public static final String	PROPERTYNAME_TURNOUT_GROUP		= "turnoutGroup";
	public static final String	PROPERTYNAME_NUMBER				= "number";
	public static final String	PROPERTYNAME_DESCRIPTION		= "description";
	public static final String	PROPERTYNAME_DEFAULT_STATE		= "defaultStateEnum";
	public static final String	PROPERTYNAME_ORIENTATION		= "orientationEnum";
	public static final String	PROPERTYNAME_ROUTE_ITEMS		= "routeItems";
	public static final String	PROPERTYNAME_ADDRESS1			= "address1";
	public static final String	PROPERTYNAME_ADDRESS2			= "address2";
	public static final String	PROPERTYNAME_BUS1				= "bus1";
	public static final String	PROPERTYNAME_BUS2				= "bus2";
	public static final String	PROPERTYNAME_ADDRESS1_SWITCHED	= "address1Switched";
	public static final String	PROPERTYNAME_ADDRESS2_SWITCHED	= "address2Switched";

	public enum TurnoutOrientation {
		NORTH, SOUTH, WEST, EAST
	};

	@Transient
	public TurnoutOrientation getOrientationEnum() {
		if (getOrientation().toUpperCase().equals("NORTH")) {
			return TurnoutOrientation.NORTH;
		} else if (getOrientation().toUpperCase().equals("SOUTH")) {
			return TurnoutOrientation.SOUTH;
		} else if (getOrientation().toUpperCase().equals("EAST")) {
			return TurnoutOrientation.EAST;
		} else if (getOrientation().toUpperCase().equals("WEST")) {
			return TurnoutOrientation.WEST;
		}
		return null;
	}

	@Transient
	public void setOrientationEnum(TurnoutOrientation orientation) {
		switch (orientation) {
		case NORTH:
			setOrientation("NORTH");
			break;
		case SOUTH:
			setOrientation("SOUTH");
			break;
		case WEST:
			setOrientation("WEST");
			break;
		case EAST:
			setOrientation("EAST");
			break;
		}
	}

	@Transient
	public TurnoutState getDefaultStateEnum() {
		if (getDefaultState().toUpperCase().equals("STRAIGHT")) {
			return TurnoutState.STRAIGHT;
		} else if (getDefaultState().toUpperCase().equals("LEFT")) {
			return TurnoutState.LEFT;
		} else if (getDefaultState().toUpperCase().equals("RIGHT")) {
			return TurnoutState.RIGHT;
		}
		return TurnoutState.UNDEF;
	}

	@Transient
	public void setDefaultStateEnum(TurnoutState state) {
		switch (state) {
		case STRAIGHT:
			setDefaultState("STRAIGHT");
			break;
		case LEFT:
			setDefaultState("LEFT");
			break;
		case RIGHT:
			setDefaultState("RIGHT");
			break;
		default:
			setDefaultState("UNDEF");
		}
	}

	@Transient
	public boolean isDefault() {
		return getTurnoutType().getTurnoutTypeEnum() == TurnoutTypes.DEFAULT;
	}

	@Transient
	public boolean isDoubleCross() {
		return getTurnoutType().getTurnoutTypeEnum() == TurnoutTypes.DOUBLECROSS;
	}

	@Transient
	public boolean isThreeWay() {
		return getTurnoutType().getTurnoutTypeEnum() == TurnoutTypes.THREEWAY;
	}

	public int compareTo(Turnout o) {
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

	public String toString() {

		String buf = "\"" + getNumber() + ": " + getTurnoutType().getTypeName()
				+ " @";

		buf += " " + getAddress1();
		if (isThreeWay())
			buf += " " + getAddress2();
		buf += " Group:" + getTurnoutGroup().toString();
		return buf;
	}

	// GENERATED BY HIBERNATE
	// Constructors

	@Override
	public int hashCode() {
		final int PRIME = 31;
		int result = 1;
		result = PRIME * result + address1;
		result = PRIME * result + (address1Switched ? 1231 : 1237);
		result = PRIME * result + address2;
		result = PRIME * result + (address2Switched ? 1231 : 1237);
		result = PRIME * result + bus1;
		result = PRIME * result + bus2;
		result = PRIME * result
				+ ((defaultState == null) ? 0 : defaultState.hashCode());
		result = PRIME * result
				+ ((description == null) ? 0 : description.hashCode());
		result = PRIME * result + id;
		result = PRIME * result + number;
		result = PRIME * result
				+ ((orientation == null) ? 0 : orientation.hashCode());
		// result = PRIME * result + ((routeItems == null) ? 0 :
		// routeItems.hashCode());
		result = PRIME * result
				+ ((turnoutGroup == null) ? 0 : turnoutGroup.hashCode());
		result = PRIME * result
				+ ((turnoutType == null) ? 0 : turnoutType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		final Turnout other = (Turnout) obj;
		if (address1 != other.address1)
			return false;
		if (address1Switched != other.address1Switched)
			return false;
		if (address2 != other.address2)
			return false;
		if (address2Switched != other.address2Switched)
			return false;
		if (bus1 != other.bus1)
			return false;
		if (bus2 != other.bus2)
			return false;
		if (defaultState == null) {
			if (other.defaultState != null)
				return false;
		} else if (!defaultState.equals(other.defaultState))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (id != other.id)
			return false;
		if (number != other.number)
			return false;
		if (orientation == null) {
			if (other.orientation != null)
				return false;
		} else if (!orientation.equals(other.orientation))
			return false;
		if (routeItems == null) {
			if (other.routeItems != null)
				return false;
		} else if (!routeItems.equals(other.routeItems))
			return false;

		return true;
	}

	/** default constructor */
	public Turnout() {
	}

	/** minimal constructor */
	public Turnout(int id, TurnoutType turnoutType, TurnoutGroup turnoutGroup,
			int number, String description, String defaultState,
			String orientation, int address1, int bus1,
			boolean address1_switched) {
		this.id = id;
		this.turnoutType = turnoutType;
		this.turnoutGroup = turnoutGroup;
		this.number = number;
		this.description = description;
		this.defaultState = defaultState;
		this.orientation = orientation;
		this.address1 = address1;
		this.bus1 = bus1;
		this.address1Switched = address1_switched;
	}

	/** full constructor */
	public Turnout(int id, TurnoutType turnoutType, TurnoutGroup turnoutGroup,
			int number, String description, String defaultState,
			String orientation, Set<RouteItem> routeItems, int address1,
			int address2, int bus1, int bus2, boolean address1_switched,
			boolean address2_switched) {
		this.id = id;
		this.turnoutType = turnoutType;
		this.turnoutGroup = turnoutGroup;
		this.number = number;
		this.description = description;
		this.defaultState = defaultState;
		this.orientation = orientation;
		this.routeItems = routeItems;
		this.address1 = address1;
		this.address2 = address2;
		this.bus1 = bus1;
		this.bus2 = bus2;
		this.address1Switched = address1_switched;
		this.address2Switched = address2_switched;

	}

	// Property accessors
	@Id
	@GeneratedValue
	@Column(name = "id", unique = true, nullable = false, insertable = true, updatable = true)
	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		int old = this.id;
		this.id = id;
		firePropertyChange(PROPERTYNAME_ID, old, id);
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "turnout_type_id", unique = false, nullable = false, insertable = true, updatable = true)
	public TurnoutType getTurnoutType() {
		return this.turnoutType;
	}

	public void setTurnoutType(TurnoutType turnoutType) {
		TurnoutType old = this.turnoutType;
		this.turnoutType = turnoutType;
		firePropertyChange(PROPERTYNAME_TURNOUT_TYPE, old, turnoutType);
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "turnout_group_id", unique = false, nullable = false, insertable = true, updatable = true)
	public TurnoutGroup getTurnoutGroup() {
		return this.turnoutGroup;
	}

	public void setTurnoutGroup(TurnoutGroup turnoutGroup) {
		TurnoutGroup old = this.turnoutGroup;
		this.turnoutGroup = turnoutGroup;
		firePropertyChange(PROPERTYNAME_TURNOUT_GROUP, old, turnoutGroup);
	}

	@Column(name = "number", unique = true, nullable = false, insertable = true, updatable = true)
	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		int old = this.number;
		this.number = number;
		firePropertyChange(PROPERTYNAME_NUMBER, old, number);
	}

	@Column(name = "description", unique = false, nullable = true, insertable = true, updatable = true)
	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		firePropertyChange(PROPERTYNAME_DESCRIPTION, old, description);
	}

	@Column(name = "default_state", unique = false, nullable = false, insertable = true, updatable = true, length = 9)
	public String getDefaultState() {
		return this.defaultState;
	}

	public void setDefaultState(String defaultState) {
		String old = this.defaultState;
		this.defaultState = defaultState;
		firePropertyChange(PROPERTYNAME_DEFAULT_STATE, old, defaultState);
	}

	@Column(name = "orientation", unique = false, nullable = false, insertable = true, updatable = true, length = 6)
	public String getOrientation() {
		return this.orientation;
	}

	public void setOrientation(String orientation) {
		String old = this.orientation;
		this.orientation = orientation;
		firePropertyChange(PROPERTYNAME_ORIENTATION, old, orientation);
	}

	@OneToMany(cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, mappedBy = "turnout")
	public Set<RouteItem> getRouteItems() {
		return this.routeItems;
	}

	public void setRouteItems(Set<RouteItem> routeItems) {
		Set<RouteItem> old = this.routeItems;
		this.routeItems = routeItems;
		firePropertyChange(PROPERTYNAME_ROUTE_ITEMS, old, routeItems);
	}

	@Column(name = "address1", unique = false, nullable = false, insertable = true, updatable = true)
	public int getAddress1() {
		return this.address1;
	}

	public void setAddress1(int address1) {
		int old = this.address1;
		this.address1 = address1;
		firePropertyChange(PROPERTYNAME_ADDRESS1, old, address1);
	}

	@Column(name = "address2", unique = false, nullable = true, insertable = true, updatable = true)
	public int getAddress2() {
		return this.address2;
	}

	public void setAddress2(int address2) {
		int old = this.address2;
		this.address2 = address2;
		firePropertyChange(PROPERTYNAME_ADDRESS2, old, address2);
	}

	@Column(name = "bus1", unique = false, nullable = false, insertable = true, updatable = true)
	public int getBus1() {
		return this.bus1;
	}

	public void setBus1(int bus1) {
		int old = this.bus1;
		this.bus1 = bus1;
		firePropertyChange(PROPERTYNAME_BUS1, old, bus1);
	}

	@Column(name = "bus2", unique = false, nullable = true, insertable = true, updatable = true)
	public int getBus2() {
		return this.bus2;
	}

	public void setBus2(int bus2) {
		int old = this.bus2;
		this.bus2 = bus2;
		firePropertyChange(PROPERTYNAME_BUS2, old, bus2);
	}

	@Column(name = "address1_switched", unique = false, nullable = false, insertable = true, updatable = true)
	public boolean isAddress1Switched() {
		return this.address1Switched;
	}

	public void setAddress1Switched(boolean address1Switched) {
		boolean old = this.address1Switched;
		this.address1Switched = address1Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS1_SWITCHED, old,
				address1Switched);
	}

	@Column(name = "address2_switched", unique = false, nullable = true, insertable = true, updatable = true)
	public boolean isAddress2Switched() {
		return this.address2Switched;
	}

	public void setAddress2Switched(boolean address2Switched) {
		boolean old = this.address2Switched;
		this.address2Switched = address2Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS2_SWITCHED, old,
				address2Switched);
	}
}
