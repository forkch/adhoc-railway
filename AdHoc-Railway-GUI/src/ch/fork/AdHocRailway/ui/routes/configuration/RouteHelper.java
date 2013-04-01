package ch.fork.AdHocRailway.ui.routes.configuration;

import javax.swing.JOptionPane;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;

public class RouteHelper {

	public static Route createDefaultRoute(final RouteManager routePersistence,
			final int nextNumber) {
		final Route newRoute = new Route();
		newRoute.setNumber(nextNumber);
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

	public static void addNewRouteDialog(final RouteGroup selectedRouteGroup) {
		int nextNumber = 0;
		final RouteManager routePersistence = AdHocRailway.getInstance()
				.getRoutePersistence();
		if (Preferences.getInstance().getBooleanValue(
				PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
			nextNumber = routePersistence
					.getNextFreeRouteNumberOfGroup(selectedRouteGroup);
			if (nextNumber == -1) {
				JOptionPane.showMessageDialog(AdHocRailway.getInstance(),
						"No more free numbers in this group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			nextNumber = routePersistence.getNextFreeRouteNumber();
		}

		final Route newTurnout = createDefaultRoute(routePersistence,
				nextNumber);

		new RouteConfig(AdHocRailway.getInstance(), newTurnout,
				selectedRouteGroup);
	}
}
