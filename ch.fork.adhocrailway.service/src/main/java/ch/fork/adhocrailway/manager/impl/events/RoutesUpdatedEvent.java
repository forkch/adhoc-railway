package ch.fork.adhocrailway.manager.impl.events;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
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
