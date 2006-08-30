
package ch.fork.AdHocRailway.domain.routes;

import java.util.List;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;

public class Router extends Thread {

    private Route   r;
    private boolean enableRoute;
    private int waitTime;
    private SwitchException switchException;

    public Router(Route route, boolean enableRoute, int waitTime) {
        this.r = route;
        this.enableRoute = enableRoute;
        this.waitTime = waitTime;
    }

    public void run() {
        try {
            if (enableRoute) {
                enableRoute();
            } else {
                disableRoute();
            }
        } catch (SwitchException e) {
            this.switchException = e;
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void disableRoute() throws SwitchException, InterruptedException {
        List<RouteItem> routeItems = r.getRouteItems();
        SwitchControl sc = SwitchControl.getInstance();
        for (RouteItem ri : routeItems) {
            Switch switchToRoute = ri.getRoutedSwitch();

            switch (ri.getPreviousSwitchState()) {
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
            //System.out.println("Switch: " + switchToRoute.getNumber() + " derouted");
            Thread.sleep(waitTime);
        }
        r.setEnabled(false);
    }

    private void enableRoute() throws SwitchException, InterruptedException {
        List<RouteItem> routeItems = r.getRouteItems();
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
            //System.out.println("Switch: " + switchToRoute.getNumber() + " routed");
            Thread.sleep(waitTime);
        }
        r.setEnabled(true);
    }

    public SwitchException getSwitchException() {
        return switchException;
    }
}
