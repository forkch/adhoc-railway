package ch.fork.AdHocRailway.services.routes;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;

public class HibernateRouteMapper {

	public static Route map(HibernateRoute hRoute) {
		Route route = new Route();
		route.setId(hRoute.getId());
		route.setName(hRoute.getName());
		route.setNumber(hRoute.getNumber());
		return route;
	}

	public static RouteGroup map(HibernateRouteGroup hRouteGroup) {
		RouteGroup group = new RouteGroup();

		group.setId(hRouteGroup.getId());
		group.setName(hRouteGroup.getName());
		group.setRouteNumberAmount(hRouteGroup.getRouteNumberAmount());
		group.setRouteNumberOffset(hRouteGroup.getRouteNumberOffset());

		return group;
	}

	public static RouteItem map(HibernateRouteItem hRouteItem) {
		RouteItem item = new RouteItem();
		item.setId(hRouteItem.getId());
		item.setRoutedState(hRouteItem.getRoutedState());
		item.setRoutedStateEnum(hRouteItem.getRoutedStateEnum());

		return item;
	}

}
