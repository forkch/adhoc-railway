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

import java.util.SortedSet;
import java.util.TreeSet;

import com.jgoodies.binding.beans.Model;

import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class TurnoutType extends Model implements java.io.Serializable,
		Comparable<TurnoutType> {

	private int id;

	private String typeName;

	private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

	private static final String PROPERTYNAME_ID = "id";
	private static final String PROPERTYNAME_TYPENAME = "typeName";
	private static final String PROPERTYNAME_TURNOUTS = "turnouts";

	@Override
	public int compareTo(TurnoutType o) {
		return typeName.compareTo(o.getTypeName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		// result = prime * result + id;
		result = prime * result
				+ ((typeName == null) ? 0 : typeName.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final TurnoutType other = (TurnoutType) obj;
		// if (id != other.id)
		// return false;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return typeName;
	}

	public SRCPTurnoutTypes getTurnoutTypeEnum() {
		if (typeName.toUpperCase().equals("DEFAULT"))
			return SRCPTurnoutTypes.DEFAULT;
		else if (typeName.toUpperCase().equals("DOUBLECROSS"))
			return SRCPTurnoutTypes.DOUBLECROSS;
		else if (typeName.toUpperCase().equals("THREEWAY"))
			return SRCPTurnoutTypes.THREEWAY;
		else if (typeName.toUpperCase().equals("CUTTER"))
			return SRCPTurnoutTypes.CUTTER;
		else
			return SRCPTurnoutTypes.UNKNOWN;

	}

	public TurnoutType() {
	}

	public TurnoutType(int id, String typeName) {
		this.id = id;
		this.typeName = typeName;
	}

	public TurnoutType(int id, String typeName, SortedSet<Turnout> turnouts) {
		this.id = id;
		this.typeName = typeName;
		this.turnouts = turnouts;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTypeName() {
		return this.typeName;
	}

	public void setTypeName(String typeName) {
		String old = this.typeName;
		this.typeName = typeName;
		firePropertyChange(PROPERTYNAME_TYPENAME, old, typeName);
	}

	public SortedSet<Turnout> getTurnouts() {
		return this.turnouts;
	}

	public void setTurnouts(SortedSet<Turnout> turnouts) {
		this.turnouts = turnouts;
	}

}
