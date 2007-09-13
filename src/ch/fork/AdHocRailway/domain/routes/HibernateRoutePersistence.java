package ch.fork.AdHocRailway.domain.routes;

import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

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
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<Route> routes = em.createQuery("from Route").getResultList();
		t.commit();
		return new TreeSet<Route>(routes);
	}

	@SuppressWarnings("unchecked")
	public Route getRouteByNumber(int number) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		Route route = (Route) em.createQuery(
				"from Route as l where l.number = ?1").setParameter(1, number)
				.getSingleResult();
		t.commit();
		return route;
	}

	public void addRoute(Route route) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(route);
		t.commit();
		t.begin();
		em.refresh(route.getRouteGroup());
		t.commit();
	}

	public void deleteRoute(Route route) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(route);
		t.commit();
	}

	public void updateRoute(Route route) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(route);
		em.refresh(route.getRouteGroup());
		t.commit();
		t.begin();
		em.refresh(route.getRouteGroup());
		t.commit();
	}

	public void refreshRoute(Route route) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(route);
		em.refresh(route.getRouteGroup());
		t.commit();
		t.begin();
		em.refresh(route.getRouteGroup());
		t.commit();
	}

	@SuppressWarnings("unchecked")
	public SortedSet<RouteGroup> getAllRouteGroups() {
		EntityTransaction t = em.getTransaction();
		t.begin();
		List<RouteGroup> routeGroups = em.createQuery("from RouteGroup")
				.getResultList();
		t.commit();
		return new TreeSet<RouteGroup>(routeGroups);
	}

	public void addRouteGroup(RouteGroup routeGroup) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(routeGroup);
		t.commit();
	}

	public void deleteRouteGroup(RouteGroup routeGroup) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(routeGroup);
		t.commit();
	}

	public void updateRouteGroup(RouteGroup routeGroup) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(routeGroup);
		t.commit();
	}

	public void refreshRouteGroup(RouteGroup routeGroup) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(routeGroup);
		t.commit();
	}
	
	public void addRouteItem(RouteItem item) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.persist(item);
		t.commit();
	}

	public void deleteRouteItem(RouteItem item) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.remove(item);
		t.commit();
	}

	public void updateRouteItem(RouteItem item) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.merge(item);
		t.commit();
	}

	public void refreshRouteItem(RouteItem item) {
		EntityTransaction t = em.getTransaction();
		t.begin();
		em.refresh(item);
		t.commit();
	}

	public int getNextFreeRouteNumber() {
		SortedSet<Route> turnouts = getAllRoutes();
		if(turnouts.isEmpty()) {
			return 1;
		}
		return turnouts.last().getNumber() + 1;
	}

}
