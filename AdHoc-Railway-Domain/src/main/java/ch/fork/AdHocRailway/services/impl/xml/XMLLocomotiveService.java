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

package ch.fork.AdHocRailway.services.impl.xml;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveService;
import ch.fork.AdHocRailway.services.locomotives.LocomotiveServiceListener;
import org.apache.log4j.Logger;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;

public class XMLLocomotiveService implements LocomotiveService {

	private static final Logger logger = Logger
			.getLogger(LocomotiveManager.class);

	private final SortedSet<Locomotive> locomotives = new TreeSet<Locomotive>();

	private final SortedSet<LocomotiveGroup> locomotiveGroups = new TreeSet<LocomotiveGroup>();

	private LocomotiveServiceListener listener;

	public XMLLocomotiveService() {
		logger.info("FileLocomotivePersistence loaded");
	}

	@Override
	public void clear() {
		locomotives.clear();
		locomotiveGroups.clear();
	}

	@Override
	public void addLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		locomotives.add(locomotive);
		locomotive.setId(UUID.randomUUID().hashCode());
		listener.locomotiveAdded(locomotive);
	}

	@Override
	public void removeLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		locomotives.remove(locomotive);
		listener.locomotiveRemoved(locomotive);

	}

	@Override
	public void updateLocomotive(final Locomotive locomotive)
			throws LocomotiveManagerException {
		locomotives.remove(locomotive);
		locomotives.add(locomotive);

		listener.locomotiveUpdated(locomotive);
	}

	@Override
	public SortedSet<LocomotiveGroup> getAllLocomotiveGroups()
			throws LocomotiveManagerException {
		return locomotiveGroups;
	}

	@Override
	public void addLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		locomotiveGroups.add(group);
		group.setId(UUID.randomUUID().hashCode());
		listener.locomotiveGroupAdded(group);
	}

	@Override
	public void removeLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		locomotiveGroups.remove(group);
		listener.locomotiveGroupRemoved(group);
	}

	@Override
	public void updateLocomotiveGroup(final LocomotiveGroup group)
			throws LocomotiveManagerException {
		locomotiveGroups.remove(group);
		locomotiveGroups.add(group);
		listener.locomotiveGroupUpdated(group);
	}

	@Override
	public void init(final LocomotiveServiceListener listener) {
		this.listener = listener;

	}

	@Override
	public void disconnect() {

	}

	public void loadLocomotiveGroupsFromXML(
			final SortedSet<LocomotiveGroup> groups) {
		locomotiveGroups.clear();
		locomotives.clear();
		if (groups != null) {
			for (final LocomotiveGroup locomotiveGroup : groups) {
				locomotiveGroup.init();
				locomotiveGroup.setId(UUID.randomUUID().hashCode());
				locomotiveGroups.add(locomotiveGroup);
				for (final Locomotive locomotive : locomotiveGroup.getLocomotives()) {
					locomotive.init();
					locomotive.setGroup(locomotiveGroup);
					locomotive.setId(UUID.randomUUID().hashCode());
					locomotives.add(locomotive);
				}
			}
		}
		listener.locomotivesUpdated(locomotiveGroups);
	}
}
