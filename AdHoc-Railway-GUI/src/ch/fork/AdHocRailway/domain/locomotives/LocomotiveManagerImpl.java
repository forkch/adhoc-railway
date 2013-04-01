/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: MemoryLocomotivePersistence.java 154 2008-03-28 14:30:54Z fork_ch $
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

package ch.fork.AdHocRailway.domain.locomotives;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveServiceListener;
import de.dermoba.srcp.model.SRCPAddress;

public class LocomotiveManagerImpl implements LocomotiveManager,
		LocomotiveServiceListener {
	private static Logger LOGGER = Logger.getLogger(LocomotiveManager.class);

	private static LocomotiveManager instance;
	private LocomotiveControlface locomotiveControl;

	private final Map<SRCPAddress, Locomotive> addressLocomotiveCache = new HashMap<SRCPAddress, Locomotive>();
	private final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
	private LocomotiveService locomotiveService;

	private final Set<LocomotiveManagerListener> listeners = new HashSet<LocomotiveManagerListener>();

	private final Set<LocomotiveManagerListener> listenersToBeRemovedInNextEvent = new HashSet<LocomotiveManagerListener>();

	private LocomotiveManagerImpl() {
		LOGGER.info("LocomotiveManager loaded");

	}

	public static LocomotiveManager getInstance() {
		if (instance == null) {
			instance = new LocomotiveManagerImpl();
		}
		return instance;
	}

	@Override
	public void addLocomotiveManagerListener(
			final LocomotiveManagerListener listener) {
		this.listeners.add(listener);
		listener.locomotivesUpdated(locomotiveGroups);
	}

	@Override
	public void removeLocomotiveManagerListenerInNextEvent(
			final LocomotiveManagerListener listener) {
		listenersToBeRemovedInNextEvent.add(listener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		clearCache();
		locomotivesUpdated(getAllLocomotiveGroups());
	}

	@Override
	public void clearToService() {
		LOGGER.debug("clearToService()");
		locomotiveService.clear();
	}

	@Override
	public List<Locomotive> getAllLocomotives() {
		return new ArrayList<Locomotive>(addressLocomotiveCache.values());
	}

	@Override
	public Locomotive getLocomotiveByBusAddress(final int bus, final int address) {
		final Locomotive locomotive = addressLocomotiveCache
				.get(new SRCPAddress(bus, address, 0, 0));
		if (locomotive != null) {
			return locomotive;
		}
		throw new LocomotiveManagerException("Locomotive with bus " + bus
				+ " and address " + address + " not found");
	}

	@Override
	public void addLocomotiveToGroup(final Locomotive locomotive,
			final LocomotiveGroup group) {
		if (group == null) {
			throw new LocomotiveManagerException(
					"Locomotive has no associated Group");
		}
		group.addLocomotive(locomotive);
		locomotive.setGroup(group);
		locomotiveService.addLocomotive(locomotive);

	}

	@Override
	public void removeLocomotiveFromGroup(final Locomotive locomotive,
			final LocomotiveGroup group) {
		locomotiveService.removeLocomotive(locomotive);
		group.removeLocomotive(locomotive);

		removeFromCache(locomotive);
	}

	@Override
	public void updateLocomotive(final Locomotive locomotive) {
		locomotiveService.updateLocomotive(locomotive);

	}

	@Override
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
		return locomotiveGroups;
	}

	@Override
	public void addLocomotiveGroup(final LocomotiveGroup group) {
		locomotiveService.addLocomotiveGroup(group);
		locomotiveGroups.add(group);
	}

	@Override
	public void deleteLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		if (group.getId() == Integer.MIN_VALUE) {
			throw new LocomotiveManagerException(
					"Cannot delete ALL_LOCOMOTIVES_GROUP");
		}
		if (!group.getLocomotives().isEmpty()) {
			throw new LocomotiveManagerException(
					"Cannot delete locomotive group with associated locomotives");
		}
		locomotiveService.removeLocomotiveGroup(group);
		locomotiveGroups.remove(group);
	}

	@Override
	public void updateLocomotiveGroup(final LocomotiveGroup group) {
		locomotiveService.updateLocomotiveGroup(group);
	}

	@Override
	public void setLocomotiveService(final LocomotiveService instance) {
		this.locomotiveService = instance;
	}

	@Override
	public void initialize() {
		clear();
		cleanupListeners();
		locomotiveService.init(this);
	}

	@Override
	public void setLocomotiveControl(
			final LocomotiveControlface locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	@Override
	public void locomotivesUpdated(
			final SortedSet<LocomotiveGroup> updatedLocomotiveGroups) {
		LOGGER.info("locomotivesUpdated: " + updatedLocomotiveGroups);
		cleanupListeners();
		clearCache();
		for (final LocomotiveGroup group : updatedLocomotiveGroups) {
			putLocomotiveGroupInCache(group);
			for (final Locomotive locomotive : group.getLocomotives()) {
				putInCache(locomotive);
			}
		}
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotivesUpdated(updatedLocomotiveGroups);
		}
	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
		LOGGER.info("locomotiveAdded: " + locomotive);
		cleanupListeners();
		putInCache(locomotive);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveAdded(locomotive);
		}
	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {
		LOGGER.info("locomotiveUpdated: " + locomotive);
		cleanupListeners();
		putInCache(locomotive);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveUpdated(locomotive);
		}
	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {
		LOGGER.info("locomotiveRemoved: " + locomotive);
		cleanupListeners();
		removeFromCache(locomotive);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveRemoved(locomotive);
		}
	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupAdded: " + group);
		cleanupListeners();
		putLocomotiveGroupInCache(group);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupAdded(group);
		}
	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupUpdated: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		putLocomotiveGroupInCache(group);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupUpdated(group);
		}
	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupRemoved: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		for (final LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupRemoved(group);
		}
	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {
		LOGGER.warn("failure", locomotiveManagerException);
		cleanupListeners();
		for (final LocomotiveManagerListener l : listeners) {
			l.failure(locomotiveManagerException);
		}
	}

	@Override
	public void disconnect() {
		cleanupListeners();
		locomotiveService.disconnect();
		locomotivesUpdated(new TreeSet<LocomotiveGroup>());
	}

	private void putLocomotiveGroupInCache(final LocomotiveGroup group) {
		locomotiveGroups.add(group);
	}

	private void removeTurnoutGroupFromCache(final LocomotiveGroup group) {
		locomotiveGroups.remove(group);
	}

	private void putInCache(final Locomotive locomotive) {
		addressLocomotiveCache.put(
				new SRCPAddress(locomotive.getBus(), locomotive.getAddress1(),
						locomotive.getBus(), locomotive.getAddress2()),
				locomotive);
		locomotiveControl.addOrUpdateLocomotive(locomotive);
	}

	private void removeFromCache(final Locomotive locomotive) {
		addressLocomotiveCache.values().remove(locomotive);
	}

	private void clearCache() {
		addressLocomotiveCache.clear();
		locomotiveGroups.clear();
	}

}
