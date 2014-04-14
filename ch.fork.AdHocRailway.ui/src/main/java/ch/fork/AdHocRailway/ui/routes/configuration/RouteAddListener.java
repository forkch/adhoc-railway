package ch.fork.AdHocRailway.ui.routes.configuration;

import ch.fork.AdHocRailway.manager.RouteManagerListener;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;

import java.util.SortedSet;

public abstract class RouteAddListener implements RouteManagerListener {

    @Override
    public void routesUpdated(final SortedSet<RouteGroup> allRouteGroups) {

    }

    @Override
    public void routeRemoved(final Route route) {

    }

    @Override
    public void routeGroupAdded(final RouteGroup routeGroup) {

    }

    @Override
    public void routeGroupRemoved(final RouteGroup routeGroup) {

    }

    @Override
    public void routeGroupUpdated(final RouteGroup routeGroup) {

    }

}
