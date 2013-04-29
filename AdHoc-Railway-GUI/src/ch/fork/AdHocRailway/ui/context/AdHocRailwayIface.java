package ch.fork.AdHocRailway.ui.context;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JToolBar;

import ch.fork.AdHocRailway.ui.EditingModeListener;

public interface AdHocRailwayIface {
	public void registerEscapeKey(final Action action);

	public void addMenu(final JMenu menu);

	public void addToolBar(final JToolBar toolbar);

	public void addEditingModeListener(final EditingModeListener l);

	public void handleException(final Exception ex);

	public void handleException(final String message, final Exception ex);

	public void registerSpaceKey(Action action);
}
