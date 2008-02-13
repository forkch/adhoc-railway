package ch.fork.AdHocRailway.domain.routes;

import com.jgoodies.binding.list.ArrayListModel;

public interface RoutePersistenceIface {

	public abstract ArrayListModel<Route> getAllRoutes()
			throws RoutePersistenceException;

	public abstract Route getRouteByNumber(int number)
			throws RoutePersistenceException;

	public abstract void addRoute(Route route) throws RoutePersistenceException;

	public abstract void deleteRoute(Route route)
			throws RoutePersistenceException;

	public abstract void updateRoute(Route route)
			throws RoutePersistenceException;

	public abstract ArrayListModel<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException;

	public abstract void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void updateRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException;

	public abstract void addRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract void updateRouteItem(RouteItem item)
			throws RoutePersistenceException;

	public abstract int getNextFreeRouteNumber()
			throws RoutePersistenceException;

	public void flush() throws RoutePersistenceException;

	public abstract void clear() throws RoutePersistenceException;

}