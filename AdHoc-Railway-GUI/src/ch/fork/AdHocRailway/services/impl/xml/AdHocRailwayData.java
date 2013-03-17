package ch.fork.AdHocRailway.services.impl.xml;

import java.util.List;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

public class AdHocRailwayData {

	private final List<LocomotiveGroup> locomotiveGroups;
	private final List<TurnoutGroup> turnoutGroups;

	private final List<RouteGroup> routeGroups;

	public AdHocRailwayData(List<LocomotiveGroup> locomotiveGroups,
			List<TurnoutGroup> turnoutGroups, List<RouteGroup> routeGroups) {
		super();
		this.locomotiveGroups = locomotiveGroups;
		this.turnoutGroups = turnoutGroups;
		this.routeGroups = routeGroups;
	}

	public List<LocomotiveGroup> getLocomotiveGroups() {
		return locomotiveGroups;
	}

	public List<TurnoutGroup> getTurnoutGroups() {
		return turnoutGroups;
	}

	public List<RouteGroup> getRouteGroups() {
		return routeGroups;
	}
}
