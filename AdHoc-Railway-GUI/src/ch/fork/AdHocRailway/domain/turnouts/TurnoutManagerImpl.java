/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryTurnoutPersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.domain.turnouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;
import de.dermoba.srcp.model.SRCPAddress;

public class TurnoutManagerImpl implements TurnoutManager,
		TurnoutServiceListener {
	static Logger LOGGER = Logger.getLogger(TurnoutManagerImpl.class);

	private final Map<SRCPAddress, Turnout> addressTurnoutCache;
	private final Map<SRCPAddress, Turnout> addressThreewayCache;
	private final Map<Integer, Turnout> numberToTurnoutCache;

	private final TurnoutService turnoutService;

	private final SortedSet<TurnoutGroup> turnoutGroups;

	private static TurnoutManager instance = null;

	private TurnoutControlIface turnoutControl = null;

	private final Set<TurnoutManagerListener> listeners = new HashSet<TurnoutManagerListener>();

	private final Set<TurnoutManagerListener> listenersToBeRemovedInNextEvent = new HashSet<TurnoutManagerListener>();

	private TurnoutManagerImpl() {
		LOGGER.info("TurnoutManagerImpl loaded");
		this.addressTurnoutCache = new HashMap<SRCPAddress, Turnout>();
		this.addressThreewayCache = new HashMap<SRCPAddress, Turnout>();
		this.numberToTurnoutCache = new HashMap<Integer, Turnout>();
		this.turnoutGroups = new TreeSet<TurnoutGroup>();

		turnoutService = SIOTurnoutService.getInstance();
		turnoutService.init(this);
	}

	public static TurnoutManager getInstance() {
		if (instance == null) {
			instance = new TurnoutManagerImpl();
		}
		return instance;
	}

	@Override
	public void addTurnoutManagerListener(TurnoutManagerListener listener) {
		this.listeners.add(listener);
		listener.turnoutsUpdated(new ArrayList<TurnoutGroup>(turnoutGroups));
	}

	@Override
	public void removeTurnoutManagerListenerInNextEvent(
			TurnoutManagerListener turnoutAddListener) {
		listenersToBeRemovedInNextEvent.add(turnoutAddListener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		this.addressTurnoutCache.clear();
		this.addressThreewayCache.clear();
		this.numberToTurnoutCache.clear();
		this.turnoutGroups.clear();
	}

	@Override
	public List<Turnout> getAllTurnouts() {
		return new ArrayList<Turnout>(numberToTurnoutCache.values());
	}

	@Override
	public Turnout getTurnoutByNumber(int number)
			throws TurnoutManagerException {
		LOGGER.debug("getTurnoutByNumber()");
		return numberToTurnoutCache.get(number);
	}

	@Override
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		LOGGER.debug("getTurnoutByAddressBus()");
		SRCPAddress key1 = new SRCPAddress(bus, address, 0, 0);
		Turnout lookup1 = addressTurnoutCache.get(key1);
		if (lookup1 != null) {
			return lookup1;
		}
		SRCPAddress key2 = new SRCPAddress(0, 0, bus, address);
		Turnout lookup2 = addressTurnoutCache.get(key2);
		if (lookup2 != null) {
			return lookup2;
		}
		Turnout threewayLookup1 = addressThreewayCache.get(key1);
		if (threewayLookup1 != null) {
			return threewayLookup1;
		}

		Turnout threewayLookup2 = addressThreewayCache.get(key2);
		if (threewayLookup2 != null) {
			return threewayLookup2;
		}

		return null;
	}

	@Override
	public void addTurnout(Turnout turnout) throws TurnoutManagerException {
		LOGGER.debug("addTurnout()");
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutManagerException("Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);
		turnoutService.addTurnout(turnout);

	}

	@Override
	public void removeTurnout(Turnout turnout) {
		LOGGER.debug("removeTurnout(" + turnout + ")");
		turnoutService.deleteTurnout(turnout);
		TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		Set<RouteItem> routeItems = turnout.getRouteItems();
		for (RouteItem ri : routeItems) {
			Route route = ri.getRoute();
			route.getRouteItems().remove(ri);
		}
	}

	@Override
	public void updateTurnout(Turnout turnout) throws TurnoutManagerException {
		LOGGER.debug("updateTurnout()");
		turnoutService.updateTurnout(turnout);
	}

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		return new ArrayList<TurnoutGroup>(turnoutGroups);
	}

	@Override
	public TurnoutGroup getTurnoutGroupByName(String name) {
		LOGGER.debug("getTurnoutGroupByName()");

		for (TurnoutGroup group : turnoutGroups) {
			if (group.getName().equals(name)) {
				return group;
			}
		}
		return null;
	}

	@Override
	public void addTurnoutGroup(TurnoutGroup group) {
		LOGGER.debug("addTurnoutGroup()");
		turnoutService.addTurnoutGroup(group);
	}

	@Override
	public void removeTurnoutGroup(TurnoutGroup group)
			throws TurnoutManagerException {
		LOGGER.debug("removeTurnoutGroup()");
		if (!group.getTurnouts().isEmpty()) {
			SortedSet<Turnout> turnouts = new TreeSet<Turnout>(
					group.getTurnouts());
			for (Turnout turnout : turnouts) {
				removeTurnout(turnout);
			}
		}
	}

	@Override
	public void updateTurnoutGroup(TurnoutGroup group) {
		LOGGER.debug("updateTurnoutGroup()");
		turnoutService.updateTurnoutGroup(group);
	}

	@Override
	public int getNextFreeTurnoutNumber() {
		LOGGER.debug("getNextFreeTurnoutNumber()");
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(getAllTurnouts());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

	@Override
	public Set<Integer> getUsedTurnoutNumbers() {
		LOGGER.debug("getUsedTurnoutNumbers()");
		return numberToTurnoutCache.keySet();
	}

	@Override
	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup) {
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(
				turnoutGroup.getTurnouts());
		int offset = turnoutGroup.getTurnoutNumberOffset();
		int amount = turnoutGroup.getTurnoutNumberAmount();

		if (turnouts.isEmpty()) {
			return offset;
		}
		int nextNumber = turnouts.last().getNumber() + 1;
		if (nextNumber < offset + amount) {
			return nextNumber;
		}
		return -1;
	}

	@Override
	public void enlargeTurnoutGroups() {
		LOGGER.debug("enlargeTurnoutGroups()");
		int runningNumber = 1;
		for (TurnoutGroup group : getAllTurnoutGroups()) {
			LOGGER.debug("offset of group " + group.getName() + ": "
					+ runningNumber);

			group.setTurnoutNumberOffset(runningNumber);
			int turnoutsInThisGroup = 0;
			for (Turnout turnout : group.getTurnouts()) {
				turnout.setNumber(runningNumber);
				turnoutsInThisGroup++;
				runningNumber++;
			}

			LOGGER.debug("actual turnoutNumberAmount "
					+ group.getTurnoutNumberAmount());
			LOGGER.debug("turnouts in this group " + turnoutsInThisGroup);
			int diff = group.getTurnoutNumberAmount() - turnoutsInThisGroup;
			LOGGER.debug("difference " + diff);
			if (diff <= 5 && diff >= 0) {
				LOGGER.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ (group.getTurnoutNumberAmount() + 10));
				group.setTurnoutNumberAmount(group.getTurnoutNumberAmount() + 10);
			} else if (diff < 0) {
				int newAmount = (int) Math.ceil(Math.abs(diff) / 10.0) * 10;
				LOGGER.debug("setting turnout amount of group "
						+ group.getName() + " to " + newAmount);
				group.setTurnoutNumberAmount(newAmount);
			}
			runningNumber = group.getTurnoutNumberOffset()
					+ group.getTurnoutNumberAmount();
			LOGGER.debug("offset of next group: " + runningNumber);
		}
		numberToTurnoutCache.clear();
		for (Turnout t : getAllTurnouts()) {
			numberToTurnoutCache.put(t.getNumber(), t);
		}

	}

	@Override
	public void initialize() {
		cleanupListeners();
	}

	@Override
	public void setTurnoutControl(TurnoutControlIface turnoutControl) {
		this.turnoutControl = turnoutControl;
	}

	@Override
	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups) {
		LOGGER.info("turnoutsUpdated: " + turnoutGroups);
		cleanupListeners();
		clear();
		for (TurnoutGroup group : turnoutGroups) {
			putTurnoutGroupInCache(group);
			for (Turnout turnout : group.getTurnouts()) {
				numberToTurnoutCache.put(turnout.getNumber(), turnout);
				putInCache(turnout);
			}
		}
		for (TurnoutManagerListener l : listeners) {
			l.turnoutsUpdated(turnoutGroups);
		}
	}

	@Override
	public void turnoutAdded(Turnout turnout) {
		LOGGER.info("turnoutAdded: " + turnout);
		cleanupListeners();
		putInCache(turnout);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutAdded(turnout);
		}
	}

	@Override
	public void turnoutUpdated(Turnout turnout) {
		LOGGER.info("turnoutUpdated: " + turnout);
		cleanupListeners();
		putInCache(turnout);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutUpdated(turnout);
		}
	}

	@Override
	public void turnoutRemoved(Turnout turnout) {
		LOGGER.info("turnoutRemoved: " + turnout);
		cleanupListeners();
		removeFromCache(turnout);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutRemoved(turnout);
		}
	}

	@Override
	public void turnoutGroupAdded(TurnoutGroup group) {
		LOGGER.info("turnoutGroupAdded: " + group);
		cleanupListeners();
		putTurnoutGroupInCache(group);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutGroupAdded(group);
		}
	}

	@Override
	public void turnoutGroupUpdated(TurnoutGroup group) {
		LOGGER.info("turnoutGroupUpdated: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		putTurnoutGroupInCache(group);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutGroupUpdated(group);
		}
	}

	@Override
	public void turnoutGroupRemoved(TurnoutGroup group) {
		LOGGER.info("turnoutGroupDeleted: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		for (TurnoutManagerListener l : listeners) {
			l.turnoutGroupRemoved(group);
		}
	}

	@Override
	public void failure(TurnoutManagerException arg0) {
		LOGGER.warn("failure", arg0);
		cleanupListeners();
		for (TurnoutManagerListener l : listeners) {
			l.failure(arg0);
		}
	}

	private void putTurnoutGroupInCache(TurnoutGroup group) {
		turnoutGroups.add(group);
	}

	private void removeTurnoutGroupFromCache(TurnoutGroup group) {
		turnoutGroups.remove(group);
	}

	private void putInCache(Turnout turnout) {
		addressTurnoutCache.put(
				new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
						turnout.getBus2(), turnout.getAddress2()), turnout);
		if (turnout.isThreeWay()) {
			addressThreewayCache.put(
					new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
							0, 0), turnout);
			addressThreewayCache.put(new SRCPAddress(0, 0, turnout.getBus2(),
					turnout.getAddress2()), turnout);
		}
		numberToTurnoutCache.put(turnout.getNumber(), turnout);
		turnoutControl.addOrUpdateTurnout(turnout);
	}

	private void removeFromCache(Turnout turnout) {
		numberToTurnoutCache.values().remove(turnout);
		addressTurnoutCache.values().remove(turnout);
		addressThreewayCache.values().remove(turnout);
	}

}
