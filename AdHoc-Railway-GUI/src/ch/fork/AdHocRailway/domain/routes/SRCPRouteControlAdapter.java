package ch.fork.AdHocRailway.domain.routes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutControlAdapter;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.routes.SRCPRoute;
import de.dermoba.srcp.model.routes.SRCPRouteChangeListener;
import de.dermoba.srcp.model.routes.SRCPRouteControl;
import de.dermoba.srcp.model.routes.SRCPRouteItem;
import de.dermoba.srcp.model.routes.SRCPRouteState;
import de.dermoba.srcp.model.turnouts.SRCPTurnout;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class SRCPRouteControlAdapter implements RouteControlIface,
		SRCPRouteChangeListener {
	private static SRCPRouteControlAdapter instance;
	private RouteManager persistence;
	private final Map<Route, SRCPRoute> routesSRCPRoutesMap;

	private final Map<SRCPRoute, Route> SRCPRoutesRoutesMap;
	private final List<RouteChangeListener> listeners;
	private final SRCPRouteControl routeControl;

	private SRCPRouteControlAdapter() {
		routeControl = SRCPRouteControl.getInstance();
		routesSRCPRoutesMap = new HashMap<Route, SRCPRoute>();
		SRCPRoutesRoutesMap = new HashMap<SRCPRoute, Route>();
		listeners = new ArrayList<RouteChangeListener>();

	}

	public static SRCPRouteControlAdapter getInstance() {
		if (instance == null) {
			instance = new SRCPRouteControlAdapter();
		}
		return instance;
	}

	@Override
	public void disableRoute(Route route) throws RouteException {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		try {
			routeControl.disableRoute(sRoute);
		} catch (SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void enableRoute(Route route) throws RouteException {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		try {
			routeControl.enableRoute(sRoute);
		} catch (SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}

	}

	@Override
	public boolean isRouteEnabled(Route route) {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		return sRoute.getRouteState().equals(SRCPRouteState.ENABLED);
	}

	@Override
	public boolean isRouting(Route route) {
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		return sRoute.getRouteState().equals(SRCPRouteState.ROUTING);
	}

	@Override
	public void previousDeviceToDefault() throws RouteException {
		try {
			routeControl.previousDeviceToDefault();
		} catch (SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void addRouteChangeListener(Route route, RouteChangeListener listener) {

		listeners.add(listener);
	}

	@Override
	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	@Override
	public void removeRouteChangeListener(Route route,
			RouteChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void setRoutePersistence(RouteManager routePersistence) {
		this.persistence = routePersistence;
	}

	@Override
	public void undoLastChange() throws RouteException {
		try {
			routeControl.undoLastChange();
		} catch (SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void update() {
		routesSRCPRoutesMap.clear();
		SRCPRoutesRoutesMap.clear();
		routeControl.removeRouteChangeListener(this);
		SRCPTurnoutControlAdapter turnoutControl = SRCPTurnoutControlAdapter
				.getInstance();
		routeControl.setRoutingDelay(Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY));

		for (Route route : persistence.getAllRoutes()) {
			SRCPRoute sRoute = new SRCPRoute();
			for (RouteItem routeItem : route.getRouteItems()) {

				SRCPRouteItem sRouteItem = new SRCPRouteItem();
				SRCPTurnout sTurnout = turnoutControl.getSRCPTurnout(routeItem
						.getTurnout());
				sRouteItem.setTurnout(sTurnout);
				switch (routeItem.getRoutedState()) {
				case LEFT:
					sRouteItem.setRoutedState(SRCPTurnoutState.LEFT);
					break;
				case RIGHT:

					sRouteItem.setRoutedState(SRCPTurnoutState.RIGHT);
					break;
				case STRAIGHT:
					sRouteItem.setRoutedState(SRCPTurnoutState.STRAIGHT);
					break;
				default:

					sRouteItem.setRoutedState(SRCPTurnoutState.UNDEF);
				}
				sRoute.addRouteItem(sRouteItem);
			}
			routesSRCPRoutesMap.put(route, sRoute);
			SRCPRoutesRoutesMap.put(sRoute, route);
		}
		routeControl.addRouteChangeListener(this);
	}

	@Override
	public void nextTurnoutDerouted(SRCPRoute sRoute) {
		Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (RouteChangeListener listener : listeners) {
			listener.nextSwitchDerouted(route);
		}
	}

	@Override
	public void nextTurnoutRouted(SRCPRoute sRoute) {
		Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (RouteChangeListener listener : listeners) {
			listener.nextSwitchRouted(route);
		}
	}

	@Override
	public void routeChanged(SRCPRoute sRoute) {
		Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (RouteChangeListener listener : listeners) {
			listener.routeChanged(route);
		}

	}

	public void setSession(SRCPSession session) {
		routeControl.setSession(session);
	}
}
