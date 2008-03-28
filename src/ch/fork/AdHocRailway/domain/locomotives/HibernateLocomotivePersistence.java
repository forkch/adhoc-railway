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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceException;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateLocomotivePersistence extends HibernatePersistence
		implements LocomotivePersistenceIface {
	private static HibernateLocomotivePersistence	instance;
	private static Logger							logger	= Logger
																	.getLogger(HibernateLocomotivePersistence.class);

	private ArrayListModel<LocomotiveGroup>			locomotiveGroupCache;
	private ArrayListModel<Locomotive>				locomotiveCache;
	private Map<LookupAddress, Locomotive>			addressLocomotiveCache;

	private HibernateLocomotivePersistence() {
		logger.info("HibernateLocomotivePersistence loded");
		this.locomotiveCache = new ArrayListModel<Locomotive>();
		this.locomotiveGroupCache = new ArrayListModel<LocomotiveGroup>();
		this.addressLocomotiveCache = new HashMap<LookupAddress, Locomotive>();

		try {
			getLocomotiveTypeByName("DELTA");
		} catch (NoResultException ex) {
			LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
			deltaType.setDrivingSteps(14);
			deltaType.setStepping(4);
			deltaType.setFunctionCount(4);
			addLocomotiveType(deltaType);
		}
		try {
			getLocomotiveTypeByName("DIGITAL");
		} catch (NoResultException ex) {
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
		addressLocomotiveCache.clear();
		locomotiveCache.clear();
		for (Locomotive locomotive : getAllLocomotivesDB()) {
			addressLocomotiveCache.put(new LookupAddress(locomotive.getBus(),
					locomotive.getAddress(), 0, 0), locomotive);
			locomotiveCache.add(locomotive);
		}
	}

	private void updateLocomotiveGroupCache() {
		locomotiveGroupCache.clear();
		for (LocomotiveGroup group : getAllLocomotiveGroupsDB()) {
			locomotiveGroupCache.add(group);
		}
	}

	public void clear() throws LocomotivePersistenceException {
		logger.debug("clear()");

		EntityManager em = getEntityManager();
		em.createNativeQuery("TRUNCATE TABLE locomotive").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE locomotive_type").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE locomotive_group").executeUpdate();
		addressLocomotiveCache.clear();
		locomotiveCache.clear();
		locomotiveGroupCache.clear();
		em.getTransaction().commit();
		HibernatePersistence.em = emf.createEntityManager();
		HibernatePersistence.em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotives()
	 */

	public ArrayListModel<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException {
		//logger.debug("getAllLocomotives()");
		if (locomotiveCache.isEmpty()) {
			updateLocomotiveCache();
		}
		return locomotiveCache;
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Locomotive> getAllLocomotivesDB()
			throws LocomotivePersistenceException {
		EntityManager em = getEntityManager();
		try {
			List<Locomotive> locs = em.createQuery("from Locomotive")
					.getResultList();
			return new TreeSet<Locomotive>(locs);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
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

		for (Locomotive l : locomotiveCache) {
			if (l.getAddress() == address && l.getBus() == bus)
				return l;
		}
		throw new LocomotivePersistenceException("Locomotive with bus " + bus
				+ " and address " + address + " not found");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("addLocomotive()");
		EntityManager em = getEntityManager();
		em.persist(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		flush();
		updateLocomotiveCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotive()");
		EntityManager em = getEntityManager();

		LocomotiveGroup group = locomotive.getLocomotiveGroup();
		group.getLocomotives().remove(locomotive);

		LocomotiveType type = locomotive.getLocomotiveType();
		type.getLocomotives().remove(locomotive);

		em.remove(locomotive);
		flush();

		updateLocomotiveCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		logger.debug("updateLocomotive()");
		EntityManager em = getEntityManager();
		em.merge(locomotive);
		flush();
		updateLocomotiveCache();
	}

	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException {
		//logger.debug("getAllLocomotiveGroups()");
		if (locomotiveGroupCache.isEmpty()) {
			updateLocomotiveGroupCache();
		}
		return locomotiveGroupCache;
	}

	@SuppressWarnings("unchecked")
	private SortedSet<LocomotiveGroup> getAllLocomotiveGroupsDB()
			throws LocomotivePersistenceException {
		logger.debug("getAllLocomotiveGroupsDB()");
		EntityManager em = getEntityManager();
		try {
			List<LocomotiveGroup> groups = em.createQuery(
					"from LocomotiveGroup").getResultList();
			return new TreeSet<LocomotiveGroup>(groups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
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
		EntityManager em = getEntityManager();
		em.persist(group);
		flush();
		updateLocomotiveGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotiveGroup()");
		EntityManager em = getEntityManager();
		if (!group.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive group with associated locomotives");
		}
		em.remove(group);
		flush();
		updateLocomotiveGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		logger.debug("updateLocomotiveGroup()");
		EntityManager em = getEntityManager();
		em.merge(group);
		flush();
		updateLocomotiveGroupCache();
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
		EntityManager em = getEntityManager();
		try {
			List<LocomotiveType> locomotiveTypes = em.createQuery(
					"from LocomotiveType").getResultList();
			return new TreeSet<LocomotiveType>(locomotiveTypes);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveTypeByName(java.lang.String)
	 */
	public LocomotiveType getLocomotiveTypeByName(String typeName)
			throws LocomotivePersistenceException {
		logger.debug("getLocomotiveTypeByName()");
		EntityManager em = getEntityManager();
		try {
			LocomotiveType locomotiveTypes = (LocomotiveType) em.createQuery(
					"from LocomotiveType as t where t.typeName = ?1")
					.setParameter(1, typeName).getSingleResult();
			return locomotiveTypes;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new TurnoutPersistenceException("Error", x);
		}
	}

	public void addLocomotiveType(LocomotiveType type) {
		logger.debug("addLocomotiveType()");
		EntityManager em = getEntityManager();
		em.persist(type);
		flush();
	}

	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		logger.debug("deleteLocomotiveType()");
		EntityManager em = getEntityManager();
		if (!type.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive type with associated locomotives");
		}
		em.remove(type);
		flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#flush()
	 */
	public void flush() throws LocomotivePersistenceException {
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
