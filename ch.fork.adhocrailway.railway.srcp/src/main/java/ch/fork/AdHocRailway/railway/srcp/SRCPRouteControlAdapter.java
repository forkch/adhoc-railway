package ch.fork.AdHocRailway.railway.srcp;

import ch.fork.AdHocRailway.controllers.ControllerException;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteItem;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.routes.*;
import de.dermoba.srcp.model.turnouts.SRCPTurnout;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SRCPRouteControlAdapter extends RouteController implements
        SRCPRouteChangeListener {

    private final SRCPTurnoutControlAdapter turnoutControl;
    private final Map<Route, SRCPRoute> routesSRCPRoutesMap = new HashMap<Route, SRCPRoute>();

    private final Map<SRCPRoute, Route> SRCPRoutesRoutesMap = new HashMap<SRCPRoute, Route>();
    private final SRCPRouteControl routeControl;

    private Route routeTemp;

    private SRCPRoute sRouteTemp;

    public SRCPRouteControlAdapter(
            final SRCPTurnoutControlAdapter turnoutControl, final TurnoutManager turnoutManager) {
        super(turnoutManager);
        this.turnoutControl = turnoutControl;
        routeControl = SRCPRouteControl.getInstance();

    }

    @Override
    public void enableRoute(final Route route) {
        final SRCPRoute sRoute = getOrCreateSRCPRoute(route);
        try {
            route.setRouting(true);
            routeControl.enableRoute(sRoute);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Route Error", e);
        }
    }

    @Override
    public void disableRoute(final Route route) {
        final SRCPRoute sRoute = getOrCreateSRCPRoute(route);
        try {
            route.setRouting(true);
            routeControl.disableRoute(sRoute);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Route Error", e);
        }
    }

    @Override
    public void toggle(final Route route) {
        final SRCPRoute sRoute = getOrCreateSRCPRoute(route);
        try {
            routeControl.toggle(sRoute);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Route Error", e);
        }
    }

    @Override
    public void toggleTest(final Route route) {
        routesSRCPRoutesMap.remove(route);
        SRCPRoutesRoutesMap.remove(sRouteTemp);
        if (routeTemp == null || !routeTemp.equals(route)) {
            routeTemp = route;
            // just create a temporary SRCPTurnout
            sRouteTemp = new SRCPRoute();
        }
        applyNewSettings(sRouteTemp, route);
        routesSRCPRoutesMap.put(route, sRouteTemp);
        SRCPRoutesRoutesMap.put(sRouteTemp, route);
        try {
            routeControl.toggle(sRouteTemp);
        } catch (final SRCPModelException e) {
            throw new ControllerException("Route Error", e);
        }
    }

    private SRCPRoute createSRCPRoute(
            final SRCPTurnoutControlAdapter turnoutControl, SRCPRoute sRoute, final Route route) {
        for (final RouteItem routeItem : route.getRoutedTurnouts()) {

            final SRCPRouteItem sRouteItem = new SRCPRouteItem();
            final SRCPTurnout sTurnout = turnoutControl
                    .getOrCreateSRCPTurnout(routeItem.getTurnout());
            sRouteItem.setTurnout(sTurnout);
            switch (routeItem.getState()) {
                case LEFT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.LEFT);
                    break;
                case RIGHT:

                    sRouteItem.setRoutedState(SRCPRouteItemState.RIGHT);
                    break;
                case STRAIGHT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.STRAIGHT);
                    break;
                case DEFAULT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.DEFAULT);
                    break;
                case NON_DEFAULT:

                    sRouteItem.setRoutedState(SRCPRouteItemState.NON_DEFAULT);
                    break;
                default:
            }
            sRoute.addRouteItem(sRouteItem);
        }
        return sRoute;
    }

    @Override
    public void setRoutingDelay(final int routingDelay) {
        routeControl.setRoutingDelay(routingDelay);
    }

    @Override
    public void nextTurnoutDerouted(final SRCPRoute sRoute) {
        final Route route = SRCPRoutesRoutesMap.get(sRoute);
        informNextTurnoutDerouted(route);
    }

    @Override
    public void nextTurnoutRouted(final SRCPRoute sRoute) {
        final Route route = SRCPRoutesRoutesMap.get(sRoute);
        informNextTurnoutRouted(route);
    }

    @Override
    public void routeChanged(final SRCPRoute sRoute) {
        final Route route = SRCPRoutesRoutesMap.get(sRoute);

        route.setRouting(false);
        if (sRoute.getRouteState().equals(SRCPRouteState.ENABLED)) {
            route.setEnabled(true);
        } else if (sRoute.getRouteState().equals(SRCPRouteState.DISABLED)) {
            route.setEnabled(false);
        }
        informRouteChanged(route);
    }

    public void setSession(final SRCPSession session) {
        routeControl.addRouteChangeListener(this);
        routeControl.setSession(session);
    }

    private void applyNewSettings(final SRCPRoute sRoute, final Route route) {
        sRoute.getRouteItems().clear();
        for (final RouteItem routeItem : route.getRoutedTurnouts()) {

            final SRCPRouteItem sRouteItem = new SRCPRouteItem();
            int number = routeItem.getTurnout().getNumber();
            final SRCPTurnout sTurnout = turnoutControl
                    .getOrCreateSRCPTurnout(turnoutManager.getTurnoutByNumber(number));
            sRouteItem.setTurnout(sTurnout);
            switch (routeItem.getState()) {
                case LEFT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.LEFT);
                    break;
                case RIGHT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.RIGHT);
                    break;
                case STRAIGHT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.STRAIGHT);
                    break;
                case DEFAULT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.DEFAULT);
                    break;
                case NON_DEFAULT:
                    sRouteItem.setRoutedState(SRCPRouteItemState.NON_DEFAULT);
                    break;
                default:
                    sRouteItem.setRoutedState(SRCPRouteItemState.UNDEF);
            }
            sRoute.addRouteItem(sRouteItem);
        }
    }

    private SRCPRoute getOrCreateSRCPRoute(final Route route) {
        if (route == null) {
            throw new IllegalArgumentException("route must not be null");
        }
        SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
        if (sRoute == null) {
            sRoute = new SRCPRoute();
        }
        applyNewSettings(sRoute, route);
        routesSRCPRoutesMap.put(route, sRoute);
        SRCPRoutesRoutesMap.put(sRoute, route);
        return sRoute;
    }

    public void registerRoutes(final List<Route> allRoutes) {
        for (Route route : allRoutes) {
            getOrCreateSRCPRoute(route);
        }
    }
}
