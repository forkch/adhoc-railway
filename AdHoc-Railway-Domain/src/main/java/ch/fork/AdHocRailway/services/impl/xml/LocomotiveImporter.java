package ch.fork.AdHocRailway.services.impl.xml;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import java.util.SortedSet;

public class LocomotiveImporter {

	public void importLocomotives(
			final LocomotiveManager locomotivePersistence,
			final SortedSet<LocomotiveGroup> groups) {

		locomotivePersistence.clearToService();

		for (final LocomotiveGroup group : groups) {
			locomotivePersistence.addLocomotiveGroup(group);

		}
		for (final LocomotiveGroup group : groups) {
			for (final Locomotive locomotive : group.getLocomotives()) {
				locomotivePersistence.addLocomotiveToGroup(locomotive, group);
			}
		}
	}
}
