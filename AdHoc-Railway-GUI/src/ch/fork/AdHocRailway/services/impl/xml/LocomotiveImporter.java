package ch.fork.AdHocRailway.services.impl.xml;

import java.util.List;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerListener;

public class LocomotiveImporter implements LocomotiveManagerListener {

	private int actualGroup = 0;
	private int actualLocomotiveInGroup = 0;
	private LocomotiveManager locomotivePersistence;
	private List<LocomotiveGroup> groups;
	private LocomotiveGroup actualSourceLocomotiveGroup;
	private LocomotiveGroup actualTargetLocomotiveGroup;

	public void importLocomotives(
			final LocomotiveManager locomotivePersistence,
			final List<LocomotiveGroup> groups) {

		this.locomotivePersistence = locomotivePersistence;
		this.groups = groups;
		locomotivePersistence.clear(true);
		locomotivePersistence.addLocomotiveManagerListener(this);

		locomotivePersistence.addLocomotiveGroup(groups.get(0));
	}

	@Override
	public void locomotiveAdded(final Locomotive locomotive) {
		actualLocomotiveInGroup++;

		if (actualLocomotiveInGroup < actualSourceLocomotiveGroup
				.getLocomotives().size()) {
			locomotivePersistence.addLocomotiveToGroup(
					actualSourceLocomotiveGroup.getLocomotives().get(
							actualLocomotiveInGroup),
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
		// TODO Auto-generated method stub

	}

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {

		actualSourceLocomotiveGroup = groups.get(actualGroup);
		actualTargetLocomotiveGroup = group;

		actualLocomotiveInGroup = 0;
		if (!actualSourceLocomotiveGroup.getLocomotives().isEmpty()) {
			locomotivePersistence.addLocomotiveToGroup(
					actualSourceLocomotiveGroup.getLocomotives().get(
							actualLocomotiveInGroup),
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
		// TODO Auto-generated method stub

	}

	@Override
	public void locomotiveGroupRemoved(final LocomotiveGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void locomotiveGroupUpdated(final LocomotiveGroup group) {
		// TODO Auto-generated method stub

	}

	@Override
	public void locomotivesUpdated(final List<LocomotiveGroup> locomotiveGroups) {
		// TODO Auto-generated method stub

	}

	@Override
	public void failure(
			final LocomotiveManagerException locomotiveManagerException) {
		// TODO Auto-generated method stub

	}
}
