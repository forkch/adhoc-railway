/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id: Preferences.java 151 2008-02-14 14:52:37Z fork_ch $
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.AdHocRailway.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ErrorPanel extends JPanel {

    private static final long serialVersionUID = 519354077339077675L;
    public static final Color COLOR = new Color(255, 186, 68);
    private JTextArea errorTextArea;
	private JLabel iconLabel;
	private final int pause = 5000;
	private float alpha = 1.0f;
	boolean active = true;

	public ErrorPanel() {
		initGUI();
	}

	private void initGUI() {
		panel = new RoundedPanel();
		panel.drawBorder = false;
		panel.arcs = new Dimension(10, 10);
		panel.setLayout(new BorderLayout(5, 5));

		errorTextArea = new JTextArea(2, 30);
		errorTextArea.setEditable(false);
		errorTextArea.setForeground(Color.BLACK);
		errorTextArea.setFont(new Font("Dialog", Font.BOLD, 12));

		iconLabel = new JLabel("");
		addMouseListener(new ErrorConfirmAction());
		errorTextArea.addMouseListener(new ErrorConfirmAction());

		panel.add(iconLabel, BorderLayout.WEST);
		panel.add(errorTextArea, BorderLayout.CENTER);
		add(panel);
	}

	public void setErrorTextIcon(final String text, final Icon icon) {
		iconLabel.setIcon(icon);
		iconLabel.setBackground(COLOR);
		setErrorText(text);

	}

	public void setErrorTextIcon(final String text, final String cause,
			final Icon icon) {
		setErrorTextIcon(text, icon);
	}

	public void setErrorText(final String text) {
		alpha = 1.0f;
		active = true;
		errorTextArea.setText(text);
		errorTextArea.setOpaque(false);
		panel.setBackground(COLOR);
		revalidate();
		repaint();

		final Thread errorPanelCloser = new Thread(waitRunner,
				"ErrorPanelCloserThread");
		errorPanelCloser.start();
	}

	private class ErrorConfirmAction extends MouseAdapter {
		@Override
		public void mouseClicked(final MouseEvent e) {
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				SwingUtilities.invokeLater(closerRunner);
			}
		}
	}

	@Override
	protected void paintComponent(final Graphics g) {
		super.paintComponent(g);
		final Composite alphaCompositge = AlphaComposite.getInstance(
				AlphaComposite.SRC_OVER, alpha);
		final Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(alphaCompositge);
	}

	final Runnable waitRunner = new Runnable() {
		@Override
		public void run() {
			try {
				Thread.sleep(pause);
				final Thread closer = new Thread(closerRunner);
				closer.start();
			} catch (final Exception e) {
				e.printStackTrace();
			}
		}
	};

	final Runnable closerRunner = new Runnable() {
		@Override
		public void run() {
			while (active) {
				repaint();
				ErrorPanel.this.alpha -= 0.05f;
				if (ErrorPanel.this.alpha < 0.1f) {
					ErrorPanel.this.alpha = 0.0f;
					active = false;
				}
				try {
					Thread.sleep(50);
				} catch (final InterruptedException e) {
					e.printStackTrace();
				}

			}
			errorTextArea.setText("");
			iconLabel.setIcon(null);
		}
	};
	private RoundedPanel panel;
}
