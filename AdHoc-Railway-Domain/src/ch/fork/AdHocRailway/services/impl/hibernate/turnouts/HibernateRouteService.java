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

package ch.fork.AdHocRailway.services.impl.hibernate.turnouts;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerException;
import ch.fork.AdHocRailway.services.impl.hibernate.HibernateUtil;
import ch.fork.AdHocRailway.services.turnouts.RouteService;
import ch.fork.AdHocRailway.services.turnouts.RouteServiceListener;

public class HibernateRouteService implements RouteService {
	private static Logger logger = Logger
			.getLogger(HibernateRouteService.class);
	private RouteServiceListener listener;

	public HibernateRouteService() {
		logger.info("HibernateRoutePersistence loaded");

	}

	@Override
	public void clear() throws RouteManagerException {
		logger.debug("clear()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			session.createSQLQuery("TRUNCATE TABLE route_item").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE route").executeUpdate();
			session.createSQLQuery("TRUNCATE TABLE route_group")
					.executeUpdate();

			transaction.commit();
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new RouteManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRoute(final Route route) throws RouteManagerException {
		logger.debug("addRoute(" + route + ")");

		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateRoute hRoute = HibernateRouteMapper.mapRoute(route);
			final Integer hRouteGroupId = route.getRouteGroup().getId();
			final HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) session
					.get(HibernateRouteGroup.class, hRouteGroupId);

			hRouteGroup.getRoutes().add(hRoute);
			hRoute.setRouteGroup(hRouteGroup);

			final Integer id = (Integer) session.save(hRoute);
			route.setId(id);
			transaction.commit();
			listener.routeAdded(route);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();

		}
	}

	@Override
	public void removeRoute(final Route route) throws RouteManagerException {
		logger.debug("deleteRoute()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = route.getId();
			final HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, id);
			session.delete(hRoute);
			transaction.commit();
			listener.routeRemoved(route);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRoute(final Route route) throws RouteManagerException {
		logger.debug("updateRoute(" + route + ")");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = route.getId();
			final HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, id);

			HibernateRouteMapper.updateHibernateRoute(hRoute, route);
			session.update(hRoute);
			transaction.commit();
			listener.routeUpdated(route);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new RouteManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRouteGroup(final RouteGroup routeGroup)
			throws RouteManagerException {
		logger.debug("addRouteGroup");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateRouteGroup hRouteGroup = HibernateRouteMapper
					.map(routeGroup);
			final Integer id = (Integer) session.save(hRouteGroup);

			routeGroup.setId(id);
			transaction.commit();
			listener.routeGroupAdded(routeGroup);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeRouteGroup(final RouteGroup routeGroup)
			throws RouteManagerException {
		logger.debug("deleteRouteGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final Integer id = routeGroup.getId();

			final HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) session
					.get(HibernateRouteGroup.class, id);
			session.delete(hRouteGroup);
			transaction.commit();
			listener.routeGroupRemoved(routeGroup);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRouteGroup(final RouteGroup routeGroup) {
		logger.debug("updateRouteGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = routeGroup.getId();
			final HibernateRouteGroup hRoute = (HibernateRouteGroup) session
					.get(HibernateRouteGroup.class, id);

			HibernateRouteMapper.updateHibernateRouteGroup(hRoute, routeGroup);
			session.update(hRoute);
			transaction.commit();
			listener.routeGroupUpdated(routeGroup);
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	@SuppressWarnings({ "rawtypes" })
	public SortedSet<RouteGroup> getAllRouteGroups()
			throws RouteManagerException {
		logger.debug("getAllRouteGroupsDB()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final List hRouteGroups = session.createQuery(
					"from HibernateRouteGroup").list();
			final SortedSet<RouteGroup> routeGroups = new TreeSet<RouteGroup>();
			for (final Iterator iterator = hRouteGroups.iterator(); iterator
					.hasNext();) {
				final HibernateRouteGroup hRouteGroup = (HibernateRouteGroup) iterator
						.next();
				final RouteGroup routeGroup = HibernateRouteMapper
						.mapHibernateRouteGroup(hRouteGroup);
				routeGroups.add(routeGroup);
			}
			transaction.commit();

			return routeGroups;
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void addRouteItem(final RouteItem item) throws RouteManagerException {
		logger.debug("addRouteItem");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final HibernateRouteItem hRouteItem = HibernateRouteMapper
					.mapRouteItem(item);
			final Integer routeId = item.getRoute().getId();
			final HibernateRoute hRoute = (HibernateRoute) session.get(
					HibernateRoute.class, routeId);
			hRoute.getRouteItems().add(hRouteItem);
			hRouteItem.setRoute(hRoute);

			final Integer turnoutId = item.getTurnout().getId();
			final HibernateTurnout hTurnout = (HibernateTurnout) session.get(
					HibernateTurnout.class, turnoutId);
			hTurnout.getRouteItems().add(hRouteItem);
			hRouteItem.setTurnout(hTurnout);

			final Integer id = (Integer) session.save(hRouteItem);
			item.setId(id);
			transaction.commit();
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void removeRouteItem(final RouteItem item)
			throws RouteManagerException {
		logger.debug("deleteRouteItem()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();
			final Integer id = item.getId();

			final HibernateRouteItem hRouteItem = (HibernateRouteItem) session
					.get(HibernateRouteItem.class, id);
			session.delete(hRouteItem);
			transaction.commit();
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void updateRouteItem(final RouteItem item)
			throws RouteManagerException {
		logger.debug("updateRouteGroup()");
		final Session session = HibernateUtil.openSession();
		Transaction transaction = null;
		try {
			transaction = session.beginTransaction();

			final Integer id = item.getId();
			final HibernateRouteItem hTurnout = (HibernateRouteItem) session
					.get(HibernateRouteItem.class, id);

			HibernateRouteMapper.updateHibernateRouteItem(hTurnout, item);
			session.update(hTurnout);
			transaction.commit();
		} catch (final HibernateException x) {
			transaction.rollback();
			throw new LocomotiveManagerException("Database Error", x);
		} finally {
			session.close();
		}
	}

	@Override
	public void init(final RouteServiceListener listener) {
		this.listener = listener;
		final SortedSet<RouteGroup> allRouteGroups = getAllRouteGroups();
		listener.routesUpdated(allRouteGroups);
	}

	@Override
	public void disconnect() {

	}
}
