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

package ch.fork.AdHocRailway.services.impl.hibernate.locomotives;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.services.impl.hibernate.HibernateUtil;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveServiceListener;

public class HibernateLocomotiveService implements LocomotiveService {
	private static Logger LOGGER = Logger
			.getLogger(HibernateLocomotiveService.class);
	private LocomotiveServiceListener listener;

	public HibernateLocomotiveService() {
		LOGGER.info("HibernateLocomotivePersistence loaded");
	}

	@Override
	public void clear() throws LocomotiveManagerException {
		LOGGER.debug("clear()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE locomotive").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE locomotive_group")
					.executeUpdate();

			transaction.commit();
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("addLocomotive()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateLocomotive hLocomotive = HibernateLocomotiveMapper
					.mapLocomotive(locomotive);
			final Integer locomotiveGroupId = locomotive.getId();

			final HibernateLocomotiveGroup hLocomotiveGroup = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, locomotiveGroupId);
			hLocomotiveGroup.getLocomotives().add(hLocomotive);
			hLocomotive.setLocomotiveGroup(hLocomotiveGroup);

			final Integer id = (Integer) session.save(hLocomotive);
			locomotive.setId(id);
			transaction.commit();
			listener.locomotiveAdded(locomotive);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("deleteLocomotive()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = locomotive.getId();
			final HibernateLocomotive hl = (HibernateLocomotive) session.get(
					HibernateLocomotive.class, id);
			session.delete(hl);
			transaction.commit();
			listener.locomotiveRemoved(locomotive);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("updateLocomotive()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = locomotive.getId();
			final HibernateLocomotive hl = (HibernateLocomotive) session.get(
					HibernateLocomotive.class, id);

			HibernateLocomotiveMapper.updateHibernateLocomotive(hl, locomotive);
			session.update(hl);
			transaction.commit();
			listener.locomotiveUpdated(locomotive);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("addLocomotiveGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final HibernateLocomotiveGroup hibernateLocomotiveGroup = HibernateLocomotiveMapper
					.mapLocomotiveGroup(group);
			final Integer id = (Integer) session.save(hibernateLocomotiveGroup);

			group.setId(id);
			transaction.commit();
			listener.locomotiveGroupAdded(group);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("deleteLocomotiveGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final Integer id = group.getId();

			final HibernateLocomotiveGroup hlg = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, id);
			session.delete(hlg);
			transaction.commit();
			listener.locomotiveGroupRemoved(group);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("updateLocomotiveGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = group.getId();
			final HibernateLocomotiveGroup hlg = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, id);

			HibernateLocomotiveMapper
					.updateHibernateLocomotiveGroup(hlg, group);
			session.update(hlg);
			transaction.commit();
			listener.locomotiveGroupUpdated(group);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotiveManagerException {
		LOGGER.debug("getAllLocomotiveGroupsDB()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final List groups = session.createQuery(
					"from HibernateLocomotiveGroup").list();

			final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
			for (final Iterator iterator = groups.iterator(); iterator
					.hasNext();) {
				final HibernateLocomotiveGroup hLocomotiveGroup = (HibernateLocomotiveGroup) iterator
						.next();
				final LocomotiveGroup locomotiveGroup = HibernateLocomotiveMapper
						.mapHibernateLocomotiveGroup(hLocomotiveGroup);
				locomotiveGroups.add(locomotiveGroup);
			}
			transaction.commit();

			return locomotiveGroups;
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}

	}

	@Override
	public void init(final LocomotiveServiceListener listener) {
		this.listener = listener;
		final SortedSet<LocomotiveGroup> allLocomotiveGroups = getAllLocomotiveGroups();
		listener.locomotivesUpdated(allLocomotiveGroups);
	}

	@Override
	public void disconnect() {

	}
}
