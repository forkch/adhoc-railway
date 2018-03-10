package ch.fork.AdHocRailway.ui.routes;

import ch.fork.AdHocRailway.model.turnouts.Route;
import ch.fork.AdHocRailway.model.turnouts.RouteGroup;
import ch.fork.AdHocRailway.ui.bus.events.ConnectedToRailwayEvent;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteHelper;
import ch.fork.AdHocRailway.ui.widgets.WidgetTab;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;


public class RouteGroupTab extends WidgetTab {

    private final RouteGroup routeGroup;
    private final Map<Route, RouteWidget> routeToRouteWidget = new HashMap<Route, RouteWidget>();
    private final RouteContext ctx;

    public RouteGroupTab(final RouteContext ctx, final RouteGroup routeGroup) {
        this.ctx = ctx;
        this.routeGroup = routeGroup;

        initTab();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {

                    if (ctx.isEditingMode()) {
                        RouteHelper.addNewRouteDialog(ctx, routeGroup);
                    }
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
        final RouteWidget routeWidget = new RouteWidget(ctx, route, false);
        //routeWidget.connectedToRailwayDevice(new ConnectedToRailwayEvent(ctx.getRailwayDeviceManager().isConnected()));

        addWidget(routeWidget);
        routeToRouteWidget.put(route, routeWidget);
        revalidate();
        repaint();
    }

    public void removeRoute(final Route route) {
        remove(routeToRouteWidget.get(route));
        revalidate();
        repaint();
    }

    public void updateRoute(final Route route) {
        routeToRouteWidget.get(route).setRoute(route);
        revalidate();
        repaint();

    }

    public void updateRouteGroup(final RouteGroup group) {

    }

}
