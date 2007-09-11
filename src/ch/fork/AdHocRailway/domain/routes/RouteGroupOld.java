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


package ch.fork.AdHocRailway.domain.routes;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class RouteGroupOld {
    private SortedSet<RouteOld> routes;
    private String            name;

    public RouteGroupOld(String name) {
        this.name = name;
        routes = new TreeSet<RouteOld>();
    }

    public void addRoute(RouteOld aSwitch) {
        routes.add(aSwitch);
    }

    public void removeRoute(RouteOld aSwitch) {
        routes.remove(aSwitch);
    }

    public void replaceRoute(RouteOld oldSwitch, RouteOld newSwitch) {
        routes.remove(oldSwitch);
        routes.add(newSwitch);
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

    public Set<RouteOld> getRoutes() {
        return routes;
    }

    public RouteGroupOld clone() {
        RouteGroupOld newSwitchGroup = new RouteGroupOld(name);
        for(RouteOld s : getRoutes()) {
        	newSwitchGroup.addRoute(s);
        }
        return newSwitchGroup;
    }

    public boolean equals(Object o) {
        if (o instanceof RouteGroupOld) {
            RouteGroupOld rg = (RouteGroupOld) o;
            return rg.getName().equals(name);
        }
        return false;
    }

}
