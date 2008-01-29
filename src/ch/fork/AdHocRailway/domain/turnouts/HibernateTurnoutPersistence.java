package ch.fork.AdHocRailway.domain.turnouts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

public class HibernateTurnoutPersistence extends HibernatePersistence implements
		TurnoutPersistenceIface {
	private static Logger logger = Logger
			.getLogger(HibernateTurnoutPersistence.class);
	private static TurnoutPersistenceIface instance;

	private Map<LookupAddress, Turnout> turnoutCache;
	private Map<LookupAddress, Turnout> threewayCache;

	private HibernateTurnoutPersistence() {
		super();
		this.turnoutCache = new HashMap<LookupAddress, Turnout>();
		this.threewayCache = new HashMap<LookupAddress, Turnout>();
	}

	public static TurnoutPersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutPersistence();
		}
		return instance;
	}

	private void updateCache() {
		turnoutCache.clear();
		for (Turnout t : getAllTurnoutsDB()) {
			turnoutCache.put(new LookupAddress(t.getBus1(), t.getAddress1(), t
					.getBus2(), t.getAddress2()), t);
			if (t.isThreeWay()) {
				threewayCache.put(new LookupAddress(t.getBus1(), t
						.getAddress1(), 0, 0), t);
				threewayCache.put(new LookupAddress(0, 0, t.getBus2(), t
						.getAddress2()), t);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#preload()
	 */
	public void preload() throws TurnoutException {
		getAllTurnoutGroups();
		getAllTurnoutGroups();
		getAllTurnoutTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	public SortedSet<Turnout> getAllTurnouts() {
		if (turnoutCache.size() == 0) {
			updateCache();
		}
		return new TreeSet<Turnout>(turnoutCache.values());
	}

	@SuppressWarnings("unchecked")
	private SortedSet<Turnout> getAllTurnoutsDB() {
		logger.debug("getAllTurnout()");
		EntityManager em = getEntityManager();
		List<Turnout> turnouts = em.createQuery("from Turnout").getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		SortedSet<Turnout> res = new TreeSet<Turnout>(turnouts);
		return res;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByNumber(int)
	 */
	public Turnout getTurnoutByNumber(int number) throws TurnoutException {
		logger.debug("getTurnoutByNumber()");
		EntityManager em = getEntityManager();
		Turnout turnout;
		try {
			turnout = (Turnout) em.createQuery(
					"from Turnout as turnout where turnout.number = ?1")
					.setParameter(1, number).getSingleResult();
			em.getTransaction().commit();
			em.getTransaction().begin();
		} catch (EntityNotFoundException e) {
			throw new TurnoutException("Turnout not found", e);
		} catch (NoResultException e) {
			throw new TurnoutException("Turnout not found", e);
		}
		return turnout;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutByAddressBus(int,
	 *      int)
	 */
	public Turnout getTurnoutByAddressBus(int bus, int address) {
		logger.debug("getTurnoutByAddressBus()");
		// EntityManager em = getEntityManager();
		// Turnout turnout = (Turnout) em.createQuery(
		// "from Turnout as turnout where turnout.bus1 = ?1 "
		// + "and turnout.address1 = ?2 or turnout.address2 = ?2")
		// .setParameter(1, bus).setParameter(2, address)
		// .getSingleResult();
		// em.getTransaction().commit();
		// em.getTransaction().begin();
		LookupAddress key1 = new LookupAddress(bus, address, 0, 0);
		Turnout lookup1 = turnoutCache.get(key1);
		if (lookup1 != null)
			return lookup1;
		LookupAddress key2 = new LookupAddress(0, 0, bus, address);
		Turnout lookup2 = turnoutCache.get(key2);
		if (lookup2 != null)
			return lookup2;
		Turnout threewayLookup1 = threewayCache.get(key1);
		if (threewayLookup1 != null)
			return threewayLookup1;

		Turnout threewayLookup2 = threewayCache.get(key2);
		if (threewayLookup2 != null)
			return threewayLookup2;
		
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void addTurnout(Turnout turnout) {
		EntityManager em = getEntityManager();
		TurnoutGroup g = em.find(TurnoutGroup.class, turnout.getTurnoutGroup()
				.getId());
		turnout.setTurnoutGroup(g);
		try {
			em.persist(turnout);
			em.refresh(turnout.getTurnoutGroup());
			em.getTransaction().commit();
			em.getTransaction().begin();
			updateCache();
		} finally {

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout) {
		EntityManager em = getEntityManager();
		turnout.getTurnoutGroup().getTurnouts().remove(turnout);
		em.remove(turnout);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#refreshTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void refreshTurnout(Turnout turnout) {
		EntityManager em = getEntityManager();
		em.refresh(turnout);
		em.refresh(turnout.getTurnoutGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void updateTurnout(Turnout turnout) {
		logger.debug("updateTurnout()");
		EntityManager em = getEntityManager();
		em.merge(turnout);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getNumberToTurnout()
	 */
	public Map<Integer, Turnout> getNumberToTurnout() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
		logger.debug("getAllTurnoutGroups()");
		EntityManager em = getEntityManager();
		List<TurnoutGroup> groups = em.createQuery("from TurnoutGroup")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<TurnoutGroup>(groups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutGroupByName(java.lang.String)
	 */
	public TurnoutGroup getTurnoutGroupByName(String name) {
		logger.debug("getTurnoutGroupByName()");
		EntityManager em = getEntityManager();
		TurnoutGroup group = (TurnoutGroup) em.createQuery(
				"from Turnout as turnout where turnout.number = ?1")
				.setParameter(1, name).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return group;
	}

	public void removeTurnoutFromGroup(TurnoutGroup group, Turnout turnout) {
		deleteTurnout(turnout);
		updateTurnoutGroup(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group) {
		EntityManager em = getEntityManager();
		// int weight = 0;
		// for(TurnoutGroup tg : getAllTurnoutGroups()) {
		// weight = tg.getWeight();
		// }
		// group.setWeight(weight + 1);
		em.persist(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void deleteTurnoutGroup(TurnoutGroup group) throws TurnoutException {
		EntityManager em = getEntityManager();
		if (group.getTurnouts().size() > 0) {
			throw new TurnoutException(
					"Cannot delete turnout group with assiciated turnouts");
		}
		em.remove(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#refreshTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void refreshTurnoutGroup(TurnoutGroup group) {
		EntityManager em = getEntityManager();
		em.refresh(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void updateTurnoutGroup(TurnoutGroup group) {
		EntityManager em = getEntityManager();
		em.merge(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutTypes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutType> getAllTurnoutTypes() {
		logger.debug("getAllTurnoutTypes()");
		EntityManager em = getEntityManager();
		List<TurnoutType> types = em.createQuery("from TurnoutType")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<TurnoutType>(types);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getTurnoutTypeByName(java.lang.String)
	 */
	public TurnoutType getTurnoutType(TurnoutTypes typeName) {
		logger.debug("getTurnoutType()");
		EntityManager em = getEntityManager();
		String typeStr = "";
		switch (typeName) {
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
				"from TurnoutType as t where t.typeName = ?1").setParameter(1,
				typeStr).getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return turnoutType;
	}

	public int getNextFreeTurnoutNumber() {
		logger.debug("getNextFreeTurnoutNumber()");
		SortedSet<Turnout> turnouts = getAllTurnouts();
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}
}
