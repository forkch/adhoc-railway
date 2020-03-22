package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.controllers.RouteController;
import ch.fork.adhocrailway.manager.LocomotiveManager;
import ch.fork.adhocrailway.manager.RouteManager;
import ch.fork.adhocrailway.manager.TurnoutManager;
import ch.fork.adhocrailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.adhocrailway.technical.configuration.Preferences;
import com.google.common.eventbus.EventBus;

import java.io.File;

public interface PersistenceManagerContext {

    String getAppUUID();

    Preferences getPreferences();

    LocomotiveManager getLocomotiveManager();

    void setLocomotiveManager(final LocomotiveManager locomotiveManager);

    EventBus getMainBus();

    TurnoutManager getTurnoutManager();

    void setTurnoutManager(final TurnoutManager turnoutManager);

    RouteManager getRouteManager();

    void setRouteManager(final RouteManager routeManager);

    void setActualFile(File file);

    public RouteController getRouteControl();

    void setSIOService(SIOService sioService);

    SIOService getSioService();
}
