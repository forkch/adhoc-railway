package ch.fork.AdHocRailway.ui.turnouts;

import static ch.fork.AdHocRailway.ui.ImageTools.createImageIconFromIconSet;

import java.awt.event.ActionEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JTabbedPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import ch.fork.AdHocRailway.domain.routes.RouteManager;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutControlIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerException;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManagerListener;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;
import ch.fork.AdHocRailway.ui.SmallToolbarButton;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutConfig;

public class TurnoutGroupsPanel extends JTabbedPane implements
		TurnoutManagerListener {
	private final Map<Integer, TurnoutGroup> indexToTurnoutGroup = new HashMap<Integer, TurnoutGroup>();

	private final Map<TurnoutGroup, TurnoutGroupTab> turnoutGroupToTurnoutGroupTab = new HashMap<TurnoutGroup, TurnoutGroupTab>();

	private final TurnoutManager turnoutPersistence = AdHocRailway
			.getInstance().getTurnoutPersistence();

	public TurnoutGroupsPanel(int tabPlacement) {
		super(tabPlacement);
		turnoutPersistence.addTurnoutManagerListener(this);

		initToolBar();
		initMenuBar();
	}

	private void updateTurnouts(List<TurnoutGroup> turnoutGroups) {
		indexToTurnoutGroup.clear();

		int i = 1;
		TurnoutControlIface turnoutControl = AdHocRailway.getInstance()
				.getTurnoutControl();

		turnoutControl.removeAllTurnoutChangeListener();

		for (TurnoutGroup turnoutGroup : turnoutGroups) {
			indexToTurnoutGroup.put(i - 1, turnoutGroup);
			addTurnoutGroup(i, turnoutGroup);
			i++;
		}
	}

	public void addTurnoutGroup(int groupNumber, TurnoutGroup turnoutGroup) {
		TurnoutGroupTab turnoutGroupTab = new TurnoutGroupTab(turnoutGroup);

		add(turnoutGroupTab, "F" + groupNumber + ": " + turnoutGroup.getName());
		turnoutGroupToTurnoutGroupTab.put(turnoutGroup, turnoutGroupTab);

	}

	private class TurnoutsStraightAction extends AbstractAction {

		public TurnoutsStraightAction() {
			super("Set all turnouts straight\u2026",
					createImageIconFromIconSet("switch.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			TurnoutStraighter s = new TurnoutStraighter();
			s.start();
		}

		private class TurnoutStraighter extends Thread {

			@Override
			public void run() {
				try {
					TurnoutManager turnoutPersistence = AdHocRailway
							.getInstance().getTurnoutPersistence();
					TurnoutControlIface turnoutControl = AdHocRailway
							.getInstance().getTurnoutControl();
					int delay = Preferences.getInstance().getIntValue(
							PreferencesKeys.ROUTING_DELAY);
					for (Turnout t : turnoutPersistence.getAllTurnouts()) {
						turnoutControl.setDefaultState(t);
						Thread.sleep(3 * delay);
					}
				} catch (TurnoutException e1) {
					ExceptionProcessor.getInstance().processException(e1);
					return;
				} catch (InterruptedException e2) {
					e2.printStackTrace();
				}
			}
		}
	}

	private class EnlargeTurnoutGroups extends AbstractAction {
		public EnlargeTurnoutGroups() {
			super("Rearranging Turnout and Route numbers (enlarge groups)");
		}

		@Override
		public void actionPerformed(ActionEvent arg0) {
			int result = JOptionPane
					.showConfirmDialog(
							AdHocRailway.getInstance(),
							"The numbering of the Turnout- and Route-Groups is now being adjusted\n"
									+ "such that each group has at least 5 free slots for the turnouts\n"
									+ "(each group has a multiple of 10 turnouts)\n\n"
									+ "Do you want to proceed ?",
							"Rearranging Turnout and Route numbers",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE,
							createImageIconFromIconSet("messagebox_info.png"));
			if (result == JOptionPane.OK_OPTION) {
				TurnoutManager turnoutPersistence = AdHocRailway.getInstance()
						.getTurnoutPersistence();
				RouteManager routePersistence = AdHocRailway.getInstance()
						.getRoutePersistence();

				turnoutPersistence.enlargeTurnoutGroups();
				routePersistence.enlargeRouteGroups();
				AdHocRailway.getInstance().updateGUI();
			}
		}
	}

	private class AddTurnoutsAction extends AbstractAction {
		public AddTurnoutsAction() {
			super("Add Turnouts\u2026",
					createImageIconFromIconSet("filenew.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if (indexToTurnoutGroup.isEmpty()) {
				JOptionPane.showMessageDialog(AdHocRailway.getInstance(),
						"Please configure a group first", "Add Turnouts",
						JOptionPane.INFORMATION_MESSAGE,
						createImageIconFromIconSet("messagebox_info.png"));
				return;
			}
			int selectedGroupPane = getSelectedIndex();

			TurnoutGroup selectedTurnoutGroup = indexToTurnoutGroup
					.get(selectedGroupPane);
			int nextNumber = 0;
			TurnoutManager turnoutPersistence = AdHocRailway.getInstance()
					.getTurnoutPersistence();
			if (Preferences.getInstance().getBooleanValue(
					PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
				nextNumber = turnoutPersistence
						.getNextFreeTurnoutNumberOfGroup(selectedTurnoutGroup);
				if (nextNumber == -1) {
					JOptionPane.showMessageDialog(AdHocRailway.getInstance(),
							"No more free numbers in this group", "Error",
							JOptionPane.ERROR_MESSAGE);
					AdHocRailway.getInstance().updateGUI();
					setSelectedIndex(selectedGroupPane);
					return;
				}
			} else {
				nextNumber = turnoutPersistence.getNextFreeTurnoutNumber();
			}

			Turnout newTurnout = new Turnout();
			newTurnout.setNumber(nextNumber);

			newTurnout.setBus1(Preferences.getInstance().getIntValue(
					PreferencesKeys.DEFAULT_TURNOUT_BUS));
			newTurnout.setBus2(Preferences.getInstance().getIntValue(
					PreferencesKeys.DEFAULT_TURNOUT_BUS));

			newTurnout.setTurnoutGroup(selectedTurnoutGroup);
			newTurnout.setDefaultState(TurnoutState.STRAIGHT);
			newTurnout.setOrientation(TurnoutOrientation.EAST);
			newTurnout.setTurnoutType(TurnoutType.DEFAULT);

			new TurnoutConfig(AdHocRailway.getInstance(), newTurnout);
			AdHocRailway.getInstance().updateGUI();
			setSelectedIndex(selectedGroupPane);
		}
	}

	private class TurnoutProgrammerAction extends AbstractAction {

		public TurnoutProgrammerAction() {
			super("Turnout Decoder Programmer\u2026",
					createImageIconFromIconSet("switch_programmer.png"));
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new SwitchProgrammer(AdHocRailway.getInstance(), AdHocRailway
					.getInstance().getSession());
		}
	}

	private class TurnoutWarmerAction extends AbstractAction {

		public TurnoutWarmerAction() {
			super("Turnout Warmer\u2026");
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			new TurnoutWarmer(AdHocRailway.getInstance(), AdHocRailway
					.getInstance().getSession());
		}
	}

	private void initToolBar() {
		/* Turnout Tools */
		JToolBar turnoutToolsToolBar = new JToolBar();
		JButton addTurnoutsButton = new SmallToolbarButton(
				new AddTurnoutsAction());
		JButton setAllSwitchesStraightButton = new SmallToolbarButton(
				new TurnoutsStraightAction());
		JButton switchProgrammerButton = new SmallToolbarButton(
				new TurnoutProgrammerAction());

		turnoutToolsToolBar.add(addTurnoutsButton);
		turnoutToolsToolBar.add(setAllSwitchesStraightButton);
		turnoutToolsToolBar.add(switchProgrammerButton);

		AdHocRailway.getInstance().addToolBar(turnoutToolsToolBar);
	}

	private void initMenuBar() {
		/* TOOLS */
		JMenu toolsMenu = new JMenu("Tools");
		JMenuItem addTurnoutsItem = new JMenuItem(new AddTurnoutsAction());
		JMenuItem turnoutsStraightItem = new JMenuItem(
				new TurnoutsStraightAction());
		JMenuItem turnoutsWarmerItem = new JMenuItem(new TurnoutWarmerAction());
		JMenuItem turnoutsProgrammerItem = new JMenuItem(
				new TurnoutProgrammerAction());

		JMenuItem enlargeTurnoutGroupsItem = new JMenuItem(
				new EnlargeTurnoutGroups());

		toolsMenu.add(addTurnoutsItem);
		toolsMenu.add(turnoutsStraightItem);
		toolsMenu.add(turnoutsWarmerItem);
		toolsMenu.add(turnoutsProgrammerItem);
		toolsMenu.addSeparator();
		toolsMenu.add(enlargeTurnoutGroupsItem);

		AdHocRailway.getInstance().addMenu(toolsMenu);
	}

	@Override
	public void turnoutsUpdated(final List<TurnoutGroup> turnoutGroups) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				updateTurnouts(turnoutGroups);
			}
		});
		revalidate();
		repaint();
	}

	@Override
	public void turnoutUpdated(final Turnout turnout) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.updateTurnout(turnout);
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

				TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.removeTurnout(turnout);

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
				TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(turnout.getTurnoutGroup());
				turnoutGroupTab.addTurnout(turnout);
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
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void turnoutGroupRemoved(final TurnoutGroup group) {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
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
				TurnoutGroupTab turnoutGroupTab = turnoutGroupToTurnoutGroupTab
						.get(group);
				turnoutGroupTab.updateTurnoutGroup(group);
				revalidate();
				repaint();
			}
		});
	}

	@Override
	public void failure(TurnoutManagerException arg0) {

	}
}
