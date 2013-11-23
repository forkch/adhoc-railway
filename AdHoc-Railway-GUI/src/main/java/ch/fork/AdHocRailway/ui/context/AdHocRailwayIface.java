package ch.fork.AdHocRailway.ui.context;

import ch.fork.AdHocRailway.ui.EditingModeListener;

import javax.swing.*;

public interface AdHocRailwayIface {
	public void registerEscapeKey(final Action action);

	public void addMenu(final JMenu menu);

	public void addToolBar(final JToolBar toolbar);

	public void addEditingModeListener(final EditingModeListener l);

	public void handleException(final Throwable ex);

	public void handleException(final String message, final Throwable ex);

	public void registerSpaceKey(Action action);

	public void initProceeded(String string);

	public void updateCommandHistory(String string);

	public void setTitle(String string);

	public void connectedToRailwayDevice(boolean enable);
}