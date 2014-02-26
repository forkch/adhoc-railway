package ch.fork.AdHocRailway.manager.impl.turnouts.events;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import com.google.common.collect.Sets;

import java.util.SortedSet;

public class RoutesUpdatedEvent {

    private final SortedSet<RouteGroup> updatedRouteGroups;

    public RoutesUpdatedEvent(final SortedSet<RouteGroup> updatedRouteGroups) {
        this.updatedRouteGroups = updatedRouteGroups;
    }

    public SortedSet<Route> getAllRoutes() {
        final SortedSet<Route> allRoutes = Sets.newTreeSet();
        for (final RouteGroup routeGroup : updatedRouteGroups) {
            allRoutes.addAll(routeGroup.getRoutes());
        }
        return allRoutes;
    }
}
