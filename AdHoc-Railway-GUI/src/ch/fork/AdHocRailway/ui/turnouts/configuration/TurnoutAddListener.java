package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.util.List;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerListener;

public abstract class TurnoutAddListener implements TurnoutManagerListener {

	@Override
	public abstract void turnoutAdded(Turnout turnout);

	@Override
	public abstract void failure(TurnoutManagerException arg0);

	@Override
	public abstract void turnoutUpdated(Turnout turnout);

	@Override
	public void turnoutsUpdated(List<TurnoutGroup> turnoutGroups) {

	}

	@Override
	public void turnoutRemoved(Turnout turnout) {

	}

	@Override
	public void turnoutGroupAdded(TurnoutGroup group) {

	}

	@Override
	public void turnoutGroupRemoved(TurnoutGroup group) {

	}

	@Override
	public void turnoutGroupUpdated(TurnoutGroup group) {

	}

}
