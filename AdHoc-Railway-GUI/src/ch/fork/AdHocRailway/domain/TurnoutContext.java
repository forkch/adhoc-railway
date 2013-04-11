package ch.fork.AdHocRailway.domain;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;

public interface TurnoutContext extends ControllerContext {

	public abstract TurnoutControlIface getTurnoutControl();

	public abstract TurnoutManager getTurnoutManager();

}