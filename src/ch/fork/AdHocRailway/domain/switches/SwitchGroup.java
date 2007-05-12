/*------------------------------------------------------------------------
 * 
 * <./domain/switches/SwitchGroup.java>  -  <>
 * 
 * begin     : Wed Aug 23 16:55:02 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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


package ch.fork.AdHocRailway.domain.switches;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SwitchGroup {
    private SortedSet<Switch> switches;
    private String            name;

    public SwitchGroup(String name) {
        this.name = name;
        switches = new TreeSet<Switch>();
    }

    public void addSwitch(Switch aSwitch) {
        switches.add(aSwitch);
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

    public SwitchGroup clone() {
        SwitchGroup newSwitchGroup = new SwitchGroup(name);
        for(Switch s : getSwitches()) {
        	newSwitchGroup.addSwitch(s);
        }
        return newSwitchGroup;
    }

    public boolean equals(Object o) {
        if (o instanceof SwitchGroup) {
            SwitchGroup sg = (SwitchGroup) o;
            return sg.getName().equals(name);
        }
        return false;
    }

}
