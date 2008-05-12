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
	private static Logger						logger	= Logger
																.getLogger(SRCPTurnoutControlAdapter.class);

	private static SRCPTurnoutControlAdapter	instance;

	private TurnoutPersistenceIface				persistence;
	private Map<Turnout, SRCPTurnout>			turnoutsSRCPTurnoutsMap;
	private Map<SRCPTurnout, Turnout>			SRCPTurnoutsTurnoutsMap;
	// private Map<SRCPTurnout, List<TurnoutChangeListener>> listeners;
	private List<TurnoutChangeListener>			listeners;

	SRCPTurnoutControl							turnoutControl;

	SRCPTurnout									sTurnoutTemp;

	Turnout										turnoutTemp;

	private SRCPTurnoutControlAdapter() {
		turnoutControl = SRCPTurnoutControl.getInstance();
		turnoutsSRCPTurnoutsMap = new HashMap<Turnout, SRCPTurnout>();
		SRCPTurnoutsTurnoutsMap = new HashMap<SRCPTurnout, Turnout>();
		// listeners = new HashMap<SRCPTurnout, List<TurnoutChangeListener>>();
		listeners = new ArrayList<TurnoutChangeListener>();
	}

	public static SRCPTurnoutControlAdapter getInstance() {
		if (instance == null)
			instance = new SRCPTurnoutControlAdapter();
		return instance;
	}

	public SRCPTurnoutState getTurnoutState(Turnout turnout) {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		return sTurnout.getTurnoutState();
	}

	public void previousDeviceToDefault() throws TurnoutException {
		try {
			turnoutControl.previousDeviceToDefault();
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void setCurvedLeft(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setCurvedLeft(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	public void setCurvedRight(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setCurvedRight(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void setDefaultState(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setDefaultState(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void setNonDefaultState(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setNonDefaultState(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void setStraight(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		try {
			turnoutControl.setStraight(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void toggleTest(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = null;
		if (sTurnoutTemp != null) {
			turnoutControl.removeTurnout(sTurnoutTemp);
		}
		System.out.println(turnoutTemp);
		if (turnoutTemp != null)
			System.out.println(turnoutTemp.equals(turnout));
		if (turnoutTemp == null || !turnoutTemp.equals(turnout)) {

			System.out.println("NEEW");
			// just create a temporary SRCPTurnout
			sTurnout = createSRCPTurnout(turnout);
			sTurnoutTemp = sTurnout;
			turnoutTemp = (Turnout) turnout.clone();
		} else {
			sTurnout = sTurnoutTemp;
		}
		turnoutControl.addTurnout(sTurnout);
		System.out.println(turnout);
		try {
			turnoutControl.toggle(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	public void toggle(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);

		try {
			turnoutControl.toggle(sTurnout);
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}

	}

	public void undoLastChange() throws TurnoutException {
		try {
			turnoutControl.undoLastChange();
		} catch (SRCPModelException e) {
			throw new TurnoutException("Turnout Error", e);
		}
	}

	public void update() {
		turnoutTemp = null;
		sTurnoutTemp = null;
		turnoutsSRCPTurnoutsMap.clear();
		SRCPTurnoutsTurnoutsMap.clear();
		turnoutControl.removeTurnoutChangeListener(this);
		turnoutControl.setInterface6051Connected(Preferences.getInstance()
				.getBooleanValue(PreferencesKeys.INTERFACE_6051));
		turnoutControl.setActivationTime(Preferences.getInstance().getIntValue(
				PreferencesKeys.ACTIVATION_TIME));
		for (Turnout turnout : persistence.getAllTurnouts()) {
			SRCPTurnout sTurnout = createSRCPTurnout(turnout);

			turnoutsSRCPTurnoutsMap.put(turnout, sTurnout);
			SRCPTurnoutsTurnoutsMap.put(sTurnout, turnout);

		}
		turnoutControl.addTurnoutChangeListener(this);

		turnoutControl.update(SRCPTurnoutsTurnoutsMap.keySet());
		// turnoutControl.setSession(session)
	}

	SRCPTurnout createSRCPTurnout(Turnout turnout) {
		SRCPTurnout sTurnout = new MMTurnout();
		sTurnout.setBus1(turnout.getBus1());
		sTurnout.setBus2(turnout.getBus2());

		sTurnout.setAddress1(turnout.getAddress1());
		sTurnout.setAddress2(turnout.getAddress2());

		sTurnout.setAddress1Switched(turnout.isAddress1Switched());
		sTurnout.setAddress2Switched(turnout.isAddress2Switched());

		switch (turnout.getDefaultStateEnum()) {
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

		switch (turnout.getTurnoutType().getTurnoutTypeEnum()) {
		case DEFAULT:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.DEFAULT);
			break;
		case DOUBLECROSS:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.DOUBLECROSS);
			break;
		case THREEWAY:
			sTurnout.setTurnoutType(SRCPTurnoutTypes.THREEWAY);
			break;
		}
		return sTurnout;
	}

	public void addTurnoutChangeListener(Turnout turnout,
			TurnoutChangeListener listener) {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		// if (listeners.get(sTurnout) == null) {
		// listeners.put(sTurnout, new ArrayList<TurnoutChangeListener>());
		// }
		// listeners.get(sTurnout).add(listener);
		listeners.add(listener);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	public void removeTurnoutChangeListener(Turnout turnout) {
		// SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		// listeners.remove(sTurnout);
	}

	public void removeTurnoutChangeListener(TurnoutChangeListener listener) {
		// listeners.keySet().remove(listener);
		listeners.remove(listener);
	}

	public TurnoutPersistenceIface getPersistence() {
		return persistence;
	}

	public void setPersistence(TurnoutPersistenceIface persistence) {
		this.persistence = persistence;
	}

	public void turnoutChanged(SRCPTurnout changedTurnout,
			SRCPTurnoutState newState) {
		informListeners(changedTurnout);
	}

	private void informListeners(SRCPTurnout changedTurnout) {
		// List<TurnoutChangeListener> ll = listeners.get(changedTurnout);
		// if (ll == null)
		// its a sub-turnout of a threeway turnout
		// return;

		Turnout turnout = SRCPTurnoutsTurnoutsMap.get(changedTurnout);
		if (turnout == null)
			turnout = turnoutTemp;
		for (TurnoutChangeListener scl : listeners)
			scl.turnoutChanged(turnout, changedTurnout.getTurnoutState());
		logger.debug("turnoutChanged(" + changedTurnout + ")");

	}

	public void setSession(SRCPSession session) {
		turnoutControl.setSession(session);
	}

	public SRCPTurnout getSRCPTurnout(Turnout turnout) {
		return turnoutsSRCPTurnoutsMap.get(turnout);
	}
}
