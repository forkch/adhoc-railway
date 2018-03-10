package ch.fork.AdHocRailway.controllers;

import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteItem;
import ch.fork.AdHocRailway.model.turnouts.Turnout;

/**
 * Created by fork on 1/2/14.
 */
public class RouteChangingThread implements Runnable, TurnoutChangeListener {

    private final Route route;
    private final boolean enable;
    private TurnoutController turnoutControl;
    private long routingDelay;
    private RouteChangingListener listener;

    public RouteChangingThread(TurnoutController turnoutControl, final Route route, final boolean enable, long routingDelay, RouteChangingListener listener) {
        this.turnoutControl = turnoutControl;
        this.route = route;
        this.enable = enable;
        this.routingDelay = routingDelay;
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            route.setRouting(true);
            turnoutControl.addGeneralTurnoutChangeListener(this);
            for (final RouteItem routeItem : route.getRoutedTurnouts()) {
                final Turnout turnout = routeItem.getTurnout();
                if (enable) {
                    switch (routeItem.getState()) {
                        case LEFT:
                            turnoutControl.setCurvedLeft(turnout);
                            break;
                        case RIGHT:
                            turnoutControl.setCurvedRight(turnout);
                            break;
                        case STRAIGHT:
                            turnoutControl.setStraight(turnout);
                            break;

                        case DEFAULT:
                            turnoutControl.setDefaultState(turnout);
                            break;
                        case NON_DEFAULT:
                            turnoutControl.setNonDefaultState(turnout);
                            break;
                        default:
                            throw new IllegalStateException(
                                    "routed state must be one of LEFT, RIGHT or STRAIGHT");

                    }
                } else {
                    turnoutControl.setDefaultState(turnout);
                }
                Thread.sleep(routingDelay);

            }
            turnoutControl.removeGeneralTurnoutChangeListener(this);
            route.setEnabled(enable);
            route.setRouting(false);
            listener.informRouteChanged(route);
        } catch (final ControllerException e) {
            e.printStackTrace();
        } catch (final InterruptedException e) {
            e.printStackTrace();
        } finally {
            route.setRouting(false);
        }
    }

    @Override
    public void turnoutChanged(final Turnout changedTurnout) {
        if (enable) {
            listener.informNextTurnoutRouted(route);
        } else {
            listener.informNextTurnoutDerouted(route);
        }
    }

    public interface RouteChangingListener {
        void informNextTurnoutRouted(Route route);

        void informNextTurnoutDerouted(Route route);

        void informRouteChanged(Route route);
    }
}
