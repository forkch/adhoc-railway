/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteGroup.java 308 2013-05-01 15:43:50Z fork_ch $
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

package ch.fork.adhocrailway.model.turnouts;

import java.beans.PropertyChangeListener;
import java.util.SortedSet;
import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.fork.adhocrailway.model.AbstractItem;

import com.google.common.collect.Sets;
import com.google.gson.annotations.Expose;

public class RouteGroup extends AbstractItem implements java.io.Serializable,
        Comparable<RouteGroup> {

    public static final String PROPERTYNAME_NAME = "name";
    @Expose
    private String id = UUID.randomUUID().toString();
    ;
    @Expose
    private String name;
    @Expose
    private SortedSet<Route> routes = Sets.newTreeSet();

    public RouteGroup() {
    }

    public String getId() {
        return this.id;
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
        changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, this.name);
    }

    public SortedSet<Route> getRoutes() {
        if (routes == null) {
            routes = Sets.newTreeSet();
        }
        return this.routes;

    }

    public void setRoutes(final SortedSet<Route> routes) {
        this.routes = routes;
    }

    public void addRoute(final Route route) {
        this.routes.add(route);

    }

    public void removeRoute(final Route route) {
        this.routes.remove(route);

    }

    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    @Override
    public int compareTo(final RouteGroup o) {
        return name.compareTo(o.getName());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof RouteGroup)) {
            return false;
        }
        return id.equals(((RouteGroup) obj).getId());

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
