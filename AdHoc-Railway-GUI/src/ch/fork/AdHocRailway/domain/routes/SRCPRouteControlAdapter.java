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

	private static final SRCPTurnoutControlAdapter turnoutControl = SRCPTurnoutControlAdapter
			.getInstance();
	private final Map<Route, SRCPRoute> routesSRCPRoutesMap = new HashMap<Route, SRCPRoute>();

	private final Map<SRCPRoute, Route> SRCPRoutesRoutesMap = new HashMap<SRCPRoute, Route>();
	private final List<RouteChangeListener> listeners = new ArrayList<RouteChangeListener>();
	private final SRCPRouteControl routeControl;

	private SRCPRouteControlAdapter() {
		routeControl = SRCPRouteControl.getInstance();

		routeControl.addRouteChangeListener(this);
		reloadConfiguration();

	}

	public static SRCPRouteControlAdapter getInstance() {
		if (instance == null) {
			instance = new SRCPRouteControlAdapter();
		}
		return instance;
	}

	@Override
	public void disableRoute(final Route route) throws RouteException {
		final SRCPRoute sRoute = getSRCPRoute(route);
		try {
			routeControl.disableRoute(sRoute);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	public SRCPRoute getSRCPRoute(final Route route) {
		final SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		return sRoute;
	}

	@Override
	public void enableRoute(final Route route) throws RouteException {
		final SRCPRoute sRoute = getSRCPRoute(route);
		try {
			routeControl.enableRoute(sRoute);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}

	}

	@Override
	public boolean isRouteEnabled(final Route route) {
		final SRCPRoute sRoute = getSRCPRoute(route);
		return sRoute.getRouteState().equals(SRCPRouteState.ENABLED);
	}

	@Override
	public boolean isRouting(final Route route) {
		final SRCPRoute sRoute = getSRCPRoute(route);
		return sRoute.getRouteState().equals(SRCPRouteState.ROUTING);
	}

	@Override
	public void previousDeviceToDefault() throws RouteException {
		try {
			routeControl.previousDeviceToDefault();
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void addRouteChangeListener(final Route route,
			final RouteChangeListener listener) {

		listeners.add(listener);
	}

	@Override
	public void removeAllRouteChangeListeners() {
		listeners.clear();
	}

	@Override
	public void removeRouteChangeListener(final Route route,
			final RouteChangeListener listener) {
		listeners.remove(listener);
	}

	@Override
	public void addOrUpdateRoute(final Route route) {

		final SRCPRoute sRoute = createSRCPRoute(turnoutControl, route);
		routesSRCPRoutesMap.put(route, sRoute);
		SRCPRoutesRoutesMap.put(sRoute, route);
	}

	public SRCPRoute createSRCPRoute(
			final SRCPTurnoutControlAdapter turnoutControl, final Route route) {
		final SRCPRoute sRoute = new SRCPRoute();
		for (final RouteItem routeItem : route.getRouteItems()) {

			final SRCPRouteItem sRouteItem = new SRCPRouteItem();
			final SRCPTurnout sTurnout = turnoutControl
					.getSRCPTurnout(routeItem.getTurnout());
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
		return sRoute;
	}

	public void reloadConfiguration() {
		routeControl.setRoutingDelay(Preferences.getInstance().getIntValue(
				PreferencesKeys.ROUTING_DELAY));
	}

	@Override
	public void nextTurnoutDerouted(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (final RouteChangeListener listener : listeners) {
			listener.nextSwitchDerouted(route);
		}
	}

	@Override
	public void nextTurnoutRouted(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (final RouteChangeListener listener : listeners) {
			listener.nextSwitchRouted(route);
		}
	}

	@Override
	public void routeChanged(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);
		for (final RouteChangeListener listener : listeners) {
			listener.routeChanged(route);
		}

	}

	public void setSession(final SRCPSession session) {
		routeControl.setSession(session);
	}
}
