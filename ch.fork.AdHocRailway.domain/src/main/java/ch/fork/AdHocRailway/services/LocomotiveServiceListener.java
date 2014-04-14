package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;

import java.util.SortedSet;

public interface LocomotiveServiceListener {

    void locomotiveAdded(final Locomotive locomotive);

    void locomotiveUpdated(final Locomotive locomotive);

    void locomotiveRemoved(final Locomotive locomotive);

    void locomotiveGroupAdded(final LocomotiveGroup group);

    void locomotiveGroupUpdated(final LocomotiveGroup group);

    void locomotiveGroupRemoved(final LocomotiveGroup group);

    void locomotivesUpdated(
            final SortedSet<LocomotiveGroup> locomotiveGroups);

    void failure(
            final ManagerException locomotiveManagerException);
}
