package ch.fork.AdHocRailway.domain.routes;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.list.ArrayListModel;

public class MemoryRoutePersistence implements RoutePersistenceIface {
	private static Logger logger = Logger.getLogger(MemoryRoutePersistence.class);
	private static MemoryRoutePersistence instance;
	private ArrayListModel<Route> routeCache;
	private ArrayListModel<RouteItem> routeItemCache;
	private ArrayListModel<RouteGroup> routeGroupCache;

	private MemoryRoutePersistence() {
		super();
		this.routeGroupCache = new ArrayListModel<RouteGroup>();
		this.routeCache = new ArrayListModel<Route>();
		this.routeItemCache = new ArrayListModel<RouteItem>();
	}

	public static MemoryRoutePersistence getInstance() {
		if (instance == null) {
			instance = new MemoryRoutePersistence();
		}
		return instance;
	}

	public void clear() {
		routeCache.clear();
		routeGroupCache.clear();
		routeItemCache.clear();
	}

	public ArrayListModel<Route> getAllRoutes() {
		return routeCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getRouteByNumber(int)
	 */
	@SuppressWarnings("unchecked")
	public Route getRouteByNumber(int number) {
		logger.debug("getRouteByNumber()");
		for (Route route : getAllRoutes()) {
			if (route.getNumber() == number)
				return route;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void addRoute(Route route) throws RoutePersistenceException {
		logger.debug("addRoute()");

		if (route.getRouteGroup() == null) {
			throw new RoutePersistenceException("Route has no associated Group");
		}
		routeCache.add(route);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void deleteRoute(Route route) throws RoutePersistenceException {
		logger.debug("deleteRoute()");
		if (!route.getRouteItems().isEmpty()) {
			throw new RoutePersistenceException(
					"Cannot delete Route-Group with associated Route-Items");
		}

		RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		Set<RouteItem> routeItems = route.getRouteItems();
		for (RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
		}
		routeCache.remove(route);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void updateRoute(Route route) {
		logger.debug("updateRoute()");
	}

	public ArrayListModel<RouteGroup> getAllRouteGroups() {
		logger.debug("getAllRouteGroups()");
		return routeGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void addRouteGroup(RouteGroup routeGroup) {
		logger.debug("addRouteGroup()");
		routeGroupCache.add(routeGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		logger.debug("deleteRouteGroup()");
		if (!routeGroup.getRoutes().isEmpty()) {
			throw new RoutePersistenceException(
					"Cannot delete Route-Group with associated Routes");
		}
		routeGroupCache.remove(routeGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void updateRouteGroup(RouteGroup routeGroup) {
		logger.debug("updateRouteGroup()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		logger.debug("addRouteItem()");

		if (item.getTurnout() == null) {
			throw new RoutePersistenceException(
					"Route has no associated Turnout");
		}
		item.getTurnout().getRouteItems().add(item);

		if (item.getRoute() == null) {
			throw new RoutePersistenceException("Route has no associated Route");
		}
		item.getRoute().getRouteItems().add(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void deleteRouteItem(RouteItem item) {
		logger.debug("deleteRouteItem()");

		Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);

		Route route = item.getRoute();
		route.getRouteItems().remove(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void updateRouteItem(RouteItem item) {
		logger.debug("updateRouteItem()");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getNextFreeRouteNumber()
	 */
	public int getNextFreeRouteNumber() {
		logger.debug("getNextFreeRouteNumber()");
		SortedSet<Route> turnouts = new TreeSet<Route>(getAllRoutes());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

}
