package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.List;

import ch.fork.AdHocRailway.domain.routes.Route;

public class RoutesConfiguration {

    private List<Route> routes;
    
    public RoutesConfiguration(List<Route> routes) {
        this.routes = routes;
    }

    public List<Route> getRoutes() {
        return routes;
    }
}
