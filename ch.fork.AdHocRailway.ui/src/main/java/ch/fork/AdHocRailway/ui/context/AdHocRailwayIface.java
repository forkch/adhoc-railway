package ch.fork.AdHocRailway.ui.context;

import javax.swing.*;

public interface AdHocRailwayIface {
    public void registerKey(final int keyCode, int modifiers, final Action action);

    public void registerEscapeKey(final Action action);

    public void registerSpaceKey(final Action action);

    public void addMenu(final JMenu menu);

    public void addToolBar(final JToolBar toolbar);

    public void handleException(final Throwable ex);

    public void handleException(final String message, final Throwable ex);

    public void initProceeded(final String string);

    public void updateCommandHistory(final String string);

    public void setTitle(final String string);

    void displayMessage(String s);
}
