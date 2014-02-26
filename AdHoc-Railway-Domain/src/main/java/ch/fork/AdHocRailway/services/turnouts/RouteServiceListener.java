package ch.fork.AdHocRailway.services.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerException;

import java.util.SortedSet;

public interface RouteServiceListener {

    void routesUpdated(SortedSet<RouteGroup> allRouteGroups);

    void routeRemoved(Route route);

    void routeAdded(Route route);

    void routeUpdated(Route route);

    void routeGroupAdded(RouteGroup routeGroup);

    void routeGroupRemoved(RouteGroup routeGroup);

    void routeGroupUpdated(RouteGroup routeGroup);

    void failure(RouteManagerException routeManagerException);

}
