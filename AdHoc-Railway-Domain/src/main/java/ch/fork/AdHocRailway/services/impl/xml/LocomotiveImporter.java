package ch.fork.AdHocRailway.services.impl.xml;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.turnouts.*;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;

public class LocomotiveImporter {

    public void importLocomotives(
            final LocomotiveManager locomotiveManager,
            final SortedSet<LocomotiveGroup> groups) {

        locomotiveManager.clearToService();

        for (final LocomotiveGroup group : groups) {
            locomotiveManager.addLocomotiveGroup(group);
        }

        for (final LocomotiveGroup group : groups) {
            for (final Locomotive locomotive : group.getLocomotives()) {
                locomotiveManager.addLocomotiveToGroup(locomotive, group);
            }
        }
    }

    public void importTurnouts(TurnoutManager turnoutManager, SortedSet<TurnoutGroup> turnoutGroups) {
        turnoutManager.clearToService();

        for(final TurnoutGroup group : turnoutGroups) {
            turnoutManager.addTurnoutGroup(group);
        }

        for(final TurnoutGroup group : turnoutGroups) {
           for(Turnout turnout : group.getTurnouts()) {
               turnoutManager.addTurnoutToGroup(turnout,group);
           }
        }
    }

    public void importRoutes(RouteManager routeManager, SortedSet<RouteGroup> routeGroups) {
        for(final RouteGroup group : routeGroups) {
            routeManager.addRouteGroup(group);
        }

        for(final RouteGroup group : routeGroups) {
            for(Route route : group.getRoutes()) {
                routeManager.addRouteToGroup(route,group);
                for(RouteItem routeItem : route.getRouteItems()) {
                    routeManager.addRouteItem(routeItem);
                }
            }
        }
    }
}
