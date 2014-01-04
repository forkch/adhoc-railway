package ch.fork.AdHocRailway.manager.impl.locomotives.events;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import com.google.common.collect.Sets;

import java.util.SortedSet;

public class LocomotivesUpdatedEvent {

	private final SortedSet<LocomotiveGroup> updatedLocomotiveGroups;

	public LocomotivesUpdatedEvent(
			final SortedSet<LocomotiveGroup> updatedLocomotiveGroups) {
		this.updatedLocomotiveGroups = updatedLocomotiveGroups;
	}

	public SortedSet<LocomotiveGroup> getUpdatedLocomotiveGroups() {
		return updatedLocomotiveGroups;
	}

	public SortedSet<Locomotive> getAllLocomotives() {
		final SortedSet<Locomotive> allLocomotives = Sets.newTreeSet();
		for (final LocomotiveGroup locomotiveGroup : updatedLocomotiveGroups) {
			allLocomotives.addAll(locomotiveGroup.getLocomotives());
		}
		return allLocomotives;
	}

}
