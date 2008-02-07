package ch.fork.AdHocRailway.domain.routes;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.Route.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class SRCPRouteControl extends Control implements RouteControlIface {
	private static Logger logger = Logger.getLogger(SRCPRouteControl.class);
	private static RouteControlIface instance;
	
	private RoutePersistenceIface persistence;

	private Map<Route, RouteChangeListener> listeners;

	private RouteState lastRouteState;
	
	private Route lastChangedRoute;

	protected String ERR_TOGGLE_FAILED = "Toggle of switch failed";

	private SRCPRouteControl() {
		listeners = new HashMap<Route, RouteChangeListener>();
	}

	public static RouteControlIface getInstance() {
		if (instance == null) {
			instance = new SRCPRouteControl();
		}
		return instance;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#enableRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void enableRoute(Route r) throws TurnoutException {
		logger.debug("enabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, true, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.ENABLED;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#disableRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void disableRoute(Route r) throws TurnoutException {
		logger.debug("disabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, false, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.DISABLED;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#addRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route, ch.fork.AdHocRailway.domain.routes.RouteChangeListener)
	 */
	public void addRouteChangeListener(Route r, RouteChangeListener listener) {
		listeners.put(r, listener);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#removeAllRouteChangeListeners()
	 */
	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#removeRouteChangeListener(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void removeRouteChangeListener(Route r) {
		listeners.remove(r);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#undoLastChange()
	 */
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

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RouteControlIface#previousDeviceToDefault()
	 */
	@Override
	public void previousDeviceToDefault() throws ControlException {
		if (lastChangedRoute == null)
			return;
		disableRoute(lastChangedRoute);
		lastChangedRoute = null;
		lastRouteState = null;
	}

	public void setRoutePersistence(RoutePersistenceIface routePersistence) {
		this.persistence = routePersistence;
		
	}
}
