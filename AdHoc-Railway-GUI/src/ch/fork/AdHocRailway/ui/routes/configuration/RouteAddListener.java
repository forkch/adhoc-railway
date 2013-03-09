package ch.fork.AdHocRailway.ui.routes.configuration;

import java.util.List;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteManagerListener;

public abstract class RouteAddListener implements RouteManagerListener {

	@Override
	public void routesUpdated(List<RouteGroup> allRouteGroups) {

	}

	@Override
	public void routeRemoved(Route route) {

	}

	@Override
	public void routeGroupAdded(RouteGroup routeGroup) {

	}

	@Override
	public void routeGroupRemoved(RouteGroup routeGroup) {

	}

	@Override
	public void routeGroupUpdated(RouteGroup routeGroup) {

	}

}
