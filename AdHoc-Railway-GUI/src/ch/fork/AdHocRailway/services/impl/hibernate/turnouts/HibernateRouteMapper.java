package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;

public class HibernateRouteMapper {

	public static Route mapHibernateRoute(HibernateRoute hRoute) {
		Route route = new Route();
		updateRoute(hRoute, route);
		return route;
	}

	private static void updateRoute(HibernateRoute hRoute, Route route) {
		route.setId(hRoute.getId());
		route.setName(hRoute.getName());
		route.setNumber(hRoute.getNumber());
	}

	public static RouteGroup mapHibernateRouteGroup(
			HibernateRouteGroup hRouteGroup) {
		RouteGroup group = new RouteGroup();

		updateRouteGroup(hRouteGroup, group);

		for (HibernateRoute hRoute : hRouteGroup.getRoutes()) {
			Route route = mapHibernateRoute(hRoute);
			group.addRoute(route);
			route.setRouteGroup(group);

			for (HibernateRouteItem hRouteItem : hRoute.getRouteItems()) {
				RouteItem routeItem = mapHibernateRouteItem(hRouteItem);
				routeItem.setRoute(route);
				route.addRouteItem(routeItem);
				routeItem.setRoute(route);
			}
		}
		return group;
	}

	private static void updateRouteGroup(HibernateRouteGroup hRouteGroup,
			RouteGroup group) {
		group.setId(hRouteGroup.getId());
		group.setName(hRouteGroup.getName());
		group.setRouteNumberAmount(hRouteGroup.getRouteNumberAmount());
		group.setRouteNumberOffset(hRouteGroup.getRouteNumberOffset());
	}

	public static RouteItem mapHibernateRouteItem(HibernateRouteItem hRouteItem) {
		RouteItem item = new RouteItem();
		updateRouteItem(item, hRouteItem);

		return item;
	}

	private static void updateRouteItem(RouteItem item,
			HibernateRouteItem hRouteItem) {
		item.setId(hRouteItem.getId());
		item.setRoutedState(HibernateTurnoutMapper.mapTurnoutState(hRouteItem
				.getRoutedState()));
		item.setTurnout(HibernateTurnoutMapper.mapTurnout(hRouteItem
				.getTurnout()));
	}

	public static HibernateRoute mapRoute(Route route) {
		HibernateRoute hRoute = new HibernateRoute();
		updateHibernateRoute(hRoute, route);
		return hRoute;
	}

	public static void updateHibernateRoute(HibernateRoute hRoute, Route route) {
		hRoute.setId(route.getId());
		hRoute.setName(route.getName());
		hRoute.setNumber(route.getNumber());
	}

	public static HibernateRouteGroup map(RouteGroup routeGroup) {
		HibernateRouteGroup hRouteGroup = new HibernateRouteGroup();
		updateHibernateRouteGroup(hRouteGroup, routeGroup);
		return hRouteGroup;
	}

	public static void updateHibernateRouteGroup(
			HibernateRouteGroup hRouteGroup, RouteGroup routeGroup) {
		hRouteGroup.setId(routeGroup.getId());
		hRouteGroup.setName(routeGroup.getName());
		hRouteGroup.setRouteNumberAmount(routeGroup.getRouteNumberAmount());
		hRouteGroup.setRouteNumberOffset(routeGroup.getRouteNumberOffset());
	}

	public static HibernateRouteItem mapRouteItem(RouteItem item) {
		HibernateRouteItem hRouteItem = new HibernateRouteItem();
		updateHibernateRouteItem(hRouteItem, item);
		return hRouteItem;
	}

	public static void updateHibernateRouteItem(HibernateRouteItem hRouteItem,
			RouteItem item) {
		hRouteItem.setId(item.getId());
		hRouteItem.setRoute(mapRoute(item.getRoute()));
		hRouteItem.setRoutedState(HibernateTurnoutMapper.mapTurnoutState(item
				.getRoutedState()));
		hRouteItem.setTurnout(HibernateTurnoutMapper.map(item.getTurnout()));
	}

}
