package ch.fork.AdHocRailway.controllers.impl.brain;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public class BrainTurnoutControlAdapter extends TurnoutController {

	private final BrainController brain;

	public BrainTurnoutControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void toggle(final Turnout turnout){
		switch (turnout.getActualState()) {
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
	public void setDefaultState(final Turnout turnout){
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
	public void setStraight(final Turnout turnout){
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
			turnout.setActualState(TurnoutState.STRAIGHT);
			informListeners(turnout);
		} catch (final BrainException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedLeft(final Turnout turnout){
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
			turnout.setActualState(TurnoutState.LEFT);
			informListeners(turnout);
		} catch (final BrainException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedRight(final Turnout turnout){
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
			turnout.setActualState(TurnoutState.RIGHT);
			informListeners(turnout);
		} catch (final BrainException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void toggleTest(final Turnout turnout){
		// TODO Auto-generated method stub
	}

	@Override
	public void setTurnoutWithAddress(final int address,
			final TurnoutState straight) {
		if (TurnoutState.STRAIGHT == straight) {
			brain.write("XT " + address + " " + "g");
		} else {
			brain.write("XT " + address + " " + "r");
	
		}
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
