package ch.fork.AdHocRailway.domain.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.exception.TurnoutException;

public interface TurnoutControlIface {

	public void update();

	public void toggle(Turnout turnout) throws TurnoutException;

	public void setDefaultState(Turnout turnout) throws TurnoutException;

	public void setNonDefaultState(Turnout turnout) throws TurnoutException;

	public void setStraight(Turnout turnout) throws TurnoutException;

	public void setCurvedLeft(Turnout turnout) throws TurnoutException;

	public void setCurvedRight(Turnout turnout) throws TurnoutException;

	public void addTurnoutChangeListener(Turnout turnout, TurnoutChangeListener listener);

	public void removeSwitchChangeListener(Turnout turnout);

	public void removeAllTurnoutChangeListener();

}