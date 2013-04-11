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
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import net.miginfocom.swing.MigLayout;
import ch.fork.AdHocRailway.domain.RouteContext;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteChangeListener;
import ch.fork.AdHocRailway.domain.routes.RouteControlIface;
import ch.fork.AdHocRailway.domain.routes.RouteException;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteConfig;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteHelper;

public class RouteWidget extends JPanel implements RouteChangeListener {

	private static final long serialVersionUID = 1639307474591899703L;
	private Route route;
	private JLabel nameLabel;
	private JLabel iconLabel;
	private Icon routeStopIcon;
	private Icon routeStartIcon;
	private JProgressBar routingProgress;
	private MouseAction mouseAction;
	private JLabel numberLabel;
	private JLabel orientationLabel;
	private final boolean testMode;
	private final RouteControlIface routeControl;
	private final RouteContext ctx;

	public RouteWidget(final RouteContext ctx, final Route route,
			final boolean testMode) {
		this.ctx = ctx;
		this.route = route;
		this.testMode = testMode;
		routeControl = ctx.getRouteControl();
		initGUI();
		routeControl.addRouteChangeListener(route, this);
		updateRoute();
	}

	private void initGUI() {
		setLayout(new MigLayout());
		setBorder(BorderFactory.createLineBorder(Color.GRAY));

		routeStopIcon = new ImageIcon(
				ClassLoader.getSystemResource("routes/route_stop.png"));
		routeStartIcon = new ImageIcon(
				ClassLoader.getSystemResource("routes/route_start.png"));

		numberLabel = new JLabel();
		orientationLabel = new JLabel();
		orientationLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
		nameLabel = new JLabel();
		nameLabel.setFont(new Font("Dialog", Font.PLAIN, 10));
		numberLabel.setFont(new Font("Dialog", Font.BOLD, 25));
		iconLabel = new JLabel(routeStopIcon);

		routingProgress = new JProgressBar(SwingConstants.HORIZONTAL, 0, route
				.getRouteItems().size());
		routingProgress.setBorder(BorderFactory.createEmptyBorder());

		routingProgress.setBackground(UIConstants.STATE_RED);
		routingProgress.setForeground(UIConstants.STATE_GREEN);
		addMouseListener(new MouseAction());

		add(numberLabel, "span 1 2");
		add(orientationLabel, "");
		add(iconLabel, "span 1 2, align right, wrap");
		add(nameLabel, "wrap");

		add(routingProgress, "span 3, h 5!, w 125!");

	}

	private void updateRoute() {
		numberLabel.setText("" + route.getNumber());
		orientationLabel.setText(route.getOrientation());
		nameLabel.setText(route.getName());
		routingProgress.setMaximum(route.getRouteItems().size());
		setToolTipText(RouteHelper.getRouteDescription(route));
	}

	private class MouseAction extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					if (routeControl.isRouting(route)) {
						return;
					}
					if (!testMode) {
						routeControl.toggle(route);
					} else {
						routeControl.toggleTest(route);
					}
					removeMouseListener(mouseAction);
				} else if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON3) {

					if (ctx.isEditingMode()) {
						displayRouteConfig();
					}
				}
			} catch (final RouteException e1) {
				ExceptionProcessor.getInstance().processException(e1);
			}
		}

		private void displayRouteConfig() {
			routeControl.removeRouteChangeListener(route, RouteWidget.this);
			new RouteConfig(ctx.getMainFrame(), ctx, route,
					route.getRouteGroup());
			routeControl.addRouteChangeListener(route, RouteWidget.this);

			routeChanged(route);
		}
	}

	@Override
	public void routeChanged(final Route changedRoute) {
		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					if (routeControl.isRouteEnabled(route)) {
						iconLabel.setIcon(routeStartIcon);
						routingProgress.setValue(routingProgress.getMaximum());
					} else {
						iconLabel.setIcon(routeStopIcon);
						routingProgress.setValue(routingProgress.getMinimum());
					}
				}
			});
		}
	}

	@Override
	public void nextTurnoutRouted(final Route changedRoute) {
		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					routingProgress.setValue(routingProgress.getValue() + 1);
				}
			});
		}
	}

	@Override
	public void nextTurnoutDerouted(final Route changedRoute) {
		if (route.equals(changedRoute)) {
			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					routingProgress.setValue(routingProgress.getValue() - 1);
				}
			});
		}
	}

	public Route getRoute() {
		return route;
	}

	public void setRoute(final Route route) {
		this.route = route;
		updateRoute();
	}
}