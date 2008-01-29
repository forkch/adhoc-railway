package ch.fork.AdHocRailway.domain.routes;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class Router extends Thread {

    private Route               route;
    private boolean             enableRoute;
    private int                 waitTime;
    private RouteChangeListener listener;
    private TurnoutException     switchException;

    public Router(Route route, boolean enableRoute, int waitTime,
        RouteChangeListener listener) {
        this.route = route;
        this.enableRoute = enableRoute;
        this.waitTime = waitTime;
        this.listener = listener;
    }

    public void run() {
        try {
            route.setChangeingRoute(true);
            if (enableRoute) {
                enableRoute();
            } else {
                disableRoute();
            }
            route.setChangeingRoute(false);
        } catch (TurnoutException e) {
            this.switchException = e;
            ExceptionProcessor.getInstance().processException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disableRoute() throws TurnoutException, InterruptedException {
        SortedSet<RouteItem> routeItems = route.getRouteItems();
        TurnoutControlIface sc = SRCPTurnoutControl.getInstance();
        for (RouteItem ri : routeItems) {
            Turnout turnoutToRoute = ri.getTurnout();

            sc.setDefaultState(turnoutToRoute);
            listener.nextSwitchDerouted();
            Thread.sleep(waitTime);
        }
        route.setRouteState(RouteState.DISABLED);
        listener.routeChanged(route);
    }

    private void enableRoute() throws TurnoutException, InterruptedException {
    	SortedSet<RouteItem> routeItems = route.getRouteItems();
        TurnoutControlIface sc = SRCPTurnoutControl.getInstance();
        for (RouteItem ri : routeItems) {
        	Turnout turnoutToRoute = ri.getTurnout();
            switch (ri.getRoutedStateEnum()) {
            case STRAIGHT:
                sc.setStraight(turnoutToRoute);
                break;
            case LEFT:
                sc.setCurvedLeft(turnoutToRoute);
                break;
            case RIGHT:
                sc.setCurvedRight(turnoutToRoute);
                break;
            }
            listener.nextSwitchRouted();
            Thread.sleep(waitTime);
        }
        route.setRouteState(RouteState.ENABLED);
        listener.routeChanged(route);
    }

    public TurnoutException getSwitchException() {
        return switchException;
    }
}
