/*------------------------------------------------------------------------
 * 
 * <./domain/locomotives/LocomotiveGroup.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:05 BST 2006
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


package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;
import java.util.TreeSet;

public class LocomotiveGroup implements Comparable {
    private String                name;
    private SortedSet<Locomotive> locomotives;

    public LocomotiveGroup(String name) {
        this.name = name;
        locomotives = new TreeSet<Locomotive>();
    }

    public void addLocomotive(Locomotive locomotive) {
        locomotives.add(locomotive);
    }

    public void removeLocomotive(Locomotive locomotive) {
        locomotives.remove(locomotive);
    }

    public void replaceLocomotive(Locomotive oldLocomotive,
        Locomotive newLocomotive) {
        locomotives.remove(oldLocomotive);
        locomotives.add(newLocomotive);
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

    public SortedSet<Locomotive> getLocomotives() {
        return locomotives;
    }

    public LocomotiveGroup clone() {
        LocomotiveGroup newLocomotiveGroup = new LocomotiveGroup(name);
        return newLocomotiveGroup;
    }

    public int compareTo(Object o) {
        if (o instanceof LocomotiveGroup) {
            LocomotiveGroup anotherLocomotiveGroup = (LocomotiveGroup) o;
            return (name.compareTo(anotherLocomotiveGroup.getName()));
        }
        return 0;
    }
}
