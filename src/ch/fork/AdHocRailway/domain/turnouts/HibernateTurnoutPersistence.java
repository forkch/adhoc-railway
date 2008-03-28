/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateTurnoutPersistence extends HibernatePersistence implements
		TurnoutPersistenceIface {
	static Logger							logger	= Logger
															.getLogger(HibernateTurnoutPersistence.class);
	private static TurnoutPersistenceIface	instance;

	private Map<LookupAddress, Turnout>		addressTurnoutCache;
	private Map<LookupAddress, Turnout>		addressThreewayCache;
	private ArrayListModel<Turnout>			turnoutCache;
	private ArrayListModel<TurnoutGroup>	turnoutGroupCache;
	private Map<Integer, Turnout>			numberToTurnoutCache;

	private HibernateTurnoutPersistence() {
		logger.info("HibernateTurnoutPersistence loaded");
		this.addressTurnoutCache = new HashMap<LookupAddress, Turnout>();
		this.addressThreewayCache = new HashMap<LookupAddress, Turnout>();
		this.turnoutCache = new ArrayListModel<Turnout>();
		this.turnoutGroupCache = new ArrayListModel<TurnoutGroup>();
		this.numberToTurnoutCache = new HashMap<Integer, Turnout>();

		try {
			getTurnoutType(TurnoutTypes.DEFAULT);
		} catch (NoResultException ex) {
			TurnoutType defaultType = new TurnoutType(0, "DEFAULT");
			try {
				addTurnoutType(defaultType);
			} catch (TurnoutPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			getTurnoutType(TurnoutTypes.DOUBLECROSS);
		} catch (NoResultException ex) {
			TurnoutType doublecrossType = new TurnoutType(0, "DOUBLECROSS");
			try {
				addTurnoutType(doublecrossType);
			} catch (TurnoutPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			getTurnoutType(TurnoutTypes.THREEWAY);
		} catch (NoResultException ex) {
			TurnoutType threewayType = new TurnoutType(0, "THREEWAY");
			try {
				addTurnoutType(threewayType);
			} catch (TurnoutPersistenceException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		updateTurnoutCache();
		updateTurnoutGroupCache();
	}

	public static TurnoutPersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutPersistence();
		}
		return instance;
	}

	private void updateTurnoutCache() {
		addressTurnoutCache.clear();
		addressThreewayCache.clear();
		numberToTurnoutCache.clear();
		turnoutCache.clear();
		SortedSet<Turnout> turnouts = getAllTurnoutsDB();
		for (Turnout t : turnouts) {
			turnoutCache.add(t);
			numberToTurnoutCache.put(t.getNumber(), t);
		}

		for (Turnout t : turnouts) {
			addressTurnoutCache.put(new LookupAddress(t.getBus1(), t
					.getAddress1(), t.getBus2(), t.getAddress2()), t);
			if (t.isThreeWay()) {
				addressThreewayCache.put(new LookupAddress(t.getBus1(), t
						.getAddress1(), 0, 0), t);
				addressThreewayCache.put(new LookupAddress(0, 0, t.getBus2(), t
						.getAddress2()), t);
			}
		}

	}

	private void updateTurnoutGroupCache() {
		turnoutGroupCache.clear();
		turnoutGroupCache.addAll(getAllTurnoutGroupsDB());
	}

	public void clear() throws TurnoutPersistenceException {
		logger.debug("clear()");
		EntityManager em = getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE turnout").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE turnout_type").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE turnout_group")
					.executeUpdate();
			turnoutGroupCache.clear();
			addressTurnoutCache.clear();
			addressThreewayCache.clear();
			numberToTurnoutCache.clear();
			turnoutCache.clear();
			em.getTransaction().commit();
			HibernatePersistence.em = emf.createEntityManager();
			HibernatePersistence.em.getTransaction().begin();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	public ArrayListModel<Turnout> getAllTurnouts()
			throws TurnoutPersistenceException {
		//logger.debug("getAllTurnouts()");
		if (addressTurnoutCache.size() == 0) {
			updateTurnoutCache();
		}
		return turnoutCache;
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Turnout> getAllTurnoutsDB()
			throws TurnoutPersistenceException {
		logger.debug("getAllTurnoutsDB()");
		EntityManager em = getEntityManager();
		try {
			List<Turnout> turnouts = em.createQuery("from Turnout")
					.getResultList();
			SortedSet<Turnout> res = new TreeSet<Turnout>(turnouts);
			return res;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByNumber(int)
	 */
	public Turnout getTurnoutByNumber(int number) {
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
		logger.debug("addTurnout()");
		EntityManager em = getEntityManager();
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);
		try {
			em.persist(turnout);
			flush();
			updateTurnoutCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnout()");
		EntityManager em = getEntityManager();

		TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		TurnoutType type = turnout.getTurnoutType();
		type.getTurnouts().remove(turnout);

		Set<RouteItem> routeItems = turnout.getRouteItems();
		for (RouteItem ri : routeItems) {

			Route route = ri.getRoute();
			route.getRouteItems().remove(ri);
			em.remove(ri);

		}
		try {
			em.remove(turnout);
			flush();

			updateTurnoutCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnout()");
		EntityManager em = getEntityManager();
		try {
			em.merge(turnout);
			flush();
			updateTurnoutCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	public ArrayListModel<TurnoutGroup> getAllTurnoutGroups() {
		//logger.debug("getAllTurnoutGroups()");
		if (turnoutGroupCache.isEmpty()) {
			updateTurnoutGroupCache();
		}
		return turnoutGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutGroup> getAllTurnoutGroupsDB()
			throws TurnoutPersistenceException {
		logger.debug("getAllTurnoutGroups()");
		EntityManager em = getEntityManager();
		try {
			List<TurnoutGroup> groups = em.createQuery("from TurnoutGroup")
					.getResultList();

			// em.getTransaction().commit();
			// em.getTransaction().begin();
			return new TreeSet<TurnoutGroup>(groups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutGroupByName(java.lang.String)
	 */
	public TurnoutGroup getTurnoutGroupByName(String name) {
		logger.debug("getTurnoutGroupByName()");
		for (TurnoutGroup group : turnoutGroupCache)
			if (group.getName().equals(name))
				return group;
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutGroup()");
		EntityManager em = getEntityManager();
		try {
			em.persist(group);

			flush();
			updateTurnoutGroupCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutGroup()");
		EntityManager em = getEntityManager();
		if (!group.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout group with associated turnouts");
		}
		try {
			em.remove(group);
			flush();
			updateTurnoutGroupCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void updateTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnoutGroup()");
		EntityManager em = getEntityManager();
		try {
			em.merge(group);
			flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes()
			throws TurnoutPersistenceException {
		logger.debug("getAllTurnoutTypes()");
		EntityManager em = getEntityManager();
		List<TurnoutType> types = em.createQuery("from TurnoutType")
				.getResultList();

		return new TreeSet<TurnoutType>(types);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutTypeByName(java.lang.String)
	 */
	public TurnoutType getTurnoutType(TurnoutTypes typeName)
			throws TurnoutPersistenceException {
		logger.debug("getTurnoutType()");
		EntityManager em = getEntityManager();
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
		try {
			TurnoutType turnoutType = (TurnoutType) em.createQuery(
					"from TurnoutType as t where t.typeName = ?1")
					.setParameter(1, typeStr).getSingleResult();

			return turnoutType;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	public int getNextFreeTurnoutNumber() {
		logger.debug("getNextFreeTurnoutNumber()");
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(getAllTurnouts());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup) {
		logger.debug("getNextFreeTurnoutNumberOfGroup()");
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

	public Set<Integer> getUsedTurnoutNumbers() {
		logger.debug("getUsedTurnoutNumbers()");
		return numberToTurnoutCache.keySet();
	}

	public void addTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutType()");
		EntityManager em = getEntityManager();
		try {
			em.persist(type);
			flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	public void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutType()");
		EntityManager em = getEntityManager();
		if (!type.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout type with associated turnouts");
		}
		try {
			em.remove(type);
			flush();
			updateTurnoutGroupCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#flush()
	 */
	public void flush() throws TurnoutPersistenceException {
		logger.debug("flush()");
		try {
			em.getTransaction().commit();
		} catch (HibernateException ex) {
			em.getTransaction().rollback();
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			em.getTransaction().begin();
			throw new TurnoutPersistenceException("Error", ex);
		}
		em.getTransaction().begin();
	}
}
