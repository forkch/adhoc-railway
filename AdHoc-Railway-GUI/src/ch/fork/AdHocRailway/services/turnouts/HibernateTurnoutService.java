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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.services.HibernateUtil;

public class HibernateTurnoutService implements TurnoutService {
	static Logger LOGGER = Logger.getLogger(HibernateTurnoutService.class);
	private static HibernateTurnoutService instance;

	private HibernateTurnoutService() {
		LOGGER.info("HibernateTurnoutPersistence loaded");
	}

	public static TurnoutService getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutService();
		}
		return instance;
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE turnout").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE turnout_group")
					.executeUpdate();

			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
	public void addTurnout(Turnout turnout) throws TurnoutManagerException {
		LOGGER.debug("addTurnout()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateTurnout hTurnout = HibernateTurnoutMapper.map(turnout);
			Integer turnoutGroupId = turnout.getTurnoutGroup().getId();
			HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, turnoutGroupId);
			hTurnoutGroup.getTurnouts().add(hTurnout);
			hTurnout.setTurnoutGroup(hTurnoutGroup);
			Integer id = (Integer) session.save(hTurnout);

			turnout.setId(id);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
	public void deleteTurnout(Turnout turnout) throws TurnoutManagerException {
		LOGGER.debug("deleteTurnout()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Integer id = turnout.getId();

			HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, id);
			session.delete(hTurnout);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
	public void updateTurnout(Turnout turnout) throws TurnoutManagerException {
		LOGGER.debug("updateTurnout()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = turnout.getId();
			HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, id);

			HibernateTurnoutMapper.updateHibernate(hTurnout, turnout);
			session.update(hTurnout);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
			throws TurnoutManagerException {
		LOGGER.debug("addTurnoutGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateTurnoutGroup hTurnoutGroup = HibernateTurnoutMapper
					.map(group);
			Integer id = (Integer) session.save(hTurnoutGroup);

			group.setId(id);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
			throws TurnoutManagerException {
		LOGGER.debug("deleteTurnoutGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = group.getId();

			HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, id);
			session.delete(hTurnoutGroup);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
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
			throws TurnoutManagerException {
		LOGGER.debug("updateTurnoutGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = group.getId();
			HibernateTurnoutGroup hTurnout = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, id);

			session.update(hTurnout);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void flush() {
		HibernatePersistence.flush();
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<TurnoutGroup> getAllTurnoutGroups() {
		LOGGER.debug("getAllTurnoutGroups()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List hTurnoutGroups = session.createQuery(
					"from HibernateTurnoutGroup").list();
			List<TurnoutGroup> turnoutGroups = new LinkedList<TurnoutGroup>();
			for (Iterator iterator = hTurnoutGroups.iterator(); iterator
					.hasNext();) {
				HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) iterator
						.next();
				TurnoutGroup turnoutGroup = HibernateTurnoutMapper
						.map(hTurnoutGroup);
				turnoutGroups.add(turnoutGroup);
			}
			transaction.commit();
			return turnoutGroups;
		} catch (HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

}
