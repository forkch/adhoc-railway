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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerImpl;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIORouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;

public class RouteManagerImpl implements RouteManager, RouteServiceListener {
	private static Logger LOGGER = Logger.getLogger(RouteManagerImpl.class);

	private static RouteManagerImpl instance;

	private final RouteService routeService;

	private final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();

	private final Map<Integer, Route> numberToRouteCache = new HashMap<Integer, Route>();

	private RouteControlIface routeControl;

	private final Set<RouteManagerListener> listeners = new HashSet<RouteManagerListener>();
	private final Set<RouteManagerListener> listenersToBeRemovedInNextEvent = new HashSet<RouteManagerListener>();

	private RouteManagerImpl() {
		LOGGER.info("RouteMangerImpl loaded");
		this.routeService = SIORouteService.getInstance();
		this.routeService.init(this);

	}

	public static RouteManager getInstance() {
		if (instance == null) {
			instance = new RouteManagerImpl();
		}
		return instance;
	}

	@Override
	public void addRouteManagerListener(RouteManagerListener listener) {
		this.listeners.add(listener);
		listener.routesUpdated(new ArrayList<RouteGroup>(routeGroups));
	}

	@Override
	public void removeRouteManagerListenerInNextEvent(
			RouteManagerListener turnoutAddListener) {
		listenersToBeRemovedInNextEvent.add(turnoutAddListener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		routeGroups.clear();
		numberToRouteCache.clear();
	}

	private List<Route> getAllRoutes() {
		LOGGER.debug("getAllRoutes()");
		return new ArrayList<Route>(numberToRouteCache.values());
	}

	@Override
	public Route getRouteByNumber(int number) {
		LOGGER.debug("getRouteByNumber()");
		for (Route route : getAllRoutes()) {
			if (route.getNumber() == number) {
				return route;
			}
		}
		return null;
	}

	@Override
	public void addRoute(Route route) throws RouteManagerException {
		LOGGER.debug("addRoute()");

		if (route.getRouteGroup() == null) {
			throw new RouteManagerException("Route has no associated Group");
		}
		this.routeService.addRoute(route);
	}

	@Override
	public void removeRoute(Route route) throws RouteManagerException {
		LOGGER.debug("removeRoute()");
		if (!route.getRouteItems().isEmpty()) {
			SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(
					route.getRouteItems());
			for (RouteItem routeitem : routeItems) {
				removeRouteItem(routeitem);
			}
		}

		this.routeService.removeRoute(route);
		RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		Set<RouteItem> routeItems = route.getRouteItems();
		for (RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
		}
		removeFromCache(route);
	}

	@Override
	public void updateRoute(Route route) {
		LOGGER.debug("updateRoute()");
		removeFromCache(route);
		this.routeService.updateRoute(route);
	}

	@Override
	public List<RouteGroup> getAllRouteGroups() {
		LOGGER.debug("getAllRouteGroups()");
		return new ArrayList<RouteGroup>(routeGroups);
	}

	@Override
	public void addRouteGroup(RouteGroup routeGroup) {
		LOGGER.debug("addRouteGroup()");
		this.routeService.addRouteGroup(routeGroup);
		routeGroups.add(routeGroup);
	}

	@Override
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException {
		LOGGER.debug("deleteRouteGroup()");
		if (!routeGroup.getRoutes().isEmpty()) {
			throw new RouteManagerException(
					"Cannot delete Route-Group with associated Routes");
		}
		this.routeService.removeRouteGroup(routeGroup);
		routeGroups.remove(routeGroup);
	}

	@Override
	public void updateRouteGroup(RouteGroup routeGroup) {
		LOGGER.debug("updateRouteGroup()");
		this.routeService.updateRouteGroup(routeGroup);
	}

	@Override
	public void addRouteItem(RouteItem item) throws RouteManagerException {
		LOGGER.debug("addRouteItem()");

		if (item.getTurnout() == null) {
			throw new RouteManagerException(
					"RouteItem has no associated Turnout");
		}
		item.getTurnout().getRouteItems().add(item);

		if (item.getRoute() == null) {
			throw new RouteManagerException("RouteItem has no associated Route");
		}
		this.routeService.addRouteItem(item);
		item.getRoute().getRouteItems().add(item);
	}

	@Override
	public void removeRouteItem(RouteItem item) {
		LOGGER.debug("removeRouteItem()");

		this.routeService.removeRouteItem(item);
		Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);

		Route route = item.getRoute();
		route.getRouteItems().remove(item);
	}

