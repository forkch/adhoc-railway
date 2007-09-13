package ch.fork.AdHocRailway.domain.turnouts;

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityTransaction;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;

public class HibernateTurnoutPersistence extends HibernatePersistence implements TurnoutPersistenceIface {

	private static TurnoutPersistenceIface instance;


	private HibernateTurnoutPersistence() {
		super();
	}

	public static TurnoutPersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutPersistence();
		}
		return instance;
	}
	
	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#preload()
	 */
	public void preload() {
		getAllTurnoutGroups();
		getAllTurnouts();
		getAllTurnoutTypes();
	}
	
	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<Turnout> getAllTurnouts() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<Turnout> turnouts = em.createQuery("from Turnout").getResultList();
		t.commit();
		return new TreeSet<Turnout>(turnouts);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByNumber(int)
	 */
	public Turnout getTurnoutByNumber(int number) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Turnout turnout = (Turnout) em.createQuery(
				"from Turnout as turnout where turnout.number = ?1")
				.setParameter(1, number).getSingleResult();
		t.commit();
		return turnout;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByAddressBus(int, int)
	 */
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Turnout turnout = (Turnout) em.createQuery(
		 "from Turnout as turnout where turnout.bus1 = ?1 " +
		 "and turnout.address1 = ?2 or turnout.address2 = ?2")
		 .setParameter(1, bus).setParameter(2, address)
		 .getSingleResult();
		t.commit();
		return turnout;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void addTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(turnout);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#refreshTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void refreshTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void updateTurnout(Turnout turnout) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(turnout);
		t.commit();
		t.begin();
		em.refresh(turnout.getTurnoutGroup());
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getNumberToTurnout()
	 */
	public Map<Integer, Turnout> getNumberToTurnout() {
		return null;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<TurnoutGroup> groups = em.createQuery("from TurnoutGroup")
				.getResultList();
		t.commit();
		return new TreeSet<TurnoutGroup>(groups);
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutGroupByName(java.lang.String)
	 */
	public TurnoutGroup getTurnoutGroupByName(String name) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		TurnoutGroup group = (TurnoutGroup) em.createQuery(
				"from Turnout as turnout where turnout.number = ?1")
				.setParameter(1, name).getSingleResult();
		t.commit();
		return group;
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		int weight = 0;
		for(TurnoutGroup tg : getAllTurnoutGroups()) {
			weight = tg.getWeight();
		}
		t.begin();
		group.setWeight(weight + 1);
		em.persist(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void deleteTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#refreshTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void refreshTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void updateTurnoutGroup(TurnoutGroup group) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(group);
		t.commit();
	}

	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<TurnoutType> types = em.createQuery("from TurnoutType")
				.getResultList();
		t.commit();
		return new TreeSet<TurnoutType>(types);
	}
	
	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutTypeByName(java.lang.String)
	 */
	public TurnoutType getTurnoutType(TurnoutTypes typeName) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		String typeStr = "";
		switch(typeName) {
		case DEFAULT:
			typeStr = "DEFAULT";
			break;
		case DOUBLECROSS:
			typeStr = "DOUBLECROSS";
			break;
		case THREEWAY:
			typeStr = "THREEWAY";
			break;
		}
		TurnoutType turnoutType = (TurnoutType) em.createQuery(
				"from TurnoutType as t where t.typeName = ?1").setParameter(
				1, typeStr).getSingleResult();
		t.commit();
		return turnoutType;
	}

	public int getNextFreeTurnoutNumber() {
		SortedSet<Turnout> turnouts = getAllTurnouts();
		if(turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}
}
