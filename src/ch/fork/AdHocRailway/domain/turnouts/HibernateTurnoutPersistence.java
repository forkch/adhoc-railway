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

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateTurnoutPersistence extends CachingTurnoutPersistence
		implements TurnoutPersistenceIface {
	static Logger							logger	= Logger
															.getLogger(HibernateTurnoutPersistence.class);
	private static TurnoutPersistenceIface	instance;

	private HibernateTurnoutPersistence() {
		logger.info("HibernateTurnoutPersistence loaded");

		updateTurnoutTypeCache();
		updateTurnoutCache();
		updateTurnoutGroupCache();
	}

	public static TurnoutPersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutPersistence();
		}
		return instance;
	}

	private void updateTurnoutTypeCache() {
		for (TurnoutType type : getAllTurnoutTypesDB()) {
			super.addTurnoutType(type);
		}
	}

	private void updateTurnoutCache() {
		SortedSet<Turnout> turnouts = getAllTurnoutsDB();
		for (Turnout t : turnouts) {
			super.addTurnout(t);
		}
	}

	private void updateTurnoutGroupCache() {
		for (TurnoutGroup group : getAllTurnoutGroupsDB()) {
			super.addTurnoutGroup(group);
		}
	}

	public void clear() throws TurnoutPersistenceException {
		logger.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE turnout").executeUpdate();
			// em.createNativeQuery("TRUNCATE TABLE
			// turnout_type").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE turnout_group")
					.executeUpdate();

			super.clear();
			em.getTransaction().commit();
			HibernatePersistence.disconnect();
			HibernatePersistence.connect();
			updateTurnoutTypeCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	public ArrayListModel<Turnout> getAllTurnouts()
			throws TurnoutPersistenceException {
		// logger.debug("getAllTurnouts()");

		if (super.getAllTurnouts().size() == 0) {
			updateTurnoutCache();
		}
		return super.getAllTurnouts();
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Turnout> getAllTurnoutsDB()
			throws TurnoutPersistenceException {
		logger.debug("getAllTurnoutsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List turnouts = em.createQuery("from Turnout").getResultList();
			SortedSet<Turnout> res = new TreeSet<Turnout>(turnouts);
			return res;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByNumber(int)
	 */
	public Turnout getTurnoutByNumber(int number) {
		logger.debug("getTurnoutByNumber()");
		return super.getTurnoutByNumber(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByAddressBus(int,
	 *      int)
	 */
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		logger.debug("getTurnoutByAddressBus()");
		return super.getTurnoutByAddressBus(bus, address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void addTurnout(Turnout turnout) throws TurnoutPersistenceException {
		logger.debug("addTurnout()");
		EntityManager em = HibernatePersistence.getEntityManager();
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);
		try {
			em.persist(turnout);
			HibernatePersistence.flush();
			super.addTurnout(turnout);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
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
		EntityManager em = HibernatePersistence.getEntityManager();

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
			HibernatePersistence.flush();

			super.deleteTurnout(turnout);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
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
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(turnout);

			HibernatePersistence.flush();
			super.updateTurnout(turnout);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	public ArrayListModel<TurnoutGroup> getAllTurnoutGroups() {
		// logger.debug("getAllTurnoutGroups()");
		if (super.getAllTurnoutGroups().isEmpty()) {
			updateTurnoutGroupCache();
		}
		return super.getAllTurnoutGroups();
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
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<TurnoutGroup> groups = em.createQuery("from TurnoutGroup")
					.getResultList();

			em.getTransaction().commit();
			em.getTransaction().begin();
			return new TreeSet<TurnoutGroup>(groups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutGroupByName(java.lang.String)
	 */
	public TurnoutGroup getTurnoutGroupByName(String name) {
		logger.debug("getTurnoutGroupByName()");
		return super.getTurnoutGroupByName(name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(group);

			HibernatePersistence.flush();
			super.addTurnoutGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
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
		EntityManager em = HibernatePersistence.getEntityManager();

		if (!group.getTurnouts().isEmpty()) {
			SortedSet<Turnout> turnouts = new TreeSet<Turnout>(group
					.getTurnouts());
			for (Turnout turnout : turnouts) {
				deleteTurnout(turnout);
			}
			// throw new TurnoutPersistenceException(
			// "Cannot delete turnout group with associated turnouts");
		}
		try {
			em.remove(group);
			HibernatePersistence.flush();
			super.deleteTurnoutGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
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
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(group);
			HibernatePersistence.flush();
			super.updateTurnoutGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutTypes()
	 */
	public SortedSet<TurnoutType> getAllTurnoutTypes()
			throws TurnoutPersistenceException {
		logger.debug("getAllTurnoutTypes()");
		return super.getAllTurnoutTypes();
	}

	@SuppressWarnings("unchecked")
	private SortedSet<TurnoutType> getAllTurnoutTypesDB() {
		logger.debug("getAllTurnoutTypessDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List turnoutTypes = em.createQuery("from TurnoutType")
					.getResultList();
			SortedSet<TurnoutType> res = new TreeSet<TurnoutType>(turnoutTypes);

			return res;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutTypeByName(java.lang.String)
	 */
	public TurnoutType getTurnoutType(SRCPTurnoutTypes typeName)
			throws TurnoutPersistenceException {
		logger.debug("getTurnoutType()");
		return super.getTurnoutType(typeName);
	}

	public void addTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			if (getTurnoutType(type.getTurnoutTypeEnum()) == null) {
				em.persist(type);
				HibernatePersistence.flush();
				super.addTurnoutType(type);
			}
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	public void deleteTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		if (!type.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout type with associated turnouts");
		}
		try {
			em.remove(type);
			HibernatePersistence.flush();
			super.deleteTurnoutType(type);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new TurnoutPersistenceException("Database Error", x);
		}
	}

	public int getNextFreeTurnoutNumber() {
		logger.debug("getNextFreeTurnoutNumber()");
		return super.getNextFreeTurnoutNumber();
	}

	public int getNextFreeTurnoutNumberOfGroup(TurnoutGroup turnoutGroup) {
		logger.debug("getNextFreeTurnoutNumberOfGroup()");
		return super.getNextFreeTurnoutNumberOfGroup(turnoutGroup);
	}

	public Set<Integer> getUsedTurnoutNumbers() {
		logger.debug("getUsedTurnoutNumbers()");
		return super.getUsedTurnoutNumbers();
	}

	public void enlargeTurnoutGroups() {
		logger.debug("enlargeTurnoutGroups()");
		super.enlargeTurnoutGroups();

		HibernatePersistence.flush();
	}

	public void flush() {
		HibernatePersistence.flush();
	}
}
