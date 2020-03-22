package ch.fork.adhocrailway.ui.locomotives.configuration;

import ch.fork.adhocrailway.manager.LocomotiveManagerListener;
import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;

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
