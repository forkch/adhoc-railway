package ch.fork.AdHocRailway.services.impl.xml;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerListener;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

public class LocomotiveImporter implements LocomotiveManagerListener {

	private int actualGroup = 0;
	private int actualLocomotiveInGroup = 0;
	private LocomotiveManager locomotivePersistence;
	private List<LocomotiveGroup> groups;
	private LocomotiveGroup actualSourceLocomotiveGroup;
	private LocomotiveGroup actualTargetLocomotiveGroup;

	public void importLocomotives(
			final LocomotiveManager locomotivePersistence,
			final SortedSet<LocomotiveGroup> groups) {

		this.locomotivePersistence = locomotivePersistence;
		this.groups = new ArrayList<LocomotiveGroup>(groups);
		locomotivePersistence.clearToService();
		locomotivePersistence.addLocomotiveManagerListener(this);

		locomotivePersistence.addLocomotiveGroup(this.groups.get(0));
	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
		actualLocomotiveInGroup++;

		if (actualLocomotiveInGroup < actualSourceLocomotiveGroup
				.getLocomotives().size()) {
			locomotivePersistence.addLocomotiveToGroup(
					new ArrayList<Locomotive>(actualSourceLocomotiveGroup
							.getLocomotives()).get(actualLocomotiveInGroup),
					actualTargetLocomotiveGroup);
		} else {
			actualGroup++;
			if (actualGroup < groups.size()) {
				locomotivePersistence.addLocomotiveGroup(groups
						.get(actualGroup));
			}
		}
	}

	@Override
	public void locomotiveUpdated(final Locomotive locomotive) {

	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {

		actualSourceLocomotiveGroup = groups.get(actualGroup);
		actualTargetLocomotiveGroup = group;

		actualLocomotiveInGroup = 0;
		if (!actualSourceLocomotiveGroup.getLocomotives().isEmpty()) {
			locomotivePersistence.addLocomotiveToGroup(
					new ArrayList<Locomotive>(actualSourceLocomotiveGroup
							.getLocomotives()).get(actualLocomotiveInGroup),
					actualTargetLocomotiveGroup);
		} else {
			actualGroup++;
			if (actualGroup < groups.size()) {
				locomotivePersistence.addLocomotiveGroup(groups
						.get(actualGroup));
			}
		}
	}

	@Override
	public void locomotiveRemoved(final Locomotive locomotive) {

	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {

	}

	@Override
	public void locomotivesUpdated(
			final SortedSet<LocomotiveGroup> locomotiveGroups) {

	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {
	}
}
