package ch.fork.AdHocRailway.ui.turnouts.configuration;

import java.lang.reflect.InvocationTargetException;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import org.apache.commons.beanutils.BeanUtils;

import ch.fork.AdHocRailway.domain.TurnoutContext;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutManager;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutOrientation;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutState;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.UIConstants;
import de.dermoba.srcp.model.turnouts.MMTurnout;

public class TurnoutHelper {

	public static void addNewTurnoutDialog(final TurnoutContext ctx,
			final TurnoutGroup selectedTurnoutGroup) {
		int nextNumber = 0;
		final TurnoutManager turnoutManager = ctx.getTurnoutManager();
		if (Preferences.getInstance().getBooleanValue(
				PreferencesKeys.USE_FIXED_TURNOUT_AND_ROUTE_GROUP_SIZES)) {
			nextNumber = turnoutManager
					.getNextFreeTurnoutNumberOfGroup(selectedTurnoutGroup);
			if (nextNumber == -1) {
				JOptionPane.showMessageDialog(AdHocRailway.getInstance(),
						"No more free numbers in this group", "Error",
						JOptionPane.ERROR_MESSAGE);
				return;
			}
		} else {
			nextNumber = turnoutManager.getNextFreeTurnoutNumber();
		}

		final Turnout newTurnout = TurnoutHelper.createDefaultTurnout(
				turnoutManager, nextNumber);

		new TurnoutConfig(AdHocRailway.getInstance(), ctx, newTurnout,
				selectedTurnoutGroup);
	}

	public static Turnout createDefaultTurnout(
			final TurnoutManager turnoutPersistence, final int nextNumber) {
		final Turnout newTurnout = new Turnout();
		newTurnout.setNumber(nextNumber);

		newTurnout.setBus1(Preferences.getInstance().getIntValue(
				PreferencesKeys.DEFAULT_TURNOUT_BUS));
		newTurnout.setBus2(Preferences.getInstance().getIntValue(
				PreferencesKeys.DEFAULT_TURNOUT_BUS));

		newTurnout
				.setAddress1(turnoutPersistence.getLastProgrammedAddress() + 1);
		newTurnout.setDefaultState(TurnoutState.STRAIGHT);
		newTurnout.setOrientation(TurnoutOrientation.EAST);
		newTurnout.setTurnoutType(TurnoutType.DEFAULT_LEFT);
		return newTurnout;
	}

	public static void validateTurnout(final TurnoutManager turnoutPersistence,
			final Turnout turnout, final JPanel panel) {
		boolean bus1Valid = true;
		if (turnout.getBus1() == 0) {
			panel.setBackground(UIConstants.ERROR_COLOR);
			bus1Valid = false;
		} else {
			panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
		}
		boolean address1Valid = true;
		if (turnout.getAddress1() == 0
				|| turnout.getAddress1() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
			panel.setBackground(UIConstants.ERROR_COLOR);
			address1Valid = false;

		} else {
			panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);

		}
		if (bus1Valid && address1Valid) {
			final int bus1 = turnout.getBus1();
			final int address1 = turnout.getAddress1();

			boolean unique1 = true;
			for (final Turnout t : turnoutPersistence.getAllTurnouts()) {
				if (t.equals(turnout)) {
					continue;
				}
				if ((t.getBus1() == bus1 && t.getAddress1() == address1)
						|| (t.getBus1() == bus1 && t.getAddress2() == address1)) {
					unique1 = false;
				}
			}
			if (!unique1) {
				panel.setBackground(UIConstants.WARN_COLOR);
			} else {
				panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
		}

		if (turnout.isThreeWay()) {
			boolean bus2Valid = true;
			if (turnout.getBus2() == 0) {
				panel.setBackground(UIConstants.ERROR_COLOR);
				bus2Valid = false;
			} else {
				panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
			boolean address2Valid = true;
			if (turnout.getAddress2() == 0
					|| turnout.getAddress2() > MMTurnout.MAX_MM_TURNOUT_ADDRESS) {
				panel.setBackground(UIConstants.ERROR_COLOR);
				address2Valid = false;
			} else {
				panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
			}
			if (bus2Valid && address2Valid) {
				final int bus2 = turnout.getBus2();
				final int address2 = turnout.getAddress2();
				boolean unique2 = true;
				for (final Turnout t : turnoutPersistence.getAllTurnouts()) {
					if (t.equals(turnout)) {
						continue;
					}
					if ((t.getBus1() == bus2 && t.getAddress1() == address2)
							|| (t.getBus2() == bus2 && t.getAddress2() == address2)) {
						unique2 = false;
					}
				}
				if (!unique2) {
					panel.setBackground(UIConstants.WARN_COLOR);
				} else {
					panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
				}
			}
		}
	}

	public static Turnout copyTurnout(final Turnout old) {
		final Turnout t = new Turnout();
		try {
			BeanUtils.copyProperties(t, old);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		return t;
	}

	public static void update(final Turnout testTurnout, final String property,
			final Object newValue) {
		try {
			BeanUtils.setProperty(testTurnout, property, newValue);
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
	}

	public static String getTurnoutDescription(final Turnout turnout) {
		if (turnout == null) {
			return "Please choose a turnout...";
		}
		final StringBuilder description = new StringBuilder();
		description.append("<html>");
		description.append("<h1>" + turnout.getNumber() + "</h1>");
		description.append("<table>");

		addTableRow("Type:", turnout.getTurnoutType().toString(), description);
		addTableRow("Default-State:", turnout.getDefaultState().toString(),
				description);
		addTableRow("Orientation:", turnout.getOrientation().toString(),
				description);

		addTableRow("Bus / Address 1:", "" + turnout.getBus1() + " / "
				+ turnout.getAddress1(), description);
		addTableRow("Address 1 Inverted:", "" + turnout.isAddress1Switched(),
				description);

		addTableRow("Bus / Address 2:", "" + turnout.getBus2() + " / "
				+ turnout.getAddress2(), description);
		addTableRow("Address 2 Inverted:", "" + turnout.isAddress2Switched(),
				description);
		description.append("</table>");
		description.append("</html>");
		return description.toString();
	}

	private static void addTableRow(final String key, final String value,
			final StringBuilder description) {
		description.append("<tr>");
		description.append("<td>");
		description.append(key);
		description.append("</td>");
		description.append("<td>");
		description.append(value);
		description.append("</td>");
		description.append("</tr>");
	}
}
