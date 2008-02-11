package ch.fork.AdHocRailway.domain.turnouts;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

import com.jgoodies.binding.list.ArrayListModel;

public class MemoryTurnoutPersistence implements TurnoutPersistenceIface {
	static Logger							logger	=
															Logger
																	.getLogger(MemoryTurnoutPersistence.class);
	private static MemoryTurnoutPersistence	instance;

	private Map<LookupAddress, Turnout>		addressTurnoutCache;
	private Map<LookupAddress, Turnout>		addressThreewayCache;
	private ArrayListModel<Turnout>			turnoutCache;
	private ArrayListModel<TurnoutGroup>	turnoutGroupCache;
	private Map<Integer, Turnout>			numberToTurnoutCache;
	private Map<String, TurnoutType>		turnoutTypes;

	private MemoryTurnoutPersistence() {
		super();
		this.addressTurnoutCache = new HashMap<LookupAddress, Turnout>();
		this.addressThreewayCache = new HashMap<LookupAddress, Turnout>();
		this.turnoutCache = new ArrayListModel<Turnout>();
		this.turnoutGroupCache = new ArrayListModel<TurnoutGroup>();
		this.numberToTurnoutCache = new HashMap<Integer, Turnout>();

		this.turnoutTypes = new HashMap<String, TurnoutType>();
		if (getTurnoutType(TurnoutTypes.DEFAULT) == null) {
			TurnoutType defaultType = new TurnoutType(0, "DEFAULT");
			addTurnoutType(defaultType);
		}
		if (getTurnoutType(TurnoutTypes.DOUBLECROSS) == null) {
			TurnoutType doublecrossType = new TurnoutType(0, "DOUBLECROSS");
			addTurnoutType(doublecrossType);
		}
		if (getTurnoutType(TurnoutTypes.THREEWAY) == null) {
			TurnoutType threewayType = new TurnoutType(0, "THREEWAY");
			addTurnoutType(threewayType);
		}
	}

	public static MemoryTurnoutPersistence getInstance() {
		if (instance == null) {
			instance = new MemoryTurnoutPersistence();
		}
		return instance;
	}

	public void clear() {
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
		LookupAddress key1 = new LookupAddress(bus, address, 0, 0);
		Turnout lookup1 = addressTurnoutCache.get(key1);
		if (lookup1 != null)
			return lookup1;
		LookupAddress key2 = new LookupAddress(0, 0, bus, address);
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

		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);

		addressTurnoutCache.put(new LookupAddress(turnout.getBus1(), turnout
				.getAddress1(), turnout.getBus2(), turnout.getAddress2()),
				turnout);
		turnoutCache.add(turnout);
		if (turnout.isThreeWay()) {
			addressThreewayCache.put(new LookupAddress(turnout.getBus1(),
					turnout.getAddress1(), 0, 0), turnout);
			addressThreewayCache.put(new LookupAddress(0, 0, turnout.getBus2(),
					turnout.getAddress2()), turnout);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout) {

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
		logger.debug("getAllTurnoutGroups()");
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
			throw new TurnoutPersistenceException(
					"Cannot delete turnout group with assiciated turnouts");
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
	public TurnoutType getTurnoutType(TurnoutTypes typeName) {
		logger.debug("getTurnoutType()");

		String typeStr = "";
		switch (typeName) {
		case DEFAULT:
			typeStr = "DEFAULT";
			break;
		case DOUBLECROSS:
			typeStr = "DOUBLECROSS";
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
		turnoutTypes.put(type.getTypeName(), type);
	}

	public void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		if (!type.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout type with associated turnouts");
		}
		turnoutTypes.values().remove(type);

	}
}
