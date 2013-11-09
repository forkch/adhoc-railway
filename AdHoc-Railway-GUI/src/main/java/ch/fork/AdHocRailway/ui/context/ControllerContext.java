package ch.fork.AdHocRailway.ui.context;

import de.dermoba.srcp.client.SRCPSession;

import javax.swing.*;

public interface ControllerContext {

	public JFrame getMainFrame();

	public AdHocRailwayIface getMainApp();

	public boolean isEditingMode();

	public abstract SRCPSession getSession();

}
