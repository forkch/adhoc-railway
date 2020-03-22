package ch.fork.adhocrailway.services;

import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;

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
