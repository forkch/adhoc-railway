package ch.fork.AdHocRailway.domain.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.ControlException;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnout;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import de.dermoba.srcp.client.SRCPSession;

public class SRCPRouteControlAdapter implements RouteControlIface,
		SRCPRouteChangeListener {
	private static Logger								logger	= Logger
																		.getLogger(SRCPRouteControlAdapter.class);

	private static SRCPRouteControlAdapter				instance;
	private RoutePersistenceIface						persistence;
	private Map<Route, SRCPRoute>						routesSRCPRoutesMap;
	private Map<SRCPRoute, List<RouteChangeListener>>	listeners;
	private SRCPRouteControl							routeControl;

	private SRCPRouteControlAdapter() {
		routeControl = SRCPRouteControl.getInstance();
		routesSRCPRoutesMap = new HashMap<Route, SRCPRoute>();
		listeners = new HashMap<SRCPRoute, List<RouteChangeListener>>();

	}

	public static SRCPRouteControlAdapter getInstance() {
		if (instance == null)
			instance = new SRCPRouteControlAdapter();
		return instance;
	}

	public void disableRoute(Route route) throws TurnoutException {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		routeControl.disableRoute(sRoute);
	}

	public void enableRoute(Route route) throws TurnoutException {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		routeControl.enableRoute(sRoute);

	}

	public SRCPRouteState getRouteState(Route route) {

		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		return sRoute.getRouteState();
	}

	public boolean isRouting(Route route) {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		return sRoute.isRouting();
	}

	public void previousDeviceToDefault() throws ControlException {
		routeControl.previousDeviceToDefault();
	}

	public void addRouteChangeListener(Route route, RouteChangeListener listener) {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		if (listeners.get(sRoute) == null) {
			listeners.put(sRoute, new ArrayList<RouteChangeListener>());
		}
		listeners.get(sRoute).add(listener);
	}

	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	public void removeRouteChangeListener(Route route,
			RouteChangeListener listener) {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		listeners.remove(sRoute);
	}

	public void setRoutePersistence(RoutePersistenceIface routePersistence) {
		this.persistence = routePersistence;
	}

	public void undoLastChange() throws ControlException {
		routeControl.undoLastChange();
	}

	public void update() {
		routesSRCPRoutesMap.clear();
		routeControl.removeRouteChangeListener(this);
		SRCPTurnoutControlAdapter turnoutControl = SRCPTurnoutControlAdapter
				.getInstance();
		for (Route route : persistence.getAllRoutes()) {
			SRCPRoute sRoute = new SRCPRoute();
			for (RouteItem routeItem : route.getRouteItems()) {

				SRCPRouteItem sRouteItem = new SRCPRouteItem();
				SRCPTurnout sTurnout = turnoutControl.getSRCPTurnout(routeItem
						.getTurnout());
				sRouteItem.setTurnout(sTurnout);
				sRouteItem.setRoutedState(routeItem.getRoutedStateEnum());
				sRoute.addRouteItem(sRouteItem);
			}
			routesSRCPRoutesMap.put(route, sRoute);
		}
		routeControl.addRouteChangeListener(this);
	}

	public void nextTurnoutDerouted(SRCPRoute sRoute) {
		for (RouteChangeListener listener : listeners.get(sRoute)) {
			listener.nextSwitchDerouted();
		}
	}

	public void nextTurnoutRouted(SRCPRoute sRoute) {
		for (RouteChangeListener listener : listeners.get(sRoute)) {
			listener.nextSwitchRouted();
		}
	}

	public void routeChanged(SRCPRoute sRoute) {
		for (RouteChangeListener listener : listeners.get(sRoute)) {
			listener.routeChanged();
		}

	}

	public void setSession(SRCPSession session) {
		routeControl.setSession(session);
	}
}
