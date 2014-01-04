package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.controllers.impl.RouteChangingThread;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;

public class BrainRouteControlAdapter extends RouteController {

	private final TurnoutController turnoutControl;

	private long routingDelay;
    private final RouteChangingThread.RouteChangingListener routeChangingListener;

    public BrainRouteControlAdapter(final TurnoutController turnoutController) {
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
	public void enableRoute(final Route r) throws RouteException {
		final Thread brainRouterThread = new Thread(new RouteChangingThread(turnoutControl,r,
				true, routingDelay, routeChangingListener));
		brainRouterThread.start();
	}

	@Override
	public void disableRoute(final Route r) throws RouteException {
		final Thread brainRouterThread = new Thread(new RouteChangingThread(turnoutControl, r,
				false, routingDelay, routeChangingListener));
		brainRouterThread.start();
	}
	@Override
	public void setRoutingDelay(final int routingDelay) {
		this.routingDelay = routingDelay;
	}



}
