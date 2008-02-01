package ch.fork.AdHocRailway.domain.locomotives;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.LookupAddress;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateLocomotivePersistence extends HibernatePersistence
		implements LocomotivePersistenceIface {
	private static LocomotivePersistenceIface instance;

	private ArrayListModel<LocomotiveGroup> locomotiveGroupCache;
	private ArrayListModel<Locomotive> locomotiveCache;
	private Map<LookupAddress, Locomotive> addressLocomotiveCache;

	private HibernateLocomotivePersistence() {
		super();
		this.locomotiveCache = new ArrayListModel<Locomotive>();
		this.locomotiveGroupCache = new ArrayListModel<LocomotiveGroup>();
		this.addressLocomotiveCache = new HashMap<LookupAddress, Locomotive>();
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

	public ArrayListModel<Locomotive> getAllLocomotives() {
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
		EntityManager em = getEntityManager();
		em.merge(locomotive);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups() {
		if (locomotiveGroupCache.isEmpty()) {
			updateLocomotiveGroupCache();
		}
		return locomotiveGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroupsDB() {
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
		// locomotiveGroupCache.add(group);
		updateLocomotiveGroupCache();
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
		// locomotiveGroupCache.remove(group);
		updateLocomotiveGroupCache();
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
