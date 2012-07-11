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

import java.util.SortedSet;
import java.util.TreeSet;

import com.jgoodies.binding.beans.Model;

public class LocomotiveType extends Model implements java.io.Serializable,
		Comparable<LocomotiveType> {

	private int id;

	private String typeName;

	private int drivingSteps;

	private int stepping;

	private int functionCount;

	public static final int PROTOCOL_VERSION = 2;

	public static final String PROTOCOL = "M";

	private SortedSet<Locomotive> locomotives = new TreeSet<Locomotive>();

	public static final String PROPERTYNAME_ID = "id";
	public static final String PROPERTYNAME_TYPE_NAME = "typeName";
	public static final String PROPERTYNAME_DRIVING_STEPS = "drivingSteps";
	public static final String PROPERTYNAME_STEPPING = "stepping";
	public static final String PROPERTYNAME_FUNCTION_COUNT = "functionCount";
	public static final String PROPERTYNAME_LOCOMOTIVES = "locomotives";

	@Override
	public int compareTo(LocomotiveType o) {
		if (this == o)
			return 0;
		if (o == null)
			return -1;
		return typeName.compareTo(o.getTypeName());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + drivingSteps;
		result = prime * result + functionCount;
		result = prime * result + id;
		result = prime * result + stepping;
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
		final LocomotiveType other = (LocomotiveType) obj;
		if (drivingSteps != other.drivingSteps)
			return false;
		if (functionCount != other.functionCount)
			return false;
		if (id != other.id)
			return false;
		if (stepping != other.stepping)
			return false;
		if (typeName == null) {
			if (other.typeName != null)
				return false;
		} else if (!typeName.equals(other.typeName))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return this.getTypeName();
	}

	public LocomotiveType() {
	}

	public LocomotiveType(int id, String typeName) {
		this.id = id;
		this.typeName = typeName;
	}

	public LocomotiveType(int id, String typeName,
			SortedSet<Locomotive> locomotives) {
		this.id = id;
		this.typeName = typeName;
		this.locomotives = locomotives;
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
		firePropertyChange(PROPERTYNAME_TYPE_NAME, old, typeName);
	}

	public int getDrivingSteps() {
		return this.drivingSteps;
	}

	public void setDrivingSteps(int drivingSteps) {
		int old = this.drivingSteps;
		this.drivingSteps = drivingSteps;
		firePropertyChange(PROPERTYNAME_DRIVING_STEPS, old, drivingSteps);
	}

	public int getStepping() {
		return this.stepping;
	}

	public void setStepping(int stepping) {
		int old = this.stepping;
		this.stepping = stepping;
		firePropertyChange(PROPERTYNAME_STEPPING, old, stepping);
	}

	public int getFunctionCount() {
		return this.functionCount;
	}

	public void setFunctionCount(int functionCount) {
		int old = functionCount;
		this.functionCount = functionCount;
		firePropertyChange(PROPERTYNAME_FUNCTION_COUNT, old, functionCount);
	}

	public SortedSet<Locomotive> getLocomotives() {
		return this.locomotives;
	}

	public void setLocomotives(SortedSet<Locomotive> locomotives) {
		SortedSet<Locomotive> old = this.locomotives;
		this.locomotives = locomotives;
		firePropertyChange(PROPERTYNAME_LOCOMOTIVES, old, locomotives);
	}

	public void addLocomotive(Locomotive locomotive) {
		locomotives.add(locomotive);
	}

}
