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

import ch.fork.AdHocRailway.controllers.RouteChangeListener;
import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.manager.turnouts.RouteException;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteConfig;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteHelper;
import com.google.common.eventbus.Subscribe;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RouteWidget extends JPanel implements RouteChangeListener {

	private static final long serialVersionUID = 1639307474591899703L;
	private Route route;
	private JLabel nameLabel;
	private JLabel iconLabel;
	private Icon routeStopIcon;
	private Icon routeStartIcon;
	private JProgressBar routingProgress;
	private JLabel numberLabel;
	private JLabel orientationLabel;
	private final boolean testMode;
	private final RouteContext ctx;

	public RouteWidget(final RouteContext ctx, final Route route,
			final boolean testMode) {
		this.ctx = ctx;
		this.route = route;
		this.testMode = testMode;
        ctx.getRouteControl().addRouteChangeListener(route, this);
        ctx.getMainBus().register(this);
		initGUI();
		updateRoute();
	}

    @Subscribe
    public void connectedToRailwayDevice(ConnectedToRailwayEvent event) {
        if(event.isConnected()) {
            ctx.getRouteControl().addRouteChangeListener(route, this);
        }else {
            ctx.getRouteControl().removeRouteChangeListener(route, this);
        }
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

		routingProgress = new JProgressBar(SwingConstants.HORIZONTAL, 0,
				getRouteItemCount());
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
		routingProgress.setMaximum(getRouteItemCount());
		setToolTipText(RouteHelper.getRouteDescription(route));
	}

	private int getRouteItemCount() {
		int count = 0;
		for (final RouteItem routeItem : route.getRouteItems()) {
			if (routeItem.getTurnout().isThreeWay()) {
				count += 2;
			} else {
				count++;
			}
		}
		return count;
	}

	private class MouseAction extends MouseAdapter {

		@Override
		public void mouseClicked(final MouseEvent e) {
			try {
				if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON1) {
					if (route.isRouting()) {
						return;
					}
					final RouteController routeControl = ctx.getRouteControl();
					if (!testMode) {
						routeControl.toggle(route);
					} else {
						routeControl.toggleTest(route);
					}
				} else if (e.getClickCount() == 1
						&& e.getButton() == MouseEvent.BUTTON3) {

					if (ctx.isEditingMode()) {
						displayRouteConfig();
					}
				}
			} catch (final RouteException e1) {
				ctx.getMainApp().handleException(e1);
			}
		}

		private void displayRouteConfig() {
			final RouteController routeControl = ctx.getRouteControl();
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
					if (route.isEnabled()) {
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