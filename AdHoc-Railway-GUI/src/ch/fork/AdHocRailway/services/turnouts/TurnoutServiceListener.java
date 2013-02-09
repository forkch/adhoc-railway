package ch.fork.AdHocRailway.services.turnouts;

import java.util.List;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

public interface TurnoutServiceListener {

	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups);

	public void turnoutUpdated(Turnout turnout);

	public void turnoutRemoved(Turnout turnout);

	public void turnoutAdded(Turnout turnout);

	public void turnoutGroupAdded(TurnoutGroup group);

	public void turnoutGroupDeleted(TurnoutGroup group);

	public void turnoutGroupUpdated(TurnoutGroup group);

}
