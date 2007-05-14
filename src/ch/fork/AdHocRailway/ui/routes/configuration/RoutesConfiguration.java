package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;

public class RoutesConfiguration {

    private Map<Integer, Route> numberToRoute;
    private List<RouteGroup> routeGroups;
    
    public RoutesConfiguration(Map<Integer, Route> numberToRoute, List<RouteGroup> routeGroups) {
        this.numberToRoute = numberToRoute;
        this.routeGroups = routeGroups;
    }

	public Map<Integer, Route> getNumberToRoute() {
		return numberToRoute;
	}

	public List<RouteGroup> getRouteGroups() {
		return routeGroups;
	}
}