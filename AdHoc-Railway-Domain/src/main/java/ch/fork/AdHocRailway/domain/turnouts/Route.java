/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Route.java 308 2013-05-01 15:43:50Z fork_ch $
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
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.beans.PropertyChangeListener;
import java.util.SortedSet;
import java.util.TreeSet;

public class Route extends AbstractItem implements java.io.Serializable,
        Comparable<Route> {

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_NUMBER = "number";
    public static final String PROPERTYNAME_NAME = "name";
    public static final String PROPERTYNAME_ORIENTATION = "orientation";
    public static final String PROPERTYNAME_ROUTE_GROUP = "routeGroup";
    public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";
    private int id = -1;
    @XStreamOmitField
    private RouteGroup routeGroup;
    private int number;
    private String name;
    private String orientation;
    private SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>();
    private transient boolean enabled = false;
    private transient boolean routing = false;

    public Route() {
    }

    public int getId() {
        return this.id;
    }

    public void setId(final int id) {
        final int old = this.id;
        this.id = id;
        changeSupport.firePropertyChange(PROPERTYNAME_ID, old, this.id);
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(final int number) {
        final int old = this.number;
        this.number = number;
        changeSupport.firePropertyChange(PROPERTYNAME_NUMBER, old, this.number);
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        final String old = name;
        this.name = name;
        changeSupport.firePropertyChange(PROPERTYNAME_NAME, old, this.name);
    }

    public String getOrientation() {
        return orientation;
    }

    public void setOrientation(final String orientation) {
        final String old = orientation;
        this.orientation = orientation;
        changeSupport.firePropertyChange(PROPERTYNAME_ORIENTATION, old,
                this.orientation);
    }

    public SortedSet<RouteItem> getRouteItems() {
        return this.routeItems;
    }

    public void setRouteItems(final SortedSet<RouteItem> routeItems) {
        final SortedSet<RouteItem> old = this.routeItems;
        this.routeItems = routeItems;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTE_ITEMS, old,
                this.routeItems);
    }

    public void addRouteItem(final RouteItem routeItem) {
        routeItems.add(routeItem);
    }

    public RouteGroup getRouteGroup() {
        return this.routeGroup;
    }

    public void setRouteGroup(final RouteGroup routeGroup) {
        final RouteGroup old = this.routeGroup;
        this.routeGroup = routeGroup;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTE_GROUP, old,
                this.routeGroup);
    }


    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    public boolean isRouting() {
        return routing;
    }

    public void setRouting(final boolean routing) {
        this.routing = routing;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public int compareTo(final Route o) {
        return Integer.compare(number, o.getNumber());
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(final Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
