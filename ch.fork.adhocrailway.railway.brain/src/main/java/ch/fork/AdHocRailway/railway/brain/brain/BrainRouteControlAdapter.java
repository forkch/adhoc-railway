package ch.fork.AdHocRailway.railway.brain.brain;

import ch.fork.AdHocRailway.controllers.RouteChangingThread;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Route;

public class BrainRouteControlAdapter extends RouteController {

    private final TurnoutController turnoutControl;
    private final RouteChangingThread.RouteChangingListener routeChangingListener;
    private long routingDelay;

    public BrainRouteControlAdapter(final TurnoutController turnoutController, TurnoutManager turnoutManager) {
        super(turnoutManager);
        this.turnoutControl = turnoutController;
        routeChangingListener = new RouteChangingThread.RouteChangingListener() {
            @Override
            public void informNextTurnoutRouted(Route route) {
                BrainRouteControlAdapter.this.informNextTurnoutRouted(route);
            }

            @Override
            public void informNextTurnoutDerouted(Route route) {
                BrainRouteControlAdapter.this.informNextTurnoutDerouted(route);

            }

            @Override
            public void informRouteChanged(Route route) {
                BrainRouteControlAdapter.this.informRouteChanged(route);
            }
        };
    }

    @Override
    public void enableRoute(final Route r) {
        final Thread brainRouterThread = new Thread(new RouteChangingThread(turnoutControl, r,
                true, routingDelay, routeChangingListener));
        brainRouterThread.start();
    }

    @Override
    public void disableRoute(final Route r) {
        final Thread brainRouterThread = new Thread(new RouteChangingThread(turnoutControl, r,
                false, routingDelay, routeChangingListener));
        brainRouterThread.start();
    }

    @Override
    public void setRoutingDelay(final int routingDelay) {
        this.routingDelay = routingDelay;
    }


}
