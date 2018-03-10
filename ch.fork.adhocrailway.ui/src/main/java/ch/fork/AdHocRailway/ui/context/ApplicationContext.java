package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.RailwayDeviceManager;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;

import javax.swing.*;
import java.io.File;
import java.util.List;
import java.util.UUID;

public class ApplicationContext implements TurnoutContext, RouteContext,
        LocomotiveContext, TrackContext, PowerContext,
        PersistenceManagerContext, RailwayDeviceManagerContext {

    public static final String APP_UUID = UUID.randomUUID().toString();

    private final EventBus mainBus = new EventBus();

    private JFrame mainFrame;

    private AdHocRailwayIface mainApp;

    private Preferences preferences;

    private TurnoutController turnoutControl;
    private TurnoutManager turnoutManager;

    private RouteController routeControl;
    private RouteManager routeManager;

    private LocomotiveController locomotiveControl;
    private LocomotiveManager locomotiveManager;

    private PowerController powerControl;
    private int activeBoosterCount;
    private SRCPLockControl lockControl;

    private SRCPSession session;

    private boolean isEditingMode;
    private RailwayDeviceManager railwayDeviceManager;
    private File actualFile;
    private File previousLocodir;
    private SIOService sioService;

    @Override
    public Preferences getPreferences() {
        return preferences;
    }

    public void setPreferences(final Preferences preferences) {
        this.preferences = preferences;
    }

    @Override
    public TurnoutController getTurnoutControl() {
        return turnoutControl;
    }

    @Override
    public void setTurnoutControl(final TurnoutController turnoutControl) {
        this.turnoutControl = turnoutControl;
    }

    @Override
    public TurnoutManager getTurnoutManager() {
        return turnoutManager;
    }

    @Override
    public List<Route> getAllRoutes() {
        return routeManager.getAllRoutes();
    }

    @Override
    public void setTurnoutManager(final TurnoutManager turnoutPersistence) {
        this.turnoutManager = turnoutPersistence;
    }

    @Override
    public RouteController getRouteControl() {
        return routeControl;
    }

    @Override
    public Route getRouteForNumber(int routeNumber) {
        return routeManager.getRouteByNumber(routeNumber);
    }

    @Override
    public void setRouteControl(final RouteController routeControl) {
        this.routeControl = routeControl;
    }

    @Override
    public RouteManager getRouteManager() {
        return routeManager;
    }

    @Override
    public void setRouteManager(final RouteManager routePersistence) {
        this.routeManager = routePersistence;
    }

    public File getActualFile() {
        return actualFile;
    }

    @Override
    public void setActualFile(File file) {
        this.actualFile = file;
    }

    @Override
    public LocomotiveController getLocomotiveControl() {
        return locomotiveControl;
    }

    @Override
    public void setLocomotiveControl(
            final LocomotiveController locomotiveControl) {
        this.locomotiveControl = locomotiveControl;
    }

    @Override
    public LocomotiveManager getLocomotiveManager() {
        return locomotiveManager;
    }

    @Override
    public void setLocomotiveManager(
            final LocomotiveManager locomotivePersistence) {
        this.locomotiveManager = locomotivePersistence;
    }

    @Override
    public File getPreviousLocoDir() {
        return previousLocodir;
    }

    @Override
    public void setPreviousLocoDir(File previousLocodir) {

        this.previousLocodir = previousLocodir;
    }

    @Override
    public PowerController getPowerControl() {
        return powerControl;
    }

    @Override
    public void setPowerController(final PowerController powerControl) {
        this.powerControl = powerControl;
    }

    public int getActiveBoosterCount() {
        return this.activeBoosterCount;
    }

    @Override
    public void setActiveBoosterCount(final int activeBoosterCount) {
        this.activeBoosterCount = activeBoosterCount;
    }

    @Override
    public SRCPLockControl getLockControl() {
        return lockControl;
    }

    @Override
    public void setLockControl(final SRCPLockControl lockControl) {
        this.lockControl = lockControl;
    }

    @Override
    public SRCPSession getSession() {
        return session;
    }

    @Override
    public void setSession(final SRCPSession session) {
        this.session = session;
    }

    @Override
    public boolean isEditingMode() {
        return isEditingMode;
    }

    @Subscribe
    public void editingModeChanged(final EditingModeEvent event) {
        this.isEditingMode = event.isEditingMode();
    }

    @Override
    public JFrame getMainFrame() {
        return mainFrame;
    }

    public void setMainFrame(final JFrame mainFrame) {
        this.mainFrame = mainFrame;
    }

    @Override
    public AdHocRailwayIface getMainApp() {
        return mainApp;
    }

    public void setMainApp(final AdHocRailwayIface mainApp) {
        this.mainApp = mainApp;
    }

    @Override
    public RailwayDeviceManager getRailwayDeviceManager() {
        return railwayDeviceManager;
    }

    public void setRailwayDeviceManager(
            final RailwayDeviceManager railwayDeviceManager) {
        this.railwayDeviceManager = railwayDeviceManager;
    }

    @Override
    public EventBus getMainBus() {
        return mainBus;
    }

    @Override
    public String getAppUUID() {
        return APP_UUID;
    }

    @Override
    public void setSIOService(SIOService sioService) {

        this.sioService = sioService;
    }

    @Override
    public SIOService getSioService() {
        return sioService;
    }
}
