package ch.fork.AdHocRailway.controllers.impl.srcp;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutException;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.turnouts.*;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class SRCPTurnoutControlAdapter extends TurnoutController implements
		SRCPTurnoutChangeListener {
	private static final Logger LOGGER = Logger
			.getLogger(SRCPTurnoutControlAdapter.class);

	private final Map<Turnout, SRCPTurnout> turnoutsSRCPTurnoutsMap = new HashMap<Turnout, SRCPTurnout>();
	private final Map<SRCPTurnout, Turnout> SRCPTurnoutsTurnoutsMap = new HashMap<SRCPTurnout, Turnout>();

	SRCPTurnoutControl turnoutControl;

	SRCPTurnout sTurnoutTemp;

	Turnout turnoutTemp;

	public SRCPTurnoutControlAdapter() {

		turnoutControl = SRCPTurnoutControl.getInstance();

		turnoutControl.addTurnoutChangeListener(this);
		reloadConfiguration();
	}

	@Override
	public void setCurvedLeft(final Turnout turnout) throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setCurvedLeft(sTurnout);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	@Override
	public void setCurvedRight(final Turnout turnout) throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setCurvedRight(sTurnout);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	@Override
	public void setDefaultState(final Turnout turnout) throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setDefaultState(sTurnout);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	@Override
	public void setStraight(final Turnout turnout) throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setStraight(sTurnout);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	@Override
	public void toggleTest(final Turnout turnout) throws TurnoutException {

		turnoutControl.removeTurnout(sTurnoutTemp);
		if (turnoutTemp == null || !turnoutTemp.equals(turnout)) {
			if (sTurnoutTemp != null) {
				turnoutControl.removeTurnout(sTurnoutTemp);
			}
			turnoutTemp = turnout;
			// just create a temporary SRCPTurnout
			sTurnoutTemp = createSRCPTurnout(turnout);
		} else {
			applyNewSettings(turnout);
			sTurnoutTemp.setInitialized(false);
		}
		turnoutControl.addTurnout(sTurnoutTemp);
		try {
			turnoutControl.toggle(sTurnoutTemp);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	@Override
	public void toggle(final Turnout turnout) throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);

		try {
			turnoutControl.toggle(sTurnout);
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	@Override
	public void reloadConfiguration() {
	}

	@Override
	public void addOrUpdateTurnout(final Turnout turnout) {
		turnoutControl.removeTurnout(getSRCPTurnout(turnout));
		final SRCPTurnout sTurnout = createSRCPTurnout(turnout);

		turnoutsSRCPTurnoutsMap.put(turnout, sTurnout);
		SRCPTurnoutsTurnoutsMap.put(sTurnout, turnout);
		turnoutControl.addTurnout(sTurnout);
	}

	@Override
	public void turnoutChanged(final SRCPTurnout changedTurnout,
			final SRCPTurnoutState newState) {
		informListeners(changedTurnout);
	}

	public void setSession(final SRCPSession session) {
		turnoutControl.setSession(session);
	}

	public SRCPTurnout getSRCPTurnout(final Turnout turnout) {
		return turnoutsSRCPTurnoutsMap.get(turnout);
	}

	private void applyNewSettings(final Turnout turnout) {
		sTurnoutTemp.setBus1(turnout.getBus1());
		sTurnoutTemp.setBus2(turnout.getBus2());
		sTurnoutTemp.setAddress1(turnout.getAddress1());
		sTurnoutTemp.setAddress2(turnout.getAddress2());
		sTurnoutTemp.setAddress1Switched(turnout.isAddress1Switched());
		sTurnoutTemp.setAddress2Switched(turnout.isAddress2Switched());
		setSRCPTurnoutDefaultState(sTurnoutTemp, turnout);
		setSRCPTurnoutType(turnout, sTurnoutTemp);
	}

	private SRCPTurnout createSRCPTurnout(final Turnout turnout) {
		final SRCPTurnout sTurnout = new MMTurnout();
		sTurnout.setBus1(turnout.getBus1());
		sTurnout.setBus2(turnout.getBus2());

		sTurnout.setAddress1(turnout.getAddress1());
		sTurnout.setAddress2(turnout.getAddress2());

		sTurnout.setAddress1Switched(turnout.isAddress1Switched());
		sTurnout.setAddress2Switched(turnout.isAddress2Switched());

		setSRCPTurnoutDefaultState(sTurnout, turnout);

		setSRCPTurnoutType(turnout, sTurnout);
		return sTurnout;
	}

	private void setSRCPTurnoutType(final Turnout turnout,
			final SRCPTurnout sTurnout) {
		switch (turnout.getTurnoutType()) {
		case DEFAULT_LEFT:
		case DEFAULT_RIGHT:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
			break;
		case DOUBLECROSS:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.DOUBLECROSS);
			break;
		case CUTTER:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.CUTTER);
			break;
		case THREEWAY:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.THREEWAY);
			break;
		default:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
		}
	}

	private void setSRCPTurnoutDefaultState(final SRCPTurnout sTurnout,
			final Turnout turnout) {
		switch (turnout.getDefaultState()) {
		case STRAIGHT:
			sTurnout.setDefaultState(SRCPTurnoutState.STRAIGHT);
			break;
		case LEFT:
			sTurnout.setDefaultState(SRCPTurnoutState.LEFT);
			break;
		case RIGHT:
			sTurnout.setDefaultState(SRCPTurnoutState.RIGHT);
			break;
		case UNDEF:
			sTurnout.setDefaultState(SRCPTurnoutState.UNDEF);
			break;
		}
	}

	private void informListeners(final SRCPTurnout changedTurnout) {
		// List<TurnoutChangeListener> ll = listeners.get(changedTurnout);
		// if (ll == null)
		// its a sub-turnout of a threeway turnout
		// return;

		Turnout turnout = SRCPTurnoutsTurnoutsMap.get(changedTurnout);
		if (turnout == null) {
			turnout = turnoutTemp;
		}

		turnout.setActualState(getTurnoutStateFromSRCPTurnoutState(changedTurnout
				.getTurnoutState()));
		informListeners(turnout);
		LOGGER.debug("turnoutChanged(" + changedTurnout + ")");

	}

	private TurnoutState getTurnoutStateFromSRCPTurnoutState(
			final SRCPTurnoutState turnoutState) {
		switch (turnoutState) {
		case LEFT:
			return TurnoutState.LEFT;
		case RIGHT:
			return TurnoutState.RIGHT;
		case STRAIGHT:

			return TurnoutState.STRAIGHT;
		case UNDEF:
		default:
			return TurnoutState.UNDEF;
		}
	}
}
