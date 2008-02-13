package ch.fork.AdHocRailway.domain.routes;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.RollbackException;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateRoutePersistence extends HibernatePersistence implements
		RoutePersistenceIface {
	private static Logger logger = Logger
			.getLogger(HibernateRoutePersistence.class);
	private static RoutePersistenceIface instance;
	private ArrayListModel<Route> routeCache;
	private ArrayListModel<RouteItem> routeItemCache;
	private ArrayListModel<RouteGroup> routeGroupCache;

	private HibernateRoutePersistence() {
		logger.info("HibernateRoutePersistence lodaded");
		this.routeGroupCache = new ArrayListModel<RouteGroup>();
		this.routeCache = new ArrayListModel<Route>();
		this.routeItemCache = new ArrayListModel<RouteItem>();
	}


	public static RoutePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateRoutePersistence();
		}
		return instance;
	}
	private void updateRouteCache() {
		routeCache.clear();
		for(Route r : getAllRoutesDB()) {
			routeCache.add(r);
		}
	}
	
	private void updateRouteItemCache() {
		routeItemCache.clear();
	}
	
	private void updateRouteGroupCache() {
		routeGroupCache.clear();
		for(RouteGroup rg : getAllRouteGroupsDB()) {
			routeGroupCache.add(rg);
		}
	}
	
	public void clear() throws RoutePersistenceException {
		logger.debug("clear()");

		EntityManager em = getEntityManager();
		em.createNativeQuery("TRUNCATE TABLE route_item").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE route").executeUpdate();
		em.createNativeQuery("TRUNCATE TABLE route_group").executeUpdate();
		routeCache.clear();
		routeItemCache.clear();
		routeGroupCache.clear();
		em.getTransaction().commit();
		HibernatePersistence.em = emf.createEntityManager();
		HibernatePersistence.em.getTransaction().begin();
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#preload()
	 */
	public void preload() {
	}

	public ArrayListModel<Route> getAllRoutes() {
		logger.debug("getAllRoutes()");
		if(routeCache.isEmpty()) {
			updateRouteCache();
		}
		return routeCache;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getAllRoutes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<Route> getAllRoutesDB() {
		logger.debug("getAllRoutesDB()");
		EntityManager em = getEntityManager();
		List<Route> routes = em.createQuery("from Route").getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<Route>(routes);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getRouteByNumber(int)
	 */
	@SuppressWarnings("unchecked")
	public Route getRouteByNumber(int number) {
		logger.debug("getRouteByNumber()");
		EntityManager em = getEntityManager();
		Route route = (Route) em.createQuery(
				"from Route as l where l.number = ?1").setParameter(1, number)
				.getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return route;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void addRoute(Route route) throws RoutePersistenceException {
		logger.debug("addRoute("+route+")");
		EntityManager em = getEntityManager();

		if (route.getRouteGroup() == null) {
			throw new RoutePersistenceException("Route has no associated Group");
		}
		route.getRouteGroup().getRoutes().add(route);
		em.persist(route);

		em.getTransaction().commit();
		em.getTransaction().begin();
		//routeCache.add(route);
		updateRouteCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void deleteRoute(Route route) throws RoutePersistenceException {
		logger.debug("deleteRoute("+route+")");
		EntityManager em = getEntityManager();
		if(!route.getRouteItems().isEmpty()) {
			throw new RoutePersistenceException("Cannot delete Route-Group with associated Route-Items");
		}
		
		RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		Set<RouteItem> routeItems = route.getRouteItems();
		for (RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
			em.remove(ri);
		}
		em.remove(route);

		em.getTransaction().commit();
		em.getTransaction().begin();
		//routeCache.remove(route);
		updateRouteCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void updateRoute(Route route) {
		logger.debug("updateRoute("+route+")");
		EntityManager em = getEntityManager();

		em.merge(route);

		em.getTransaction().commit();
		em.getTransaction().begin();
		updateRouteCache();
	}

	public ArrayListModel<RouteGroup> getAllRouteGroups() {
		logger.debug("getAllRouteGroups()");
		if(routeGroupCache.isEmpty()) {
			updateRouteGroupCache();
		}
		return routeGroupCache;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getAllRouteGroups()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<RouteGroup> getAllRouteGroupsDB() {
		logger.debug("getAllRouteGroupsDB()");
		EntityManager em = getEntityManager();
		List<RouteGroup> routeGroups = em.createQuery("from RouteGroup")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<RouteGroup>(routeGroups);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void addRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.persist(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
		//routeGroupCache.add(routeGroup);
		updateRouteGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void deleteRouteGroup(RouteGroup routeGroup) throws RoutePersistenceException {
		EntityManager em = getEntityManager();
		if(!routeGroup.getRoutes().isEmpty()) {
			throw new RoutePersistenceException("Cannot delete Route-Group with associated Routes");
		}
		
		em.remove(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
		//routeGroupCache.remove(routeGroup);
		updateRouteGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void updateRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.merge(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
		updateRouteGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		EntityManager em = getEntityManager();
		
		if(item.getTurnout() == null) {
			throw new RoutePersistenceException("Route has no associated Turnout");
		}
		item.getTurnout().getRouteItems().add(item);

		if(item.getRoute() == null) {
			throw new RoutePersistenceException("Route has no associated Route");
		}
		item.getRoute().getRouteItems().add(item);
		em.persist(item);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void deleteRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		
		Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);
		
		Route route = item.getRoute();
		route.getRouteItems().remove(item);
		
		em.remove(item);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void updateRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		em.merge(item);
		em.refresh(item.getRoute());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getNextFreeRouteNumber()
	 */
	public int getNextFreeRouteNumber() {
		SortedSet<Route> turnouts = new TreeSet<Route>(getAllRoutes());
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}


	/* (non-Javadoc)
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#flush()
	 */
	public void flush() throws RoutePersistenceException {
		logger.debug("flush()");
		try {
			em.getTransaction().commit();
		} catch (RollbackException ex) {
			em.getTransaction().begin();
			throw new RoutePersistenceException("Error", ex);
		}
		em.getTransaction().begin();
		
	}

}
