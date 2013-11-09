package ch.fork.AdHocRailway.services.impl.xml;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

import java.util.SortedSet;

public class AdHocRailwayData {

	private final SortedSet<LocomotiveGroup> locomotiveGroups;
	private final SortedSet<TurnoutGroup> turnoutGroups;
	private final SortedSet<RouteGroup> routeGroups;

	public AdHocRailwayData(final SortedSet<LocomotiveGroup> locomotiveGroups,
			final SortedSet<TurnoutGroup> turnoutGroups,
			final SortedSet<RouteGroup> routeGroups) {
		super();
		this.locomotiveGroups = locomotiveGroups;
		this.turnoutGroups = turnoutGroups;
		this.routeGroups = routeGroups;
	}

	public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
		return locomotiveGroups;
	}

	public SortedSet<TurnoutGroup> getTurnoutGroups() {
		return turnoutGroups;
	}

	public SortedSet<RouteGroup> getRouteGroups() {
		return routeGroups;
	}
}
