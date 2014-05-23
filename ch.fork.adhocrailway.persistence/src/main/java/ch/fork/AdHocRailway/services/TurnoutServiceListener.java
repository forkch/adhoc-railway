package ch.fork.AdHocRailway.services;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;

import java.util.SortedSet;

public interface TurnoutServiceListener {

    public void turnoutsUpdated(SortedSet<TurnoutGroup> turnoutGroups);

    public void turnoutAdded(Turnout turnout);

    public void turnoutUpdated(Turnout turnout);

    public void turnoutRemoved(Turnout turnout);

    public void turnoutGroupAdded(TurnoutGroup group);

    public void turnoutGroupUpdated(TurnoutGroup group);

    public void turnoutGroupRemoved(TurnoutGroup group);

    public void failure(AdHocServiceException arg0);
}