	@Override
	public void updateRouteItem(RouteItem item) {
		LOGGER.debug("updateRouteItem()");
		this.routeService.updateRouteItem(item);

	}

	@Override
	public int getNextFreeRouteNumber() {
		LOGGER.debug("getNextFreeRouteNumber()");
		SortedSet<Route> turnouts = new TreeSet<Route>(getAllRoutes());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
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

	private void reassignTurnoutToRouteItem(RouteItem routeItem) {
		TurnoutManager tm = TurnoutManagerImpl.getInstance();

		int number = routeItem.getTurnout().getNumber();
		routeItem.setTurnout(tm.getTurnoutByNumber(number));
	}

	private void putInCache(Route route) {
		numberToRouteCache.put(route.getNumber(), route);
		routeControl.addOrUpdateRoute(route);

	}

	private void putRouteGroupInCache(RouteGroup group) {
		routeGroups.add(group);
	}

	private void removeFromCache(Route route) {
		numberToRouteCache.values().remove(route.getNumber());
	}

	private void removeRouteGroupInCache(RouteGroup group) {
		routeGroups.remove(group);
	}

	@Override
	public void initialize() {
		cleanupListeners();
	}

	@Override
	public void setRouteControl(RouteControlIface routeControl) {
		this.routeControl = routeControl;
	}

	@Override
	public void routesUpdated(List<RouteGroup> allRouteGroups) {
		for (RouteGroup group : allRouteGroups) {
			routeGroups.add(group);
			for (Route route : group.getRoutes()) {
				putInCache(route);
				for (RouteItem routeItem : route.getRouteItems()) {
					reassignTurnoutToRouteItem(routeItem);
				}
			}
		}
		for (RouteManagerListener l : listeners) {
			l.routesUpdated(allRouteGroups);
		}
	}

	@Override
	public void routeAdded(Route route) {
		LOGGER.info("routeAdded: " + route);
		putInCache(route);
		for (RouteManagerListener l : listeners) {
			l.routeAdded(route);
		}
	}

	@Override
	public void routeUpdated(Route route) {
		LOGGER.info("routeUpdated: " + route);
		cleanupListeners();
		putInCache(route);
		for (RouteManagerListener l : listeners) {
			l.routeUpdated(route);
		}
	}

	@Override
	public void routeRemoved(Route route) {
		LOGGER.info("routeRemoved: " + route);
		removeFromCache(route);
		for (RouteManagerListener l : listeners) {
			l.routeRemoved(route);
		}
	}

	@Override
	public void routeGroupAdded(RouteGroup routeGroup) {
		LOGGER.info("routeGroupAdded: " + routeGroup);
		cleanupListeners();
		putRouteGroupInCache(routeGroup);
		for (RouteManagerListener l : listeners) {
			l.routeGroupAdded(routeGroup);
		}
	}

	@Override
	public void routeGroupUpdated(RouteGroup routeGroup) {
		LOGGER.info("routeGroupUpdated: " + routeGroup);
		cleanupListeners();
		removeRouteGroupInCache(routeGroup);
		putRouteGroupInCache(routeGroup);
		for (RouteManagerListener l : listeners) {
			l.routeGroupRemoved(routeGroup);
		}
	}

	@Override
	public void routeGroupRemoved(RouteGroup routeGroup) {
		LOGGER.info("routeGroupRemoved: " + routeGroup);
		cleanupListeners();
		removeRouteGroupInCache(routeGroup);
		for (RouteManagerListener l : listeners) {
			l.routeGroupRemoved(routeGroup);
		}
	}

	@Override
	public void failure(RouteManagerException turnoutManagerException) {
		// TODO Auto-generated method stub

	}

}
