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

package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.services.impl.hibernate.HibernateUtil;
import ch.fork.AdHocRailway.services.turnouts.TurnoutService;
import ch.fork.AdHocRailway.services.turnouts.TurnoutServiceListener;

public class HibernateTurnoutService implements TurnoutService {
	static Logger LOGGER = Logger.getLogger(HibernateTurnoutService.class);
	private static final HibernateTurnoutService INSTANCE = new HibernateTurnoutService();
	private TurnoutServiceListener listener;

	private HibernateTurnoutService() {
		LOGGER.info("HibernateTurnoutPersistence loaded");
	}

	public static TurnoutService getInstance() {
		return INSTANCE;
	}

	@Override
	public void clear() {
		LOGGER.debug("clear()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE turnout").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE turnout_group")
					.executeUpdate();

			transaction.commit();
		} catch (final HibernateException x) {
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
	public void addTurnout(final Turnout turnout)
			throws TurnoutManagerException {
		LOGGER.debug("addTurnout()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateTurnout hTurnout = HibernateTurnoutMapper
					.map(turnout);
			final Integer turnoutGroupId = turnout.getTurnoutGroup().getId();
			final HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, turnoutGroupId);
			hTurnoutGroup.getTurnouts().add(hTurnout);
			hTurnout.setTurnoutGroup(hTurnoutGroup);
			final Integer id = (Integer) session.save(hTurnout);

			turnout.setId(id);
			transaction.commit();
			listener.turnoutAdded(turnout);
		} catch (final HibernateException x) {
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
	public void removeTurnout(final Turnout turnout)
			throws TurnoutManagerException {
		LOGGER.debug("deleteTurnout()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final Integer id = turnout.getId();

			final HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, id);
			session.delete(hTurnout);
			transaction.commit();
			listener.turnoutRemoved(turnout);
		} catch (final HibernateException x) {
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
	public void updateTurnout(final Turnout turnout)
			throws TurnoutManagerException {
		LOGGER.debug("updateTurnout()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = turnout.getId();
			final HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, id);

			HibernateTurnoutMapper.updateHibernate(hTurnout, turnout);
			session.update(hTurnout);
			transaction.commit();
			listener.turnoutUpdated(turnout);
		} catch (final HibernateException x) {
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
	public void addTurnoutGroup(final TurnoutGroup group)
			throws TurnoutManagerException {
		LOGGER.debug("addTurnoutGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateTurnoutGroup hTurnoutGroup = HibernateTurnoutMapper
					.map(group);
			final Integer id = (Integer) session.save(hTurnoutGroup);

			group.setId(id);
			transaction.commit();
			listener.turnoutGroupAdded(group);
		} catch (final HibernateException x) {
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
	public void removeTurnoutGroup(final TurnoutGroup group)
			throws TurnoutManagerException {
		LOGGER.debug("deleteTurnoutGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = group.getId();

			final HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, id);
			session.delete(hTurnoutGroup);
			transaction.commit();
			listener.turnoutGroupRemoved(group);
		} catch (final HibernateException x) {
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
	public void updateTurnoutGroup(final TurnoutGroup group)
			throws TurnoutManagerException {
		LOGGER.debug("updateTurnoutGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = group.getId();
			final HibernateTurnoutGroup hTurnout = (HibernateTurnoutGroup) session
					.get(HibernateTurnoutGroup.class, id);

			session.update(hTurnout);
			transaction.commit();
			listener.turnoutGroupUpdated(group);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		LOGGER.debug("getAllTurnoutGroups()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final List hTurnoutGroups = session.createQuery(
					"from HibernateTurnoutGroup").list();
			final SortedSet<TurnoutGroup> turnoutGroups = new TreeSet<TurnoutGroup>();
			for (final Iterator iterator = hTurnoutGroups.iterator(); iterator
					.hasNext();) {
				final HibernateTurnoutGroup hTurnoutGroup = (HibernateTurnoutGroup) iterator
						.next();
				final TurnoutGroup turnoutGroup = HibernateTurnoutMapper
						.map(hTurnoutGroup);
				turnoutGroups.add(turnoutGroup);
			}
			transaction.commit();
			return turnoutGroups;
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new TurnoutManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void init(final TurnoutServiceListener listener) {
		this.listener = listener;
		final SortedSet<TurnoutGroup> allTurnoutGroups = new TreeSet<TurnoutGroup>(
				getAllTurnoutGroups());
		listener.turnoutsUpdated(allTurnoutGroups);
	}

	@Override
	public void disconnect() {

	}

}
