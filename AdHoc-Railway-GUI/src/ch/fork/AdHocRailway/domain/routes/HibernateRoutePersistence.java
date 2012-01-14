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
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;

import com.jgoodies.binding.list.ArrayListModel;

public class HibernateRoutePersistence extends CachingRoutePersistence {
	private static Logger					logger	= Logger
															.getLogger(HibernateRoutePersistence.class);
	private static RoutePersistenceIface	instance;

	private HibernateRoutePersistence() {
		super();
		super.clear();
		logger.info("HibernateRoutePersistence loaded");

		updateRouteCache();
		updateRouteGroupCache();
	}

	public static RoutePersistenceIface getInstance() {
		if (instance == null) {
			instance = new HibernateRoutePersistence();
		}
		return instance;
	}

	private void updateRouteCache() {
		
		for (Route r : getAllRoutesDB()) {
			super.addRoute(r);
		}
	}


	private void updateRouteGroupCache() {
		
		for (RouteGroup rg : getAllRouteGroupsDB()) {
			super.addRouteGroup(rg);
		}
	}

	public void clear() throws RoutePersistenceException {
		logger.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE route_item").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE route").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE route_group").executeUpdate();
			
			super.clear();

			em.getTransaction().commit();
			HibernatePersistence.disconnect();
			HibernatePersistence.connect();
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
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
		// logger.debug("getAllRoutes()");
		if (super.getAllRoutes().isEmpty()) {
			updateRouteCache();
		}
		return super.getAllRoutes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getAllRoutes()
	 */
	@SuppressWarnings("unchecked")
	public SortedSet<Route> getAllRoutesDB() {
		logger.debug("getAllRoutesDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<Route> routes = em.createQuery("from Route").getResultList();
			HibernatePersistence.flush();
			return new TreeSet<Route>(routes);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
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
		return super.getRouteByNumber(number);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void addRoute(Route route) throws RoutePersistenceException {
		logger.debug("addRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();

		if (route.getRouteGroup() == null) {
			throw new RoutePersistenceException("Route has no associated Group");
		}
		try {
			route.getRouteGroup().getRoutes().add(route);
			em.persist(route);

			HibernatePersistence.flush();
			super.addRoute(route);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void deleteRoute(Route route) throws RoutePersistenceException {
		logger.debug("deleteRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();
		if (!route.getRouteItems().isEmpty()) {
			SortedSet<RouteItem> routeItems = new TreeSet<RouteItem>(route
					.getRouteItems());
			for (RouteItem routeitem : routeItems) {
				deleteRouteItem(routeitem);
			}
			// throw new RoutePersistenceException(
			// "Cannot delete Route-Group with associated Route-Items");
		}
		try {
			RouteGroup group = route.getRouteGroup();
			group.getRoutes().remove(route);

			Set<RouteItem> routeItems = route.getRouteItems();
			for (RouteItem ri : routeItems) {
				route.getRouteItems().remove(ri);
				em.remove(ri);
			}
			em.remove(route);

			HibernatePersistence.flush();
			super.deleteRoute(route);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(ch.fork.AdHocRailway.domain.routes.Route)
	 */
	public void updateRoute(Route route) throws RoutePersistenceException {
		logger.debug("updateRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(route);
			HibernatePersistence.flush();
			super.updateRoute(route);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	public ArrayListModel<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException {
		// logger.debug("getAllRouteGroups()");
		if (super.getAllRouteGroups().isEmpty()) {
			updateRouteGroupCache();
		}
		return super.getAllRouteGroups();
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
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<RouteGroup> routeGroups = em.createQuery("from RouteGroup")
					.getResultList();

			return new TreeSet<RouteGroup>(routeGroups);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(routeGroup);
			HibernatePersistence.flush();
			super.addRouteGroup(routeGroup);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void deleteRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		if (!routeGroup.getRoutes().isEmpty()) {
			throw new RoutePersistenceException(
					"Cannot delete Route-Group with associated Routes");
		}
		try {
			em.remove(routeGroup);
			HibernatePersistence.flush();
			super.deleteRouteGroup(routeGroup);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteGroup(ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	public void updateRouteGroup(RouteGroup routeGroup) {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(routeGroup);
			HibernatePersistence.flush();
			super.updateRouteGroup(routeGroup);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();

		if (item.getTurnout() == null) {
			throw new RoutePersistenceException(
					"Route has no associated Turnout");
		}
		try {
			item.getTurnout().getRouteItems().add(item);

			if (item.getRoute() == null) {
				throw new RoutePersistenceException(
						"Route has no associated Route");
			}
			item.getRoute().getRouteItems().add(item);
			em.persist(item);

			HibernatePersistence.flush();
			super.addRouteItem(item);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			Turnout turnout = item.getTurnout();
			turnout.getRouteItems().remove(item);

			Route route = item.getRoute();
			route.getRouteItems().remove(item);

			em.remove(item);

			HibernatePersistence.flush();
			super.deleteRouteItem(item);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem(ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	public void updateRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(item);
			em.refresh(item.getRoute());
			HibernatePersistence.flush();
			super.updateRouteItem(item);
		} catch (HibernateException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		} catch (PersistenceException x) {
			em.close();
			HibernatePersistence.connect();
			throw new RoutePersistenceException("Database Error", x);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getNextFreeRouteNumber()
	 */
	public int getNextFreeRouteNumber() {
		return super.getNextFreeRouteNumber();
	}
	
	public void flush() {
		HibernatePersistence.flush();
	}

	public void reload() {
		
//		EntityManager em = HibernatePersistence.getEntityManager();
//		try {
//			for(RouteGroup g : getAllRouteGroups()) {
//				em.refresh(g);
//			}
//		} catch (HibernateException x) {
//			em.close();
//			HibernatePersistence.connect();
//			throw new RoutePersistenceException("Database Error", x);
//		} catch (PersistenceException x) {
//			em.close();
//			HibernatePersistence.connect();
//			throw new RoutePersistenceException("Database Error", x);
//		}
		super.clear();
		logger.info("HibernateRoutePersistence reloaded");

	}

}
