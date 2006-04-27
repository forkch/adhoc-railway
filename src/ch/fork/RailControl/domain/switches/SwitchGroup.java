/*------------------------------------------------------------------------
 * 
 * o   o   o   o          University of Applied Sciences Bern
 *             :          Department Computer Sciences
 *             :......o   
 *
 * <SwitchSection.java>  -  <>
 * 
 * begin     : Apr 10, 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : mullb@bfh.ch
 * language  : java
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

package ch.fork.RailControl.domain.switches;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SwitchGroup {
	private SortedSet<Switch> switches;
	private String name;
	public SwitchGroup(String name) {
		this.name = name;
		switches = new TreeSet<Switch>();
	}
	
	public void addSwitch(Switch aSwitch) {
		switches.add(aSwitch);
        SwitchControl.getInstance().addSwitch(aSwitch);
	}
	
	public void removeSwitch(Switch aSwitch) {
		switches.remove(aSwitch);
	}

	public void replaceSwitch(Switch oldSwitch, Switch newSwitch) {
		switches.remove(oldSwitch);
		switches.add(newSwitch);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String toString() {
		return name;
	}

	public Set<Switch> getSwitches() {
		return switches;
	}

}
