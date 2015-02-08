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

package ch.fork.AdHocRailway.model.turnouts;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.fork.AdHocRailway.model.AbstractItem;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

public class TurnoutGroup extends AbstractItem implements java.io.Serializable,
        Comparable<TurnoutGroup> {

    public static final String PROPERTYNAME_NAME = "name";
    @Expose
    private String id = UUID.randomUUID().toString();
    ;
    @Expose
    private String name;
    @Expose
    private SortedSet<Turnout> turnouts = Sets.newTreeSet();

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
        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof TurnoutGroup)) {
            return false;
        }
        return id.equals(((TurnoutGroup) obj).getId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
