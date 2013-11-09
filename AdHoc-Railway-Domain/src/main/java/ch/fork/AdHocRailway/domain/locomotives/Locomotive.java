/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Locomotive.java 308 2013-05-01 15:43:50Z fork_ch $
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

import ch.fork.AdHocRailway.domain.AbstractItem;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.SortedSet;
import java.util.TreeSet;

public class Locomotive extends AbstractItem implements Serializable,
		Comparable<Locomotive> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7581060269617994905L;

	private int id = -1;

	private String name;

	private String desc;

	private String image;
	private LocomotiveType type;
	private int address1;
	private int address2;

	private int bus;

	private SortedSet<LocomotiveFunction> functions = new TreeSet<LocomotiveFunction>();

	private LocomotiveGroup group;
	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_NAME = "name";
	public static final String PROPERTYNAME_DESCRIPTION = "desc";
	public static final String PROPERTYNAME_IMAGE = "image";
	public static final String PROPERTYNAME_LOCOMOTIVE_TYPE = "type";
	public static final String PROPERTYNAME_ADDRESS1 = "address1";
	public static final String PROPERTYNAME_ADDRESS2 = "address2";
	public static final String PROPERTYNAME_BUS = "bus";

	public static final String PROPERTYNAME_FUNCTIONS = "functions";
	public static final String PROPERTYNAME_LOCOMOTIVE_GROUP = "group";

	private transient int currentSpeed = 0;

	private transient LocomotiveDirection currentDirection = LocomotiveDirection.FORWARD;

	private transient boolean[] currentFunctions = new boolean[] { false,
			false, false, false, false };

	public Locomotive() {
	}

	@Override
	public void init() {
		super.init();
		currentSpeed = 0;
		currentDirection = LocomotiveDirection.FORWARD;
		currentFunctions = new boolean[] { false, false, false, false, false };
	}

	public int getId() {
		return this.id;
	}

	public void setId(final int id) {
		final int old = this.id;
		this.id = id;
		changeSupport.firePropertyChange(PROPERTYNAME_ID, old, this.id);
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		final String old = this.name;
		this.name = name;
		changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, this.name);
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(final String description) {
		final String old = this.desc;
		this.desc = description;
		changeSupport.firePropertyChange(PROPERTYNAME_DESCRIPTION, old,
				this.desc);
	}

	public String getImage() {
		return this.image;
	}

	public void setImage(final String image) {
		final String old = this.image;
		this.image = image;
		changeSupport.firePropertyChange(PROPERTYNAME_IMAGE, old, this.image);
	}

	public LocomotiveType getType() {
		return this.type;
	}

	public void setType(final LocomotiveType locomotiveType) {
		final LocomotiveType old = this.type;
		this.type = locomotiveType;
		changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVE_TYPE, old,
				this.type);
	}

	public int getAddress1() {
		return this.address1;
	}

	public void setAddress1(final int address1) {
		final int old = this.address1;
		this.address1 = address1;
		changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS1, old,
				this.address1);
	}

	public int getAddress2() {
		return this.address2;
	}

	public void setAddress2(final int address2) {
		final int old = this.address2;
		this.address2 = address2;
		changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS2, old,
				this.address2);
	}

	public int getBus() {
		return bus;
	}

	public void setBus(final int bus) {
		this.bus = bus;
	}

	public SortedSet<LocomotiveFunction> getFunctions() {
		return functions;
	}

	public void setFunctions(final SortedSet<LocomotiveFunction> functions) {
		final SortedSet<LocomotiveFunction> old = this.functions;
		this.functions = functions;
		changeSupport
				.firePropertyChange(PROPERTYNAME_NAME, old, this.functions);
	}

	public void addLocomotiveFunction(final LocomotiveFunction function) {
		this.functions.add(function);
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

	public LocomotiveGroup getGroup() {
		return this.group;
	}

	public void setGroup(final LocomotiveGroup locomotiveGroup) {
		final LocomotiveGroup old = this.group;
		this.group = locomotiveGroup;
		changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVE_GROUP, old,
				this.group);
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public void addPropertyChangeListener(final PropertyChangeListener x) {
		changeSupport.addPropertyChangeListener(x);
	}

	public void removePropertyChangeListener(final PropertyChangeListener x) {
		changeSupport.removePropertyChangeListener(x);
	}

	public int getCurrentSpeed() {
		return currentSpeed;
	}

	public void setCurrentSpeed(final int currentSpeed) {
		this.currentSpeed = currentSpeed;
	}

	public LocomotiveDirection getCurrentDirection() {
		return currentDirection;
	}

	public void setCurrentDirection(final LocomotiveDirection currentDirection) {
		this.currentDirection = currentDirection;

	}

	public boolean[] getCurrentFunctions() {
		return currentFunctions;
	}

	public void setCurrentFunctions(final boolean[] currentFunctions) {
		this.currentFunctions = currentFunctions;

	}
}
