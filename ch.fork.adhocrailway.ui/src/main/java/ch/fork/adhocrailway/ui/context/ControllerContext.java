package ch.fork.adhocrailway.ui.context;

import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.ui.RailwayDeviceManager;
import com.google.common.eventbus.EventBus;
import de.dermoba.srcp.client.SRCPSession;

import javax.swing.*;

public interface ControllerContext {

    public RailwayDeviceManager getRailwayDeviceManager();

    public Preferences getPreferences();

    public JFrame getMainFrame();

    public AdHocRailwayIface getMainApp();

    public boolean isEditingMode();

    public abstract SRCPSession getSession();

    public EventBus getMainBus();

}
