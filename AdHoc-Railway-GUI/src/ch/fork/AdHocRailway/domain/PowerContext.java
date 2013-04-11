package ch.fork.AdHocRailway.domain;

import de.dermoba.srcp.model.power.SRCPPowerControl;

public interface PowerContext extends ControllerContext {
	public SRCPPowerControl getPowerControl();
}
