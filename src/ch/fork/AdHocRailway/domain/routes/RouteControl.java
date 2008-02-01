package ch.fork.AdHocRailway.domain.routes;

import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class RouteControl extends Control {

	private static RouteControl instance;
	
	private RoutePersistenceIface persistence = HibernateRoutePersistence.getInstance();

	private Map<Route, RouteChangeListener> listeners;

	private RouteState lastRouteState;
	
	private Route lastChangedRoute;

	protected String ERR_TOGGLE_FAILED = "Toggle of switch failed";

	private RouteControl() {
		listeners = new HashMap<Route, RouteChangeListener>();
	}

	public static RouteControl getInstance() {
		if (instance == null) {
			instance = new RouteControl();
		}
		return instance;
	}

	public void enableRoute(Route r) throws TurnoutException {
		// System.out.println("enabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, true, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.ENABLED;
	}

	public void disableRoute(Route r) throws TurnoutException {
		// System.out.println("disabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, false, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.DISABLED;
	}

	public void addRouteChangeListener(Route r, RouteChangeListener listener) {
		listeners.put(r, listener);
	}

	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	public void removeRouteChangeListener(Route r) {
		listeners.remove(r);
	}

	@Override
	public void undoLastChange() throws ControlException {
		if (lastChangedRoute == null)
			return;
		switch (lastRouteState) {
		case ENABLED:
			disableRoute(lastChangedRoute);
			break;
		case DISABLED:
			enableRoute(lastChangedRoute);
			break;
		}
		lastChangedRoute = null;
		lastRouteState = null;
	}

	@Override
	public void previousDeviceToDefault() throws ControlException {
		if (lastChangedRoute == null)
			return;
		disableRoute(lastChangedRoute);
		lastChangedRoute = null;
		lastRouteState = null;
	}
}
