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

package ch.fork.AdHocRailway.services.turnouts;

import java.util.ArrayList;
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
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;

public class HibernateTurnoutService implements TurnoutService {
	static Logger logger = Logger.getLogger(HibernateTurnoutService.class);
	private static HibernateTurnoutService instance;

	private HibernateTurnoutService() {
		logger.info("HibernateTurnoutPersistence loaded");
	}

	public static TurnoutService getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutService();
		}
		return instance;
	}

	@Override
	public void clear() {
		logger.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE turnout").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE turnout_group")
					.executeUpdate();

			em.getTransaction().commit();
			HibernatePersistence.disconnect();
			HibernatePersistence.connect();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
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
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
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
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout
	 * (ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	@Override
	public void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnout()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(turnout);
			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup
	 * (ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void addTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(group);

			HibernatePersistence.flush();
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
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("deleteTurnoutGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();

		if (!group.getTurnouts().isEmpty()) {
			SortedSet<Turnout> turnouts = new TreeSet<Turnout>(
					group.getTurnouts());
			for (Turnout turnout : turnouts) {
				deleteTurnout(turnout);
			}
			// throw new TurnoutPersistenceException(
			// "Cannot delete turnout group with associated turnouts");
		}
		try {
			em.remove(group);
			HibernatePersistence.flush();
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
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#
	 * updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	@Override
	public void updateTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnoutGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(group);
			HibernatePersistence.flush();
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

	@Override
	public void addTurnoutType(TurnoutType type)
			throws TurnoutPersistenceException {
		logger.debug("addTurnoutType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(type);
			HibernatePersistence.flush();
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

	@Override
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

	@Override
	public void flush() {
		HibernatePersistence.flush();
	}

	@Override
	public List<Turnout> getAllTurnouts() {
		logger.debug("getAllTurnoutsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateTurnout> hTurnouts = em.createQuery(
					"from HibernateTurnout").getResultList();
			List<Turnout> turnouts = new ArrayList<Turnout>();

			for (HibernateTurnout hTurnout : hTurnouts) {
				Turnout turnout = HibernateTurnoutMapper.map(hTurnout);
				turnouts.add(turnout);
			}

			return turnouts;
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

	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		logger.debug("getAllTurnoutGroups()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateTurnoutGroup> hTurnoutGroups = em.createQuery(
					"from HibernateTurnoutGroup").getResultList();

			List<TurnoutGroup> turnoutGroups = new ArrayList<TurnoutGroup>();
			for (HibernateTurnoutGroup hGroup : hTurnoutGroups) {
				TurnoutGroup group = HibernateTurnoutMapper.map(hGroup);
				turnoutGroups.add(group);
			}
			return turnoutGroups;
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

	@Override
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		logger.debug("getAllTurnoutTypessDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateTurnoutType> hTurnoutTypes = em.createQuery(
					"from HibernateTurnoutType").getResultList();
			SortedSet<TurnoutType> turnoutTypes = new TreeSet<TurnoutType>();

			for (HibernateTurnoutType hType : hTurnoutTypes) {
				TurnoutType type = HibernateTurnoutMapper.map(hType);
				turnoutTypes.add(type);
			}

			return turnoutTypes;
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
}
