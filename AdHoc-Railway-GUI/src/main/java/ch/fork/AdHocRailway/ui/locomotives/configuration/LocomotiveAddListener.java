package ch.fork.AdHocRailway.ui.locomotives.configuration;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManagerListener;

import java.util.SortedSet;

public abstract class LocomotiveAddListener implements
		LocomotiveManagerListener {

	@Override
	public void locomotiveGroupAdded(final LocomotiveGroup group) {

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

}
