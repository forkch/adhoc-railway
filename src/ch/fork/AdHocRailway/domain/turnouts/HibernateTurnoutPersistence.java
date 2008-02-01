package ch.fork.AdHocRailway.domain.turnouts;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityNotFoundException;
import javax.persistence.NoResultException;
import javax.persistence.RollbackException;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.LookupAddress;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType.TurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateTurnoutPersistence extends HibernatePersistence implements
		TurnoutPersistenceIface {
	private static Logger logger = Logger
			.getLogger(HibernateTurnoutPersistence.class);
	private static TurnoutPersistenceIface instance;

	private Map<LookupAddress, Turnout> addressTurnoutCache;
	private Map<LookupAddress, Turnout> addressThreewayCache;
	private ArrayListModel<Turnout> turnoutCache;
	private ArrayListModel<TurnoutGroup> turnoutGroupCache;

	private HibernateTurnoutPersistence() {
		super();
		this.addressTurnoutCache = new HashMap<LookupAddress, Turnout>();
		this.addressThreewayCache = new HashMap<LookupAddress, Turnout>();
		this.turnoutCache = new ArrayListModel<Turnout>();
		this.turnoutGroupCache = new ArrayListModel<TurnoutGroup>();
	}

	public static TurnoutPersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateTurnoutPersistence();
		}
		return instance;
	}

	private void updateTurnoutCache() {
		addressTurnoutCache.clear();
		turnoutCache.clear();
		SortedSet<Turnout> turnouts = getAllTurnoutsDB();
		turnoutCache.addAll(turnouts);

		for (Turnout t : turnouts) {
			addressTurnoutCache.put(new LookupAddress(t.getBus1(), t
					.getAddress1(), t.getBus2(), t.getAddress2()), t);
			if (t.isThreeWay()) {
				addressThreewayCache.put(new LookupAddress(t.getBus1(), t
						.getAddress1(), 0, 0), t);
				addressThreewayCache.put(new LookupAddress(0, 0, t.getBus2(), t
						.getAddress2()), t);
			}
		}

	}

	private void updateTurnoutGroupCache() {
		turnoutGroupCache.clear();
		System.out.println(getAllTurnoutGroupsDB());
		turnoutGroupCache.addAll(getAllTurnoutGroupsDB());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#preload()
	 */
	public void preload() throws TurnoutException {
		getAllTurnoutGroupsDB();
		getAllTurnoutGroupsDB();
		getAllTurnoutTypes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnouts()
	 */
	public ArrayListModel<Turnout> getAllTurnouts() {
		if (addressTurnoutCache.size() == 0) {
			updateTurnoutCache();
		}
		return turnoutCache;
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
		LookupAddress key1 = new LookupAddress(bus, address, 0, 0);
		Turnout lookup1 = addressTurnoutCache.get(key1);
		if (lookup1 != null)
			return lookup1;
		LookupAddress key2 = new LookupAddress(0, 0, bus, address);
		Turnout lookup2 = addressTurnoutCache.get(key2);
		if (lookup2 != null)
			return lookup2;
		Turnout threewayLookup1 = addressThreewayCache.get(key1);
		if (threewayLookup1 != null)
			return threewayLookup1;

		Turnout threewayLookup2 = addressThreewayCache.get(key2);
		if (threewayLookup2 != null)
			return threewayLookup2;

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void addTurnout(Turnout turnout) throws TurnoutPersistenceException {
		EntityManager em = getEntityManager();
		if (turnout.getTurnoutGroup() == null) {
			throw new TurnoutPersistenceException(
					"Turnout has no associated Group");
		}
		turnout.getTurnoutGroup().getTurnouts().add(turnout);
		em.persist(turnout);

		em.getTransaction().commit();
		em.getTransaction().begin();

		// turnoutCache.add(turnout);
		// addressTurnoutCache.put(new LookupAddress(turnout.getBus1(), turnout
		// .getAddress1(), turnout.getBus2(), turnout.getAddress2()),
		// turnout);
		// if (turnout.isThreeWay()) {
		// addressThreewayCache.put(new LookupAddress(turnout.getBus1(),
		// turnout.getAddress1(), 0, 0), turnout);
		// addressThreewayCache.put(new LookupAddress(0, 0, turnout.getBus2(),
		// turnout.getAddress2()), turnout);
		// }

		updateTurnoutCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void deleteTurnout(Turnout turnout) {

		EntityManager em = getEntityManager();

		TurnoutGroup group = turnout.getTurnoutGroup();
		group.getTurnouts().remove(turnout);

		TurnoutType type = turnout.getTurnoutType();
		type.getTurnouts().remove(turnout);

		Set<RouteItem> routeItems = turnout.getRouteItems();
		for (RouteItem ri : routeItems) {

			Route route = ri.getRoute();
			route.getRouteItems().remove(ri);
			em.remove(ri);

		}
		em.remove(turnout);

		em.getTransaction().commit();
		em.getTransaction().begin();

		// turnoutCache.remove(turnout);
		// addressTurnoutCache.remove(new LookupAddress(turnout.getBus1(),
		// turnout
		// .getAddress1(), turnout.getBus2(), turnout.getAddress2()));
		// if (turnout.isThreeWay()) {
		// addressThreewayCache.remove(new LookupAddress(turnout.getBus1(),
		// turnout.getAddress1(), 0, 0));
		// addressThreewayCache.remove(new LookupAddress(0, 0, turnout
		// .getBus2(), turnout.getAddress2()));
		// }

		updateTurnoutCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#updateTurnout(ch.fork.AdHocRailway.domain.turnouts.Turnout)
	 */
	public void updateTurnout(Turnout turnout)
			throws TurnoutPersistenceException {
		logger.debug("updateTurnout()");
		EntityManager em = getEntityManager();
		em.merge(turnout);
		try {
			em.getTransaction().commit();
		} catch (RollbackException ex) {
			em.getTransaction().begin();
			throw new TurnoutPersistenceException("Duplicate Number");
		}
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getNumberToTurnout()
	 */
	public Map<Integer, Turnout> getNumberToTurnout() {
		return null;
	}

	public ArrayListModel<TurnoutGroup> getAllTurnoutGroups() {
		if (turnoutGroupCache.isEmpty()) {
			updateTurnoutGroupCache();
		}
		return turnoutGroupCache;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#getAllTurnoutGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<TurnoutGroup> getAllTurnoutGroupsDB() {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#addTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void addTurnoutGroup(TurnoutGroup group) {
		EntityManager em = getEntityManager();
		em.persist(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateTurnoutGroupCache();
		// this.turnoutGroupCache.add(group);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface#deleteTurnoutGroup(ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup)
	 */
	public void deleteTurnoutGroup(TurnoutGroup group)
			throws TurnoutPersistenceException {
		EntityManager em = getEntityManager();
		if (!group.getTurnouts().isEmpty()) {
			throw new TurnoutPersistenceException(
					"Cannot delete turnout group with assiciated turnouts");
		}
		em.remove(group);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateTurnoutGroupCache();
		// this.turnoutGroupCache.remove(group);
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
		SortedSet<Turnout> turnouts = new TreeSet<Turnout>(getAllTurnouts());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}
}
