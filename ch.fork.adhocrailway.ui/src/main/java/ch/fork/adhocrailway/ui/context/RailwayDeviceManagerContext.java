package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.LocomotiveController;
import ch.fork.adhocrailway.controllers.PowerController;
import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.manager.LocomotiveManager;
import ch.fork.adhocrailway.manager.RouteManager;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.technical.configuration.Preferences;
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
