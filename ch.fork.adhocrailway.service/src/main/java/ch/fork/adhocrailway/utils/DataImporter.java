package ch.fork.adhocrailway.utils;

import ch.fork.adhocrailway.manager.LocomotiveManager;
import ch.fork.adhocrailway.manager.RouteManager;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;
import ch.fork.adhocrailway.model.turnouts.*;

import java.util.HashSet;
import java.util.SortedSet;

public class DataImporter {

    public void importLocomotives(
            final LocomotiveManager locomotiveManager,
            final SortedSet<LocomotiveGroup> groups) {

        locomotiveManager.clearToService();

        for (final LocomotiveGroup group : groups) {
            locomotiveManager.addLocomotiveGroup(group);
            for (Locomotive locomotive : group.getLocomotives()) {
                locomotive.setGroupId(group.getId());
            }
        }

        for (final LocomotiveGroup group : groups) {
            for (final Locomotive locomotive : group.getLocomotives()) {
                locomotiveManager.addLocomotiveToGroup(locomotive, group);
            }
        }
    }

    public void importTurnouts(TurnoutManager turnoutManager, SortedSet<TurnoutGroup> turnoutGroups) {
        turnoutManager.clearToService();

        for (final TurnoutGroup group : turnoutGroups) {
            turnoutManager.addTurnoutGroup(group);
        }

        for (final TurnoutGroup group : turnoutGroups) {
            for (Turnout turnout : new HashSet<>(group.getTurnouts())) {
                turnoutManager.addTurnoutToGroup(turnout, group);
            }
        }
    }

    public void importRoutes(RouteManager routeManager, SortedSet<RouteGroup> routeGroups) {
        routeManager.clearToService();
        for (final RouteGroup group : routeGroups) {
            routeManager.addRouteGroup(group);
            for (Route route : group.getRoutes()) {
                route.setGroupId(group.getId());
            }
        }

        for (final RouteGroup group : routeGroups) {
            for (Route route : new HashSet<>(group.getRoutes())) {
                routeManager.addRouteToGroup(route, group);
                for (RouteItem routeItem : new HashSet<>(route.getRoutedTurnouts())) {
                    if (routeItem.getTurnout() != null) {
                        routeManager.addRouteItemToGroup(routeItem, route);
                    }
                }
            }
        }
    }
}
