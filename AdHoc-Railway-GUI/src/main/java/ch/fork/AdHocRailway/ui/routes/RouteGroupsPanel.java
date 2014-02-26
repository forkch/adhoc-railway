package ch.fork.AdHocRailway.ui.routes;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerException;
import ch.fork.AdHocRailway.manager.turnouts.RouteManagerListener;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import com.google.common.eventbus.Subscribe;

import javax.swing.*;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

public class RouteGroupsPanel extends JTabbedPane implements
        RouteManagerListener {

    private final Map<Integer, RouteGroup> indexToRouteGroup = new HashMap<Integer, RouteGroup>();

    private final Map<RouteGroup, RouteGroupTab> routeGroupToRouteGroupTab = new HashMap<RouteGroup, RouteGroupTab>();

    private final RouteManager routePersistence;

    private final RouteContext ctx;
    private boolean disableListener;

    public RouteGroupsPanel(final RouteContext ctx, final int tabPlacement) {
        super(tabPlacement);
        this.ctx = ctx;
        ctx.getMainBus().register(this);
        routePersistence = ctx.getRouteManager();
        routePersistence.addRouteManagerListener(this);

    }

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
        updateRoutes(ctx.getRouteManager().getAllRouteGroups());
    }

    private void updateRoutes(final SortedSet<RouteGroup> routeGroups) {
        indexToRouteGroup.clear();
        removeAll();
        routeGroupToRouteGroupTab.clear();

        int i = 1;

        final RouteController routeControl = ctx.getRouteControl();
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
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
        if (disableListener) {
            return;
        }
    }
}
