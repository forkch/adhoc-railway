package ch.fork.AdHocRailway.domain.routes;

import java.util.Collection;

import ch.fork.AdHocRailway.domain.routes.RouteOld.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControl;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

import com.sun.java.util.jar.pack.Instruction.Switch;

public class Router extends Thread {

    private RouteOld               route;
    private boolean             enableRoute;
    private int                 waitTime;
    private RouteChangeListener listener;
    private TurnoutException     switchException;

    public Router(RouteOld route, boolean enableRoute, int waitTime,
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
        Collection<RouteItemOld> routeItems = route.getRouteItems();
        TurnoutControl sc = TurnoutControl.getInstance();
        for (RouteItemOld ri : routeItems) {
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

    private void enableRoute() throws TurnoutException, InterruptedException {
        Collection<RouteItemOld> routeItems = route.getRouteItems();
        TurnoutControl sc = TurnoutControl.getInstance();
        for (RouteItemOld ri : routeItems) {
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

    public TurnoutException getSwitchException() {
        return switchException;
    }
}
