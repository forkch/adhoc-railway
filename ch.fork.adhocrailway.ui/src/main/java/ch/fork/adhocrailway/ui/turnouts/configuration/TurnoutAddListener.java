package ch.fork.adhocrailway.ui.turnouts.configuration;

import ch.fork.adhocrailway.manager.TurnoutManagerListener;
import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;
import ch.fork.adhocrailway.services.AdHocServiceException;

import java.util.SortedSet;

public abstract class TurnoutAddListener implements TurnoutManagerListener {

    @Override
    public abstract void turnoutAdded(Turnout turnout);

    @Override
    public abstract void failure(AdHocServiceException serviceException);

    @Override
    public abstract void turnoutUpdated(Turnout turnout);

    @Override
    public void turnoutsUpdated(final SortedSet<TurnoutGroup> turnoutGroups) {

    }

    @Override
    public void turnoutRemoved(final Turnout turnout) {

    }

    @Override
    public void turnoutGroupAdded(final TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupRemoved(final TurnoutGroup group) {

    }

    @Override
    public void turnoutGroupUpdated(final TurnoutGroup group) {

    }
}
