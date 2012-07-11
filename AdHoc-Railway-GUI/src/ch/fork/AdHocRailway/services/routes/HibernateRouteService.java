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

package ch.fork.AdHocRailway.services.routes;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceException;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;

import ch.fork.AdHocRailway.domain.HibernatePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;

public class HibernateRouteService implements RouteService {
	private static Logger logger = Logger
			.getLogger(HibernateRouteService.class);
	private static RouteService instance;

	private HibernateRouteService() {
		logger.info("HibernateRoutePersistence loaded");

	}

	public static RouteService getInstance() {
		if (instance == null) {
			instance = new HibernateRouteService();
		}
		return instance;
	}

	@Override
	public void clear() throws RoutePersistenceException {
		logger.debug("clear()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.createNativeQuery("TRUNCATE TABLE route_item").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE route").executeUpdate();
			em.createNativeQuery("TRUNCATE TABLE route_group").executeUpdate();

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getAllRoutes()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<Route> getAllRoutes() {
		logger.debug("getAllRoutesDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateRoute> hRoutes = em
					.createQuery("from HibernateRoute").getResultList();
			HibernatePersistence.flush();

			ArrayList<Route> routes = new ArrayList<Route>();
			for (HibernateRoute hRoute : hRoutes) {
				Route route = HibernateRouteMapper.map(hRoute);
				route.setRouteGroupId(hRoute.getRouteGroup().getId());
				for (HibernateRouteItem hItem : hRoute.getRouteItems()) {
					route.addRouteItemId(hItem.getId());

				}
				routes.add(route);
			}
			return routes;
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRoute(ch.
	 * fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void addRoute(Route route) throws RoutePersistenceException {
		logger.debug("addRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();

		try {
			em.persist(route);

			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRoute(
	 * ch.fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void deleteRoute(Route route) throws RoutePersistenceException {
		logger.debug("deleteRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.remove(route);
			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRoute(
	 * ch.fork.AdHocRailway.domain.routes.Route)
	 */
	@Override
	public void updateRoute(Route route) throws RoutePersistenceException {
		logger.debug("updateRoute(" + route + ")");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(route);
			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#getAllRouteGroups
	 * ()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public List<RouteGroup> getAllRouteGroups()
			throws RoutePersistenceException {
		logger.debug("getAllRouteGroupsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateRouteGroup> hRouteGroups = em.createQuery(
					"from HibernateRouteGroup").getResultList();

			ArrayList<RouteGroup> routeGroups = new ArrayList<RouteGroup>();
			for (HibernateRouteGroup hGroup : hRouteGroups) {
				RouteGroup group = HibernateRouteMapper.map(hGroup);
				routeGroups.add(group);
			}
			return routeGroups;
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
	public void addRouteGroup(RouteGroup routeGroup)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.persist(routeGroup);
			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteGroup
	 * (ch.fork.AdHocRailway.domain.routes.RouteGroup)
	 */
	@Override
	public void updateRouteGroup(RouteGroup routeGroup) {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(routeGroup);
			HibernatePersistence.flush();
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

	@Override
	@SuppressWarnings("unchecked")
	public List<RouteItem> getAllRouteItems() throws RoutePersistenceException {
		logger.debug("getAllRouteGroupsDB()");
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			List<HibernateRouteItem> hRouteGroups = em.createQuery(
					"from HibernateRouteItem").getResultList();

			ArrayList<RouteItem> routeItems = new ArrayList<RouteItem>();
			for (HibernateRouteItem hItem : hRouteGroups) {
				RouteItem item = HibernateRouteMapper.map(hItem);
				item.setTurnoutId(hItem.getTurnout().getId());
				routeItems.add(item);
			}
			return routeItems;
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#addRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void addRouteItem(RouteItem item) throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();

		try {
			item.getRoute().getRouteItems().add(item);
			em.persist(item);

			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#deleteRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void deleteRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.remove(item);

			HibernatePersistence.flush();
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
	 * @see
	 * ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface#updateRouteItem
	 * (ch.fork.AdHocRailway.domain.routes.RouteItem)
	 */
	@Override
	public void updateRouteItem(RouteItem item)
			throws RoutePersistenceException {
		EntityManager em = HibernatePersistence.getEntityManager();
		try {
			em.merge(item);
			em.refresh(item.getRoute());
			HibernatePersistence.flush();
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

	@Override
	public void flush() {
		HibernatePersistence.flush();
	}

}
