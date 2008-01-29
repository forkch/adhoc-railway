package ch.fork.AdHocRailway.domain.locomotives;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import ch.fork.AdHocRailway.domain.HibernatePersistence;

public class HibernateLocomotivePersistence extends HibernatePersistence
		implements LocomotivePersistenceIface {
	private static LocomotivePersistenceIface instance;

	private Map<int[], Locomotive> locomotiveCache;

	private HibernateLocomotivePersistence() {
		super();
		this.locomotiveCache = new HashMap<int[], Locomotive>();
	}

	public static LocomotivePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateLocomotivePersistence();
		}
		return instance;
	}

	private void updateCache() {
		locomotiveCache.clear();
		for (Locomotive l : getAllLocomotivesDB()) {
			locomotiveCache.put(new int[] { l.getBus(), l.getAddress() }, l);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#preload()
	 */
	public void preload() {
		getAllLocomotives();
		getAllLocomotiveGroups();
		getAllLocomotiveTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotives()
	 */

	public SortedSet<Locomotive> getAllLocomotives() {
		if(locomotiveCache.size() == 0) {
			updateCache();
		}
		return new TreeSet<Locomotive>(locomotiveCache.values());
	}
	
	@SuppressWarnings("unchecked")
	private SortedSet<Locomotive> getAllLocomotivesDB() {
		EntityManager em = getEntityManager();
		List<Locomotive> locs = em.createQuery("from Locomotive")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<Locomotive>(locs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByNumber(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByNumber(int number) {
		EntityManager em = getEntityManager();
		Locomotive loc = (Locomotive) em.createQuery(
				"from Locomotive as l where l.number = ?1").setParameter(1,
				number).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return loc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByAddress(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByBusAddress(int bus, int address) {
		EntityManager em = getEntityManager();
		Locomotive loc = (Locomotive) em.createQuery(
				"from Locomotive as l where l.address = ?1").setParameter(1,
				address).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return loc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void addLocomotive(Locomotive locomotive) {
		EntityManager em = getEntityManager();
		em.persist(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive) {
		EntityManager em = getEntityManager();
		em.remove(locomotive);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive) {
		EntityManager em = getEntityManager();
		em.merge(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		em.refresh(locomotive.getLocomotiveGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#refreshLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void refreshLocomotive(Locomotive locomotive) {
		EntityManager em = getEntityManager();
		em.refresh(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		em.refresh(locomotive.getLocomotiveGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
		EntityManager em = getEntityManager();
		List<LocomotiveGroup> groups = em.createQuery("from LocomotiveGroup")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<LocomotiveGroup>(groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void addLocomotiveGroup(LocomotiveGroup group) {
		EntityManager em = getEntityManager();
		em.persist(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void deleteLocomotiveGroup(LocomotiveGroup group) {
		EntityManager em = getEntityManager();
		em.remove(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		EntityManager em = getEntityManager();
		em.merge(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#refreshLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void refreshLocomotiveGroup(LocomotiveGroup group) {
		EntityManager em = getEntityManager();
		em.refresh(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		EntityManager em = getEntityManager();
		List<LocomotiveType> locomotiveTypes = em.createQuery(
				"from LocomotiveType").getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<LocomotiveType>(locomotiveTypes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveTypeByName(java.lang.String)
	 */
	public LocomotiveType getLocomotiveTypeByName(String typeName) {
		EntityManager em = getEntityManager();
		LocomotiveType locomotiveTypes = (LocomotiveType) em.createQuery(
				"from LocomotiveType as t where t.typeName = ?1").setParameter(
				1, typeName).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return locomotiveTypes;
	}
}
