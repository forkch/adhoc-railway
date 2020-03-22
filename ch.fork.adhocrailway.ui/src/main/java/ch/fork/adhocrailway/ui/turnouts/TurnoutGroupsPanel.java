package ch.fork.adhocrailway.ui.turnouts;

import ch.fork.adhocrailway.controllers.TurnoutController;
import ch.fork.adhocrailway.manager.TurnoutManagerListener;
import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;
import ch.fork.adhocrailway.services.AdHocServiceException;
import ch.fork.adhocrailway.technical.configuration.Preferences;
import ch.fork.adhocrailway.technical.configuration.PreferencesKeys;
import ch.fork.adhocrailway.ui.BrainTerminal;
import ch.fork.adhocrailway.ui.bus.events.ConnectedToPersistenceEvent;
import ch.fork.adhocrailway.ui.bus.events.EndImportEvent;
import ch.fork.adhocrailway.ui.bus.events.StartImportEvent;
import ch.fork.adhocrailway.ui.context.EditingModeEvent;
import ch.fork.adhocrailway.ui.context.TurnoutContext;
import ch.fork.adhocrailway.ui.turnouts.configuration.TurnoutHelper;
import ch.fork.adhocrailway.ui.utils.ImageTools;
import ch.fork.adhocrailway.ui.widgets.SmallToolbarButton;
import com.google.common.eventbus.Subscribe;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import static ch.fork.adhocrailway.ui.utils.ImageTools.createImageIconFromCustom;
import static ch.fork.adhocrailway.ui.utils.ImageTools.createImageIconFromIconSet;

