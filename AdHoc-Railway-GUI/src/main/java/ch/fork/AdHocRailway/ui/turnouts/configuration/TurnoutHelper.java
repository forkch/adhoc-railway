package ch.fork.AdHocRailway.ui.turnouts.configuration;

import ch.fork.AdHocRailway.domain.turnouts.*;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.technical.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.ui.UIConstants;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import de.dermoba.srcp.model.turnouts.MMTurnout;
import org.apache.commons.beanutils.BeanUtils;

import javax.swing.*;
import java.util.List;

public class TurnoutHelper {

    public static void addNewTurnoutDialog(final TurnoutContext ctx,
                                           final TurnoutGroup selectedTurnoutGroup) {
        int nextNumber = 0;
        final TurnoutManager turnoutManager = ctx.getTurnoutManager();
        nextNumber = turnoutManager.getNextFreeTurnoutNumber();

        final Turnout newTurnout = TurnoutHelper.createDefaultTurnout(
                turnoutManager, nextNumber);

        new TurnoutConfig(ctx.getMainFrame(), ctx, newTurnout,
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
        newTurnout.setType(TurnoutType.DEFAULT_LEFT);
        return newTurnout;
    }

    public static void validateTurnout(final TurnoutManager turnoutPersistence,
                                       final Turnout turnout, final JPanel panel) {
        if (isBusValid(turnout.getBus1())) {
            panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
        } else {
            panel.setBackground(UIConstants.ERROR_COLOR);
        }
        if (isAddressValid(turnout.getAddress1())) {
            panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
        } else {
            panel.setBackground(UIConstants.ERROR_COLOR);
        }
        if (isBusAddressUnique(turnout.getBus1(), turnout.getAddress1(),
                turnout, turnoutPersistence.getAllTurnouts())) {
            panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
        } else {
            panel.setBackground(UIConstants.WARN_COLOR);
        }

        if (turnout.isThreeWay()) {
            if (isBusAddressUnique(turnout.getBus2(), turnout.getAddress2(),
                    turnout, turnoutPersistence.getAllTurnouts())) {
                panel.setBackground(UIConstants.DEFAULT_PANEL_COLOR);
            } else {
                panel.setBackground(UIConstants.WARN_COLOR);
            }
        }
    }

    public static boolean isNumberValid(final Turnout tempTurnout,
                                        final Turnout currentTurnout, final TurnoutManager turnoutManager) {
        if (tempTurnout.getNumber() == 0) {
            return false;
        }
        if (!turnoutManager.isTurnoutNumberFree(tempTurnout.getNumber())
                && tempTurnout.getNumber() != currentTurnout.getNumber()) {
            return false;
        }
        return true;
    }

    public static boolean isAddressValid(final int address) {
        return address != 0 || address > MMTurnout.MAX_MM_TURNOUT_ADDRESS;
    }

    public static boolean isBusValid(final int bus) {
        return bus != 0;
    }

    public static boolean isBusAddressUnique(final int bus1,
                                             final int address1, final Turnout turnoutToValidate,
                                             final List<Turnout> allTurnouts) {
        if (isBusValid(bus1) && isAddressValid(address1)) {

            boolean unique1 = true;
            for (final Turnout t : allTurnouts) {
                if (t.equals(turnoutToValidate)) {
                    continue;
                }
                if ((t.getBus1() == bus1 && t.getAddress1() == address1)
                        || (t.getBus1() == bus1 && t.getAddress2() == address1)) {
                    unique1 = false;
                }
            }
            return unique1;
        }
        return false;
    }

    public static boolean isTurnoutReadyToTest(final Turnout turnout) {
        if (!isAddressValid(turnout.getAddress1())
                || !isBusValid(turnout.getBus1())) {
            return false;
        }
        if (turnout.isThreeWay()) {
            if (!isAddressValid(turnout.getAddress2())
                    || !isBusValid(turnout.getBus2())) {
                return false;
            }
        }

        return true;
    }

    public static Turnout copyTurnout(final Turnout old) {
        final Turnout t = new Turnout(old);
        return t;
    }

    public static void update(final Turnout testTurnout, final String property,
                              final Object newValue) {
        try {
            BeanUtils.setProperty(testTurnout, property, newValue);
        } catch (Exception e) {
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

        addTableRow("Type:", turnout.getType().toString(), description);
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
