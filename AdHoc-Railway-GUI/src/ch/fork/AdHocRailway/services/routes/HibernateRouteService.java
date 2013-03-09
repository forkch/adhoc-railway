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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RouteManagerException;
import ch.fork.AdHocRailway.services.HibernateUtil;
import ch.fork.AdHocRailway.services.turnouts.HibernateTurnout;

public class HibernateRouteService implements RouteService {
	private static Logger logger = Logger
			.getLogger(HibernateRouteService.class);
	private static RouteService instance;
	private RouteServiceListener listener;

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
	public void clear() throws RouteManagerException {
		logger.debug("clear()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE route_item").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE route").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE route_group")
					.executeUpdate();

			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new RouteManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRoute(Route route) throws RouteManagerException {
		logger.debug("addRoute(" + route + ")");

		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateRoute hRoute = HibernateRouteMapper.mapRoute(route);
			Integer hRouteGroupId = route.getRouteGroup().getId();
			HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) session
					.get(HibernateRouteGroup.class, hRouteGroupId);

			hRouteGroup.getRoutes().add(hRoute);
			hRoute.setRouteGroup(hRouteGroup);

			Integer id = (Integer) session.save(hRoute);
			route.setId(id);
			transaction.commit();
			listener.routeAdded(route);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();

		}
	}

	@Override
	public void removeRoute(Route route) throws RouteManagerException {
		logger.debug("deleteRoute()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = route.getId();
			HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, id);
			session.delete(hRoute);
			transaction.commit();
			listener.routeRemoved(route);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRoute(Route route) throws RouteManagerException {
		logger.debug("updateRoute(" + route + ")");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = route.getId();
			HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, id);

			HibernateRouteMapper.updateHibernateRoute(hRoute, route);
			session.update(hRoute);
			transaction.commit();
			listener.routeUpdated(route);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new RouteManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException {
		logger.debug("addRouteGroup");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateRouteGroup hRouteGroup = HibernateRouteMapper
					.map(routeGroup);
			Integer id = (Integer) session.save(hRouteGroup);

			routeGroup.setId(id);
			transaction.commit();
			listener.routeGroupAdded(routeGroup);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeRouteGroup(RouteGroup routeGroup)
			throws RouteManagerException {
		logger.debug("deleteRouteGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Integer id = routeGroup.getId();

			HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) session
					.get(HibernateRouteGroup.class, id);
			session.delete(hRouteGroup);
			transaction.commit();
			listener.routeGroupRemoved(routeGroup);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRouteGroup(RouteGroup routeGroup) {
		logger.debug("updateRouteGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = routeGroup.getId();
			HibernateRouteGroup hRoute = (HibernateRouteGroup) session.get(
					HibernateRouteGroup.class, id);

			HibernateRouteMapper.updateHibernateRouteGroup(hRoute, routeGroup);
			session.update(hRoute);
			transaction.commit();
			listener.routeGroupUpdated(routeGroup);
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public List<RouteGroup> getAllRouteGroups() throws RouteManagerException {
		logger.debug("getAllRouteGroupsDB()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			List hRouteGroups = session.createQuery("from HibernateRouteGroup")
					.list();
			List<RouteGroup> routeGroups = new LinkedList<RouteGroup>();
			for (Iterator iterator = hRouteGroups.iterator(); iterator
					.hasNext();) {
				HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) iterator
						.next();
				RouteGroup routeGroup = HibernateRouteMapper
						.mapHibernateRouteGroup(hRouteGroup);
				routeGroups.add(routeGroup);
			}
			transaction.commit();

			return routeGroups;
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRouteItem(RouteItem item) throws RouteManagerException {
		logger.debug("addRouteItem");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			HibernateRouteItem hRouteItem = HibernateRouteMapper
					.mapRouteItem(item);
			Integer routeId = item.getRoute().getId();
			HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, routeId);
			hRoute.getRouteItems().add(hRouteItem);
			hRouteItem.setRoute(hRoute);

			Integer turnoutId = item.getTurnout().getId();
			HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, turnoutId);
			hTurnout.getRouteItems().add(hRouteItem);
			hRouteItem.setTurnout(hTurnout);

			Integer id = (Integer) session.save(hRouteItem);
			item.setId(id);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeRouteItem(RouteItem item) throws RouteManagerException {
		logger.debug("deleteRouteItem()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			Integer id = item.getId();

			HibernateRouteItem hRouteItem = (HibernateRouteItem) session.get(
					HibernateRouteItem.class, id);
			session.delete(hRouteItem);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRouteItem(RouteItem item) throws RouteManagerException {
		logger.debug("updateRouteGroup()");
		Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			Integer id = item.getId();
			HibernateRouteItem hTurnout = (HibernateRouteItem) session.get(
					HibernateRouteItem.class, id);

			HibernateRouteMapper.updateHibernateRouteItem(hTurnout, item);
			session.update(hTurnout);
			transaction.commit();
		} catch (HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void init(RouteServiceListener listener) {
		this.listener = listener;
		List<RouteGroup> allRouteGroups = getAllRouteGroups();
		listener.routesUpdated(allRouteGroups);
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}
}
