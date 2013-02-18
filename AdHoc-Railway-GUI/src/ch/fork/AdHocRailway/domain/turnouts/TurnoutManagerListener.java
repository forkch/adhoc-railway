package ch.fork.AdHocRailway.domain.turnouts;

import java.util.List;

public interface TurnoutManagerListener {
	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups);

	public void turnoutUpdated(Turnout turnout);

	public void turnoutRemoved(Turnout turnout);

	public void turnoutAdded(Turnout turnout);

	public void turnoutGroupAdded(TurnoutGroup group);

	public void turnoutGroupRemoved(TurnoutGroup group);

	public void turnoutGroupUpdated(TurnoutGroup group);

	public void failure(TurnoutManagerException arg0);
}
