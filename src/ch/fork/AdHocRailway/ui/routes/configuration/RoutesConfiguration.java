package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.routes.RouteGroupOld;
import ch.fork.AdHocRailway.domain.routes.RouteOld;

public class RoutesConfiguration {

    private Map<Integer, RouteOld> numberToRoute;
    private List<RouteGroupOld> routeGroups;
    
    public RoutesConfiguration(Map<Integer, RouteOld> numberToRoute, List<RouteGroupOld> routeGroups) {
        this.numberToRoute = numberToRoute;
        this.routeGroups = routeGroups;
    }

	public Map<Integer, RouteOld> getNumberToRoute() {
		return numberToRoute;
	}

	public List<RouteGroupOld> getRouteGroups() {
		return routeGroups;
	}
}