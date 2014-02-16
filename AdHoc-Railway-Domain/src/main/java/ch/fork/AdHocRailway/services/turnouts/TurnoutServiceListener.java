package ch.fork.AdHocRailway.services.turnouts;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;

public interface TurnoutServiceListener {

	public void turnoutsUpdated(SortedSet<TurnoutGroup> turnoutGroups);

	public void turnoutAdded(Turnout turnout);

	public void turnoutUpdated(Turnout turnout);

	public void turnoutRemoved(Turnout turnout);

	public void turnoutGroupAdded(TurnoutGroup group);

	public void turnoutGroupUpdated(TurnoutGroup group);

	public void turnoutGroupRemoved(TurnoutGroup group);

	public void failure(TurnoutManagerException arg0);
}
