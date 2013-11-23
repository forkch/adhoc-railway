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
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;

import javax.swing.*;

public class ApplicationContext implements TurnoutContext, RouteContext,
		LocomotiveContext, TrackContext, PowerContext {

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

	public void setTurnoutControl(final TurnoutController turnoutControl) {
		this.turnoutControl = turnoutControl;
	}

	@Override
	public TurnoutManager getTurnoutManager() {
		return turnoutManager;
	}

	public void setTurnoutManager(final TurnoutManager turnoutPersistence) {
		this.turnoutManager = turnoutPersistence;
	}

	@Override
	public RouteController getRouteControl() {
		return routeControl;
	}

	public void setRouteControl(final RouteController routeControl) {
		this.routeControl = routeControl;
	}

	@Override
	public RouteManager getRouteManager() {
		return routeManager;
	}

	public void setRouteManager(final RouteManager routePersistence) {
		this.routeManager = routePersistence;
	}

	@Override
	public LocomotiveController getLocomotiveControl() {
		return locomotiveControl;
	}

	public void setLocomotiveControl(
			final LocomotiveController locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	@Override
	public LocomotiveManager getLocomotiveManager() {
		return locomotiveManager;
	}

	public void setLocomotiveManager(
			final LocomotiveManager locomotivePersistence) {
		this.locomotiveManager = locomotivePersistence;
	}

	@Override
	public PowerController getPowerControl() {
		return powerControl;
	}

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

	public SRCPLockControl getLockControl() {
		return lockControl;
	}

	public void setLockControl(final SRCPLockControl lockControl) {
		this.lockControl = lockControl;
	}

	@Override
	public SRCPSession getSession() {
		return session;
	}

	public void setSession(final SRCPSession session) {
		this.session = session;
	}

	@Override
	public boolean isEditingMode() {
		return isEditingMode;
	}

	public void setEditingMode(final boolean isEditingMode) {
		this.isEditingMode = isEditingMode;
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
}
