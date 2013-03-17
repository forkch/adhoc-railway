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

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.fork.AdHocRailway.domain.routes.RouteItem;

import com.jgoodies.binding.beans.Model;

public class Turnout extends Model implements java.io.Serializable,
		Comparable<Turnout> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8221315886983696988L;

	private int id = -1;

	private TurnoutType turnoutType;

	private TurnoutGroup turnoutGroup;

	private int number;

	private String description;

	private TurnoutState defaultState;

	private TurnoutOrientation orientation;

	private Set<RouteItem> routeItems = new HashSet<RouteItem>(0);

	private int address1;

	private int address2;

	private int bus1;

	private int bus2;

	private boolean address1Switched;

	private boolean address2Switched;

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TURNOUT_TYPE = "turnoutType";
	public static final String PROPERTYNAME_TURNOUT_GROUP = "turnoutGroup";
	public static final String PROPERTYNAME_NUMBER = "number";
	public static final String PROPERTYNAME_DESCRIPTION = "description";
	public static final String PROPERTYNAME_DEFAULT_STATE = "defaultState";
	public static final String PROPERTYNAME_ORIENTATION = "orientation";
	public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";
	public static final String PROPERTYNAME_ADDRESS1 = "address1";
	public static final String PROPERTYNAME_ADDRESS2 = "address2";
	public static final String PROPERTYNAME_BUS1 = "bus1";
	public static final String PROPERTYNAME_BUS2 = "bus2";
	public static final String PROPERTYNAME_ADDRESS1_SWITCHED = "address1Switched";
	public static final String PROPERTYNAME_ADDRESS2_SWITCHED = "address2Switched";

	public Turnout() {
	}

	public Turnout(int number, TurnoutType turnoutType, int bus1, int address1,
			boolean address1_switched, TurnoutState defaultState,
			TurnoutOrientation orientation, String description,
			TurnoutGroup turnoutGroup) {
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

	public TurnoutType getTurnoutType() {
		return this.turnoutType;
	}

	public void setTurnoutType(TurnoutType turnoutType) {
		TurnoutType old = this.turnoutType;
		this.turnoutType = turnoutType;
		firePropertyChange(PROPERTYNAME_TURNOUT_TYPE, old, turnoutType);
	}

	public TurnoutGroup getTurnoutGroup() {
		return this.turnoutGroup;
	}

	public void setTurnoutGroup(TurnoutGroup turnoutGroup) {
		TurnoutGroup old = this.turnoutGroup;
		this.turnoutGroup = turnoutGroup;
		firePropertyChange(PROPERTYNAME_TURNOUT_GROUP, old, turnoutGroup);
	}

	public int getNumber() {
		return this.number;
	}

	public void setNumber(int number) {
		int old = this.number;
		this.number = number;
		firePropertyChange(PROPERTYNAME_NUMBER, old, number);
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		String old = this.description;
		this.description = description;
		firePropertyChange(PROPERTYNAME_DESCRIPTION, old, description);
	}

	public TurnoutState getDefaultState() {
		return this.defaultState;
	}

	public void setDefaultState(TurnoutState defaultState) {
		TurnoutState old = this.defaultState;
		this.defaultState = defaultState;
		firePropertyChange(PROPERTYNAME_DEFAULT_STATE, old, defaultState);
	}

	public TurnoutOrientation getOrientation() {
		return this.orientation;
	}

	public void setOrientation(TurnoutOrientation orientation) {
		TurnoutOrientation old = this.orientation;
		this.orientation = orientation;
		firePropertyChange(PROPERTYNAME_ORIENTATION, old, orientation);
	}

	public Set<RouteItem> getRouteItems() {
		return this.routeItems;
	}

	public void setRouteItems(Set<RouteItem> routeItems) {
		this.routeItems = routeItems;
	}

	public int getAddress1() {
		return this.address1;
	}

	public void setAddress1(int address1) {
		int old = this.address1;
		this.address1 = address1;
		firePropertyChange(PROPERTYNAME_ADDRESS1, old, address1);
	}

	public int getAddress2() {
		return this.address2;
	}

	public void setAddress2(int address2) {
		int old = this.address2;
		this.address2 = address2;
		firePropertyChange(PROPERTYNAME_ADDRESS2, old, address2);
	}

	public int getBus1() {
		return this.bus1;
	}

	public void setBus1(int bus1) {
		int old = this.bus1;
		this.bus1 = bus1;
		firePropertyChange(PROPERTYNAME_BUS1, old, bus1);
	}

	public int getBus2() {
		return this.bus2;
	}

	public void setBus2(int bus2) {
		int old = this.bus2;
		this.bus2 = bus2;
		firePropertyChange(PROPERTYNAME_BUS2, old, bus2);
	}

	public boolean isAddress1Switched() {
		return this.address1Switched;
	}

	public void setAddress1Switched(boolean address1Switched) {
		boolean old = this.address1Switched;
		this.address1Switched = address1Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS1_SWITCHED, old,
				address1Switched);
	}

	public boolean isAddress2Switched() {
		return this.address2Switched;
	}

	public void setAddress2Switched(boolean address2Switched) {
		boolean old = this.address2Switched;
		this.address2Switched = address2Switched;
		firePropertyChange(PROPERTYNAME_ADDRESS2_SWITCHED, old,
				address2Switched);
	}

	public boolean isDefault() {
		return getTurnoutType() == TurnoutType.DEFAULT;
	}

	public boolean isDoubleCross() {
		return getTurnoutType() == TurnoutType.DOUBLECROSS;
	}

	public boolean isThreeWay() {
		return getTurnoutType() == TurnoutType.THREEWAY;
	}

	public boolean isCutter() {
		return getTurnoutType() == TurnoutType.CUTTER;
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
		Turnout other = (Turnout) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(Turnout o) {
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
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
