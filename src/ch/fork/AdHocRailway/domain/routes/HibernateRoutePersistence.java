/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.domain.routes;

import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateRoutePersistence extends HibernatePersistence implements
		RoutePersistenceIface {
	private static Logger					logger	= Logger
															.getLogger(HibernateRoutePersistence.class);
	private static RoutePersistenceIface	instance;
	private ArrayListModel<Route>			routeCache;
	private ArrayListModel<RouteItem>		routeItemCache;
	private ArrayListModel<RouteGroup>		routeGroupCache;

	private HibernateRoutePersistence() {
		logger.info("HibernateRoutePersistence lodaded");
		this.routeGroupCache = new ArrayListModel<RouteGroup>();
		this.routeCache = new ArrayListModel<Route>();
		this.routeItemCache = new ArrayListModel<RouteItem>();
		updateRouteCache();
		updateRouteGroupCache();
		updateRouteItemCache();
	}

	public static RoutePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateRoutePersistence();
		}
		return instance;
	}

	private void updateRouteCache() {
		routeCache.clear();
		for (Route r : getAllRoutesDB()) {
			routeCache.add(r);
		}
	}

	private void updateRouteItemCache() {
		routeItemCache.clear();
	}

	private void updateRouteGroupCache() {
		routeGroupCache.clear();
		for (RouteGroup rg : getAllRouteGroupsDB()) {
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

	public ArrayListModel<Route> getAllRoutes()
			throws RoutePersistenceException {
		//logger.debug("getAllRoutes()");
		if (routeCache.isEmpty()) {
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
		try {
			List<Route> routes = em.createQuery("from Route").getResultList();

			return new TreeSet<Route>(routes);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new RoutePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getRouteByNumber(int)
	 */
	@SuppressWarnings("unchecked")
	public Route getRouteByNumber(int number) throws RoutePersistenceException {
		logger.debug("getRouteByNumber()");
		for (Route r : routeCache) {
			if (r.getNumber() == number)
				return r;
		}
		throw new RoutePersistenceException("Route with number " + number
				+ " not found");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void addRoute(Route route) throws RoutePersistenceException {
		logger.debug("addRoute(" + route + ")");
		EntityManager em = getEntityManager();

		if (route.getRouteGroup() == null) {
			throw new RoutePersistenceException("Route has no associated Group");
		}
		route.getRouteGroup().getRoutes().add(route);
		em.persist(route);

		flush();
		updateRouteCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void deleteRoute(Route route) throws RoutePersistenceException {
		logger.debug("deleteRoute(" + route + ")");
		EntityManager em = getEntityManager();
		if (!route.getRouteItems().isEmpty()) {
			SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(route
					.getRouteItems());
			for (RouteItem routeitem : routeItems) {
				deleteRouteItem(routeitem);
			}
			// throw new RoutePersistenceException(
			// "Cannot delete Route-Group with associated Route-Items");
		}

		RouteGroup group = route.getRouteGroup();
		group.getRoutes().remove(route);

		Set<RouteItem> routeItems = route.getRouteItems();
		for (RouteItem ri : routeItems) {
			route.getRouteItems().remove(ri);
			em.remove(ri);
		}
		em.remove(route);

		flush();
		updateRouteCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void updateRoute(Route route) throws RoutePersistenceException {
		logger.debug("updateRoute(" + route + ")");
		EntityManager em = getEntityManager();

		em.merge(route);
		flush();
		updateRouteCache();
	}

	public ArrayListModel<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException {
		//logger.debug("getAllRouteGroups()");
		if (routeGroupCache.isEmpty()) {
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
	public SortedSet<RouteGroup> getAllRouteGroupsDB()
			throws RoutePersistenceException {
		logger.debug("getAllRouteGroupsDB()");
		EntityManager em = getEntityManager();
		try {
			List<RouteGroup> routeGroups = em.createQuery("from RouteGroup")
					.getResultList();

			return new TreeSet<RouteGroup>(routeGroups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			throw new RoutePersistenceException("Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		EntityManager em = getEntityManager();
		em.persist(routeGroup);
		flush();
		updateRouteGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		EntityManager em = getEntityManager();
		if (!routeGroup.getRoutes().isEmpty()) {
			throw new RoutePersistenceException(
					"Cannot delete Route-Group with associated Routes");
		}

		em.remove(routeGroup);
		flush();
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
		flush();
		updateRouteGroupCache();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		EntityManager em = getEntityManager();

		if (item.getTurnout() == null) {
			throw new RoutePersistenceException(
					"Route has no associated Turnout");
		}
		item.getTurnout().getRouteItems().add(item);

		if (item.getRoute() == null) {
			throw new RoutePersistenceException("Route has no associated Route");
		}
		item.getRoute().getRouteItems().add(item);
		em.persist(item);
		flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = getEntityManager();

		Turnout turnout = item.getTurnout();
		turnout.getRouteItems().remove(item);

		Route route = item.getRoute();
		route.getRouteItems().remove(item);

		em.remove(item);
		flush();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void updateRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = getEntityManager();
		em.merge(item);
		em.refresh(item.getRoute());
		flush();
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#flush()
	 */
	public void flush() throws RoutePersistenceException {
		logger.debug("flush()");
		try {
			em.getTransaction().commit();
		} catch (HibernateException ex) {
			em.getTransaction().rollback();
			em.close();
			HibernatePersistence.em = emf.createEntityManager();
			em.getTransaction().begin();
			throw new RoutePersistenceException("Error", ex);
		}
		em.getTransaction().begin();

	}

}
