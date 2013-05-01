package ch.fork.AdHocRailway.services.turnouts;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;

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
