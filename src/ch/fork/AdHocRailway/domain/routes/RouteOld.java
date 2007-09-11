
package ch.fork.AdHocRailway.domain.routes;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class RouteOld implements Comparable {

    private SortedSet<RouteItemOld> routeItems;
    private String          name;
    private int             number;
    private RouteState		routeState;
    private boolean         changeingRoute = false;
    public enum RouteState {
        ENABLED, DISABLED
    };


    public RouteOld(String name, int number) {
        this.name = name;
        this.number = number;
        routeItems = new TreeSet<RouteItemOld>();
    }

    public void addRouteItem(RouteItemOld item) {
        routeItems.add(item);
    }

    public void removeRouteItem(RouteItemOld item) {
        routeItems.remove(item);
    }

    public Set<RouteItemOld> getRouteItems() {
        return routeItems;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean equals(Object o) {
        if (o instanceof RouteOld) {
            RouteOld route = (RouteOld) o;
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
        RouteOld newRoute = new RouteOld(name, number);
        for (RouteItemOld origItem : routeItems) {
            newRoute.addRouteItem((RouteItemOld) origItem.clone());
        }
        return newRoute;
    }

	public int compareTo(Object o) {
		if (o instanceof RouteOld) {
			RouteOld r = (RouteOld) o;
			if (number < r.getNumber()) {
                return -1;
            } else if (number > r.getNumber()) {
                return 1;
            } else {
                return 0;
            }
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
