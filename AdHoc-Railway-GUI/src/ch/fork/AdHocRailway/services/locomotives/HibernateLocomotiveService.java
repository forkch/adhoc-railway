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

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;

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

	// private void updateLocomotiveTypeCache() {
	// for (LocomotiveType type : getAllLocomotiveTypesDB()) {
	// super.addLocomotiveType(type);
	// }
	// }

	// private void updateLocomotiveCache() {
	// for (Locomotive locomotive : getAllLocomotivesDB()) {
	// super.addLocomotive(locomotive);
	// }
	// }

	// private void updateLocomotiveGroupCache() {
	// for (LocomotiveGroup group : getAllLocomotiveGroupsDB()) {
	// super.addLocomotiveGroup(group);
	// }
	// }

	@Override
	public void clear() throws LocomotivePersistenceException {
		LOGGER.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE locomotive").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE locomotive_group")
					.executeUpdate();

			em.getTransaction().commit();
			HibernatePersistence.disconnect();
			HibernatePersistence.connect();

			// updateLocomotiveTypeCache();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public void addLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		LOGGER.debug("addLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {

			locomotive.getLocomotiveGroup().getLocomotives().add(locomotive);
			em.persist(locomotive);
			em.refresh(locomotive.getLocomotiveGroup());
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public void deleteLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		LOGGER.debug("deleteLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			LocomotiveGroup group = locomotive.getLocomotiveGroup();
			group.getLocomotives().remove(locomotive);

			LocomotiveType type = locomotive.getLocomotiveType();
			type.getLocomotives().remove(locomotive);

			em.remove(locomotive);
			HibernatePersistence.flush();

		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#
	 * updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	@Override
	public void updateLocomotive(Locomotive locomotive)
			throws LocomotivePersistenceException {
		LOGGER.debug("updateLocomotive()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(locomotive);
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#
	 * addLocomotiveGroup
	 * (ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	@Override
	public void addLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		LOGGER.debug("addLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(group);
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#
	 * deleteLocomotiveGroup
	 * (ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	@Override
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		LOGGER.debug("deleteLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			if (!group.getLocomotives().isEmpty()) {
				throw new LocomotivePersistenceException(
						"Cannot delete locomotive group with associated locomotives");
			}
			em.remove(group);
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#
	 * updateLocomotiveGroup
	 * (ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	@Override
	public void updateLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		LOGGER.debug("updateLocomotiveGroup()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(group);
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public void addLocomotiveType(LocomotiveType type) {
		LOGGER.debug("addLocomotiveType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(type);
			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		LOGGER.debug("deleteLocomotiveType()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			if (!type.getLocomotives().isEmpty()) {
				throw new LocomotivePersistenceException(
						"Cannot delete locomotive type with associated locomotives");
			}
			em.remove(type);

			HibernatePersistence.flush();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public void flush() {
		HibernatePersistence.flush();
	}

	@Override
	public SortedSet<Locomotive> getAllLocomotives()
			throws LocomotivePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateLocomotive> locs = em.createQuery(
					"from HibernateLocomotive").getResultList();

			HibernatePersistence.flush();

			SortedSet<Locomotive> locomotives = new TreeSet<Locomotive>();
			for (HibernateLocomotive hibernateLocomotive : locs) {
				Locomotive locomotive = HibernateLocomotiveMapper
						.mapLocomotive(hibernateLocomotive);
				locomotives.add(locomotive);

			}

			return locomotives;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotivePersistenceException {
		LOGGER.debug("getAllLocomotiveGroupsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();

		try {
			List<HibernateLocomotiveGroup> groups = em.createQuery(
					"from HibernateLocomotiveGroup").getResultList();

			HibernatePersistence.flush();

			SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
			for (HibernateLocomotiveGroup hlocomotiveGroup : groups) {
				LocomotiveGroup locomotiveGroup = HibernateLocomotiveMapper
						.mapLocomotiveGroup(hlocomotiveGroup);
				locomotiveGroups.add(locomotiveGroup);

			}
			return locomotiveGroups;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}

	@Override
	public SortedSet<LocomotiveType> getAllLocomotiveTypes()
			throws LocomotivePersistenceException {
		LOGGER.debug("getAllLocomotiveTypessDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateLocomotiveType> hLocomotiveTypes = em.createQuery(
					"from HibernateLocomotiveType").getResultList();
			SortedSet<LocomotiveType> locomotiveTypes = new TreeSet<LocomotiveType>();

			for (HibernateLocomotiveType hLocomotiveType : hLocomotiveTypes) {
				LocomotiveType locomotiveType = HibernateLocomotiveMapper
						.mapLocomotiveType(hLocomotiveType);
				locomotiveTypes.add(locomotiveType);
			}
			HibernatePersistence.flush();
			return locomotiveTypes;
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new LocomotivePersistenceException("Database Error", x);
		}
	}
}
