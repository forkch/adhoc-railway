package ch.fork.AdHocRailway.ui.routes;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.ui.WidgetTab;

public class RouteGroupTab extends WidgetTab {

	private final RouteGroup routeGroup;
	private final Map<Route, RouteWidget> routeToRouteWidget = new HashMap<Route, RouteWidget>();

	public RouteGroupTab(RouteGroup routeGroup) {
		this.routeGroup = routeGroup;

		initTab();
		this.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {
					System.out.println("er");
				}
			}
		});
	}

	private void initTab() {
		for (Route turnout : routeGroup.getRoutes()) {
			addRoute(turnout);
		}
	}

	public void addRoute(Route route) {
		RouteWidget routeWidget = new RouteWidget(route);
		addWidget(routeWidget);
		routeToRouteWidget.put(route, routeWidget);
	}

	public void removeRoute(Route route) {
		remove(routeToRouteWidget.get(route));
	}

	public void updateRoute(Route route) {
		routeToRouteWidget.get(route).setRoute(route);

	}

	public void updateRouteGroup(RouteGroup group) {

	}

}
