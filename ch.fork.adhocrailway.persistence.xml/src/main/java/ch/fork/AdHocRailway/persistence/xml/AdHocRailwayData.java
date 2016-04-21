package ch.fork.AdHocRailway.persistence.xml;

import java.util.Set;
import java.util.SortedSet;

import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;

import com.google.common.collect.Sets;

public class AdHocRailwayData {

    private Set<LocomotiveGroup> locomotiveGroups = Sets.newHashSet();
    private Set<TurnoutGroup> turnoutGroups = Sets.newHashSet();
    private Set<RouteGroup> routeGroups = Sets.newHashSet();

    public AdHocRailwayData() {
    }

    public AdHocRailwayData(final Set<LocomotiveGroup> locomotiveGroups,
                            final Set<TurnoutGroup> turnoutGroups,
                            final Set<RouteGroup> routeGroups) {
        super();
        this.locomotiveGroups = locomotiveGroups;
        this.turnoutGroups = turnoutGroups;
        this.routeGroups = routeGroups;
        if (locomotiveGroups == null) {
            this.locomotiveGroups = Sets.newTreeSet();
        }
        if (turnoutGroups == null) {
            this.turnoutGroups = Sets.newTreeSet();
        }
        if (routeGroups == null) {
            this.routeGroups = Sets.newTreeSet();
        }

    }

    public SortedSet<LocomotiveGroup> getLocomotiveGroups() {
        return Sets.newTreeSet(locomotiveGroups);
    }

    public SortedSet<TurnoutGroup> getTurnoutGroups() {
        return Sets.newTreeSet(turnoutGroups);
    }

    public SortedSet<RouteGroup> getRouteGroups() {
        return Sets.newTreeSet(routeGroups);
    }

    public void cleanup() {
        if (locomotiveGroups == null) {
            this.locomotiveGroups = Sets.newTreeSet();
        }
        if (turnoutGroups == null) {
            this.turnoutGroups = Sets.newTreeSet();
        }
        if (routeGroups == null) {
            this.routeGroups = Sets.newTreeSet();
        }
    }
}