public class TurnoutGroupsPanel extends JTabbedPane implements
        TurnoutManagerListener {

    private final Map<Integer, TurnoutGroup> indexToTurnoutGroup = new HashMap<Integer, TurnoutGroup>();

    private final Map<TurnoutGroup, TurnoutGroupTab> turnoutGroupToTurnoutGroupTab = new HashMap<TurnoutGroup, TurnoutGroupTab>();

    private final TurnoutContext ctx;
    private JMenuItem addTurnoutsItem;
    private JMenuItem turnoutsProgrammerItem;
    private JButton addTurnoutsButton;
    private JButton turnoutProgrammerButton;
    private boolean disableListener;

    public TurnoutGroupsPanel(final TurnoutContext turnoutCtx,
                              final int tabPlacement) {
        super(tabPlacement);
        this.ctx = turnoutCtx;


        initToolBar();
        initMenuBar();
        initShortcuts();

        ctx.getMainBus().register(this);
    }

    @Subscribe
    public void connectedToPersistence(final ConnectedToPersistenceEvent event) {
        ctx.getTurnoutManager().addTurnoutManagerListener(this);
        updateTurnouts(ctx.getTurnoutManager().getAllTurnoutGroups());
    }

    @Subscribe
    public void startImport(final StartImportEvent event) {
        disableListener = true;
    }

    @Subscribe
    public void endImport(final EndImportEvent event) {
        disableListener = false;
        updateTurnouts(ctx.getTurnoutManager().getAllTurnoutGroups());
    }

    private void initShortcuts() {
        ctx.getMainApp().registerKey(KeyEvent.VK_T, InputEvent.CTRL_DOWN_MASK, new AddTurnoutsAction());

    }

    private void updateTurnouts(final SortedSet<TurnoutGroup> turnoutGroups) {
        indexToTurnoutGroup.clear();
        removeAll();
        turnoutGroupToTurnoutGroupTab.clear();
        int i = 1;

        for (final TurnoutGroup turnoutGroup : turnoutGroups) {
            indexToTurnoutGroup.put(i - 1, turnoutGroup);
            addTurnoutGroup(i, turnoutGroup);
            i++;
        }
    }

    public void addTurnoutGroup(final int groupNumber,
                                final TurnoutGroup turnoutGroup) {
        final TurnoutGroupTab turnoutGroupTab = new TurnoutGroupTab(ctx,
                turnoutGroup);

        add(turnoutGroupTab, groupNumber + ": " + turnoutGroup.getName());
        turnoutGroupToTurnoutGroupTab.put(turnoutGroup, turnoutGroupTab);

    }

    private void initToolBar() {
        /* Turnout Tools */
        final JToolBar turnoutToolsToolBar = new JToolBar();
        addTurnoutsButton = new SmallToolbarButton(new AddTurnoutsAction());
        final JButton setAllSwitchesStraightButton = new SmallToolbarButton(
                new TurnoutsStraightAction());
        turnoutProgrammerButton = new SmallToolbarButton(
                new TurnoutProgrammerAction());

        turnoutToolsToolBar.add(setAllSwitchesStraightButton);
        turnoutToolsToolBar.add(addTurnoutsButton);
        turnoutToolsToolBar.add(turnoutProgrammerButton);

        ctx.getMainApp().addToolBar(turnoutToolsToolBar);
    }

    private void initMenuBar() {
        /* TOOLS */
        final JMenu toolsMenu = new JMenu("Tools");
        addTurnoutsItem = new JMenuItem(new AddTurnoutsAction());
        final JMenuItem turnoutsStraightItem = new JMenuItem(
                new TurnoutsStraightAction());
        final JMenuItem turnoutsWarmerItem = new JMenuItem(
                new TurnoutTesterAction());
        turnoutsProgrammerItem = new JMenuItem(new TurnoutProgrammerAction());

        toolsMenu.add(turnoutsStraightItem);
        toolsMenu.add(turnoutsWarmerItem);
        toolsMenu.addSeparator();
        toolsMenu.add(addTurnoutsItem);
        toolsMenu.add(turnoutsProgrammerItem);

        ctx.getMainApp().addMenu(toolsMenu);
    }

    @Override
    public void turnoutsUpdated(final SortedSet<TurnoutGroup> turnoutGroups) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateTurnouts(turnoutGroups);

            }
        });
    }

    @Override
    public void turnoutUpdated(final Turnout turnout) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
                        .get(turnout.getTurnoutGroup());
                turnoutGroupTab.updateTurnout(turnout);
            }

        });
    }

    @Override
    public void turnoutRemoved(final Turnout turnout) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {

                final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
                        .get(turnout.getTurnoutGroup());
                turnoutGroupTab.removeTurnout(turnout);
            }
        });

    }

    @Override
    public void turnoutAdded(final Turnout turnout) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
                        .get(turnout.getTurnoutGroup());
                turnoutGroupTab.addTurnout(turnout);
            }
        });

    }

    @Override
    public void turnoutGroupAdded(final TurnoutGroup group) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateTurnouts(ctx.getTurnoutManager().getAllTurnoutGroups());

            }
        });
    }

    @Override
    public void turnoutGroupRemoved(final TurnoutGroup group) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                updateTurnouts(ctx.getTurnoutManager().getAllTurnoutGroups());
            }
        });

    }

    @Override
    public void turnoutGroupUpdated(final TurnoutGroup group) {
        if (disableListener) {
            return;
        }
        SwingUtilities.invokeLater(new Runnable() {

            @Override
            public void run() {
                final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
                        .get(group);
                turnoutGroupTab.updateTurnoutGroup(group);

            }
        });
    }

    @Override
    public void failure(final AdHocServiceException serviceException) {
        if (disableListener) {
            return;
        }
    }

    @Subscribe
    public void editingModeChanged(final EditingModeEvent event) {
        final boolean editing = event.isEditingMode();
        addTurnoutsItem.setEnabled(editing);
        addTurnoutsButton.setEnabled(editing);
        turnoutsProgrammerItem.setEnabled(editing);
        turnoutProgrammerButton.setEnabled(editing);

    }

    private void revalidateAllTurnouts() {
        for (final TurnoutGroupTab tgt : turnoutGroupToTurnoutGroupTab.values()) {
            tgt.revalidateTurnouts();
        }
    }

    private class TurnoutsStraightAction extends AbstractAction {


        public TurnoutsStraightAction() {
            super("Set all turnouts straight\u2026",
                    createImageIconFromCustom("switch.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            final TurnoutStraighter s = new TurnoutStraighter();
            s.start();
        }

        private class TurnoutStraighter extends Thread {

            @Override
            public void run() {
                try {
                    final TurnoutController turnoutControl = ctx
                            .getTurnoutControl();
                    int delay = Preferences.getInstance().getIntValue(
                            PreferencesKeys.ROUTING_DELAY);
                    if(delay < 500) {
                        delay = 500;
                    }
                    for (final Turnout t : ctx.getTurnoutManager().getAllTurnouts()) {
                        if(!t.isLinkedToRoute()) {
                            turnoutControl.setDefaultState(t);
                        }
                        Thread.sleep(delay);
                    }
                } catch (final InterruptedException e1) {
                    ctx.getMainApp().handleException(e1);
                }
            }
        }
    }

    private class AddTurnoutsAction extends AbstractAction {

        public AddTurnoutsAction() {
            super("Add Turnouts\u2026",
                    createImageIconFromIconSet("document-new.png"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            if (indexToTurnoutGroup.isEmpty()) {
                JOptionPane.showMessageDialog(ctx.getMainFrame(),
                        "Please configure a group first", "Add Turnouts",
                        JOptionPane.INFORMATION_MESSAGE,
                        createImageIconFromIconSet("dialog-warning.png"));
                return;
            }
            final int selectedGroupPane = getSelectedIndex();

            final TurnoutGroup selectedTurnoutGroup = indexToTurnoutGroup
                    .get(selectedGroupPane);

            TurnoutHelper.addNewTurnoutDialog(ctx, selectedTurnoutGroup);
        }
    }

    private class TurnoutProgrammerAction extends AbstractAction {


        public TurnoutProgrammerAction() {
            super("Turnout Decoder Programmer\u2026",
                    ImageTools
                            .createImageIconFromCustom("switch_programmer.png")
            );
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            new TurnoutProgrammer(ctx.getMainFrame(), ctx);
        }
    }


    private class TurnoutTesterAction extends AbstractAction {


        public TurnoutTesterAction() {
            super("Turnout tester\u2026");
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            new TurnoutTester(ctx.getMainFrame(), ctx);
        }
    }

}
