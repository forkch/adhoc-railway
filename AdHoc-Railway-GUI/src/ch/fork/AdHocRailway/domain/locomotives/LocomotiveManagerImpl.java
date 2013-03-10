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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
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
	private final LocomotiveService locomotiveService;
	private final LocomotiveGroup ALL_LOCOMOTIVE_GROUP = new LocomotiveGroup(
			Integer.MIN_VALUE, "All");

	private final Set<LocomotiveManagerListener> listeners = new HashSet<LocomotiveManagerListener>();

	private final Set<LocomotiveManagerListener> listenersToBeRemovedInNextEvent = new HashSet<LocomotiveManagerListener>();

	private LocomotiveManagerImpl() {
		LOGGER.info("LocomotiveManager loaded");

		locomotiveService = SIOLocomotiveService.getInstance();
		locomotiveService.init(this);
	}

	public static LocomotiveManager getInstance() {
		if (instance == null) {
			instance = new LocomotiveManagerImpl();
		}
		return instance;
	}

	@Override
	public void addLocomotiveManagerListener(LocomotiveManagerListener listener) {
		this.listeners.add(listener);
		listener.locomotivesUpdated(new ArrayList<LocomotiveGroup>(
				locomotiveGroups));
	}

	@Override
	public void removeLocomotiveManagerListenerInNextEvent(
			LocomotiveManagerListener listener) {
		listenersToBeRemovedInNextEvent.add(listener);
	}

	private void cleanupListeners() {
		listeners.removeAll(listenersToBeRemovedInNextEvent);
		listenersToBeRemovedInNextEvent.clear();
	}

	@Override
	public void clear() {
		addressLocomotiveCache.clear();
		locomotiveGroups.clear();
		ALL_LOCOMOTIVE_GROUP.getLocomotives().clear();
	}

	@Override
	public List<Locomotive> getAllLocomotives() {
		return new ArrayList<Locomotive>(addressLocomotiveCache.values());
	}

	@Override
	public Locomotive getLocomotiveByBusAddress(int bus, int address) {
		Locomotive locomotive = addressLocomotiveCache.get(new SRCPAddress(bus,
				address, 0, 0));
		if (locomotive != null) {
			return locomotive;
		}
		throw new LocomotiveManagerException("Locomotive with bus " + bus
				+ " and address " + address + " not found");
	}

	@Override
	public void addLocomotive(Locomotive locomotive) {
		if (locomotive.getLocomotiveGroup() == null) {
			throw new LocomotiveManagerException(
					"Locomotive has no associated Group");
		}
		locomotive.getLocomotiveGroup().getLocomotives().add(locomotive);
		locomotiveService.addLocomotive(locomotive);

	}

	@Override
	public void deleteLocomotive(Locomotive locomotive) {
		locomotiveService.removeLocomotive(locomotive);
		locomotive.getLocomotiveGroup().getLocomotives().remove(locomotive);

		removeFromCache(locomotive);
	}

	@Override
	public void updateLocomotive(Locomotive locomotive) {
		locomotiveService.updateLocomotive(locomotive);

	}

	@Override
	public List<LocomotiveGroup> getAllLocomotiveGroups() {
		LinkedList<LocomotiveGroup> allLocomotiveGroups = new LinkedList<LocomotiveGroup>();
		// allLocomotiveGroups.addFirst(ALL_LOCOMOTIVE_GROUP);
		allLocomotiveGroups.addAll(locomotiveGroups);
		return allLocomotiveGroups;
	}

	@Override
	public void addLocomotiveGroup(LocomotiveGroup group) {
		locomotiveService.addLocomotiveGroup(group);
		locomotiveGroups.add(group);
	}

	@Override
	public void deleteLocomotiveGroup(LocomotiveGroup group)
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
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		updateLocomotiveGroup(group);
	}

	@Override
	public void initialize() {
		cleanupListeners();
	}

	@Override
	public void setLocomotiveControl(LocomotiveControlface locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	@Override
	public void locomotivesUpdated(List<LocomotiveGroup> locomotiveGroups) {
		LOGGER.info("locomotivesUpdated: " + locomotiveGroups);
		cleanupListeners();
		clear();
		for (LocomotiveGroup group : locomotiveGroups) {
			putTurnoutGroupInCache(group);
			for (Locomotive locomotive : group.getLocomotives()) {
				putInCache(locomotive);
			}
		}
		for (LocomotiveManagerListener l : listeners) {
			l.locomotivesUpdated(locomotiveGroups);
		}
	}

	@Override
	public void locomotiveAdded(Locomotive locomotive) {
		LOGGER.info("locomotiveAdded: " + locomotive);
		cleanupListeners();
		putInCache(locomotive);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveAdded(locomotive);
		}
	}

	@Override
	public void locomotiveUpdated(Locomotive locomotive) {
		LOGGER.info("locomotiveUpdated: " + locomotive);
		cleanupListeners();
		putInCache(locomotive);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveUpdated(locomotive);
		}
	}

	@Override
	public void locomotiveRemoved(Locomotive locomotive) {
		LOGGER.info("locomotiveRemoved: " + locomotive);
		cleanupListeners();
		removeFromCache(locomotive);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveRemoved(locomotive);
		}
	}

	@Override
	public void locomotiveGroupAdded(LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupAdded: " + group);
		cleanupListeners();
		putTurnoutGroupInCache(group);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupAdded(group);
		}
	}

	@Override
	public void locomotiveGroupUpdated(LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupUpdated: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		putTurnoutGroupInCache(group);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupUpdated(group);
		}
	}

	@Override
	public void locomotiveGroupRemoved(LocomotiveGroup group) {
		LOGGER.info("locomotiveGroupRemoved: " + group);
		cleanupListeners();
		removeTurnoutGroupFromCache(group);
		for (LocomotiveManagerListener l : listeners) {
			l.locomotiveGroupRemoved(group);
		}
	}

	@Override
	public void failure(LocomotiveManagerException locomotiveManagerException) {
		LOGGER.warn("failure", locomotiveManagerException);
		cleanupListeners();
		for (LocomotiveManagerListener l : listeners) {
			l.failure(locomotiveManagerException);
		}
	}

	private void putTurnoutGroupInCache(LocomotiveGroup group) {
		locomotiveGroups.add(group);
	}

	private void removeTurnoutGroupFromCache(LocomotiveGroup group) {
		locomotiveGroups.remove(group);
	}

	private void putInCache(Locomotive locomotive) {
		addressLocomotiveCache.put(new SRCPAddress(locomotive.getBus(),
				locomotive.getAddress(), 0, 0), locomotive);
		ALL_LOCOMOTIVE_GROUP.getLocomotives().add(locomotive);
		locomotiveControl.addOrUpdateLocomotive(locomotive);
	}

	private void removeFromCache(Locomotive locomotive) {
		addressLocomotiveCache.values().remove(locomotive);
		ALL_LOCOMOTIVE_GROUP.getLocomotives().remove(locomotive);
	}
}
