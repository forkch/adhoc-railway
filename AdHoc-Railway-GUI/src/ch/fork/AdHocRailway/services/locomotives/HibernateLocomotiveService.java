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

package ch.fork.AdHocRailway.services.locomotives;

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
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.services.HibernateUtil;

public class HibernateLocomotiveService implements LocomotiveService {
	private static HibernateLocomotiveService instance;
	private static Logger LOGGER = Logger
			.getLogger(HibernateLocomotiveService.class);

	private HibernateLocomotiveService() {
		LOGGER.info("HibernateLocomotivePersistence loaded");
	}

	public static LocomotiveService getInstance() {
		if (instance == null) {
			instance = new HibernateLocomotiveService();
		}
		return instance;
	}

	@Override
	public void clear() throws LocomotiveManagerException {
		LOGGER.debug("clear()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE locomotive").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE locomotive_group")
					.executeUpdate();

			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("addLocomotive()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateLocomotive hLocomotive = HibernateLocomotiveMapper
					.mapLocomotive(locomotive);
			Integer locomotiveGroupId = locomotive.getId();

			HibernateLocomotiveGroup hLocomotiveGroup = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, locomotiveGroupId);
			hLocomotiveGroup.getLocomotives().add(hLocomotive);
			hLocomotive.setLocomotiveGroup(hLocomotiveGroup);

			Integer id = (Integer) session.save(hLocomotive);
			locomotive.setId(id);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void deleteLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("deleteLocomotive()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = locomotive.getId();
			HibernateLocomotive hl = (HibernateLocomotive) session.get(
					HibernateLocomotive.class, id);
			session.delete(hl);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateLocomotive(Locomotive locomotive)
			throws LocomotiveManagerException {
		LOGGER.debug("updateLocomotive()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = locomotive.getId();
			HibernateLocomotive hl = (HibernateLocomotive) session.get(
					HibernateLocomotive.class, id);

			HibernateLocomotiveMapper.updateHibernateLocomotive(hl, locomotive);
			session.update(hl);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("addLocomotiveGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			HibernateLocomotiveGroup hibernateLocomotiveGroup = HibernateLocomotiveMapper
					.mapLocomotiveGroup(group);
			Integer id = (Integer) session.save(hibernateLocomotiveGroup);

			group.setId(id);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("deleteLocomotiveGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Integer id = group.getId();

			HibernateLocomotiveGroup hlg = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, id);
			session.delete(hlg);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotiveManagerException {
		LOGGER.debug("updateLocomotiveGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = group.getId();
			HibernateLocomotiveGroup hlg = (HibernateLocomotiveGroup) session
					.get(HibernateLocomotiveGroup.class, id);

			HibernateLocomotiveMapper
					.updateHibernateLocomotiveGroup(hlg, group);
			session.update(hlg);
			transaction.commit();
		} catch (HibernateException x) {
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
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			List groups = session.createQuery("from HibernateLocomotiveGroup")
					.list();

			SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
			for (Iterator iterator = groups.iterator(); iterator.hasNext();) {
				HibernateLocomotiveGroup hLocomotiveGroup = (HibernateLocomotiveGroup) iterator
						.next();
				LocomotiveGroup locomotiveGroup = HibernateLocomotiveMapper
						.mapHibernateLocomotiveGroup(hLocomotiveGroup);
				locomotiveGroups.add(locomotiveGroup);
			}
			transaction.commit();

			return locomotiveGroups;
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}

	}
}
