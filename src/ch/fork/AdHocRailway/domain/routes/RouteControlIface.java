package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

public interface RouteControlIface {

	public abstract void enableRoute(Route r) throws TurnoutException;

	public abstract void disableRoute(Route r) throws TurnoutException;

	public abstract void addRouteChangeListener(Route r,
			RouteChangeListener listener);

	public abstract void removeAllRouteChangeListeners();

	public abstract void removeRouteChangeListener(Route r);

	public abstract void undoLastChange() throws ControlException;

	public abstract void previousDeviceToDefault() throws ControlException;

	public abstract void setRoutePersistence(
			RoutePersistenceIface routePersistence);

}