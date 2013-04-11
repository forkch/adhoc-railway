package ch.fork.AdHocRailway.domain;

import javax.swing.JFrame;

import de.dermoba.srcp.client.SRCPSession;

public interface ControllerContext {

	public JFrame getMainFrame();

	public AdHocRailwayIface getMainApp();

	public boolean isEditingMode();

	public abstract SRCPSession getSession();

}
