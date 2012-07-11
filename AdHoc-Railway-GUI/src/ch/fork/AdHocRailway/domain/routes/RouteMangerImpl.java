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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerImpl;
import ch.fork.AdHocRailway.services.routes.HibernateRouteService;
import ch.fork.AdHocRailway.services.routes.RouteService;

import com.jgoodies.binding.list.ArrayListModel;

public class RouteMangerImpl implements RouteManager {
	private static Logger LOGGER = Logger.getLogger(RouteMangerImpl.class);

	private static RouteMangerImpl instance;

	private final Map<Integer, Route> idToRoute;
	private final Map<Integer, RouteItem> idToRouteItem;
	private final Map<Integer, RouteGroup> idToRouteGroup;

	private final RouteService routeService;

	private RouteMangerImpl() {
		LOGGER.info("RouteMangerImpl loaded");
		this.idToRoute = new HashMap<Integer, Route>();
		this.idToRouteGroup = new HashMap<Integer, RouteGroup>();
		this.idToRouteItem = new HashMap<Integer, RouteItem>();

		this.routeService = HibernateRouteService.getInstance();

		reload();
	}

	public static RouteManager getInstance() {
		if (instance == null)
			instance = new RouteMangerImpl();
		return instance;
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		idToRoute.clear();
		idToRouteGroup.clear();
		idToRouteItem.clear();
	}

	@Override
	public ArrayListModel<Route> getAllRoutes() {
		LOGGER.debug("getAllRoutes()");
		return new ArrayListModel<Route>(idToRoute.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getRouteByNumber
	 * (int)
	 */
	@Override
	public Route getRouteByNumber(int number) {
		LOGGER.debug("getRouteByNumber()");
		for (Route route : getAllRoutes()) {
			if (route.getNumber() == number)
				return route;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.
	 * fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void addRoute(Route route) throws RoutePersistenceException {
		LOGGER.debug("addRoute()");
		this.routeService.addRoute(route);

		if (route.getRouteGroup() == null) {
			throw new RoutePersistenceException("Route has no associated Group");
		}
		idToRoute.put(route.getId(), route);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(
	 * ch.fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void deleteRoute(Route route) throws RoutePersistenceException {
		LOGGER.debug("deleteRoute()");
		if (!route.getRouteItems().isEmpty()) {
			SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(
					route.getRouteItems());
			for (RouteItem routeitem : routeItems) {
				deleteRouteItem(routeitem);
			}
		}

		this.routeService.deleteRoute(route);
		RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		Set<RouteItem> routeItems = route.getRouteItems();
		for (RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
		}
		idToRoute.remove(route.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(
	 * ch.fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void updateRoute(Route route) {
		LOGGER.debug("updateRoute()");
		this.routeService.updateRoute(route);
	}

	@Override
	public ArrayListModel<RouteGroup> getAllRouteGroups() {
		LOGGER.debug("getAllRouteGroups()");
		return new ArrayListModel<RouteGroup>(idToRouteGroup.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
	public void addRouteGroup(RouteGroup routeGroup) {
		LOGGER.debug("addRouteGroup()");
		this.routeService.addRouteGroup(routeGroup);
		idToRouteGroup.put(routeGroup.getId(), routeGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		LOGGER.debug("deleteRouteGroup()");
		if (!routeGroup.getRoutes().isEmpty()) {
			throw new RoutePersistenceException(
					"Cannot delete Route-Group with associated Routes");
		}
		this.routeService.deleteRouteGroup(routeGroup);
		idToRouteGroup.remove(routeGroup.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
	public void updateRouteGroup(RouteGroup routeGroup) {
		LOGGER.debug("updateRouteGroup()");
		this.routeService.updateRouteGroup(routeGroup);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		LOGGER.debug("addRouteItem()");

		if (item.getTurnout() == null) {
			throw new RoutePersistenceException(
					"Route has no associated Turnout");
		}
		item.getTurnout().getRouteItems().add(item);

		if (item.getRoute() == null) {
			throw new RoutePersistenceException("Route has no associated Route");
		}
		this.routeService.addRouteItem(item);
		item.getRoute().getRouteItems().add(item);
		idToRouteItem.put(item.getId(), item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void deleteRouteItem(RouteItem item) {
		LOGGER.debug("deleteRouteItem()");

		this.routeService.deleteRouteItem(item);
		Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);

		Route route = item.getRoute();
		route.getRouteItems().remove(item);
		idToRouteItem.remove(item.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void updateRouteItem(RouteItem item) {
		LOGGER.debug("updateRouteItem()");
		this.routeService.updateRouteItem(item);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#
	 * getNextFreeRouteNumber()
	 */
	@Override
	public int getNextFreeRouteNumber() {
		LOGGER.debug("getNextFreeRouteNumber()");
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
	@Override
	public void flush() throws RoutePersistenceException {
		LOGGER.debug("flush()");
	}

	@Override
	public void enlargeRouteGroups() {
		LOGGER.debug("enlargeRouteGroups()");
		int runningNumber = 1;
		for (RouteGroup group : getAllRouteGroups()) {
			LOGGER.debug("offset of group " + group.getName() + ": "
					+ runningNumber);

			group.setRouteNumberOffset(runningNumber);
			int routesInThisGroup = 0;
			for (Route route : group.getRoutes()) {
				route.setNumber(runningNumber);
				routesInThisGroup++;
				runningNumber++;
			}

			LOGGER.debug("actual routeNumberAmount "
					+ group.getRouteNumberAmount());
			LOGGER.debug("routes in this group " + routesInThisGroup);
			int diff = group.getRouteNumberAmount() - routesInThisGroup;
			LOGGER.debug("difference " + diff);
			if (diff <= 5 && diff >= 0) {
				LOGGER.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ (group.getRouteNumberAmount() + 10));
				group.setRouteNumberAmount(group.getRouteNumberAmount() + 10);
			} else if (diff < 0) {
				int newAmount = (int) Math.ceil(Math.abs(diff) / 10.0) * 10;
				LOGGER.debug("setting turnout amount of group "
						+ group.getName() + " to " + newAmount);
				group.setRouteNumberAmount(newAmount);
			}
			runningNumber = group.getRouteNumberOffset()
					+ group.getRouteNumberAmount();
			LOGGER.debug("offset of next group: " + runningNumber);
		}

	}

	@Override
	public int getNextFreeRouteNumberOfGroup(RouteGroup routeGroup) {
		SortedSet<Route> routes = new TreeSet<Route>(routeGroup.getRoutes());
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

	@Override
	public void reload() {
		for (RouteGroup routeGroup : this.routeService.getAllRouteGroups()) {
			idToRouteGroup.put(routeGroup.getId(), routeGroup);
		}
		for (RouteItem routeItem : this.routeService.getAllRouteItems()) {
			Turnout turnout = TurnoutManagerImpl.getInstance().getTurnoutById(
					routeItem.getTurnoutId());
			routeItem.setTurnout(turnout);
			idToRouteItem.put(routeItem.getId(), routeItem);
		}

		for (Route route : this.routeService.getAllRoutes()) {
			RouteGroup group = idToRouteGroup.get(route.getRouteGroupId());

			if (group == null) {
				LOGGER.error("group null of route " + route.getNumber());
			} else {
				LOGGER.info("route " + route.getNumber() + " belongs to group "
						+ group.getName());
				route.setRouteGroup(group);
				group.addRoute(route);
			}
			for (int id : route.getRouteItemIds()) {
				route.getRouteItems().add(idToRouteItem.get(id));
			}
			idToRoute.put(route.getId(), route);

		}
	}

}
