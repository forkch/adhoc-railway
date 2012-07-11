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

	private final Map<SRCPAddress, Locomotive> addressLocomotiveCache;
	private final Map<String, LocomotiveType> locomotiveTypes;
	private final Map<Integer, LocomotiveGroup> idToLocomotiveGroup;

	private final Map<Integer, LocomotiveType> idToLocomotiveType;

	private final LocomotiveService locomotiveService;

	private final Map<Integer, Locomotive> idToLocomotive;

	private LocomotiveManagerImpl() {
		LOGGER.info("LocomotiveManager loaded");
		this.addressLocomotiveCache = new HashMap<SRCPAddress, Locomotive>();
		this.idToLocomotive = new HashMap<Integer, Locomotive>();
		this.idToLocomotiveGroup = new HashMap<Integer, LocomotiveGroup>();
		this.idToLocomotiveType = new HashMap<Integer, LocomotiveType>();
		this.locomotiveTypes = new HashMap<String, LocomotiveType>();

		this.locomotiveService = HibernateLocomotiveService.getInstance();

		reload();
	}

	public static LocomotiveManagerImpl getInstance() {
		if (instance == null)
			instance = new LocomotiveManagerImpl();
		return instance;
	}

	@Override
	public void clear() {
		idToLocomotive.clear();
		addressLocomotiveCache.clear();
		locomotiveTypes.clear();
		idToLocomotiveGroup.clear();
		idToLocomotiveType.clear();
	}

	public void preload() {
	}

	@Override
	public ArrayListModel<Locomotive> getAllLocomotives() {
		return new ArrayListModel<Locomotive>(idToLocomotive.values());
	}

	@Override
	public Locomotive getLocomotiveByBusAddress(int bus, int address) {
		Locomotive locomotive = addressLocomotiveCache.get(new SRCPAddress(bus,
				address, 0, 0));
		if (locomotive != null)
			return locomotive;
		throw new LocomotivePersistenceException("Locomotive with bus " + bus
				+ " and address " + address + " not found");
	}

	@Override
	public void addLocomotive(Locomotive locomotive) {

		locomotiveService.addLocomotive(locomotive);

		addressLocomotiveCache.put(new SRCPAddress(locomotive.getBus(),
				locomotive.getAddress(), 0, 0), locomotive);
		idToLocomotive.put(locomotive.getId(), locomotive);
	}

	@Override
	public void deleteLocomotive(Locomotive locomotive) {
		locomotiveService.deleteLocomotive(locomotive);
		locomotive.getLocomotiveGroup().getLocomotives().remove(locomotive);
		locomotive.getLocomotiveType().getLocomotives().remove(locomotive);

		idToLocomotive.remove(locomotive.getId());
		addressLocomotiveCache.values().remove(locomotive);

	}

	@Override
	public void updateLocomotive(Locomotive locomotive) {
		locomotiveService.updateLocomotive(locomotive);

	}

	@Override
	public ArrayListModel<LocomotiveGroup> getAllLocomotiveGroups() {
		return new ArrayListModel<LocomotiveGroup>(idToLocomotiveGroup.values());
	}

	@Override
	public LocomotiveGroup getLocomotiveGroupById(int id) {
		return idToLocomotiveGroup.get(id);
	}

	@Override
	public LocomotiveType getLocomotiveTypeById(int id) {
		return idToLocomotiveType.get(id);
	}

	@Override
	public void addLocomotiveGroup(LocomotiveGroup group) {
		locomotiveService.addLocomotiveGroup(group);
		idToLocomotiveGroup.put(group.getId(), group);

	}

	@Override
	public void deleteLocomotiveGroup(LocomotiveGroup group)
			throws LocomotivePersistenceException {
		if (!group.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive group with associated locomotives");
		}
		locomotiveService.deleteLocomotiveGroup(group);
		idToLocomotiveGroup.remove(group.getId());
	}

	@Override
	public void updateLocomotiveGroup(LocomotiveGroup group) {
		updateLocomotiveGroup(group);
	}

	@Override
	public SortedSet<LocomotiveType> getAllLocomotiveTypes() {
		return new TreeSet<LocomotiveType>(locomotiveTypes.values());
	}

	@Override
	public LocomotiveType getLocomotiveTypeByName(String typeName) {
		for (LocomotiveType type : locomotiveTypes.values()) {
			if (type.getTypeName().equals(typeName))
				return type;
		}
		return null;
	}

	@Override
	public void addLocomotiveType(LocomotiveType type) {
		locomotiveService.addLocomotiveType(type);
		locomotiveTypes.put(type.getTypeName(), type);
		idToLocomotiveType.put(type.getId(), type);
	}

	@Override
	public void deleteLocomotiveType(LocomotiveType type)
			throws LocomotivePersistenceException {
		if (!type.getLocomotives().isEmpty()) {
			throw new LocomotivePersistenceException(
					"Cannot delete locomotive type with associated locomotives");
		}
		locomotiveService.deleteLocomotiveType(type);
		locomotiveTypes.values().remove(type);

	}

	@Override
	@Deprecated
	public void flush() throws LocomotivePersistenceException {
		LOGGER.debug("flush()");
	}

	@Override
	public void reload() {
		for (LocomotiveGroup group : locomotiveService.getAllLocomotiveGroups()) {
			idToLocomotiveGroup.put(group.getId(), group);
		}
		for (LocomotiveType type : locomotiveService.getAllLocomotiveTypes()) {
			idToLocomotiveType.put(type.getId(), type);
		}
		for (Locomotive locomotive : locomotiveService.getAllLocomotives()) {
			idToLocomotive.put(locomotive.getId(), locomotive);

			LocomotiveType type = getLocomotiveTypeById(locomotive
					.getLocomotiveTypeId());
			LocomotiveGroup group = getLocomotiveGroupById(locomotive
					.getLocomotiveGroupId());

			if (type == null) {
				LOGGER.error("locomotive type null of locomotive "
						+ locomotive.getName());
			} else {

				LOGGER.debug("locomotive " + locomotive.getName()
						+ " belongs to group " + group.getName());
				locomotive.setLocomotiveGroup(group);
				group.addLocomotive(locomotive);

			}
			if (group == null) {
				LOGGER.error("locomotive group null of locomotive "
						+ locomotive.getName());
			} else {
				LOGGER.debug("locomotive " + locomotive.getName()
						+ " has type " + type.getTypeName());

				locomotive.setLocomotiveType(type);
				type.addLocomotive(locomotive);

			}

		}
	}
}
