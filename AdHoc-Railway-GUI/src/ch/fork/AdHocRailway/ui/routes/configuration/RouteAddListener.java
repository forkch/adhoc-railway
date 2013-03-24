package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManagerListener;

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
