package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;

import com.google.common.eventbus.EventBus;

import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;

public interface RailwayDeviceManagerContext {

	EventBus getMainBus();

	Preferences getPreferences();

	void setPowerController(final PowerController powerControl);

	void setLocomotiveControl(final LocomotiveController locomotiveControl);

	void setTurnoutControl(final TurnoutController turnoutControl);

	void setLockControl(final SRCPLockControl instance);

	void setRouteControl(final RouteController routeControl);

	void setSession(final SRCPSession session);

	TurnoutController getTurnoutControl();

	TurnoutManager getTurnoutManager();

	SRCPSession getSession();

	PowerController getPowerControl();

	RouteController getRouteControl();

	SRCPLockControl getLockControl();

	LocomotiveController getLocomotiveControl();

}
