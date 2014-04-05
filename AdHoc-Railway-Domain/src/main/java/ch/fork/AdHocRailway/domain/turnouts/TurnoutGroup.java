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
import com.google.gson.annotations.Expose;
import com.sun.xml.internal.bind.v2.runtime.RuntimeUtil;
import org.apache.commons.lang3.builder.*;

import java.beans.PropertyChangeListener;
import java.util.SortedSet;
import java.util.TreeSet;

public class TurnoutGroup extends AbstractItem implements java.io.Serializable,
        Comparable<TurnoutGroup> {

    @Expose
    private String id;

    @Expose
    private String name;

    @Expose
    private SortedSet<Turnout> turnouts = new TreeSet<Turnout>();

    public static final String PROPERTYNAME_NAME = "name";

    public TurnoutGroup() {
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
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
        return new CompareToBuilder().append(getName(), o.getName()).build();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        TurnoutGroup rhs = (TurnoutGroup) obj;
        return new EqualsBuilder().append(id, rhs.getId()).build().booleanValue();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(false, ToStringStyle.SHORT_PREFIX_STYLE).append(name).build();
    }
}
