package ch.fork.AdHocRailway.ui.routes;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.ui.WidgetTab;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteHelper;

public class RouteGroupTab extends WidgetTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1559441698783240568L;
	private final RouteGroup routeGroup;
	private final Map<Route, RouteWidget> routeToRouteWidget = new HashMap<Route, RouteWidget>();

	public RouteGroupTab(final RouteGroup routeGroup) {
		this.routeGroup = routeGroup;

		initTab();

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {

					RouteHelper.addNewRouteDialog(routeGroup);
				}
			}
		});
	}

	private void initTab() {
		for (final Route turnout : routeGroup.getRoutes()) {
			addRoute(turnout);
		}
	}

	public void addRoute(final Route route) {
		final RouteWidget routeWidget = new RouteWidget(route);
		addWidget(routeWidget);
		routeToRouteWidget.put(route, routeWidget);
	}

	public void removeRoute(final Route route) {
		remove(routeToRouteWidget.get(route));
	}

	public void updateRoute(final Route route) {
		routeToRouteWidget.get(route).setRoute(route);

	}

	public void updateRouteGroup(final RouteGroup group) {

	}

}
