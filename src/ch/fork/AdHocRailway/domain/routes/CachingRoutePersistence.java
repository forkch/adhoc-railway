/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryRoutePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.domain.routes;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.list.ArrayListModel;

public class CachingRoutePersistence implements RoutePersistenceIface {
	private static Logger					logger	= Logger
															.getLogger(CachingRoutePersistence.class);
	
	private ArrayListModel<Route>			routeCache;
	private ArrayListModel<RouteItem>		routeItemCache;
	private ArrayListModel<RouteGroup>		routeGroupCache;

	public CachingRoutePersistence() {
		logger.info("CachingRoutePersistence loaded");
		this.routeGroupCache = new ArrayListModel<RouteGroup>();
		this.routeCache = new ArrayListModel<Route>();
		this.routeItemCache = new ArrayListModel<RouteItem>();
	}

	public void clear() {
		routeCache.clear();
		routeGroupCache.clear();
		routeItemCache.clear();
	}

	public ArrayListModel<Route> getAllRoutes() {
		//logger.debug("getAllRoutes()");
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
			SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(route
					.getRouteItems());
			for (RouteItem routeitem : routeItems) {
				deleteRouteItem(routeitem);
			}
			// throw new RoutePersistenceException(
			// "Cannot delete Route-Group with associated Route-Items");
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
		//logger.debug("getAllRouteGroups()");
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#flush()
	 */
	public void flush() throws RoutePersistenceException {
		logger.debug("flush()");
	}

	public void enlargeRouteGroups() {
		logger.debug("enlargeRouteGroups()");
		int runningNumber = 1;
		for (RouteGroup group : getAllRouteGroups()) {
			logger.debug("offset of group " + group.getName() + ": "
					+ runningNumber);
			
			group.setRouteNumberOffset(runningNumber);
			int routesInThisGroup = 0;
			for (Route route : group.getRoutes()) {
				route.setNumber(runningNumber);
				routesInThisGroup++;
				runningNumber++;
			}

			logger.debug("actual routeNumberAmount " + group.getRouteNumberAmount());
			logger.debug("routes in this group " + routesInThisGroup);
			int diff = group.getRouteNumberAmount() - routesInThisGroup;
			logger.debug("difference " + diff);
			if (diff <= 5 && diff >= 0) {
				logger.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ (group.getRouteNumberAmount() + 10));
				group
						.setRouteNumberAmount(group.getRouteNumberAmount() + 10);
			} else if(diff < 0) {
				int newAmount = (int)Math.ceil(Math.abs(diff)/10.0)*10;
				logger.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ newAmount);
				group
						.setRouteNumberAmount(newAmount);
			}
			runningNumber = group.getRouteNumberOffset() + group.getRouteNumberAmount();
			logger.debug("offset of next group: " + runningNumber);
		}
		
	}

	public int getNextFreeRouteNumberOfGroup(RouteGroup routeGroup) {
		SortedSet<Route> routes = new TreeSet<Route>(routeGroup
				.getRoutes());
		int offset = routeGroup.getRouteNumberOffset();
		int amount = routeGroup.getRouteNumberAmount();

		if (routes.isEmpty()) {
			return offset;
		}
		int nextNumber = routes.last().getNumber() + 1;
		if (nextNumber < offset + amount) {
			return nextNumber;
		}
		return -1;
	}

}
