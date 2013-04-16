package ch.fork.AdHocRailway.ui.context;

import javax.swing.JFrame;

import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControlface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import de.dermoba.srcp.client.SRCPSession;
import de.dermoba.srcp.model.locking.SRCPLockControl;
import de.dermoba.srcp.model.power.SRCPPowerControl;

public class ApplicationContext implements TurnoutContext, RouteContext,
		LocomotiveContext, TrackContext, PowerContext {

	private JFrame mainFrame;

	private AdHocRailwayIface mainApp;

	private Preferences preferences;

	private TurnoutControlIface turnoutControl;
	private TurnoutManager turnoutManager;

	private RouteControlIface routeControl;
	private RouteManager routeManager;

	private LocomotiveControlface locomotiveControl;
	private LocomotiveManager locomotiveManager;

	private SRCPPowerControl powerControl;
	private int activeBoosterCount;
	private SRCPLockControl lockControl;

	private SRCPSession session;

	private boolean isEditingMode;

	public Preferences getPreferences() {
		return preferences;
	}

	public void setPreferences(final Preferences preferences) {
		this.preferences = preferences;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.TurnoutContext#getTurnoutControl()
	 */
	@Override
	public TurnoutControlIface getTurnoutControl() {
		return turnoutControl;
	}

	public void setTurnoutControl(final TurnoutControlIface turnoutControl) {
		this.turnoutControl = turnoutControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.TurnoutContext#getTurnoutManager()
	 */
	@Override
	public TurnoutManager getTurnoutManager() {
		return turnoutManager;
	}

	public void setTurnoutManager(final TurnoutManager turnoutPersistence) {
		this.turnoutManager = turnoutPersistence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.RouteContext#getRouteControl()
	 */
	@Override
	public RouteControlIface getRouteControl() {
		return routeControl;
	}

	public void setRouteControl(final RouteControlIface routeControl) {
		this.routeControl = routeControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.RouteContext#getRouteManager()
	 */
	@Override
	public RouteManager getRouteManager() {
		return routeManager;
	}

	public void setRouteManager(final RouteManager routePersistence) {
		this.routeManager = routePersistence;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.LocomotiveContext#getLocomotiveControl()
	 */
	@Override
	public LocomotiveControlface getLocomotiveControl() {
		return locomotiveControl;
	}

	public void setLocomotiveControl(
			final LocomotiveControlface locomotiveControl) {
		this.locomotiveControl = locomotiveControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ch.fork.AdHocRailway.domain.LocomotiveContext#getLocomotiveManager()
	 */
	@Override
	public LocomotiveManager getLocomotiveManager() {
		return locomotiveManager;
	}

	public void setLocomotiveManager(
			final LocomotiveManager locomotivePersistence) {
		this.locomotiveManager = locomotivePersistence;
	}

	@Override
	public SRCPPowerControl getPowerControl() {
		return powerControl;
	}

	public void setPowerControl(final SRCPPowerControl powerControl) {
		this.powerControl = powerControl;
	}

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

}
