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

import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import com.jgoodies.binding.beans.Model;

public class Locomotive extends Model implements Serializable,
		Comparable<Locomotive> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7581060269617994905L;

	private int id = -1;

	private LocomotiveGroup group;

	private LocomotiveType type;

	private String name;

	private String desc;

	private String image;

	private int address1;
	private int address2;

	private int bus;

	private SortedSet<LocomotiveFunction> functions = new TreeSet<LocomotiveFunction>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_LOCOMOTIVE_GROUP = "group";
	public static final String PROPERTYNAME_LOCOMOTIVE_TYPE = "type";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_DESCRIPTION = "desc";
	public static final String PROPERTYNAME_IMAGE = "image";
	public static final String PROPERTYNAME_ADDRESS1 = "address1";
	public static final String PROPERTYNAME_ADDRESS2 = "address2";
	public static final String PROPERTYNAME_BUS = "bus";

	public Locomotive() {
	}

	@Override
	public int compareTo(final Locomotive o) {
		if (this == o) {
			return 0;
		}
		if (o == null) {
			return -1;
		}
		if (name == null) {
			if (id > o.getId()) {
				return 1;
			} else if (id == o.getId()) {
				return 0;
			} else {
				return -1;
			}
		} else {
			return name.compareTo(o.getName());
		}

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
		final Locomotive other = (Locomotive) obj;
		if (id != other.id) {
			return false;
		}
		return true;
	}

	public int getAddress1() {
		return this.address1;
	}

	public int getAddress2() {
		return this.address2;
	}

	public String getDesc() {
		return this.desc;
	}

	public SortedSet<LocomotiveFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(final SortedSet<LocomotiveFunction> functions) {
		this.functions = functions;
	}

	public LocomotiveGroup getGroup() {
		return this.group;
	}

	public int getId() {
		return this.id;
	}

	public String getImage() {
		return this.image;
	}

	public String getName() {
		return this.name;
	}

	public LocomotiveType getType() {
		return this.type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	public void setAddress1(final int address1) {
		this.address1 = address1;
	}

	public void setAddress2(final int address2) {
		this.address2 = address2;
	}

	public void setDesc(final String description) {
		this.desc = description;
	}

	public void setGroup(final LocomotiveGroup locomotiveGroup) {
		this.group = locomotiveGroup;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public void setImage(final String image) {
		this.image = image;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public void setType(final LocomotiveType locomotiveType) {
		final LocomotiveType old = this.type;
		this.type = locomotiveType;
		firePropertyChange(PROPERTYNAME_LOCOMOTIVE_TYPE, old, locomotiveType);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public void addLocomotiveFunction(final LocomotiveFunction function) {
		this.functions.add(function);
	}

	public int getBus() {
		return bus;
	}

	public void setBus(final int bus) {
		this.bus = bus;
	}

	public int getEmergencyStopFunction() {
		int i = 0;
		for (final LocomotiveFunction function : functions) {
			if (function.isEmergencyBrakeFunction()) {
				return i;
			}
			i++;
		}
		return -1;
	}

	public LocomotiveFunction getFunction(final int functionNumber) {
		for (final LocomotiveFunction function : functions) {
			if (function.getNumber() == functionNumber) {
				return function;
			}
		}
		return null;
	}

}
