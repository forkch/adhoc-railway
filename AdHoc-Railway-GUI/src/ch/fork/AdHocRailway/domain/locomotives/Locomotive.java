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

	private int address;

	private int bus;
	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_LOCOMOTIVE_GROUP = "group";
	public static final String PROPERTYNAME_LOCOMOTIVE_TYPE = "type";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_DESCRIPTION = "desc";
	public static final String PROPERTYNAME_IMAGE = "image";
	public static final String PROPERTYNAME_ADDRESS = "address";
	public static final String PROPERTYNAME_BUS = "bus";

	public Locomotive() {
	}

	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		this.id = id;
	}

	public LocomotiveGroup getGroup() {
		return this.group;
	}

	public void setGroup(final LocomotiveGroup locomotiveGroup) {
		this.group = locomotiveGroup;
	}

	public LocomotiveType getType() {
		return this.type;
	}

	public void setType(final LocomotiveType locomotiveType) {
		this.type = locomotiveType;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(final String description) {
		this.desc = description;
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(final String image) {
		this.image = image;
	}

	public int getAddress() {
		return this.address;
	}

	public void setAddress(final int address) {
		this.address = address;
	}

	public int getBus() {
		return this.bus;
	}

	public void setBus(final int bus) {
		this.bus = bus;
	}

	public int[] getAddresses() {
		return new int[] { address };
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
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
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

}
