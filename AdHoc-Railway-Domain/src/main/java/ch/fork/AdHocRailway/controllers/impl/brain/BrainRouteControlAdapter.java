package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutChangeListener;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public class BrainRouteControlAdapter extends RouteController {

	private final TurnoutController turnoutControl;

	private long routingDelay;

	public BrainRouteControlAdapter(final TurnoutController turnoutController) {
		this.turnoutControl = turnoutController;
	}

	@Override
	public void enableRoute(final Route r) throws RouteException {
		final Thread brainRouterThread = new Thread(new BrainRouterThread(r,
				true));
		brainRouterThread.start();
	}

	@Override
	public void disableRoute(final Route r) throws RouteException {
		final Thread brainRouterThread = new Thread(new BrainRouterThread(r,
				false));
		brainRouterThread.start();
	}

	@Override
	public void toggle(final Route route) throws RouteException {
		if (route.isEnabled()) {
			disableRoute(route);
		} else {
			enableRoute(route);
		}
	}

	@Override
	public void toggleTest(final Route route) throws RouteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrUpdateRoute(final Route route) {

	}

	@Override
	public void setRoutingDelay(final int routingDelay) {
		this.routingDelay = routingDelay;
	}

	class BrainRouterThread implements Runnable, TurnoutChangeListener {

		private final Route route;
		private final boolean enable;

		public BrainRouterThread(final Route route, final boolean enable) {
			this.route = route;
			this.enable = enable;
		}

		@Override
		public void run() {
			try {
				route.setRouting(true);
				turnoutControl.addTurnoutChangeListener(this);
				for (final RouteItem routeItem : route.getRouteItems()) {
					final Turnout turnout = routeItem.getTurnout();
					if (enable) {
						switch (routeItem.getRoutedState()) {
						case LEFT:
							turnoutControl.setCurvedLeft(turnout);

							break;
						case RIGHT:
							turnoutControl.setCurvedRight(turnout);
							break;
						case STRAIGHT:
							turnoutControl.setStraight(turnout);
							break;
						case UNDEF:
						default:
							throw new IllegalStateException(
									"routed state must be one of LEFT, RIGHT or STRAIGHT");

						}
					} else {
						turnoutControl.setDefaultState(turnout);
					}
					Thread.sleep(routingDelay);

				}
				turnoutControl.removeTurnoutChangeListener(this);
				route.setEnabled(enable);
				route.setRouting(false);
				informRouteChanged(route);
			} catch (final TurnoutException e) {
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
				informNextTurnoutRouted(route);
			} else {
				informNextTurnoutDerouted(route);
			}
		}
	}

}
