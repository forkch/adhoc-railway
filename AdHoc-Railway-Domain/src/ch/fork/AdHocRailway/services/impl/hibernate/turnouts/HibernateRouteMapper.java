package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;

public class HibernateRouteMapper {

	public static Route mapHibernateRoute(final HibernateRoute hRoute) {
		final Route route = new Route();
		updateRoute(hRoute, route);
		return route;
	}

	private static void updateRoute(final HibernateRoute hRoute,
			final Route route) {
		route.setId(hRoute.getId());
		route.setName(hRoute.getName());
		route.setNumber(hRoute.getNumber());
	}

	public static RouteGroup mapHibernateRouteGroup(
			final HibernateRouteGroup hRouteGroup) {
		final RouteGroup group = new RouteGroup();

		updateRouteGroup(hRouteGroup, group);

		for (final HibernateRoute hRoute : hRouteGroup.getRoutes()) {
			final Route route = mapHibernateRoute(hRoute);
			group.addRoute(route);
			route.setRouteGroup(group);

			for (final HibernateRouteItem hRouteItem : hRoute.getRouteItems()) {
				final RouteItem routeItem = mapHibernateRouteItem(hRouteItem);
				routeItem.setRoute(route);
				route.addRouteItem(routeItem);
				routeItem.setRoute(route);
			}
		}
		return group;
	}

	private static void updateRouteGroup(final HibernateRouteGroup hRouteGroup,
			final RouteGroup group) {
		group.setId(hRouteGroup.getId());
		group.setName(hRouteGroup.getName());
	}

	public static RouteItem mapHibernateRouteItem(
			final HibernateRouteItem hRouteItem) {
		final RouteItem item = new RouteItem();
		updateRouteItem(item, hRouteItem);

		return item;
	}

	private static void updateRouteItem(final RouteItem item,
			final HibernateRouteItem hRouteItem) {
		item.setId(hRouteItem.getId());
		item.setRoutedState(HibernateTurnoutMapper.mapTurnoutState(hRouteItem
				.getRoutedState()));
		item.setTurnout(HibernateTurnoutMapper.mapTurnout(hRouteItem
				.getTurnout()));
	}

	public static HibernateRoute mapRoute(final Route route) {
		final HibernateRoute hRoute = new HibernateRoute();
		updateHibernateRoute(hRoute, route);
		return hRoute;
	}

	public static void updateHibernateRoute(final HibernateRoute hRoute,
			final Route route) {
		hRoute.setId(route.getId());
		hRoute.setName(route.getName());
		hRoute.setNumber(route.getNumber());
	}

	public static HibernateRouteGroup map(final RouteGroup routeGroup) {
		final HibernateRouteGroup hRouteGroup = new HibernateRouteGroup();
		updateHibernateRouteGroup(hRouteGroup, routeGroup);
		return hRouteGroup;
	}

	public static void updateHibernateRouteGroup(
			final HibernateRouteGroup hRouteGroup, final RouteGroup routeGroup) {
		hRouteGroup.setId(routeGroup.getId());
		hRouteGroup.setName(routeGroup.getName());
	}

	public static HibernateRouteItem mapRouteItem(final RouteItem item) {
		final HibernateRouteItem hRouteItem = new HibernateRouteItem();
		updateHibernateRouteItem(hRouteItem, item);
		return hRouteItem;
	}

	public static void updateHibernateRouteItem(
			final HibernateRouteItem hRouteItem, final RouteItem item) {
		hRouteItem.setId(item.getId());
		hRouteItem.setRoute(mapRoute(item.getRoute()));
		hRouteItem.setRoutedState(HibernateTurnoutMapper.mapTurnoutState(item
				.getRoutedState()));
		hRouteItem.setTurnout(HibernateTurnoutMapper.map(item.getTurnout()));
	}

}
