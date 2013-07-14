package ch.fork.AdHocRailway.controllers.impl.brain;

import java.io.IOException;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;

public class BrainTurnoutControlAdapter extends TurnoutController {

	private final BrainController brain;

	public BrainTurnoutControlAdapter(final BrainController brain) {
		this.brain = brain;
	}

	@Override
	public void toggle(final Turnout turnout) throws TurnoutException {
	}

	@Override
	public void setDefaultState(final Turnout turnout) throws TurnoutException {
		try {
			brain.write("XT " + turnout.getAddress1() + " g");
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setNonDefaultState(final Turnout turnout)
			throws TurnoutException {
		try {
			brain.write("XT " + turnout.getAddress1() + " r");
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setStraight(final Turnout turnout) throws TurnoutException {
		try {
			brain.write("XT " + turnout.getAddress1() + " g");
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedLeft(final Turnout turnout) throws TurnoutException {
		try {
			brain.write("XT " + turnout.getAddress1() + " r");
		} catch (final IOException e) {
			throw new TurnoutException(e);
		}
	}

	@Override
	public void setCurvedRight(final Turnout turnout) throws TurnoutException {
		try {
			brain.write("XT " + turnout.getAddress1() + " r");
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
		// TODO Auto-generated method stub

	}

	@Override
	public void reloadConfiguration() {
		// TODO Auto-generated method stub

	}

	public static void main(final String[] args) throws Exception {
		final Turnout t = new Turnout();
		t.setAddress1(1);

		final BrainController brain2 = BrainController.getInstance();
		final BrainTurnoutControlAdapter brainTurnoutControlAdapter = new BrainTurnoutControlAdapter(
				brain2);
		System.in.read();
		brain2.write("XGO");
		System.in.read();

		brainTurnoutControlAdapter.setStraight(t);
		System.in.read();
		brainTurnoutControlAdapter.setCurvedLeft(t);

	}
}
