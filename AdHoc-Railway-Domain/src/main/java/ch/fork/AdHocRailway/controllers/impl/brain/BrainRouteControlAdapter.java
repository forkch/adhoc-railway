package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.RouteChangeListener;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;

public class BrainRouteControlAdapter extends RouteController {

	@Override
	public void enableRoute(Route r) throws RouteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void disableRoute(Route r) throws RouteException {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isRouteEnabled(Route route) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isRouting(Route route) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void toggle(Route route) throws RouteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void toggleTest(Route route) throws RouteException {
		// TODO Auto-generated method stub

	}

	@Override
	public void addOrUpdateRoute(Route route) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addRouteChangeListener(Route r, RouteChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeAllRouteChangeListeners() {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeRouteChangeListener(Route r, RouteChangeListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setRoutingDelay(int intValue) {
		// TODO Auto-generated method stub

	}

}
