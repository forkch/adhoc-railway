package ch.fork.adhocrailway.services;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;

import java.util.SortedSet;

public interface RouteServiceListener {

    void routesUpdated(SortedSet<RouteGroup> allRouteGroups);

    void routeRemoved(Route route);

    void routeAdded(Route route);

    void routeUpdated(Route route);

    void routeGroupAdded(RouteGroup routeGroup);

    void routeGroupRemoved(RouteGroup routeGroup);

    void routeGroupUpdated(RouteGroup routeGroup);

    void failure(AdHocServiceException routeManagerException);

}
