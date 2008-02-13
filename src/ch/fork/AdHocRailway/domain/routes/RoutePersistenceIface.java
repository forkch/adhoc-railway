package ch.fork.AdHocRailway.domain.routes;

import com.jgoodies.binding.list.ArrayListModel;

public interface RoutePersistenceIface {

	public abstract ArrayListModel<Route> getAllRoutes();

	public abstract Route getRouteByNumber(int number);

	public abstract void addRoute(Route route) throws RoutePersistenceException;

	public abstract void deleteRoute(Route route)
			throws RoutePersistenceException;

	public abstract void updateRoute(Route route);

	public abstract ArrayListModel<RouteGroup> getAllRouteGroups();

	public abstract void addRouteGroup(RouteGroup routeGroup);

	public abstract void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void updateRouteGroup(RouteGroup routeGroup);

	public abstract void addRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void deleteRouteItem(RouteItem item);

	public abstract void updateRouteItem(RouteItem item);

	public abstract int getNextFreeRouteNumber();

	public void flush() throws RoutePersistenceException;
	
	public abstract void clear() throws RoutePersistenceException;

}