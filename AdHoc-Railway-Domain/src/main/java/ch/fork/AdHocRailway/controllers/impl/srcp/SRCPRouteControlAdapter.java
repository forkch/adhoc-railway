package ch.fork.AdHocRailway.controllers.impl.srcp;

import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.routes.SRCPRoute;
import de.dermoba.srcp.model.routes.SRCPRouteChangeListener;
import de.dermoba.srcp.model.routes.SRCPRouteControl;
import de.dermoba.srcp.model.routes.SRCPRouteItem;
import de.dermoba.srcp.model.routes.SRCPRouteState;
import de.dermoba.srcp.model.turnouts.SRCPTurnout;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;

public class SRCPRouteControlAdapter extends RouteController implements
		SRCPRouteChangeListener {

	private final SRCPTurnoutControlAdapter turnoutControl;
	private final Map<Route, SRCPRoute> routesSRCPRoutesMap = new HashMap<Route, SRCPRoute>();

	private final Map<SRCPRoute, Route> SRCPRoutesRoutesMap = new HashMap<SRCPRoute, Route>();
	private final SRCPRouteControl routeControl;

	private Route routeTemp;

	private SRCPRoute sRouteTemp;

	public SRCPRouteControlAdapter(
			final SRCPTurnoutControlAdapter turnoutControl) {
		this.turnoutControl = turnoutControl;
		routeControl = SRCPRouteControl.getInstance();

	}

	@Override
	public void enableRoute(final Route route) throws RouteException {
		final SRCPRoute sRoute = getSRCPRoute(route);
		try {
			route.setRouting(true);
			routeControl.enableRoute(sRoute);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void disableRoute(final Route route) throws RouteException {
		final SRCPRoute sRoute = getSRCPRoute(route);
		try {
			route.setRouting(true);
			routeControl.disableRoute(sRoute);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void toggle(final Route route) throws RouteException {
		final SRCPRoute sRoute = getSRCPRoute(route);
		try {
			routeControl.toggle(sRoute);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	@Override
	public void toggleTest(final Route route) throws RouteException {
		routesSRCPRoutesMap.remove(route);
		SRCPRoutesRoutesMap.remove(sRouteTemp);
		if (routeTemp == null || !routeTemp.equals(route)) {
			routeTemp = route;
			// just create a temporary SRCPTurnout
			sRouteTemp = createSRCPRoute(turnoutControl, route);
		} else {
			applyNewSettings(route);
		}
		routesSRCPRoutesMap.put(route, sRouteTemp);
		SRCPRoutesRoutesMap.put(sRouteTemp, route);
		try {
			routeControl.toggle(sRouteTemp);
		} catch (final SRCPModelException e) {
			throw new RouteException("Route Error", e);
		}
	}

	private SRCPRoute createSRCPRoute(
			final SRCPTurnoutControlAdapter turnoutControl, final Route route) {
		final SRCPRoute sRoute = new SRCPRoute();
		for (final RouteItem routeItem : route.getRouteItems()) {

			final SRCPRouteItem sRouteItem = new SRCPRouteItem();
			final SRCPTurnout sTurnout = turnoutControl
					.getOrCreateSRCPTurnout(routeItem.getTurnout());
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

	@Override
	public void setRoutingDelay(final int routingDelay) {
		routeControl.setRoutingDelay(routingDelay);
	}

	@Override
	public void nextTurnoutDerouted(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);
		informNextTurnoutDerouted(route);
	}

	@Override
	public void nextTurnoutRouted(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);
		informNextTurnoutRouted(route);
	}

	@Override
	public void routeChanged(final SRCPRoute sRoute) {
		final Route route = SRCPRoutesRoutesMap.get(sRoute);

		route.setRouting(false);
		if (sRoute.getRouteState().equals(SRCPRouteState.ENABLED)) {
			route.setEnabled(true);
		} else if (sRoute.getRouteState().equals(SRCPRouteState.DISABLED)) {
			route.setEnabled(false);
		}
		informRouteChanged(route);
	}

	public void setSession(final SRCPSession session) {
		routeControl.addRouteChangeListener(this);
		routeControl.setSession(session);
	}

	private void applyNewSettings(final Route route) {
		sRouteTemp.getRouteItems().clear();
		for (final RouteItem routeItem : route.getRouteItems()) {

			final SRCPRouteItem sRouteItem = new SRCPRouteItem();
			final SRCPTurnout sTurnout = turnoutControl
					.getOrCreateSRCPTurnout(routeItem.getTurnout());
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
			sRouteTemp.addRouteItem(sRouteItem);
		}
	}

	private SRCPRoute getSRCPRoute(final Route route) {
		if (route == null) {
			throw new IllegalArgumentException("route must not be null");
		}
		SRCPRoute sRoute = routesSRCPRoutesMap.get(route);
		if (sRoute == null) {

			sRoute = createSRCPRoute(turnoutControl, route);
			routesSRCPRoutesMap.put(route, sRoute);
			SRCPRoutesRoutesMap.put(sRoute, route);
		}
		return sRoute;
	}
}
