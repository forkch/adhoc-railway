package ch.fork.AdHocRailway.domain.locomotives;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.LookupAddress;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateLocomotivePersistence extends HibernatePersistence
		implements LocomotivePersistenceIface {
	private static HibernateLocomotivePersistence instance;
	private static Logger logger = Logger.getLogger(HibernateLocomotivePersistence.class);

	private ArrayListModel<LocomotiveGroup> locomotiveGroupCache;
	private ArrayListModel<Locomotive> locomotiveCache;
	private Map<LookupAddress, Locomotive> addressLocomotiveCache;

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

	public ArrayListModel<Locomotive> getAllLocomotives() {
		logger.debug("getAllLocomotives()");
		if (locomotiveCache.isEmpty()) {
			updateLocomotiveCache();
		}
		return locomotiveCache;
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
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByAddress(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByBusAddress(int bus, int address) {
		logger.debug("getLocomotiveByBusAddress()");
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
		logger.debug("addLocomotive()");
		EntityManager em = getEntityManager();
		em.persist(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();

		// locomotiveCache.add(locomotive);
		// addressLocomotiveCache.put(new LookupAddress( locomotive.getBus(),
		// locomotive.getAddress(),0,0 ), locomotive);
		updateLocomotiveCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive) {
		logger.debug("deleteLocomotive()");
		EntityManager em = getEntityManager();

		LocomotiveGroup group = locomotive.getLocomotiveGroup();
		group.getLocomotives().remove(locomotive);

		LocomotiveType type = locomotive.getLocomotiveType();
		type.getLocomotives().remove(locomotive);

		em.remove(locomotive);
		em.getTransaction().commit();
		em.getTransaction().begin();

		// locomotiveCache.remove(locomotive);
		// addressLocomotiveCache.remove(new LookupAddress( locomotive.getBus(),
		// locomotive.getAddress(),0,0 ));

		updateLocomotiveCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive) {
		logger.debug("updateLocomotive()");
		EntityManager em = getEntityManager();
		em.merge(locomotive);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateLocomotiveCache();
	}

	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups() {
		logger.debug("getAllLocomotiveGroups()");
		if (locomotiveGroupCache.isEmpty()) {
			updateLocomotiveGroupCache();
		}
		return locomotiveGroupCache;
	}
	
	@SuppressWarnings("unchecked")
	private SortedSet<LocomotiveGroup> getAllLocomotiveGroupsDB() {
		logger.debug("getAllLocomotiveGroupsDB()");
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
		logger.debug("addLocomotiveGroup()");
		EntityManager em = getEntityManager();
		em.persist(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
		// locomotiveGroupCache.add(group);
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
		em.getTransaction().commit();
		em.getTransaction().begin();
		// locomotiveGroupCache.remove(group);
		updateLocomotiveGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		logger.debug("updateLocomotiveGroup()");
		EntityManager em = getEntityManager();
		em.merge(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateLocomotiveGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		logger.debug("getAllLocomotiveTypes()");
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
		logger.debug("getLocomotiveTypeByName()");
		EntityManager em = getEntityManager();
		LocomotiveType locomotiveTypes = (LocomotiveType) em.createQuery(
				"from LocomotiveType as t where t.typeName = ?1").setParameter(
				1, typeName).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return locomotiveTypes;
	}

	public void addLocomotiveType(LocomotiveType type) {
		logger.debug("addLocomotiveType()");
		EntityManager em = getEntityManager();
		em.persist(type);
		em.getTransaction().commit();
		em.getTransaction().begin();
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
		em.getTransaction().commit();
		em.getTransaction().begin();
	}
}
