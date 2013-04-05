package ch.fork.AdHocRailway.ui.routes.configuration;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;

import org.apache.commons.beanutils.BeanUtils;

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

	public static Route copyRoute(final Route old) {
		final Route r = new Route();
		try {
			BeanUtils.copyProperties(r, old);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return r;
	}

	public static void update(final Route testRoute, final String property,
			final Object newValue) {
		try {
			BeanUtils.setProperty(testRoute, property, newValue);
		} catch (IllegalAccessException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static String getRouteDescription(final Route route) {
		if (route == null) {
			return "Please choose a route...";
		}
		final StringBuilder description = new StringBuilder();
		description.append("<html>");
		description.append("<h1>" + route.getNumber() + "</h1>");
		description.append("<table>");

		addTableRow("Name:", route.getName(), description);
		addTableRow("Orientation:", route.getOrientation().toString(),
				description);

		description.append("</table>");
		description.append("<h3>Turnouts</h3>");
		description.append("<table>");

		for (final RouteItem item : route.getRouteItems()) {
			addTableRow("", "" + item.getTurnout().getNumber() + ": "
					+ item.getRoutedState().toString(), description);
		}

		description.append("</table>");
		description.append("</html>");
		return description.toString();
	}

	private static void addTableRow(final String key, final String value,
			final StringBuilder description) {
		description.append("<tr>");
		description.append("<td>");
		description.append(key);
		description.append("</td>");
		description.append("<td>");
		description.append(value);
		description.append("</td>");
		description.append("</tr>");
	}
}
