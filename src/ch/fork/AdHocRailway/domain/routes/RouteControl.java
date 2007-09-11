package ch.fork.AdHocRailway.domain.routes;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.exception.ControlException;
import ch.fork.AdHocRailway.domain.routes.RouteOld.RouteState;
import ch.fork.AdHocRailway.domain.turnouts.exception.SwitchException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class RouteControl extends Control {

	private static RouteControl instance;

	private Map<RouteOld, RouteChangeListener> listeners;

	private SortedSet<RouteOld> routes;

	private Map<Integer, RouteOld> numberToRoutes;

	private List<RouteGroupOld> routeGroups;

	private RouteOld lastChangedRoute;

	private RouteState lastRouteState;

	protected String ERR_TOGGLE_FAILED = "Toggle of switch failed";

	private RouteControl() {
		listeners = new HashMap<RouteOld, RouteChangeListener>();
		routes = new TreeSet<RouteOld>();
		numberToRoutes = new HashMap<Integer, RouteOld>();
		routeGroups = new ArrayList<RouteGroupOld>();
	}

	public static RouteControl getInstance() {
		if (instance == null) {
			instance = new RouteControl();
		}
		return instance;
	}

	public void registerRoute(RouteOld route) {
		routes.add(route);
		numberToRoutes.put(route.getNumber(), route);
	}

	public void unregisterRoute(RouteOld route) {
		routes.remove(route.getName());
		numberToRoutes.remove(route.getNumber());
	}

	public void registerRoutes(Collection<RouteOld> routesToRegister) {
		for (RouteOld route : routesToRegister) {
			routes.add(route);
			numberToRoutes.put(route.getNumber(), route);
		}
	}

	public void registerRouteGroup(RouteGroupOld rg) {
		routeGroups.add(rg);
	}

	public void registerRouteGroups(Collection<RouteGroupOld> rgs) {
		routeGroups.addAll(rgs);
	}

	public void unregisterAllRouteGroups() {
		routeGroups.clear();
	}

	public List<RouteGroupOld> getRouteGroups() {
		return routeGroups;
	}

	public void clear() {
		routes.clear();
		numberToRoutes.clear();
		routeGroups.clear();
	}

	public SortedSet<RouteOld> getRoutes() {
		return routes;
	}

	public Map<Integer, RouteOld> getNumberToRoutes() {
		return numberToRoutes;
	}

	public void enableRoute(RouteOld r) throws SwitchException {
		// System.out.println("enabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		System.out.println(listeners.get(r).hashCode());
		Router switchRouter = new Router(r, true, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.ENABLED;
	}

	public void disableRoute(RouteOld r) throws SwitchException {
		// System.out.println("disabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Router switchRouter = new Router(r, false, waitTime, listeners.get(r));
		switchRouter.start();
		lastChangedRoute = r;
		lastRouteState = RouteState.DISABLED;
	}

	public void addRouteChangeListener(RouteOld r, RouteChangeListener listener) {
		listeners.put(r, listener);
	}

	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	public void removeRouteChangeListener(RouteOld r) {
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

	@Override
	public void commitTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void rollbackTransaction() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void startTransaction() {
		// TODO Auto-generated method stub
		
	}
}
