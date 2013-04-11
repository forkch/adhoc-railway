package ch.fork.AdHocRailway.ui.context;

import de.dermoba.srcp.model.power.SRCPPowerControl;

public interface PowerContext extends ControllerContext {
	public SRCPPowerControl getPowerControl();
}
