/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: LocomotiveGroup.java 308 2013-05-01 15:43:50Z fork_ch $
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
import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.PropertyChangeListener;
import java.util.SortedSet;

public class LocomotiveGroup extends AbstractItem implements
        java.io.Serializable, Comparable<LocomotiveGroup> {

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_NAME = "name";
    public static final String PROPERTYNAME_LOCOMOTIVES = "locomotives";
    @XStreamAsAttribute
    @Expose
    private String id;
    @XStreamAsAttribute
    @Expose
    private String name;
    @Expose
    private SortedSet<Locomotive> locomotives = Sets.newTreeSet();

    public LocomotiveGroup() {
        super();
    }

    public LocomotiveGroup(String id, String name) {
        this.id = id;
        this.name = name;

    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        final String old = this.id;
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

    public SortedSet<Locomotive> getLocomotives() {
        return this.locomotives;
    }

    public void setLocomotives(final SortedSet<Locomotive> locomotives) {
        final SortedSet<Locomotive> old = this.locomotives;
        this.locomotives = locomotives;
        changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVES, old,
                this.locomotives);

    }

    public void addLocomotive(final Locomotive locomotive) {
        if (!locomotives.contains(locomotive)) {
            locomotives.add(locomotive);
        }
        changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVES,
                this.locomotives, this.locomotives);
    }

    public void removeLocomotive(final Locomotive locomotive) {
        locomotives.remove(locomotive);
        changeSupport.firePropertyChange(PROPERTYNAME_LOCOMOTIVES,
                this.locomotives, this.locomotives);
    }

    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    @Override
    public int compareTo(final LocomotiveGroup o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof LocomotiveGroup)) {
            return false;
        }
        return id.equals(((LocomotiveGroup) obj).getId());
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

}
