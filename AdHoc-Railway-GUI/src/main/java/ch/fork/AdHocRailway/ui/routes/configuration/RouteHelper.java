package ch.fork.AdHocRailway.ui.routes.configuration;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import org.apache.commons.beanutils.BeanUtils;

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
        for (final RouteItem origRouteItem : selectedRoute.getRoutedTurnouts()) {
            final RouteItem routeItem = copyRouteItem(origRouteItem);
            routeItem.setRoute(newRoute);
            newRoute.addRouteItem(routeItem);
        }

        return newRoute;

    }

    private static RouteItem copyRouteItem(final RouteItem origRouteItem) {
        final RouteItem item = new RouteItem();
        item.setTurnout(origRouteItem.getTurnout());
        item.setState(origRouteItem.getState());
        return item;
    }

    public static void addNewRouteDialog(final RouteContext ctx,
                                         final RouteGroup selectedRouteGroup) {
        final RouteManager routePersistence = ctx.getRouteManager();
        final int nextNumber = routePersistence.getNextFreeRouteNumber();

        final Route newTurnout = createDefaultRoute(routePersistence,
                nextNumber);

        new RouteConfig(ctx.getMainFrame(), ctx, newTurnout, selectedRouteGroup, true);
    }

    public static Route copyRoute(final Route old) {
        final Route r = new Route();
        try {
            BeanUtils.copyProperties(r, old);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return r;
    }

    public static void update(final Route testRoute, final String property,
                              final Object newValue) {
        try {
            BeanUtils.setProperty(testRoute, property, newValue);
        } catch (Exception e) {
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
        String orientation = "";
        if (route.getOrientation() != null) {
            orientation = route.getOrientation().toString();
        }
        addTableRow("Orientation:", orientation, description);

        description.append("</table>");
        description.append("<h3>Turnouts</h3>");
        description.append("<table>");

        for (final RouteItem item : route.getRoutedTurnouts()) {
            addTableRow("", "" + item.getTurnout().getNumber() + ": "
                    + item.getState().toString(), description);
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

    public static boolean isNumberValid(final Route routeToValidate,
                                        final Route currentRoute, final RouteManager routeManager) {
        if (routeToValidate.getNumber() == 0) {
            return false;
        }
        if (!routeManager.isRouteNumberFree(routeToValidate.getNumber())
                && routeToValidate.getNumber() != currentRoute.getNumber()) {
            return false;
        }
        return true;
    }
}
