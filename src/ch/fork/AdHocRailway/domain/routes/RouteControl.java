package ch.fork.AdHocRailway.domain.routes;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import ch.fork.AdHocRailway.domain.Control;
import ch.fork.AdHocRailway.domain.switches.exception.SwitchException;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;

public class RouteControl extends Control {

	private static RouteControl instance;

	private SortedSet<Route> routes;

	private RouteControl() {
		routes = new TreeSet<Route>();
	}

	public static RouteControl getInstance() {
		if (instance == null) {
			instance = new RouteControl();
		}
		return instance;
	}

	public void registerRoute(Route route) {
		routes.add(route);
	}

	public void unregisterRoute(Route route) {
		routes.remove(route.getName());
	}

	public void registerRoutes(Collection<Route> routesToRegister) {
		for (Route route : routesToRegister) {
			routes.add(route);
		}
	}

	public void unregisterAllRoutes() {
		routes.clear();
	}

	public SortedSet<Route> getRoutes() {
		return routes;
	}

	public void enableRoute(Route r) throws SwitchException {
		// System.out.println("enabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Thread switchRouter = new Router(r, true, waitTime);
		switchRouter.start();
	}

	public void disableRoute(Route r) throws SwitchException {
		// System.out.println("disabling route: " + r);
		int waitTime = Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY);
		Thread switchRouter = new Router(r, false, waitTime);
		switchRouter.start();
	}
}
