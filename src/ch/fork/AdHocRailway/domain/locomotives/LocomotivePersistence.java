package ch.fork.AdHocRailway.domain.locomotives;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class LocomotivePersistence {
	private static LocomotivePersistence instance;

	protected EntityManager em;

	private LocomotivePersistence() {// Start EntityManagerFactory
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("adhocrailway");
		// First unit of work
		this.em = emf.createEntityManager();
	}

	public static LocomotivePersistence getInstance() {
		if (instance == null) {
			instance = new LocomotivePersistence();
		}
		return instance;
	}
	
	public void preload() {
		getAllLocomotiveGroups();
		getAllLocomotives();
		getAllLocomotiveTypes();
	}
	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
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

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
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

	/**
	 * Get a SortedSet of Locomotives.
	 * 
	 * @return locomotives
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

	public void addLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(locomotive);
		t.commit();
		t.begin();
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
	}

	public void deleteLocomotive(Locomotive locomotive) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(locomotive);
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
		t.begin();
		em.refresh(locomotive.getLocomotiveGroup());
		t.commit();
	}

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

	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<LocomotiveGroup> groups = em.createQuery("from LocomotiveGroup")
				.getResultList();
		t.commit();
		return new TreeSet<LocomotiveGroup>(groups);
	}

	public void addLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(group);
		t.commit();
	}

	public void deleteLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(group);
		t.commit();
	}

	public void updateLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(group);
		t.commit();
	}

	public void refreshLocomotiveGroup(LocomotiveGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(group);
		t.commit();
	}

	@SuppressWarnings("unchecked")
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<LocomotiveType> locomotiveTypes = em.createQuery(
				"from LocomotiveType").getResultList();
		t.commit();
		return new TreeSet<LocomotiveType>(locomotiveTypes);
	}

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
