package ch.fork.AdHocRailway.ui.routes.configuration;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManager;

public class RouteHelper {

	public static Route createDefaultRoute(final RouteManager routePersistence,
			final RouteGroup selectedRouteGroup, final int nextNumber) {
		final Route newRoute = new Route();
		newRoute.setNumber(nextNumber);
		newRoute.setRouteGroup(selectedRouteGroup);
		return newRoute;
	}

	public static Route copyRoute(final RouteManager routePersistence,
			final Route selectedRoute, final RouteGroup selectedRouteGroup,
			final int nextNumber) {
		final Route newRoute = new Route();
		newRoute.setName(selectedRoute.getName());
		newRoute.setOrientation(selectedRoute.getOrientation());
		newRoute.setNumber(nextNumber);
		newRoute.setRouteGroup(selectedRouteGroup);
		for (final RouteItem origRouteItem : selectedRoute.getRouteItems()) {
			final RouteItem routeItem = copyRouteItem(origRouteItem);
			routeItem.setRoute(newRoute);
			newRoute.addRouteItem(routeItem);
		}

		return newRoute;

	}

	private static RouteItem copyRouteItem(final RouteItem origRouteItem) {
		final RouteItem item = new RouteItem();
		item.setTurnout(origRouteItem.getTurnout());
		item.setRoutedState(origRouteItem.getRoutedState());
		return item;
	}
}
