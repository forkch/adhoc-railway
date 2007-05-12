
package ch.fork.AdHocRailway.domain.routes;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class Route implements Comparable {

    private SortedSet<RouteItem> routeItems;
    private String          name;
    private int             number;
    private RouteState		routeState;
    private boolean         changeingRoute = false;
    public enum RouteState {
        ENABLED, DISABLED
    };


    public Route(String name, int number) {
        this.name = name;
        this.number = number;
        routeItems = new TreeSet<RouteItem>();
    }

    public void addRouteItem(RouteItem item) {
        routeItems.add(item);
    }

    public void removeRouteItem(RouteItem item) {
        routeItems.remove(item);
    }

    public Set<RouteItem> getRouteItems() {
        return routeItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o instanceof Route) {
            Route route = (Route) o;
            return route.getName().equals(name);
        }
        return false;
    }
    
    public int hashCode() {
        return name.hashCode();
    }

    public RouteState getRouteState() {
        return this.routeState;
    }

    protected void setRouteState(RouteState routeState) {
        this.routeState = routeState;
    }
    
    public boolean isChangeingRoute() {
        return changeingRoute;
    }

    
    protected void setChangeingRoute(boolean routing) {
        this.changeingRoute = routing;
    }

    public String toString() {
        /*
        String output = name + ": {";
        for (RouteItem r : routeItems) {
            output += " " + r + " ,";
        }
        return output += "}";
        */
        return name;
    }

    public Object clone() {
        Route newRoute = new Route(name, number);
        for (RouteItem origItem : routeItems) {
            newRoute.addRouteItem((RouteItem) origItem.clone());
        }
        return newRoute;
    }

	public int compareTo(Object o) {
		if (o instanceof Route) {
			Route r = (Route) o;
			return name.compareTo(r.getName());
		}
		return -1;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}
}
