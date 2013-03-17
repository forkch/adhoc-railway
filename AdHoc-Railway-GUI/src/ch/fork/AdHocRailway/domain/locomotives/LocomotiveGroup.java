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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.List;

import com.jgoodies.binding.beans.Model;

public class LocomotiveGroup extends Model implements java.io.Serializable,
		Comparable<LocomotiveGroup> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7262606789136545713L;

	private int id;

	private String name;

	private List<Locomotive> locomotives = new ArrayList<Locomotive>();

	private static final String PROPERTYNAME_NAME = "name";

	@Override
	public int compareTo(final LocomotiveGroup o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return -1;
		}
		return name.compareTo(o.getName());
	}

	@Override
	public int hashCode() {
		return Integer.valueOf(id).hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		final LocomotiveGroup other = (LocomotiveGroup) obj;
		if (id != other.id) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return name;
	}

	public LocomotiveGroup() {
	}

	public LocomotiveGroup(final int id, final String name) {
		this.id = id;
		this.name = name;
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
		firePropertyChange(PROPERTYNAME_NAME, old, name);
	}

	public List<Locomotive> getLocomotives() {
		return this.locomotives;
	}

	public void setLocomotives(final List<Locomotive> locomotives) {
		this.locomotives = locomotives;
	}

	public void addLocomotive(final Locomotive locomotive) {
		if (!locomotives.contains(locomotive)) {
			locomotives.add(locomotive);
		}
	}

	public void removeLocomotive(final Locomotive locomotive) {
		locomotives.remove(locomotive);
	}

}
