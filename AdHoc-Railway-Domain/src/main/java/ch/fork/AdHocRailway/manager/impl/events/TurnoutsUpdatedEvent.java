package ch.fork.AdHocRailway.manager.impl.events;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import com.google.common.collect.Sets;

import java.util.SortedSet;

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
