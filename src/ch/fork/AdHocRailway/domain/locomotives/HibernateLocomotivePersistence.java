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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateLocomotivePersistence extends
		CachingLocomotivePersistence {
	private static HibernateLocomotivePersistence	instance;
	private static Logger							logger	= Logger
																	.getLogger(HibernateLocomotivePersistence.class);

	private HibernateLocomotivePersistence() {
		super();
		super.clear();
		logger.info("HibernateLocomotivePersistence loaded");

		if (getLocomotiveTypeByName("DELTA") == null) {
			LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
			deltaType.setDrivingSteps(14);
			deltaType.setStepping(4);
			deltaType.setFunctionCount(4);
			addLocomotiveType(deltaType);
		}
		if (getLocomotiveTypeByName("DIGITAL") == null) {

			LocomotiveType digitalType = new LocomotiveType(0, "DIGITAL");
			digitalType.setDrivingSteps(28);
			digitalType.setStepping(2);
			digitalType.setFunctionCount(5);
			addLocomotiveType(digitalType);
		}
		updateLocomotiveCache();
		updateLocomotiveGroupCache();
	}

	public static LocomotivePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateLocomotivePersistence();
		}
		return instance;
	}

	private void updateLocomotiveCache() {
		for (Locomotive locomotive : getAllLocomotivesDB()) {
			super.addLocomotive(locomotive);
		}
	}

	private void updateLocomotiveGroupCache() {
		for (LocomotiveGroup group : getAllLocomotiveGroupsDB()) {
			super.addLocomotiveGroup(group);
		}
	}

	public void clear() throws LocomotivePersistenceException {
		logger.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE locomotive").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE locomotive_type")
					.executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE locomotive_group")
					.executeUpdate();

			super.clear();

			em.getTransaction().commit();
			HibernatePersistence.disconnect();
			HibernatePersistence.connect();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotives()
	 */

	public ArrayListModel<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException {
		// logger.debug("getAllLocomotives()");
		if (super.getAllLocomotives().isEmpty()) {
			updateLocomotiveCache();
		}
		return super.getAllLocomotives();
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Locomotive> getAllLocomotivesDB()
			throws LocomotivePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<Locomotive> locs = em.createQuery("from Locomotive")
					.getResultList();
			return new TreeSet<Locomotive>(locs);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByAddress(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByBusAddress(int bus, int address)
			throws LocomotivePersistenceException {
		logger.debug("getLocomotiveByBusAddress()");

		return super.getLocomotiveByBusAddress(bus, address);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("addLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {

			locomotive.getLocomotiveGroup().getLocomotives().add(locomotive);
			em.persist(locomotive);
			em.refresh(locomotive.getLocomotiveGroup());
			HibernatePersistence.flush();
			super.addLocomotive(locomotive);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			LocomotiveGroup group = locomotive.getLocomotiveGroup();
			group.getLocomotives().remove(locomotive);

			LocomotiveType type = locomotive.getLocomotiveType();
			type.getLocomotives().remove(locomotive);

			em.remove(locomotive);
			HibernatePersistence.flush();

			super.deleteLocomotive(locomotive);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("updateLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(locomotive);
			HibernatePersistence.flush();
			super.updateLocomotive(locomotive);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException {
		// logger.debug("getAllLocomotiveGroups()");
		if (super.getAllLocomotiveGroups().isEmpty()) {
			updateLocomotiveGroupCache();
		}
		return super.getAllLocomotiveGroups();
	}

	@SuppressWarnings("unchecked")
	private SortedSet<LocomotiveGroup> getAllLocomotiveGroupsDB()
			throws LocomotivePersistenceException {
		logger.debug("getAllLocomotiveGroupsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();

		try {
			List<LocomotiveGroup> groups = em.createQuery(
					"from LocomotiveGroup").getResultList();
			return new TreeSet<LocomotiveGroup>(groups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		logger.debug("addLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(group);
			HibernatePersistence.flush();
			super.addLocomotiveGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			if (!group.getLocomotives().isEmpty()) {
				throw new LocomotivePersistenceException(
						"Cannot delete locomotive group with associated locomotives");
			}
			em.remove(group);
			HibernatePersistence.flush();
			super.deleteLocomotiveGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		logger.debug("updateLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(group);
			HibernatePersistence.flush();
			super.updateLocomotiveGroup(group);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException {
		logger.debug("getAllLocomotiveTypes()");
		return super.getAllLocomotiveTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveTypeByName(java.lang.String)
	 */
	public LocomotiveType getLocomotiveTypeByName(String typeName)
			throws LocomotivePersistenceException {
		logger.debug("getLocomotiveTypeByName()");
		return super.getLocomotiveTypeByName(typeName);
	}

	public void addLocomotiveType(LocomotiveType type) {
		logger.debug("addLocomotiveType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(type);
			HibernatePersistence.flush();
			super.addLocomotiveType(type);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotiveType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			if (!type.getLocomotives().isEmpty()) {
				throw new LocomotivePersistenceException(
						"Cannot delete locomotive type with associated locomotives");
			}
			em.remove(type);

			HibernatePersistence.flush();
			super.deleteLocomotiveType(type);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			HibernatePersistence.getEntityManager().getTransaction().begin();
			throw new LocomotivePersistenceException("Error", x);
		}
	}

	public void flush() {
		HibernatePersistence.flush();
	}
}
