/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: RouteItem.java 308 2013-05-01 15:43:50Z fork_ch $
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

import java.util.UUID;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import ch.fork.AdHocRailway.model.AbstractItem;

import com.google.gson.annotations.Expose;

public class RouteItem extends AbstractItem implements java.io.Serializable,
        Comparable<RouteItem> {

    public static final String PROPERTYNAME_ID = "id";
    public static final String PROPERTYNAME_TURNOUT = "turnout";
    public static final String PROPERTYNAME_ROUTE = "route";
    public static final String PROPERTYNAME_ROUTED_STATE = "state";
    @Expose
    private String id = UUID.randomUUID().toString();
    @Expose
    private int turnoutNumber;
    @Expose
    private RouteItemState state;

    private transient String turnoutId;

    private transient Turnout turnout;

    private transient Route route;


    public RouteItem() {
    }

    public RouteItem(RouteItem routeItem) {
        this.id = routeItem.getId();
        this.turnoutNumber = routeItem.getTurnoutNumber();
        this.state = routeItem.getState();
        this.turnout = new Turnout(routeItem.getTurnout());
    }

    public String getId() {
        return this.id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public Turnout getTurnout() {
        return this.turnout;
    }

    public void setTurnout(final Turnout turnout) {
        final Turnout old = this.turnout;
        this.turnout = turnout;
        this.turnoutNumber = turnout.getNumber();
        changeSupport.firePropertyChange(PROPERTYNAME_TURNOUT, old,
                this.turnout);
    }

    public Route getRoute() {
        return this.route;
    }

    public void setRoute(final Route route) {
        final Route old = this.route;
        this.route = route;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTE, old, this.route);
    }

    public RouteItemState getState() {
        return this.state;
    }

    public void setState(final RouteItemState state) {
        final RouteItemState old = this.state;
        this.state = state;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTED_STATE, old,
                this.state);
    }

    @Override
    public int compareTo(final RouteItem o) {
        if(turnout == null) {
            return -1;
        }
        if(o.getTurnout() == null) {
            return 1;
        }
        return Integer.compare(turnout.getNumber(), o.getTurnout().getNumber());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof RouteItem)) {
            return false;
        }
        return id.equals(((RouteItem) obj).getId());

    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public int getTurnoutNumber() {
        return turnoutNumber;
    }

    public void setTurnoutNumber(int turnoutNumber) {
        this.turnoutNumber = turnoutNumber;
    }

    public String getTurnoutId() {
        return turnoutId;
    }

    public void setTurnoutId(String turnoutId) {
        this.turnoutId = turnoutId;
    }
}
