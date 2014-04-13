package ch.fork.AdHocRailway.ui.routes;

import ch.fork.AdHocRailway.controllers.RouteController;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.manager.ManagerException;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.RouteManagerListener;
import ch.fork.AdHocRailway.ui.bus.events.EndImportEvent;
import ch.fork.AdHocRailway.ui.bus.events.StartImportEvent;
import ch.fork.AdHocRailway.ui.context.RouteContext;
import ch.fork.AdHocRailway.ui.routes.configuration.RouteHelper;
import com.google.common.eventbus.Subscribe;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIconFromIconSet;

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
        initShortcuts();
    }

    private void initShortcuts() {
        ctx.getMainApp().registerKey(KeyEvent.VK_R, InputEvent.CTRL_DOWN_MASK, new AddRoutesAction());
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
                updateRoutes(ctx.getRouteManager().getAllRouteGroups());
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
                updateRoutes(ctx.getRouteManager().getAllRouteGroups());
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
    public void failure(final ManagerException arg0) {
        if (disableListener) {
            return;
        }
    }

    private class AddRoutesAction extends AbstractAction {
        public AddRoutesAction() {
            super("Add Routes\u2026",
                    createImageIconFromIconSet("document-new.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (indexToRouteGroup.isEmpty()) {
                JOptionPane.showMessageDialog(ctx.getMainFrame(),
                        "Please configure a group first", "Add Route",
                        JOptionPane.INFORMATION_MESSAGE,
                        createImageIconFromIconSet("dialog-warning.png"));
                return;
            }
            final int selectedGroupPane = getSelectedIndex();

            final RouteGroup selectedTurnoutGroup = indexToRouteGroup
                    .get(selectedGroupPane);

            RouteHelper.addNewRouteDialog(ctx, selectedTurnoutGroup);
        }
    }
}
