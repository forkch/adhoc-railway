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
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;

public class RouteManagerImpl implements RouteManager, RouteServiceListener {
	private static Logger LOGGER = Logger.getLogger(RouteManagerImpl.class);

	private static RouteManagerImpl instance;

	private RouteService routeService;

	private final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();

	private final Map<Integer, Route> numberToRouteCache = new HashMap<Integer, Route>();

	private RouteControlIface routeControl;

	private final Set<RouteManagerListener> listeners = new HashSet<RouteManagerListener>();
	private final Set<RouteManagerListener> listenersToBeRemovedInNextEvent = new HashSet<RouteManagerListener>();

	private int lastProgrammedNumber = 0;

	private RouteManagerImpl() {
		LOGGER.info("RouteMangerImpl loaded");

	}

	public static RouteManager getInstance() {
		if (instance == null) {
			instance = new RouteManagerImpl();
		}
		return instance;
	}

	@Override
	public void addRouteManagerListener(final RouteManagerListener listener) {
		this.listeners.add(listener);
		listener.routesUpdated(routeGroups);
	}

	@Override
	public void removeRouteManagerListenerInNextEvent(
			final RouteManagerListener turnoutAddListener) {
		listenersToBeRemovedInNextEvent.add(turnoutAddListener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		clearCache();
		routesUpdated(getAllRouteGroups());
	}

	@Override
	public void clearToService() {
		LOGGER.debug("clearToService()");
		routeService.clear();
	}

	private List<Route> getAllRoutes() {
		LOGGER.debug("getAllRoutes()");
		return new ArrayList<Route>(numberToRouteCache.values());
	}

	@Override
	public Route getRouteByNumber(final int number) {
		LOGGER.debug("getRouteByNumber()");
		for (final Route route : getAllRoutes()) {
			if (route.getNumber() == number) {
				return route;
			}
		}
		return null;
	}

	@Override
	public void addRouteToGroup(final Route route, final RouteGroup group)
			throws RouteManagerException {
		LOGGER.debug("addRouteToGroup()");

		group.addRoute(route);
		route.setRouteGroup(group);
		this.routeService.addRoute(route);
		lastProgrammedNumber = route.getNumber();
	}

	@Override
	public void removeRoute(final Route route) throws RouteManagerException {
		LOGGER.debug("removeRoute()");
		if (!route.getRouteItems().isEmpty()) {
			final SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(
					route.getRouteItems());
			for (final RouteItem routeitem : routeItems) {
				removeRouteItem(routeitem);
			}
		}

		this.routeService.removeRoute(route);
		final RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		final Set<RouteItem> routeItems = route.getRouteItems();
		for (final RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
		}
		removeFromCache(route);
	}

	@Override
	public void updateRoute(final Route route) {
		LOGGER.debug("updateRoute()");
		removeFromCache(route);
		this.routeService.updateRoute(route);
	}

	@Override
	public SortedSet<RouteGroup> getAllRouteGroups() {
		LOGGER.debug("getAllRouteGroups()");
		return routeGroups;
	}

	@Override
	public void addRouteGroup(final RouteGroup routeGroup) {
		LOGGER.debug("addRouteGroup()");
		this.routeService.addRouteGroup(routeGroup);
		routeGroups.add(routeGroup);
	}

	@Override
	public void removeRouteGroup(final RouteGroup routeGroup)
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
	public void updateRouteGroup(final RouteGroup routeGroup) {
		LOGGER.debug("updateRouteGroup()");
		this.routeService.updateRouteGroup(routeGroup);
	}

	@Override
	public void addRouteItem(final RouteItem item) throws RouteManagerException {
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
	public void removeRouteItem(final RouteItem item) {
		LOGGER.debug("removeRouteItem()");

		this.routeService.removeRouteItem(item);
		final Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);

		final Route route = item.getRoute();
		route.getRouteItems().remove(item);
	}

	@Override
	public void updateRouteItem(final RouteItem item) {
		LOGGER.debug("updateRouteItem()");
		this.routeService.updateRouteItem(item);

	}

	@Override
	public int getNextFreeRouteNumber() {
		LOGGER.debug("getNextFreeRouteNumber()");
		// final SortedSet<Route> turnouts = new TreeSet<Route>(getAllRoutes());
		// if (turnouts.isEmpty()) {
		// return 1;
		// }
		// return turnouts.last().getNumber() + 1;
		return lastProgrammedNumber + 1;
	}

	@Override
	public void enlargeRouteGroups() {
		LOGGER.debug("enlargeRouteGroups()");
		int runningNumber = 1;
		for (final RouteGroup group : getAllRouteGroups()) {
			LOGGER.debug("offset of group " + group.getName() + ": "
					+ runningNumber);

			group.setRouteNumberOffset(runningNumber);
			int routesInThisGroup = 0;
			for (final Route route : group.getRoutes()) {
				route.setNumber(runningNumber);
				routesInThisGroup++;
				runningNumber++;
			}

			LOGGER.debug("actual routeNumberAmount "
					+ group.getRouteNumberAmount());
			LOGGER.debug("routes in this group " + routesInThisGroup);
			final int diff = group.getRouteNumberAmount() - routesInThisGroup;
			LOGGER.debug("difference " + diff);
			if (diff <= 5 && diff >= 0) {
				LOGGER.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ (group.getRouteNumberAmount() + 10));
				group.setRouteNumberAmount(group.getRouteNumberAmount() + 10);
			} else if (diff < 0) {
				final int newAmount = (int) Math.ceil(Math.abs(diff) / 10.0) * 10;
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
	public int getNextFreeRouteNumberOfGroup(final RouteGroup routeGroup) {
		final SortedSet<Route> routes = new TreeSet<Route>(
				routeGroup.getRoutes());
		final int offset = routeGroup.getRouteNumberOffset();
		final int amount = routeGroup.getRouteNumberAmount();

		if (routes.isEmpty()) {
			return offset;
		}
		final int nextNumber = routes.last().getNumber() + 1;
		if (nextNumber < offset + amount) {
			return nextNumber;
		}
		return -1;
	}

	@Override
	public void setRouteService(final RouteService instance) {
		this.routeService = instance;
	}

	@Override
	public void initialize() {
		clear();
		cleanupListeners();

		routeService.init(this);
	}

	@Override
	public void setRouteControl(final RouteControlIface routeControl) {
		this.routeControl = routeControl;
	}

	@Override
	public void routesUpdated(final SortedSet<RouteGroup> updatedRouteGroups) {
		LOGGER.info("routesUpdated: " + updatedRouteGroups);
		cleanupListeners();
		clearCache();
		for (final RouteGroup group : updatedRouteGroups) {
			putRouteGroupInCache(group);
			for (final Route route : group.getRoutes()) {
				putInCache(route);
				for (final RouteItem routeItem : route.getRouteItems()) {
					reassignTurnoutToRouteItem(routeItem);
				}
			}
		}
		for (final RouteManagerListener l : listeners) {
			l.routesUpdated(updatedRouteGroups);
		}
	}

	@Override
	public void routeAdded(final Route route) {
		LOGGER.info("routeAdded: " + route);
		cleanupListeners();
		putInCache(route);
		for (final RouteManagerListener l : listeners) {
			l.routeAdded(route);
		}
	}

	@Override
	public void routeUpdated(final Route route) {
		LOGGER.info("routeUpdated: " + route);
		cleanupListeners();
		putInCache(route);
		for (final RouteManagerListener l : listeners) {
			l.routeUpdated(route);
		}
	}

	@Override
	public void routeRemoved(final Route route) {
		LOGGER.info("routeRemoved: " + route);
		cleanupListeners();
		removeFromCache(route);
		for (final RouteManagerListener l : listeners) {
			l.routeRemoved(route);
		}
	}

	@Override
	public void routeGroupAdded(final RouteGroup routeGroup) {
		LOGGER.info("routeGroupAdded: " + routeGroup);
		cleanupListeners();
		putRouteGroupInCache(routeGroup);
		for (final RouteManagerListener l : listeners) {
			l.routeGroupAdded(routeGroup);
		}
	}

	@Override
	public void routeGroupUpdated(final RouteGroup routeGroup) {
		LOGGER.info("routeGroupUpdated: " + routeGroup);
		cleanupListeners();
		removeRouteGroupInCache(routeGroup);
		putRouteGroupInCache(routeGroup);
		for (final RouteManagerListener l : listeners) {
			l.routeGroupUpdated(routeGroup);
		}
	}

	@Override
	public void routeGroupRemoved(final RouteGroup routeGroup) {
		LOGGER.info("routeGroupRemoved: " + routeGroup);
		cleanupListeners();
		removeRouteGroupInCache(routeGroup);
		for (final RouteManagerListener l : listeners) {
			l.routeGroupRemoved(routeGroup);
		}
	}

	@Override
	public void failure(final RouteManagerException routeManagerException) {
		LOGGER.info("failure: " + routeManagerException);
		cleanupListeners();
		for (final RouteManagerListener l : listeners) {
			l.failure(routeManagerException);
		}
	}

	@Override
	public void disconnect() {
		cleanupListeners();
		routeService.disconnect();
		routesUpdated(new TreeSet<RouteGroup>());
	}

	private void reassignTurnoutToRouteItem(final RouteItem routeItem) {
		final TurnoutManager tm = TurnoutManagerImpl.getInstance();

		final int number = routeItem.getTurnout().getNumber();
		routeItem.setTurnout(tm.getTurnoutByNumber(number));
	}

	private void putInCache(final Route route) {
		numberToRouteCache.put(route.getNumber(), route);
		routeControl.addOrUpdateRoute(route);

	}

	private void putRouteGroupInCache(final RouteGroup group) {
		routeGroups.add(group);
	}

	private void removeFromCache(final Route route) {
		numberToRouteCache.values().remove(route.getNumber());
	}

	private void removeRouteGroupInCache(final RouteGroup group) {
		routeGroups.remove(group);
	}

	private void clearCache() {
		routeGroups.clear();
		numberToRouteCache.clear();
	}

}
