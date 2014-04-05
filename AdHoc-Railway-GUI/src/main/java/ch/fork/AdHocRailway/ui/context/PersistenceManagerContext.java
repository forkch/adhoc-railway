package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import com.google.common.eventbus.EventBus;

import java.io.File;

public interface PersistenceManagerContext {

    Preferences getPreferences();

    LocomotiveManager getLocomotiveManager();

    void setLocomotiveManager(final LocomotiveManager locomotiveManager);

    EventBus getMainBus();

    TurnoutManager getTurnoutManager();

    void setTurnoutManager(final TurnoutManager turnoutManager);

    RouteManager getRouteManager();

    void setRouteManager(final RouteManager routeManager);

    void setActualFile(File file);
}
