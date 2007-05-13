package ch.fork.AdHocRailway.domain.routes;

import java.util.Collection;

import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class Router extends Thread {

    private Route               route;
    private boolean             enableRoute;
    private int                 waitTime;
    private RouteChangeListener listener;
    private SwitchException     switchException;

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
        } catch (SwitchException e) {
            this.switchException = e;
            ExceptionProcessor.getInstance().processException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disableRoute() throws SwitchException, InterruptedException {
        Collection<RouteItem> routeItems = route.getRouteItems();
        SwitchControl sc = SwitchControl.getInstance();
        for (RouteItem ri : routeItems) {
            Switch switchToRoute = ri.getRoutedSwitch();

            /*switch (switchToRoute.getDefaultState()) {
            case STRAIGHT:
                sc.setStraight(switchToRoute);
                break;
            case LEFT:
                sc.setCurvedLeft(switchToRoute);
                break;
            case RIGHT:
                sc.setCurvedRight(switchToRoute);
                break;
            }
            */
            sc.setDefaultState(switchToRoute);
            listener.nextSwitchDerouted();
            Thread.sleep(waitTime);
        }
        route.setRouteState(RouteState.DISABLED);
        listener.routeChanged(route);
    }

    private void enableRoute() throws SwitchException, InterruptedException {
        Collection<RouteItem> routeItems = route.getRouteItems();
        SwitchControl sc = SwitchControl.getInstance();
        for (RouteItem ri : routeItems) {
            Switch switchToRoute = ri.getRoutedSwitch();
            ri.setPreviousSwitchState(switchToRoute.getSwitchState());
            switch (ri.getRoutedSwitchState()) {
            case STRAIGHT:
                sc.setStraight(switchToRoute);
                break;
            case LEFT:
                sc.setCurvedLeft(switchToRoute);
                break;
            case RIGHT:
                sc.setCurvedRight(switchToRoute);
                break;
            }
            listener.nextSwitchRouted();
            Thread.sleep(waitTime);
        }
        route.setRouteState(RouteState.ENABLED);
        listener.routeChanged(route);
    }

    public SwitchException getSwitchException() {
        return switchException;
    }
}
