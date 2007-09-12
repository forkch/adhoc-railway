package ch.fork.AdHocRailway.domain.turnouts;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class TurnoutPersistence {

	private static TurnoutPersistence instance;

	protected EntityManager em;

	private TurnoutPersistence() {// Start EntityManagerFactory
		EntityManagerFactory emf = Persistence
				.createEntityManagerFactory("adhocrailway");
		// First unit of work
		this.em = emf.createEntityManager();
		for(Turnout t : getAllTurnouts()) {
			System.out.println(t);
		}
	}

	public static TurnoutPersistence getInstance() {
		if (instance == null) {
			instance = new TurnoutPersistence();
		}
		return instance;
	}
	
	public void preload() {
		getAllTurnoutGroups();
		getAllTurnouts();
		getAllTurnoutTypes();
	}
	
	@SuppressWarnings("unchecked")
	public SortedSet<Turnout> getAllTurnouts() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<Turnout> turnouts = em.createQuery("from Turnout").getResultList();
		t.commit();
		return new TreeSet<Turnout>(turnouts);
	}

	public Turnout getTurnoutByNumber(int number) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Turnout turnout = (Turnout) em.createQuery(
				"from Turnout as turnout where turnout.number = ?1")
				.setParameter(1, number).getSingleResult();
		t.commit();
		return turnout;
	}

	public Turnout getTurnoutByAddressBus(int bus, int address) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		// Turnout turnout = (Turnout) em.createQuery(
		// "from Turnout as turnout where turnout.number = ?1")
		// .setParameter(1, number).getSingleResult();
		t.commit();
		return null;
	}

	public void addTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	public void deleteTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	public void refreshTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	public void updateTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	public Map<Integer, Turnout> getNumberToTurnout() {
		return null;
	}

	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<TurnoutGroup> groups = em.createQuery("from TurnoutGroup")
				.getResultList();
		t.commit();
		return new TreeSet<TurnoutGroup>(groups);
	}

	public TurnoutGroup getTurnoutGroupByName(String name) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		TurnoutGroup group = (TurnoutGroup) em.createQuery(
				"from Turnout as turnout where turnout.number = ?1")
				.setParameter(1, name).getSingleResult();
		t.commit();
		return group;
	}

	public void addTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(group);
		t.commit();
	}

	public void deleteTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(group);
		t.commit();
	}

	public void refreshTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(group);
		t.commit();
		t.begin();
	}

	public void updateTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(group);
		t.commit();
	}

	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<TurnoutType> types = em.createQuery("from TurnoutType")
				.getResultList();
		t.commit();
		return new TreeSet<TurnoutType>(types);
	}
	
	public TurnoutType getTurnoutTypeByName(String typeName) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		TurnoutType turnoutType = (TurnoutType) em.createQuery(
				"from TurnoutType as t where t.typeName = ?1").setParameter(
				1, typeName).getSingleResult();
		t.commit();
		return turnoutType;
	}

}
