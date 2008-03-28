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

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

public class ErrorPanel
		extends JPanel {
	
	private JTextArea	errorTextArea;
	private JLabel		iconLabel;
	private Color		defaultColor;
	private int			pause	= 5000;
	private float		alpha = 1.0f;
	boolean				active	= true;
	
	public ErrorPanel() {
		initGUI();
	}
	
	private void initGUI() {
		setLayout(new BorderLayout(5, 5));
		
		errorTextArea = new JTextArea(2, 30);
		errorTextArea.setEditable(false);
		errorTextArea.setForeground(Color.BLACK);
		defaultColor = errorTextArea.getBackground();
		errorTextArea.setFont(new Font("Dialog", Font.BOLD, 12));
		
		iconLabel = new JLabel("");
		addMouseListener(new ErrorConfirmAction());
		errorTextArea.addMouseListener(new ErrorConfirmAction());
		
		add(iconLabel, BorderLayout.WEST);
		add(errorTextArea, BorderLayout.CENTER);
	}
	
	public void setErrorTextIcon(String text, Icon icon) {
		iconLabel.setIcon(icon);
		iconLabel.setBackground(new Color(255, 177, 177));
		setErrorText(text);
		
	}
	
	public void setErrorText(String text) {
		alpha = 1.0f;
		active = true;
		errorTextArea.setText(text);
		errorTextArea.setBackground(new Color(255, 177, 177));
		revalidate();
		repaint();
		final Runnable closerRunner = new Runnable() {
			public void run() {
				try {
					Thread.sleep(pause);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				while (active) {
					repaint();
					ErrorPanel.this.alpha -= 0.1f;
					if (ErrorPanel.this.alpha < 0.1f) {
						active = false;
					}
					
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					
				}
				errorTextArea.setBackground(ErrorPanel.this.defaultColor);
				errorTextArea.setText("");
				iconLabel.setIcon(null);
			}
		};
		Runnable waitRunner = new Runnable() {
			public void run() {
				try {
					Thread.sleep(pause);
					SwingUtilities.invokeLater(closerRunner);
				} catch (Exception e) {
					e.printStackTrace();
					// can catch InvocationTargetException
					// can catch InterruptedException
				}
			}
		};
		Thread errorPanelCloser =
				new Thread(closerRunner, "ErrorPanelCloserThread");
		errorPanelCloser.start();
	}
	
	private class ErrorConfirmAction
			extends MouseAdapter {
		public void mouseClicked(MouseEvent e) {
			
			if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON1) {
				errorTextArea.setBackground(defaultColor);
				errorTextArea.setText("");
				iconLabel.setIcon(null);
			}
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Composite alphaCompositge =
				AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha);
		Graphics2D g2 = (Graphics2D) g;
		g2.setComposite(alphaCompositge);
	}
}
