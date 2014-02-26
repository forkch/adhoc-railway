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

import ch.fork.AdHocRailway.domain.AbstractItem;

import java.beans.PropertyChangeListener;
import java.util.SortedSet;
import java.util.TreeSet;

public class TurnoutGroup extends AbstractItem implements java.io.Serializable,
        Comparable<TurnoutGroup> {

    private static final long serialVersionUID = 8822984732725579518L;

    private int id;

    private String name;

    private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_NAME = "name";

    public TurnoutGroup() {
    }

    public int getId() {
        return id;
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
        changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, name);
    }

    public SortedSet<Turnout> getTurnouts() {
        return this.turnouts;
    }

    public void setTurnouts(final SortedSet<Turnout> turnouts) {
        this.turnouts = turnouts;
    }

    public void addTurnout(final Turnout turnout) {
        this.turnouts.add(turnout);
    }

    public void removeTurnout(final Turnout turnout) {
        this.turnouts.remove(turnout);
    }

    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    @Override
    public int compareTo(final TurnoutGroup o) {
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
        final TurnoutGroup other = (TurnoutGroup) obj;
        if (id != other.id) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return name;
    }
}
