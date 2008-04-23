package ch.fork.AdHocRailway.domain.turnouts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.Constants;
import de.dermoba.srcp.client.SRCPSession;

public class SRCPTurnoutControlAdapter implements TurnoutControlIface,
		SRCPTurnoutChangeListener {
	private static Logger									logger	= Logger
																			.getLogger(SRCPTurnoutControlAdapter.class);

	private static SRCPTurnoutControlAdapter				instance;

	private TurnoutPersistenceIface							persistence;
	private Map<Turnout, SRCPTurnout>						turnoutsSRCPTurnoutsMap;
	private Map<SRCPTurnout, Turnout>						SRCPTurnoutsTurnoutsMap;
	private Map<SRCPTurnout, List<TurnoutChangeListener>>	listeners;

	private SRCPTurnoutControl								turnoutControl;

	private SRCPTurnoutControlAdapter() {
		turnoutControl = SRCPTurnoutControl.getInstance();
		turnoutsSRCPTurnoutsMap = new HashMap<Turnout, SRCPTurnout>();
		SRCPTurnoutsTurnoutsMap = new HashMap<SRCPTurnout, Turnout>();
		listeners = new HashMap<SRCPTurnout, List<TurnoutChangeListener>>();
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
		turnoutControl.previousDeviceToDefault();

	}

	public void refresh(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.refresh(sTurnout);

	}

	public void setCurvedLeft(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.setCurvedLeft(sTurnout);

	}

	public void setCurvedRight(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.setCurvedRight(sTurnout);

	}

	public void setDefaultState(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.setDefaultState(sTurnout);

	}

	public void setNonDefaultState(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.setNonDefaultState(sTurnout);

	}

	public void setStraight(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.setStraight(sTurnout);

	}

	public void toggle(Turnout turnout) throws TurnoutException {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		turnoutControl.toggle(sTurnout);

	}

	public void undoLastChange() throws TurnoutException {
		turnoutControl.undoLastChange();

	}

	public void update() {
		turnoutsSRCPTurnoutsMap.clear();
		turnoutControl.removeTurnoutChangeListener(this);
		for (Turnout turnout : persistence.getAllTurnouts()) {
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

			turnoutsSRCPTurnoutsMap.put(turnout, sTurnout);
			SRCPTurnoutsTurnoutsMap.put(sTurnout, turnout);

		}
		turnoutControl.addTurnoutChangeListener(this);

		turnoutControl.update(SRCPTurnoutsTurnoutsMap.keySet());

	}

	public void addTurnoutChangeListener(Turnout turnout,
			TurnoutChangeListener listener) {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		if (listeners.get(sTurnout) == null) {
			listeners.put(sTurnout, new ArrayList<TurnoutChangeListener>());
		}
		listeners.get(sTurnout).add(listener);
	}

	public void removeAllTurnoutChangeListener() {
		listeners.clear();
	}

	public void removeTurnoutChangeListener(Turnout turnout) {
		SRCPTurnout sTurnout = turnoutsSRCPTurnoutsMap.get(turnout);
		listeners.remove(sTurnout);
	}

	public void removeTurnoutChangeListener(TurnoutChangeListener listener) {
		listeners.keySet().remove(listener);
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
		List<TurnoutChangeListener> ll = listeners.get(changedTurnout);
		if (ll == null)
			// its a sub-turnout of a threeway turnout
			return;

		Turnout turnout = SRCPTurnoutsTurnoutsMap.get(changedTurnout);
		for (TurnoutChangeListener scl : ll)
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
