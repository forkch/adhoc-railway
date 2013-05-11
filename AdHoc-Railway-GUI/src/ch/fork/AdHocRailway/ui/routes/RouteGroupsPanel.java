package ch.fork.AdHocRailway.ui.routes;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerException;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerListener;
import ch.fork.AdHocRailway.ui.context.RouteContext;

public class RouteGroupsPanel extends JTabbedPane implements
		RouteManagerListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8209638004262038025L;

	private final Map<Integer, RouteGroup> indexToRouteGroup = new HashMap<Integer, RouteGroup>();

	private final Map<RouteGroup, RouteGroupTab> routeGroupToRouteGroupTab = new HashMap<RouteGroup, RouteGroupTab>();

	private final RouteManager routePersistence;
	private final RouteController routeControl;

	private final RouteContext ctx;

	public RouteGroupsPanel(final RouteContext ctx, final int tabPlacement) {
		super(tabPlacement);
		this.ctx = ctx;
		routePersistence = ctx.getRouteManager();
		routeControl = ctx.getRouteControl();
		routePersistence.addRouteManagerListener(this);

	}

	private void updateRoutes(final SortedSet<RouteGroup> routeGroups) {
		indexToRouteGroup.clear();
		removeAll();
		routeGroupToRouteGroupTab.clear();

		int i = 1;

		routeControl.removeAllRouteChangeListeners();

		for (final RouteGroup routeGroup : routeGroups) {
			indexToRouteGroup.put(i - 1, routeGroup);
			addRouteGroup(i, routeGroup);
			i++;
		}
	}

	public void addRouteGroup(final int groupNumber, final RouteGroup routeGroup) {
		final RouteGroupTab routeGroupTab = new RouteGroupTab(ctx, routeGroup);

		add(routeGroupTab, groupNumber + ": " + routeGroup.getName());
		routeGroupToRouteGroupTab.put(routeGroup, routeGroupTab);

	}

	@Override
	public void routesUpdated(final SortedSet<RouteGroup> routeGroups) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateRoutes(routeGroups);
			}
		});
		revalidate();
		repaint();
	}

	@Override
	public void routeUpdated(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.updateRoute(route);
				revalidate();
				repaint();

			}
		});

	}

	@Override
	public void routeRemoved(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {

				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.removeRoute(route);

				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void routeAdded(final Route route) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(route.getRouteGroup());
				routeGroupTab.addRoute(route);
				revalidate();
				repaint();

			}
		});

	}

	@Override
	public void routeGroupAdded(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				addRouteGroup(-1, group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void routeGroupRemoved(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(group);
				remove(routeGroupTab);
				revalidate();
				repaint();
			}
		});

	}

	@Override
	public void routeGroupUpdated(final RouteGroup group) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final RouteGroupTab routeGroupTab = routeGroupToRouteGroupTab
						.get(group);
				routeGroupTab.updateRouteGroup(group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void failure(final RouteManagerException arg0) {

	}
}
