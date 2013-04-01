package ch.fork.AdHocRailway.domain.turnouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.SRCPModelException;
import de.dermoba.srcp.model.turnouts.MMTurnout;
import de.dermoba.srcp.model.turnouts.SRCPTurnout;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutChangeListener;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutControl;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutState;
import de.dermoba.srcp.model.turnouts.SRCPTurnoutTypes;

public class SRCPTurnoutControlAdapter implements TurnoutControlIface,
		SRCPTurnoutChangeListener {
	private static Logger logger = Logger
			.getLogger(SRCPTurnoutControlAdapter.class);

	private static SRCPTurnoutControlAdapter instance;

	private final Map<Turnout, SRCPTurnout> turnoutsSRCPTurnoutsMap;
	private final Map<SRCPTurnout, Turnout> SRCPTurnoutsTurnoutsMap;
	private final List<TurnoutChangeListener> listeners;

	SRCPTurnoutControl turnoutControl;

	SRCPTurnout sTurnoutTemp;

	Turnout turnoutTemp;

	private SRCPTurnoutControlAdapter() {

		turnoutsSRCPTurnoutsMap = new HashMap<Turnout, SRCPTurnout>();
		SRCPTurnoutsTurnoutsMap = new HashMap<SRCPTurnout, Turnout>();
		listeners = new ArrayList<TurnoutChangeListener>();

		turnoutControl = SRCPTurnoutControl.getInstance();

		turnoutControl.addTurnoutChangeListener(this);
		reloadConfiguration();
	}

	public static SRCPTurnoutControlAdapter getInstance() {
		if (instance == null) {
			instance = new SRCPTurnoutControlAdapter();
		}
		return instance;
	}

	@Override
	public SRCPTurnoutState getTurnoutState(final Turnout turnout) {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		return sTurnout.getTurnoutState();
	}

	@Override
	public void previousDeviceToDefault() throws TurnoutException {
		try {
			turnoutControl.previousDeviceToDefault();
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

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
	public void setNonDefaultState(final Turnout turnout)
			throws TurnoutException {
		final SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setNonDefaultState(sTurnout);
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
	public void undoLastChange() throws TurnoutException {
		try {
			turnoutControl.undoLastChange();
		} catch (final SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	@Override
	public void update() {
		turnoutTemp = null;
		sTurnoutTemp = null;
		turnoutsSRCPTurnoutsMap.clear();
		SRCPTurnoutsTurnoutsMap.clear();
		turnoutControl.removeTurnoutChangeListener(this);

	}

	@Override
	public void reloadConfiguration() {
		turnoutControl.setInterface6051Connected(Preferences.getInstance()
				.getBooleanValue(PreferencesKeys.INTERFACE_6051));
		turnoutControl.setTurnoutActivationTime(Preferences.getInstance()
				.getIntValue(PreferencesKeys.ACTIVATION_TIME));
	}

	@Override
	public void addOrUpdateTurnout(final Turnout turnout) {
		turnoutControl.removeTurnout(getSRCPTurnout(turnout));
		final SRCPTurnout sTurnout = createSRCPTurnout(turnout);

		turnoutsSRCPTurnoutsMap.put(turnout, sTurnout);
		SRCPTurnoutsTurnoutsMap.put(sTurnout, turnout);
		turnoutControl.addTurnout(sTurnout);
	}

	SRCPTurnout createSRCPTurnout(final Turnout turnout) {
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

	@Override
	public void addTurnoutChangeListener(final Turnout turnout,
			final TurnoutChangeListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	@Override
	public void removeTurnoutChangeListener(final Turnout turnout) {
		// SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		// listeners.remove(sTurnout);
	}

	@Override
	public void removeTurnoutChangeListener(final TurnoutChangeListener listener) {
		// listeners.keySet().remove(listener);
		listeners.remove(listener);
	}

	@Override
	public void turnoutChanged(final SRCPTurnout changedTurnout,
			final SRCPTurnoutState newState) {
		informListeners(changedTurnout);
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
		for (final TurnoutChangeListener scl : listeners) {
			scl.turnoutChanged(turnout, changedTurnout.getTurnoutState());
		}
		logger.debug("turnoutChanged(" + changedTurnout + ")");

	}

	public void setSession(final SRCPSession session) {
		turnoutControl.setSession(session);
	}

	public SRCPTurnout getSRCPTurnout(final Turnout turnout) {
		return turnoutsSRCPTurnoutsMap.get(turnout);
	}
}
