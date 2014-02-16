package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.controllers.TurnoutController;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManagerListener;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.EditingModeListener;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;
import ch.fork.AdHocRailway.ui.widgets.SmallToolbarButton;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import static ch.fork.AdHocRailway.ui.tools.ImageTools.createImageIconFromIconSet;

public class TurnoutGroupsPanel extends JTabbedPane implements
		TurnoutManagerListener, EditingModeListener {
	private static final long serialVersionUID = 4422288695074160221L;

	private final Map<Integer, TurnoutGroup> indexToTurnoutGroup = new HashMap<Integer, TurnoutGroup>();

	private final Map<TurnoutGroup, TurnoutGroupTab> turnoutGroupToTurnoutGroupTab = new HashMap<TurnoutGroup, TurnoutGroupTab>();

	private final TurnoutManager turnoutPersistence;

	private JMenuItem addTurnoutsItem;

	private JMenuItem turnoutsProgrammerItem;

	private JButton addTurnoutsButton;

	private JButton turnoutProgrammerButton;

	private final TurnoutContext ctx;

	public TurnoutGroupsPanel(final TurnoutContext turnoutCtx,
			final int tabPlacement) {
		super(tabPlacement);
		this.ctx = turnoutCtx;
		turnoutPersistence = turnoutCtx.getTurnoutManager();
		turnoutPersistence.addTurnoutManagerListener(this);

		initToolBar();
		initMenuBar();
		initActionListeners();
		ctx.getMainApp().addEditingModeListener(this);
	}

	private void initActionListeners() {
	}

	private void updateTurnouts(final SortedSet<TurnoutGroup> turnoutGroups) {
		indexToTurnoutGroup.clear();
		removeAll();
		revalidate();
		repaint();
		turnoutGroupToTurnoutGroupTab.clear();
		int i = 1;

		final TurnoutController turnoutControl = ctx.getTurnoutControl();

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

	private class TurnoutsStraightAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -7388480296763893134L;

		public TurnoutsStraightAction() {
			super("Set all turnouts straight\u2026",
					createImageIconFromIconSet("switch.png"));
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
					final int delay = Preferences.getInstance().getIntValue(
							PreferencesKeys.ROUTING_DELAY);
					for (final Turnout t : turnoutPersistence.getAllTurnouts()) {
						turnoutControl.setDefaultState(t);
						Thread.sleep(delay);
					}
				} catch (final InterruptedException e1) {
					ctx.getMainApp().handleException(e1);
				}
			}
		}
	}

	private class AddTurnoutsAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 8397984042487108076L;

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
						createImageIconFromIconSet("dialog-information.png"));
				return;
			}
			final int selectedGroupPane = getSelectedIndex();

			final TurnoutGroup selectedTurnoutGroup = indexToTurnoutGroup
					.get(selectedGroupPane);

			TurnoutHelper.addNewTurnoutDialog(ctx, selectedTurnoutGroup);
		}
	}

	private class TurnoutProgrammerAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = 4307628017748686166L;

		public TurnoutProgrammerAction() {
			super("Turnout Decoder Programmer\u2026",
					createImageIconFromIconSet("switch_programmer.png"));
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			new TurnoutProgrammer(ctx.getMainFrame(), ctx);
		}
	}

	private class TurnoutWarmerAction extends AbstractAction {

		/**
		 * 
		 */
		private static final long serialVersionUID = -4069163470504661773L;

		public TurnoutWarmerAction() {
			super("Turnout Warmer\u2026");
		}

		@Override
		public void actionPerformed(final ActionEvent e) {
			new TurnoutWarmer(ctx.getMainFrame(), ctx);
		}
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
				new TurnoutWarmerAction());
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
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateTurnouts(turnoutGroups);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void turnoutUpdated(final Turnout turnout) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.updateTurnout(turnout);
				revalidateAllTurnouts();
				revalidate();
				repaint();

			}

		});

	}

	@Override
	public void turnoutRemoved(final Turnout turnout) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.removeTurnout(turnout);

				revalidateAllTurnouts();
				revalidate();
				repaint();
			}
		});

	}

	@Override
	public void turnoutAdded(final Turnout turnout) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.addTurnout(turnout);

				revalidateAllTurnouts();
				revalidate();
				repaint();

			}
		});

	}

	@Override
	public void turnoutGroupAdded(final TurnoutGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				addTurnoutGroup(-1, group);

			}
		});
	}

	@Override
	public void turnoutGroupRemoved(final TurnoutGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(group);
				remove(turnoutGroupTab);
				revalidate();
				repaint();
			}
		});

	}

	@Override
	public void turnoutGroupUpdated(final TurnoutGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				final TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(group);
				turnoutGroupTab.updateTurnoutGroup(group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void failure(final TurnoutManagerException arg0) {

	}

	@Override
	public void editingModeChanged(final boolean editing) {
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
}
