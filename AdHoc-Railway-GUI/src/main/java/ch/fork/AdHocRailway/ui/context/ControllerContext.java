package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.ui.RailwayDeviceManager;
import com.google.common.eventbus.EventBus;
import de.dermoba.srcp.client.SRCPSession;

import javax.swing.*;

public interface ControllerContext {

	public RailwayDeviceManager getRailwayDeviceManager();

	public JFrame getMainFrame();

	public AdHocRailwayIface getMainApp();

	public boolean isEditingMode();

	public abstract SRCPSession getSession();

	public EventBus getMainBus();

}
