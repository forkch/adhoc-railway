package ch.fork.AdHocRailway.domain.locomotives;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityTransaction;

import ch.fork.AdHocRailway.domain.HibernatePersistence;

public class HibernateLocomotivePersistence extends HibernatePersistence implements LocomotivePersistenceIface {
	private static LocomotivePersistenceIface instance;


	private HibernateLocomotivePersistence() {
		super();
	}

	public static LocomotivePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateLocomotivePersistence();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#preload()
	 */
	public void preload() {
		getAllLocomotiveGroups();
		getAllLocomotives();
		getAllLocomotiveTypes();
	}
	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotives()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<Locomotive> getAllLocomotives() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<Locomotive> locs = em.createQuery("from Locomotive")
				.getResultList();
		t.commit();
		return new TreeSet<Locomotive>(locs);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByNumber(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByNumber(int number) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Locomotive loc = (Locomotive) em.createQuery(
				"from Locomotive as l where l.number = ?1").setParameter(1,
				number).getSingleResult();
		t.commit();
		return loc;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveByAddress(int)
	 */
	@SuppressWarnings("unchecked")
	public Locomotive getLocomotiveByAddress(int address) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Locomotive loc = (Locomotive) em.createQuery(
				"from Locomotive as l where l.address = ?1").setParameter(1,
				address).getSingleResult();
		t.commit();
		return loc;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void addLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(locomotive);
		t.commit();
		t.begin();
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void deleteLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(locomotive);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void updateLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
		t.begin();
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#refreshLocomotive(ch.fork.AdHocRailway.domain.locomotives.Locomotive)
	 */
	public void refreshLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
		t.begin();
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<LocomotiveGroup> groups = em.createQuery("from LocomotiveGroup")
				.getResultList();
		t.commit();
		return new TreeSet<LocomotiveGroup>(groups);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#addLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void addLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#deleteLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void deleteLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#updateLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#refreshLocomotiveGroup(ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup)
	 */
	public void refreshLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getAllLocomotiveTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<LocomotiveType> locomotiveTypes = em.createQuery(
				"from LocomotiveType").getResultList();
		t.commit();
		return new TreeSet<LocomotiveType>(locomotiveTypes);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface#getLocomotiveTypeByName(java.lang.String)
	 */
	public LocomotiveType getLocomotiveTypeByName(String typeName) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		LocomotiveType locomotiveTypes = (LocomotiveType) em.createQuery(
				"from LocomotiveType as t where t.typeName = ?1").setParameter(
				1, typeName).getSingleResult();
		t.commit();
		return locomotiveTypes;
	}
}
