package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.manager.TurnoutManagerListener;
import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;

import java.util.SortedSet;

public abstract class TurnoutAddListener implements TurnoutManagerListener {

    @Override
    public abstract void turnoutAdded(Turnout turnout);

    @Override
    public abstract void failure(ManagerException arg0);

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
