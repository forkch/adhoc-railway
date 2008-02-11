package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;

public class SRCPRoute {
	
	private Route route;

	private RouteState routeState;

	private boolean routing;
	
	public SRCPRoute(Route route) {
		this.route = route;
	}

	public RouteState getRouteState() {
		return routeState;
	}

	protected void setRouteState(RouteState routeState) {
		this.routeState = routeState;
	}

	public void setChangingRoute(boolean changingRoute) {
		this.routing = changingRoute;
	}

	public boolean isRouting() {
		return routing;
	}
}
