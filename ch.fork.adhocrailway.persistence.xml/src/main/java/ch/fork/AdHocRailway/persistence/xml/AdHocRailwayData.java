package ch.fork.AdHocRailway.persistence.xml;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;

import java.util.SortedSet;

public class AdHocRailwayData {

    private SortedSet<LocomotiveGroup> locomotiveGroups;
    private SortedSet<TurnoutGroup> turnoutGroups;
    private SortedSet<RouteGroup> routeGroups;

    public AdHocRailwayData() {

    }
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
