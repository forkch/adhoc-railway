package ch.fork.adhocrailway.services;

import ch.fork.adhocrailway.model.locomotives.Locomotive;
import ch.fork.adhocrailway.model.locomotives.LocomotiveGroup;

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
            final AdHocServiceException locomotiveManagerException);
}
