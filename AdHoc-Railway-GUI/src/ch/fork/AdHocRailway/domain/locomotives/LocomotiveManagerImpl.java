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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.services.locomotives.HibernateLocomotiveService;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;

import com.jgoodies.binding.list.ArrayListModel;

import de.dermoba.srcp.model.SRCPAddress;

public class LocomotiveManagerImpl implements LocomotiveManager {
	private static Logger LOGGER = Logger.getLogger(LocomotiveManager.class);

	private static LocomotiveManagerImpl instance;

	private final Map<SRCPAddress, Locomotive> addressLocomotiveCache = new HashMap<SRCPAddress, Locomotive>();
	private final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();
	private final LocomotiveService locomotiveService = HibernateLocomotiveService
			.getInstance();
	private final LocomotiveGroup ALL_LOCOMOTIVE_GROUP = new LocomotiveGroup(
			Integer.MIN_VALUE, "All");

	private LocomotiveManagerImpl() {
		LOGGER.info("LocomotiveManager loaded");

		reload();
	}

	public static LocomotiveManagerImpl getInstance() {
		if (instance == null) {
			instance = new LocomotiveManagerImpl();
		}
		return instance;
	}

	@Override
	public void clear() {
		addressLocomotiveCache.clear();
		locomotiveGroups.clear();
		ALL_LOCOMOTIVE_GROUP.getLocomotives().clear();
	}

	@Override
	public List<Locomotive> getAllLocomotives() {
		return new ArrayListModel<Locomotive>(addressLocomotiveCache.values());
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

		putInCache(locomotive);
	}

	private void putInCache(Locomotive locomotive) {
		addressLocomotiveCache.put(new SRCPAddress(locomotive.getBus(),
				locomotive.getAddress(), 0, 0), locomotive);
		ALL_LOCOMOTIVE_GROUP.getLocomotives().add(locomotive);
	}

	@Override
	public void deleteLocomotive(Locomotive locomotive) {
		locomotiveService.deleteLocomotive(locomotive);
		locomotive.getLocomotiveGroup().getLocomotives().remove(locomotive);

		removeFromCache(locomotive);

	}

	private void removeFromCache(Locomotive locomotive) {
		addressLocomotiveCache.values().remove(locomotive);
		ALL_LOCOMOTIVE_GROUP.getLocomotives().remove(locomotive);
	}

	@Override
	public void updateLocomotive(Locomotive locomotive) {
		locomotiveService.updateLocomotive(locomotive);

	}

	@Override
	public List<LocomotiveGroup> getAllLocomotiveGroups() {
		LinkedList<LocomotiveGroup> allLocomotiveGroups = new LinkedList<LocomotiveGroup>();
		allLocomotiveGroups.addFirst(ALL_LOCOMOTIVE_GROUP);
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
		locomotiveService.deleteLocomotiveGroup(group);
		locomotiveGroups.remove(group);
	}

	@Override
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		updateLocomotiveGroup(group);
	}

	@Override
	public void reload() {
		clear();
		for (LocomotiveGroup group : locomotiveService.getAllLocomotiveGroups()) {
			locomotiveGroups.add(group);
			for (Locomotive locomotive : group.getLocomotives()) {
				putInCache(locomotive);
			}
		}
	}
}
