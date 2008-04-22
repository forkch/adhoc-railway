package ch.fork.AdHocRailway.domain.turnouts;

public interface SRCPTurnoutChangeListener {

	public void turnoutChanged(SRCPTurnout changedTurnout, SRCPTurnoutState newState);

}
