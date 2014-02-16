package ch.fork.AdHocRailway.manager.impl.turnouts.events;

import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

import com.google.common.collect.Sets;

public class TurnoutsUpdatedEvent {

	private final SortedSet<TurnoutGroup> updatedTurnouts;

	public TurnoutsUpdatedEvent(final SortedSet<TurnoutGroup> updatedTurnouts) {
		this.updatedTurnouts = updatedTurnouts;
	}

	public SortedSet<Turnout> getAllTurnouts() {
		final SortedSet<Turnout> allTurnouts = Sets.newTreeSet();
		for (final TurnoutGroup turnoutGroup : updatedTurnouts) {
			allTurnouts.addAll(turnoutGroup.getTurnouts());
		}
		return allTurnouts;
	}

}
