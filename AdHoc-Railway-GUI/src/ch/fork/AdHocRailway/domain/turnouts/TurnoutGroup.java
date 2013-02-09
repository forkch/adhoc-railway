/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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

import java.util.SortedSet;
import java.util.TreeSet;

import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import com.jgoodies.binding.beans.Model;

public class TurnoutGroup extends Model implements java.io.Serializable,
		Comparable<TurnoutGroup> {

	private int id;

	private String name;

	private int turnoutNumberOffset;

	private int turnoutNumberAmount;

	private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_TURNOUT_NUMBER_OFFSET = "turnoutNumberOffset";
	public static final String PROPERTYNAME_TURNOUT_NUMBER_AMOUNT = "turnoutNumberAmount";

	@Override
	public int compareTo(TurnoutGroup o) {
		return name.compareTo(o.getName());
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
		TurnoutGroup other = (TurnoutGroup) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	public TurnoutGroup() {
	}

	public TurnoutGroup(String name, int turnoutNumberOffset,
			int turnoutNumberAmount) {
		this.name = name;
		this.turnoutNumberOffset = turnoutNumberOffset;
		this.turnoutNumberAmount = turnoutNumberAmount;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		String old = this.name;
		this.name = name;
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	public int getTurnoutNumberOffset() {
		return this.turnoutNumberOffset;
	}

	public void setTurnoutNumberOffset(int turnoutNumberOffset) {
		int old = this.turnoutNumberOffset;
		this.turnoutNumberOffset = turnoutNumberOffset;
		firePropertyChange(PROPERTYNAME_ID, old, turnoutNumberOffset);
	}

	public int getTurnoutNumberAmount() {
		return this.turnoutNumberAmount;
	}

	public void setTurnoutNumberAmount(int turnoutNumberAmount) {
		int old = this.turnoutNumberAmount;
		this.turnoutNumberAmount = turnoutNumberAmount;
		firePropertyChange(PROPERTYNAME_ID, old, turnoutNumberAmount);
	}

	public SortedSet<Turnout> getTurnouts() {
		return this.turnouts;
	}

	@Sort(type = SortType.NATURAL)
	public void setTurnouts(SortedSet<Turnout> turnouts) {
		this.turnouts = turnouts;
	}

	public void addTurnout(Turnout turnout) {
		this.turnouts.add(turnout);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

}
