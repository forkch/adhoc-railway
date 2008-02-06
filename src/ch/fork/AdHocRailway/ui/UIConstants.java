package ch.fork.AdHocRailway.ui;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.JTextField;

public interface UIConstants {
    public static final double ACTUAL_CONFIG_VERSION = 0.3;
	public static final Color ERROR_COLOR = new Color(255, 177, 177);
	public static final Color WARN_COLOR = new Color(255, 255, 128);
	public static final Color DEFAULT_TEXTFIELD_COLOR = new JTextField().getBackground();
	public static final Color DEFAULT_PANEL_COLOR = new JPanel().getBackground();
}
