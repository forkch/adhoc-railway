package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import com.google.common.eventbus.EventBus;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;

public interface RailwayDeviceManagerContext {

    EventBus getMainBus();

    Preferences getPreferences();

    void setPowerController(final PowerController powerControl);

    TurnoutController getTurnoutControl();

    void setTurnoutControl(final TurnoutController turnoutControl);

    TurnoutManager getTurnoutManager();

    SRCPSession getSession();

    void setSession(final SRCPSession session);

    PowerController getPowerControl();

    RouteController getRouteControl();

    void setRouteControl(final RouteController routeControl);

    SRCPLockControl getLockControl();

    void setLockControl(final SRCPLockControl instance);

    LocomotiveController getLocomotiveControl();

    void setLocomotiveControl(final LocomotiveController locomotiveControl);

    RouteManager getRouteManager();

    LocomotiveManager getLocomotiveManager();

}
