package ch.fork.AdHocRailway.domain.routes;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import ch.fork.AdHocRailway.domain.HibernatePersistence;

public class HibernateRoutePersistence extends HibernatePersistence {

	private static HibernateRoutePersistence instance;

	private HibernateRoutePersistence() {
		super();
	}

	public static HibernateRoutePersistence getInstance() {
		if (instance == null) {
			instance = new HibernateRoutePersistence();
		}
		return instance;
	}

	public void preload() {
	}

	@SuppressWarnings("unchecked")
	public SortedSet<Route> getAllRoutes() {
		EntityManager em = getEntityManager();
		List<Route> routes = em.createQuery("from Route").getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<Route>(routes);
	}

	@SuppressWarnings("unchecked")
	public Route getRouteByNumber(int number) {
		EntityManager em = getEntityManager();
		Route route = (Route) em.createQuery(
				"from Route as l where l.number = ?1").setParameter(1, number)
				.getSingleResult();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return route;
	}

	public void addRoute(Route route) {
		EntityManager em = getEntityManager();
		em.persist(route);
		em.refresh(route.getRouteGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void deleteRoute(Route route) {
		EntityManager em = getEntityManager();
		em.remove(route);
		em.refresh(route.getRouteGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void updateRoute(Route route) {
		EntityManager em = getEntityManager();
		em.merge(route);
		em.refresh(route);
		em.refresh(route.getRouteGroup());
		em.refresh(route.getRouteItems());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void refreshRoute(Route route) {
		EntityManager em = getEntityManager();
		em.refresh(route);
		em.refresh(route.getRouteGroup());
		em.refresh(route.getRouteGroup());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	@SuppressWarnings("unchecked")
	public SortedSet<RouteGroup> getAllRouteGroups() {
		EntityManager em = getEntityManager();
		List<RouteGroup> routeGroups = em.createQuery("from RouteGroup")
				.getResultList();
		em.getTransaction().commit();
		em.getTransaction().begin();
		return new TreeSet<RouteGroup>(routeGroups);
	}

	public void addRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.persist(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void deleteRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.remove(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void updateRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.merge(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void refreshRouteGroup(RouteGroup routeGroup) {
		EntityManager em = getEntityManager();
		em.refresh(routeGroup);
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void addRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		em.persist(item);
		em.refresh(item.getRoute());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void deleteRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		em.remove(item);
		em.refresh(item.getRoute());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void updateRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		em.merge(item);
		em.refresh(item.getRoute());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public void refreshRouteItem(RouteItem item) {
		EntityManager em = getEntityManager();
		em.refresh(item);
		em.refresh(item.getRoute());
		em.getTransaction().commit();
		em.getTransaction().begin();
	}

	public int getNextFreeRouteNumber() {
		SortedSet<Route> turnouts = getAllRoutes();
		if (turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

}
