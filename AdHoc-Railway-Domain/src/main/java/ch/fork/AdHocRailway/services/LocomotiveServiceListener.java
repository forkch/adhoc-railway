package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.manager.ManagerException;

import java.util.SortedSet;

public interface LocomotiveServiceListener {

    public void locomotiveAdded(final Locomotive locomotive);

    public void locomotiveUpdated(final Locomotive locomotive);

    public void locomotiveRemoved(final Locomotive locomotive);

    public void locomotiveGroupAdded(final LocomotiveGroup group);

    public void locomotiveGroupUpdated(final LocomotiveGroup group);

    public void locomotiveGroupRemoved(final LocomotiveGroup group);

    public void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> locomotiveGroups);

    public void failure(
            final ManagerException locomotiveManagerException);
}
