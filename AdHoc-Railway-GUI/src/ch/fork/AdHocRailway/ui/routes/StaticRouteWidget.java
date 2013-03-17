/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
 * version   : $Id$
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

package ch.fork.AdHocRailway.ui.routes;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteChangeListener;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.ui.AdHocRailway;

public class StaticRouteWidget extends JPanel implements RouteChangeListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4978823383737145288L;
	private Route			route;
	private JLabel			nameLabel;
	private JLabel			iconLabel;
	private Icon			routeStopIcon;
	private Icon			routeStartIcon;
	private JProgressBar	routingProgress;
	private JPanel			southPanel;
	private JLabel			numberLabel;

	public StaticRouteWidget(Route route) {
		this.route = route;
		RouteControlIface routeControl = AdHocRailway.getInstance()
				.getRouteControl();
		initGUI();
		routeControl.addRouteChangeListener(route, this);
	}

	private void initGUI() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		setBorder(BorderFactory.createLineBorder(Color.GRAY));

		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.LINE_AXIS));
		routeStopIcon = new ImageIcon(ClassLoader
				.getSystemResource("routes/route_stop.png"));
		routeStartIcon = new ImageIcon(ClassLoader
				.getSystemResource("routes/route_start.png"));
		numberLabel = new JLabel("" + route.getNumber());
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 30));
		nameLabel = new JLabel(route.getName());
		iconLabel = new JLabel(routeStopIcon);

		routingProgress = new JProgressBar(SwingConstants.HORIZONTAL, 0, route
				.getRouteItems().size());
		routingProgress.setPreferredSize(new Dimension((int) routingProgress
				.getPreferredSize().getWidth(), 8));
		routingProgress.setBorder(BorderFactory.createEmptyBorder());

		routingProgress.setBackground(Color.RED);
		routingProgress.setForeground(Color.GREEN);

		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(numberLabel);
		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(nameLabel);
		northPanel.add(Box.createGlue());
		northPanel.add(Box.createHorizontalStrut(5));
		northPanel.add(iconLabel);
		northPanel.add(Box.createHorizontalStrut(5));

		southPanel = new JPanel();
		southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.LINE_AXIS));
		southPanel.add(Box.createHorizontalStrut(5));
		southPanel.add(routingProgress);
		southPanel.add(Box.createHorizontalStrut(5));
		add(Box.createVerticalStrut(5));
		add(northPanel);
		add(southPanel);
		add(Box.createVerticalStrut(5));
	}

	@Override
	public void routeChanged(Route changedRoute) {
		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					RouteControlIface routeControl = AdHocRailway.getInstance()
							.getRouteControl();
					if (routeControl.isRouteEnabled(route)) {
						iconLabel.setIcon(routeStartIcon);
						routingProgress.setForeground(Color.GREEN);
					} else {
						iconLabel.setIcon(routeStopIcon);
						routingProgress.setBackground(Color.RED);
					}
					StaticRouteWidget.this.revalidate();
					StaticRouteWidget.this.repaint();
				}
			});
		}
	}

	@Override
	public void nextSwitchRouted(Route changedRoute) {
		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					routingProgress.setValue(routingProgress.getValue() + 1);
					StaticRouteWidget.this.revalidate();
					StaticRouteWidget.this.repaint();
				}
			});
		}
	}

	@Override
	public void nextSwitchDerouted(Route changedRoute) {

		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					routingProgress.setValue(routingProgress.getValue() - 1);
					StaticRouteWidget.this.revalidate();
					StaticRouteWidget.this.repaint();
				}
			});
		}
	}

	public Route getRoute() {
		return route;
	}
}