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

import com.jgoodies.binding.list.ArrayListModel;

import de.dermoba.srcp.model.SRCPAddress;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public abstract class CachingTurnoutPersistence implements TurnoutPersistenceIface {
	static Logger							logger	= Logger
															.getLogger(CachingTurnoutPersistence.class);
	
	private Map<SRCPAddress, Turnout>		addressTurnoutCache;
	private Map<SRCPAddress, Turnout>		addressThreewayCache;
	private ArrayListModel<Turnout>			turnoutCache;
	private ArrayListModel<TurnoutGroup>	turnoutGroupCache;
	private Map<Integer, Turnout>			numberToTurnoutCache;
	private Map<String, TurnoutType>		turnoutTypes;

	public CachingTurnoutPersistence() {
		logger.info("CachingTurnoutPersistence loaded");
		this.addressTurnoutCache = new HashMap<SRCPAddress, Turnout>();
		this.addressThreewayCache = new HashMap<SRCPAddress, Turnout>();
		this.turnoutCache = new ArrayListModel<Turnout>();
		this.turnoutGroupCache = new ArrayListModel<TurnoutGroup>();
		this.numberToTurnoutCache = new HashMap<Integer, Turnout>();
		this.turnoutTypes = new HashMap<String, TurnoutType>();
	}

	public void clear() {
		logger.debug("clear()");
		this.addressTurnoutCache.clear();
		this.addressThreewayCache.clear();
		this.turnoutCache.clear();
		this.turnoutGroupCache.clear();
		this.numberToTurnoutCache.clear();
		this.turnoutTypes.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#preload()
	 */
	public void preload() throws TurnoutException {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	public ArrayListModel<Turnout> getAllTurnouts() {
		//logger.debug("getAllTurnouts()");
		return turnoutCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByNumber(int)
	 */
	public Turnout getTurnoutByNumber(int number)
			throws TurnoutPersistenceException {
		logger.debug("getTurnoutByNumber()");
		return numberToTurnoutCache.get(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByAddressBus(int,
	 *      int)
	 */
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		logger.debug("getTurnoutByAddressBus()");
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
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void addTurnout(Turnout turnout) throws TurnoutPersistenceException {

		logger.debug("addTurnout()");
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);

		addressTurnoutCache.put(new SRCPAddress(turnout.getBus1(), turnout
				.getAddress1(), turnout.getBus2(), turnout.getAddress2()),
				turnout);
		turnoutCache.add(turnout);
		if (turnout.isThreeWay()) {
			addressThreewayCache.put(new SRCPAddress(turnout.getBus1(),
					turnout.getAddress1(), 0, 0), turnout);
			addressThreewayCache.put(new SRCPAddress(0, 0, turnout.getBus2(),
					turnout.getAddress2()), turnout);
		}
		numberToTurnoutCache.put(turnout.getNumber(), turnout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout) {

		logger.debug("deleteTurnout(" + turnout + ")");
		TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		TurnoutType type = turnout.getTurnoutType();
		type.getTurnouts().remove(turnout);

		Set<RouteItem> routeItems = turnout.getRouteItems();
		for (RouteItem ri : routeItems) {

			Route route = ri.getRoute();
			route.getRouteItems().remove(ri);

		}

		turnoutCache.remove(turnout);
		numberToTurnoutCache.values().remove(turnout);
		addressTurnoutCache.values().remove(turnout);
		addressThreewayCache.values().remove(turnout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnout()");
	}

	public ArrayListModel<TurnoutGroup> getAllTurnoutGroups() {
		//logger.debug("getAllTurnoutGroups()");
		return turnoutGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutGroupByName(java.lang.String)
	 */
	public TurnoutGroup getTurnoutGroupByName(String name) {
		logger.debug("getTurnoutGroupByName()");

		for (TurnoutGroup group : turnoutGroupCache) {
			if (group.getName().equals(name))
				return group;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group) {
		logger.debug("addTurnoutGroup()");
		turnoutGroupCache.add(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutGroup()");
		if (!group.getTurnouts().isEmpty()) {
			SortedSet<Turnout> turnouts = new TreeSet<Turnout>(group.getTurnouts());
			for (Turnout turnout : turnouts) {
				deleteTurnout(turnout);
			}
			//throw new TurnoutPersistenceException(
			//		"Cannot delete turnout group with assiciated turnouts");
		}
		turnoutGroupCache.remove(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void updateTurnoutGroup(TurnoutGroup group) {
		logger.debug("updateTurnoutGroup()");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		logger.debug("getAllTurnoutTypes()");
		return new TreeSet<TurnoutType>(turnoutTypes.values());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutTypeByName(java.lang.String)
	 */
	public TurnoutType getTurnoutType(SRCPTurnoutTypes typeName) {
		logger.debug("getTurnoutType()");

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
		for (TurnoutType type : turnoutTypes.values()) {
			if (type.getTypeName().equals(typeStr))
				return type;
		}
		return null;
	}

	public int getNextFreeTurnoutNumber() {
		logger.debug("getNextFreeTurnoutNumber()");
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(getAllTurnouts());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

	public Set<Integer> getUsedTurnoutNumbers() {
		logger.debug("getUsedTurnoutNumbers()");
		return numberToTurnoutCache.keySet();
	}

	public void addTurnoutType(TurnoutType type) {
		logger.debug("addTurnoutType()");
		turnoutTypes.put(type.getTypeName(), type);
	}

	public void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutType()");
		if (!type.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout type with associated turnouts");
		}
		turnoutTypes.values().remove(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#flush()
	 */
	public void flush() {
		logger.debug("flush()");
	}

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup) {
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(turnoutGroup
				.getTurnouts());
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

	public void enlargeTurnoutGroups() {
		logger.debug("enlargeTurnoutGroups()");
		int runningNumber = 1;
		for (TurnoutGroup group : getAllTurnoutGroups()) {
			logger.debug("offset of group " + group.getName() + ": "
					+ runningNumber);
			
			group.setTurnoutNumberOffset(runningNumber);
			int turnoutsInThisGroup = 0;
			for (Turnout turnout : group.getTurnouts()) {
				turnout.setNumber(runningNumber);
				turnoutsInThisGroup++;
				runningNumber++;
			}

			logger.debug("actual turnoutNumberAmount " + group.getTurnoutNumberAmount());
			logger.debug("turnouts in this group " + turnoutsInThisGroup);
			int diff = group.getTurnoutNumberAmount() - turnoutsInThisGroup;
			logger.debug("difference " + diff);
			if (diff <= 5 && diff >= 0) {
				logger.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ (group.getTurnoutNumberAmount() + 10));
				group
						.setTurnoutNumberAmount(group.getTurnoutNumberAmount() + 10);
			} else if(diff < 0) {
				int newAmount = (int)Math.ceil(Math.abs(diff)/10.0)*10;
				logger.debug("setting turnout amount of group "
						+ group.getName() + " to "
						+ newAmount);
				group
						.setTurnoutNumberAmount(newAmount);
			}
			runningNumber = group.getTurnoutNumberOffset() + group.getTurnoutNumberAmount();
			logger.debug("offset of next group: " + runningNumber);
		}
		numberToTurnoutCache.clear();
		for(Turnout t : getAllTurnouts()) {
			numberToTurnoutCache.put(t.getNumber(), t);
		}
		
		
	}
}
