/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Turnout.java 308 2013-05-01 15:43:50Z fork_ch $
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
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import org.apache.commons.lang3.builder.*;

import java.beans.PropertyChangeListener;
import java.util.HashSet;
import java.util.Set;

public class Turnout extends AbstractItem implements java.io.Serializable,
        Comparable<Turnout> {

    @Expose
    private String id = null;

    @Expose
    private int number;

    @Expose
    private String description;

    @Expose
    private TurnoutType type;

    @Expose
    private TurnoutState defaultState;

    @Expose
    private TurnoutOrientation orientation;

    @Expose
    private int bus1;

    @Expose
    private int address1;

    @Expose
    private boolean address1Switched;

    @Expose
    private int bus2;

    @Expose
    private int address2;

    @Expose
    private boolean address2Switched;

    @XStreamAsAttribute
    @Expose
    private String groupId;

    public static final String PROPERTYNAME_NUMBER = "number";
    public static final String PROPERTYNAME_DESCRIPTION = "description";
    public static final String PROPERTYNAME_TURNOUT_TYPE = "type";
    public static final String PROPERTYNAME_DEFAULT_STATE = "defaultState";
    public static final String PROPERTYNAME_ORIENTATION = "orientation";
    public static final String PROPERTYNAME_BUS1 = "bus1";
    public static final String PROPERTYNAME_ADDRESS1 = "address1";
    public static final String PROPERTYNAME_ADDRESS1_SWITCHED = "address1Switched";
    public static final String PROPERTYNAME_BUS2 = "bus2";
    public static final String PROPERTYNAME_ADDRESS2 = "address2";
    public static final String PROPERTYNAME_ADDRESS2_SWITCHED = "address2Switched";
    public static final String PROPERTYNAME_ROUTE_ITEMS = "routeItems";
    public static final String PROPERTYNAME_TURNOUT_GROUP = "turnoutGroup";

    private transient Set<RouteItem> routeItems = new HashSet<RouteItem>();
    private transient TurnoutGroup turnoutGroup;
    private transient TurnoutState actualState = TurnoutState.UNDEF;


    public Turnout() {

    }

    @Override
    public void init() {
        super.init();
        actualState = TurnoutState.UNDEF;
    }

    public String getId() {
        return id;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public int getNumber() {
        return this.number;
    }

    public void setNumber(final int number) {
        final int old = this.number;
        this.number = number;
        changeSupport.firePropertyChange(PROPERTYNAME_NUMBER, old, this.number);
    }

    public TurnoutType getType() {
        return this.type;
    }

    public void setType(final TurnoutType type) {
        final TurnoutType old = this.type;
        this.type = type;
        changeSupport.firePropertyChange(PROPERTYNAME_TURNOUT_TYPE, old,
                this.type);
    }

    public String getDescription() {
        return this.description;
    }

    public void setDescription(final String description) {
        final String old = this.description;
        this.description = description;
        changeSupport.firePropertyChange(PROPERTYNAME_DESCRIPTION, old,
                this.description);
    }

    public TurnoutState getDefaultState() {
        return this.defaultState;
    }

    public void setDefaultState(final TurnoutState defaultState) {
        final TurnoutState old = this.defaultState;
        this.defaultState = defaultState;
        changeSupport.firePropertyChange(PROPERTYNAME_DEFAULT_STATE, old,
                this.defaultState);
    }

    public TurnoutOrientation getOrientation() {
        return this.orientation;
    }

    public void setOrientation(final TurnoutOrientation orientation) {
        final TurnoutOrientation old = this.orientation;
        this.orientation = orientation;
        changeSupport.firePropertyChange(PROPERTYNAME_ORIENTATION, old,
                this.orientation);
    }

    public int getBus1() {
        return this.bus1;
    }

    public void setBus1(final int bus1) {
        final int old = this.bus1;
        this.bus1 = bus1;
        changeSupport.firePropertyChange(PROPERTYNAME_BUS1, old, this.bus1);
    }

    public int getAddress1() {
        return this.address1;
    }

    public void setAddress1(final int address1) {
        final int old = this.address1;
        this.address1 = address1;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS1, old,
                this.address1);
    }

    public boolean isAddress1Switched() {
        return this.address1Switched;
    }

    public void setAddress1Switched(final boolean address1Switched) {
        final boolean old = this.address1Switched;
        this.address1Switched = address1Switched;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS1_SWITCHED, old,
                this.address1Switched);
    }

    public int getBus2() {
        return this.bus2;
    }

    public void setBus2(final int bus2) {
        final int old = this.bus2;
        this.bus2 = bus2;
        changeSupport.firePropertyChange(PROPERTYNAME_BUS2, old, this.bus2);
    }

    public int getAddress2() {
        return this.address2;
    }

    public void setAddress2(final int address2) {
        final int old = this.address2;
        this.address2 = address2;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS2, old,
                this.address2);
    }

    public boolean isAddress2Switched() {
        return this.address2Switched;
    }

    public void setAddress2Switched(final boolean address2Switched) {
        final boolean old = this.address2Switched;
        this.address2Switched = address2Switched;
        changeSupport.firePropertyChange(PROPERTYNAME_ADDRESS2_SWITCHED, old,
                this.address2Switched);
    }

    public boolean isDefaultLeft() {
        return getType() == TurnoutType.DEFAULT_LEFT;
    }

    public boolean isDefaultRight() {
        return getType() == TurnoutType.DEFAULT_RIGHT;
    }

    public boolean isDoubleCross() {
        return getType() == TurnoutType.DOUBLECROSS;
    }

    public boolean isThreeWay() {
        return getType() == TurnoutType.THREEWAY;
    }

    public boolean isCutter() {
        return getType() == TurnoutType.CUTTER;
    }

    public TurnoutGroup getTurnoutGroup() {
        return this.turnoutGroup;
    }

    public void setTurnoutGroup(final TurnoutGroup turnoutGroup) {
        final TurnoutGroup old = this.turnoutGroup;
        this.turnoutGroup = turnoutGroup;
        changeSupport.firePropertyChange(PROPERTYNAME_TURNOUT_GROUP, old,
                this.turnoutGroup);
    }

    public Set<RouteItem> getRouteItems() {
        return this.routeItems;
    }

    public void setRouteItems(final Set<RouteItem> routeItems) {
        final Set<RouteItem> old = this.routeItems;
        this.routeItems = routeItems;
        changeSupport.firePropertyChange(PROPERTYNAME_ROUTE_ITEMS, old,
                this.orientation);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(id).hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        Turnout rhs = (Turnout) obj;
        return new EqualsBuilder().append(id, rhs.getId()).build().booleanValue();
    }

    @Override
    public int compareTo(final Turnout o) {
        return new CompareToBuilder().append(number, o.getNumber()).build();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append(number).append(type).append(bus1).append(address1).append(address1Switched).append(bus2).append(address2).append(address2Switched).build();
    }

    public void addPropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.addPropertyChangeListener(x);
    }

    public void removePropertyChangeListener(final PropertyChangeListener x) {
        changeSupport.removePropertyChangeListener(x);
    }

    public TurnoutState getActualState() {
        return actualState;
    }

    public void setActualState(final TurnoutState actualState) {
        this.actualState = actualState;
    }

    public TurnoutState getToggledState() {
        if (TurnoutType.THREEWAY.equals(getType())) {
            if (TurnoutState.STRAIGHT.equals(actualState)) {
                return TurnoutState.RIGHT;
            }
            if (TurnoutState.LEFT.equals(actualState)) {
                return TurnoutState.STRAIGHT;
            }
            if (TurnoutState.RIGHT.equals(actualState)) {
                return TurnoutState.LEFT;
            }
        } else {
            if (TurnoutState.STRAIGHT.equals(actualState)) {
                return TurnoutState.LEFT;
            }
            if (TurnoutState.LEFT.equals(actualState) || TurnoutState.RIGHT.equals(actualState)) {
                return TurnoutState.STRAIGHT;
            }
        }
        if (TurnoutState.UNDEF.equals(actualState)) {
            return TurnoutState.STRAIGHT;
        }
        return TurnoutState.UNDEF;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getGroupId() {
        return groupId;
    }
}
