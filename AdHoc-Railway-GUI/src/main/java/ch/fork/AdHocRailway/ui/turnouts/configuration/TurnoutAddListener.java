package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerListener;

import java.util.SortedSet;

public abstract class TurnoutAddListener implements TurnoutManagerListener {

    @Override
    public abstract void turnoutAdded(Turnout turnout);

    @Override
    public abstract void failure(TurnoutManagerException arg0);

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
