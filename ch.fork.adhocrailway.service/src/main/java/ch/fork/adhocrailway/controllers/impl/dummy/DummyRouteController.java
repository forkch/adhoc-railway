package ch.fork.adhocrailway.controllers.impl.dummy;

import ch.fork.adhocrailway.controllers.RouteChangingThread;
import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.model.turnouts.Route;

/**
 * Created by bmu on 24.03.2014.
 */
public class DummyRouteController extends RouteController {
    private final RouteChangingThread.RouteChangingListener routeChangingListener;
    private final TurnoutController turnoutControl;
    private int routingDelay;

    public DummyRouteController(final TurnoutController turnoutControl, TurnoutManager turnoutManager) {
        super(turnoutManager);
        this.turnoutControl = turnoutControl;
        routeChangingListener = new RouteChangingThread.RouteChangingListener() {
            @Override
            public void informNextTurnoutRouted(final Route route) {
                DummyRouteController.super.informNextTurnoutRouted(route);
            }

            @Override
            public void informNextTurnoutDerouted(final Route route) {
                DummyRouteController.super.informNextTurnoutDerouted(route);
            }

            @Override
            public void informRouteChanged(final Route route) {
                DummyRouteController.super.informRouteChanged(route);
            }
        };
    }

    @Override
    public void enableRoute(final Route r) {
        final Thread brainRouterThread = new Thread(
                new RouteChangingThread(turnoutControl, r, true,
                        routingDelay, routeChangingListener)
        );
        brainRouterThread.start();
    }

    @Override
    public void disableRoute(final Route r) {

        final Thread brainRouterThread = new Thread(
                new RouteChangingThread(turnoutControl, r, false,
                        routingDelay, routeChangingListener)
        );
        brainRouterThread.start();
    }

    @Override
    public void setRoutingDelay(final int routingDelay) {

        this.routingDelay = routingDelay;
    }
}
