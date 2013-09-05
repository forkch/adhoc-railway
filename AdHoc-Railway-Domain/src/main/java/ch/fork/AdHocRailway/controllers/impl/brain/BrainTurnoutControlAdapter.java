package ch.fork.AdHocRailway.controllers.impl.brain;

import java.io.IOException;
import java.util.Map;
import com.google.common.collect.Maps;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public class BrainTurnoutControlAdapter extends TurnoutController {

	private final BrainController brain;
	private final Map<Turnout, TurnoutState> turnoutStates = Maps.newHashMap();

	public BrainTurnoutControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void toggle(final Turnout turnout) throws TurnoutException {
		if (!turnoutStates.containsKey(turnout)) {
			return;
		}
		switch (turnoutStates.get(turnout)) {
		case LEFT:
			if (turnout.isThreeWay()) {
				setCurvedRight(turnout);
			} else {
				setStraight(turnout);
			}
			break;
		case RIGHT:
			setStraight(turnout);
			break;
		case STRAIGHT:
			setCurvedLeft(turnout);
			break;
		case UNDEF:
			setStraight(turnout);
			break;
		default:
			break;
		}
	}

	@Override
	public void setDefaultState(final Turnout turnout) throws TurnoutException {
		if (!turnoutStates.containsKey(turnout)) {
			return;
		}
		final TurnoutState defaultState = turnout.getDefaultState();
		switch (defaultState) {
		case LEFT:
			setCurvedLeft(turnout);
			break;
		case RIGHT:
			setCurvedRight(turnout);
			break;
		case STRAIGHT:
			setStraight(turnout);
			break;
		case UNDEF:
			throw new IllegalArgumentException(
					"UNDEF is not a valid default state");
		}
	}

	@Override
	public void setStraight(final Turnout turnout) throws TurnoutException {
		if (!turnoutStates.containsKey(turnout)) {
			return;
		}
		try {
			if (turnout.isThreeWay()) {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getGreenPort(turnout.isAddress1Switched()));
				brain.write("XT " + turnout.getAddress2() + " "
						+ getGreenPort(turnout.isAddress2Switched()));
			} else {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getGreenPort(turnout.isAddress1Switched()));
			}
			turnoutStates.put(turnout, TurnoutState.STRAIGHT);
			informListeners(turnout, TurnoutState.STRAIGHT);
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedLeft(final Turnout turnout) throws TurnoutException {
		if (!turnoutStates.containsKey(turnout)) {
			return;
		}
		try {
			if (turnout.isThreeWay()) {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getRedPort(turnout.isAddress1Switched()));
				brain.write("XT " + turnout.getAddress2() + " "
						+ getGreenPort(turnout.isAddress2Switched()));
			} else {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getRedPort(turnout.isAddress1Switched()));
			}
			turnoutStates.put(turnout, TurnoutState.LEFT);
			informListeners(turnout, TurnoutState.LEFT);
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedRight(final Turnout turnout) throws TurnoutException {
		if (!turnoutStates.containsKey(turnout)) {
			return;
		}
		try {
			if (turnout.isThreeWay()) {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getGreenPort(turnout.isAddress1Switched()));
				brain.write("XT " + turnout.getAddress2() + " "
						+ getRedPort(turnout.isAddress2Switched()));
			} else {
				brain.write("XT " + turnout.getAddress1() + " "
						+ getRedPort(turnout.isAddress1Switched()));
			}
			turnoutStates.put(turnout, TurnoutState.RIGHT);
			informListeners(turnout, TurnoutState.RIGHT);
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void toggleTest(final Turnout turnout) throws TurnoutException {
		// TODO Auto-generated method stub
	}

	@Override
	public void addOrUpdateTurnout(final Turnout turnout) {
		turnoutStates.remove(turnout);
		turnoutStates.put(turnout, TurnoutState.UNDEF);
	}

	@Override
	public void reloadConfiguration() {

	}

	private String getGreenPort(final boolean inverted) {
		return inverted ? "r" : "g";
	}

	private String getRedPort(final boolean inverted) {
		return getGreenPort(!inverted);
	}

}
