package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.controllers.LocomotiveController;
import ch.fork.AdHocRailway.controllers.PowerController;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.RailwayDeviceManager;
import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;

import javax.swing.*;
import java.io.File;

public class ApplicationContext implements TurnoutContext, RouteContext,
        LocomotiveContext, TrackContext, PowerContext,
        PersistenceManagerContext, RailwayDeviceManagerContext {

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
    public void setTurnoutManager(final TurnoutManager turnoutPersistence) {
        this.turnoutManager = turnoutPersistence;
    }

    @Override
    public RouteController getRouteControl() {
        return routeControl;
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

    @Override
    public void setActualFile(File file) {
        this.actualFile = file;
    }

    public File getActualFile() {
        return actualFile;
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
    public void setPreviousLocoDir(File previousLocodir) {

        this.previousLocodir = previousLocodir;
    }

    @Override
    public File getPreviousLocoDir() {
        return previousLocodir;
    }

    @Override
    public void setLocomotiveManager(
            final LocomotiveManager locomotivePersistence) {
        this.locomotiveManager = locomotivePersistence;
    }

    @Override
    public PowerController getPowerControl() {
        return powerControl;
    }

    @Override
    public void setPowerController(final PowerController powerControl) {
        this.powerControl = powerControl;
    }

    @Override
    public void setActiveBoosterCount(final int activeBoosterCount) {
        this.activeBoosterCount = activeBoosterCount;
    }

    public int getActiveBoosterCount() {
        return this.activeBoosterCount;
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

    public void setRailwayDeviceManager(
            final RailwayDeviceManager railwayDeviceManager) {
        this.railwayDeviceManager = railwayDeviceManager;
    }

    @Override
    public RailwayDeviceManager getRailwayDeviceManager() {
        return railwayDeviceManager;
    }

    @Override
    public EventBus getMainBus() {
        return mainBus;
    }
}
