package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.util.List;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManagerListener;

public abstract class LocomotiveAddListener implements
		LocomotiveManagerListener {

	@Override
	public void locomotiveGroupAdded(LocomotiveGroup group) {

	}

	@Override
	public void locomotiveRemoved(Locomotive locomotive) {

	}

	@Override
	public void locomotiveGroupRemoved(LocomotiveGroup group) {

	}

	@Override
	public void locomotiveGroupUpdated(LocomotiveGroup group) {

	}

	@Override
	public void locomotivesUpdated(List<LocomotiveGroup> locomotiveGroups) {

	}

}
