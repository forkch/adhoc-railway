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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.services.turnouts.HibernateTurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;

import com.jgoodies.binding.list.ArrayListModel;

import de.dermoba.srcp.model.SRCPAddress;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class TurnoutManagerImpl implements TurnoutManger {
	static Logger LOGGER = Logger.getLogger(TurnoutManagerImpl.class);

	private final Map<SRCPAddress, Turnout> addressTurnoutCache;
	private final Map<SRCPAddress, Turnout> addressThreewayCache;
	private final Map<Integer, Turnout> numberToTurnoutCache;
	private final Map<Integer, Turnout> idToTurnout;
	private final Map<Integer, TurnoutGroup> idToTurnoutGroup;
	private final Map<Integer, TurnoutType> idToTurnoutType;

	private final TurnoutService turnoutService;

	private static TurnoutManagerImpl instance = null;

	private TurnoutManagerImpl() {
		LOGGER.info("TurnoutManagerImpl loaded");
		this.addressTurnoutCache = new HashMap<SRCPAddress, Turnout>();
		this.addressThreewayCache = new HashMap<SRCPAddress, Turnout>();
		this.idToTurnout = new HashMap<Integer, Turnout>();
		this.idToTurnoutGroup = new HashMap<Integer, TurnoutGroup>();
		this.numberToTurnoutCache = new HashMap<Integer, Turnout>();
		this.idToTurnoutType = new HashMap<Integer, TurnoutType>();

		turnoutService = HibernateTurnoutService.getInstance();

		reload();
	}

	public static TurnoutManagerImpl getInstance() {
		if (instance == null)
			instance = new TurnoutManagerImpl();
		return instance;
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		this.addressTurnoutCache.clear();
		this.addressThreewayCache.clear();
		this.idToTurnout.clear();
		this.idToTurnoutGroup.clear();
		this.numberToTurnoutCache.clear();
		this.idToTurnoutType.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#preload()
	 */
	public void preload() throws TurnoutException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts
	 * ()
	 */
	@Override
	public ArrayListModel<Turnout> getAllTurnouts() {
		return new ArrayListModel<Turnout>(idToTurnout.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * getTurnoutByNumber(int)
	 */
	@Override
	public Turnout getTurnoutByNumber(int number)
			throws TurnoutPersistenceException {
		LOGGER.debug("getTurnoutByNumber()");
		return numberToTurnoutCache.get(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * getTurnoutByAddressBus(int, int)
	 */
	@Override
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		LOGGER.debug("getTurnoutByAddressBus()");
		SRCPAddress key1 = new SRCPAddress(bus, address, 0, 0);
		Turnout lookup1 = addressTurnoutCache.get(key1);
		if (lookup1 != null)
			return lookup1;
		SRCPAddress key2 = new SRCPAddress(0, 0, bus, address);
		Turnout lookup2 = addressTurnoutCache.get(key2);
		if (lookup2 != null)
			return lookup2;
		Turnout threewayLookup1 = addressThreewayCache.get(key1);
		if (threewayLookup1 != null)
			return threewayLookup1;

		Turnout threewayLookup2 = addressThreewayCache.get(key2);
		if (threewayLookup2 != null)
			return threewayLookup2;

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
	public void addTurnout(Turnout turnout) throws TurnoutPersistenceException {
		LOGGER.debug("addTurnout()");
		turnoutService.addTurnout(turnout);
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);

		addressTurnoutCache.put(
				new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
						turnout.getBus2(), turnout.getAddress2()), turnout);
		idToTurnout.put(turnout.getId(), turnout);
		if (turnout.isThreeWay()) {
			addressThreewayCache.put(
					new SRCPAddress(turnout.getBus1(), turnout.getAddress1(),
							0, 0), turnout);
			addressThreewayCache.put(new SRCPAddress(0, 0, turnout.getBus2(),
					turnout.getAddress2()), turnout);
		}
		numberToTurnoutCache.put(turnout.getNumber(), turnout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
	public void deleteTurnout(Turnout turnout) {

		LOGGER.debug("deleteTurnout(" + turnout + ")");
		turnoutService.deleteTurnout(turnout);
		TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		TurnoutType type = turnout.getTurnoutType();
		type.getTurnouts().remove(turnout);

		Set<RouteItem> routeItems = turnout.getRouteItems();
		for (RouteItem ri : routeItems) {

			Route route = ri.getRoute();
			route.getRouteItems().remove(ri);

		}

		idToTurnout.remove(turnout);
		numberToTurnoutCache.values().remove(turnout);
		addressTurnoutCache.values().remove(turnout);
		addressThreewayCache.values().remove(turnout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
	public void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		LOGGER.debug("updateTurnout()");
		turnoutService.updateTurnout(turnout);
	}

	@Override
	public ArrayListModel<TurnoutGroup> getAllTurnoutGroups() {
		return new ArrayListModel<TurnoutGroup>(idToTurnoutGroup.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * getTurnoutGroupByName(java.lang.String)
	 */
	@Override
	public TurnoutGroup getTurnoutGroupByName(String name) {
		LOGGER.debug("getTurnoutGroupByName()");

		for (TurnoutGroup group : idToTurnoutGroup.values()) {
			if (group.getName().equals(name))
				return group;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup
	 * (ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void addTurnoutGroup(TurnoutGroup group) {
		LOGGER.debug("addTurnoutGroup()");
		turnoutService.addTurnoutGroup(group);
		idToTurnoutGroup.put(group.getId(), group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		LOGGER.debug("deleteTurnoutGroup()");
		if (!group.getTurnouts().isEmpty()) {
			SortedSet<Turnout> turnouts = new TreeSet<Turnout>(
					group.getTurnouts());
			for (Turnout turnout : turnouts) {
				deleteTurnout(turnout);
			}
		}
		turnoutService.deleteTurnoutGroup(group);
		idToTurnoutGroup.remove(group.getId());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void updateTurnoutGroup(TurnoutGroup group) {
		LOGGER.debug("updateTurnoutGroup()");
		turnoutService.updateTurnoutGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * getAllTurnoutTypes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		LOGGER.debug("getAllTurnoutTypes()");
		return new TreeSet<TurnoutType>(idToTurnoutType.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * getTurnoutTypeByName(java.lang.String)
	 */
	@Override
	public TurnoutType getTurnoutType(SRCPTurnoutTypes typeName) {
		LOGGER.debug("getTurnoutType()");

		String typeStr = "";
		switch (typeName) {
		case DEFAULT:
			typeStr = "DEFAULT";
			break;
		case DOUBLECROSS:
			typeStr = "DOUBLECROSS";
			break;
		case CUTTER:
			typeStr = "CUTTER";
			break;
		case THREEWAY:
			typeStr = "THREEWAY";
			break;
		}
		for (TurnoutType type : idToTurnoutType.values()) {
			if (type.getTypeName().equals(typeStr))
				return type;
		}
		return null;
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
	public void addTurnoutType(TurnoutType type) {
		LOGGER.debug("addTurnoutType()");
		idToTurnoutType.put(type.getId(), type);
	}

	@Override
	public void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		LOGGER.debug("deleteTurnoutType()");
		if (!type.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout type with associated turnouts");
		}
		idToTurnoutType.values().remove(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#flush()
	 */
	@Override
	public void flush() {
		LOGGER.debug("flush()");
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
	public void reload() {
		for (TurnoutGroup group : turnoutService.getAllTurnoutGroups()) {
			idToTurnoutGroup.put(group.getId(), group);
		}
		for (TurnoutType type : turnoutService.getAllTurnoutTypes()) {
			idToTurnoutType.put(type.getId(), type);
		}
		for (Turnout turnout : turnoutService.getAllTurnouts()) {
			idToTurnout.put(turnout.getId(), turnout);
			numberToTurnoutCache.put(turnout.getNumber(), turnout);

			TurnoutGroup group = idToTurnoutGroup.get(turnout
					.getTurnoutGroupId());
			TurnoutType type = idToTurnoutType.get(turnout.getTurnoutTypeId());

			if (type == null) {
				LOGGER.error("type null of turnout " + turnout.getNumber());
			} else {
				LOGGER.info("turnout " + turnout.getNumber()
						+ " belongs to group " + group.getName());
				turnout.setTurnoutGroup(group);
				group.addTurnout(turnout);
			}
			if (group == null) {
				LOGGER.error("group null of turnout " + turnout.getNumber());
			} else {
				LOGGER.info("turnout " + turnout.getNumber() + " has type "
						+ type.getTypeName());
				turnout.setTurnoutType(type);
			}

		}

	}

	public Turnout getTurnoutById(int turnoutId) {
		return idToTurnout.get(turnoutId);
	}
}
