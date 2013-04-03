package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.ui.AdHocRailway;
import ch.fork.AdHocRailway.ui.WidgetTab;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;

public class TurnoutGroupTab extends WidgetTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877319981355821341L;
	private final TurnoutGroup turnoutGroup;
	private final Map<Turnout, TurnoutWidget> turnoutToTurnoutWidget = new HashMap<Turnout, TurnoutWidget>();

	public TurnoutGroupTab(final TurnoutGroup turnoutGroup) {
		this.turnoutGroup = turnoutGroup;

		initTab();

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(final MouseEvent e) {
				if (e.getClickCount() == 2
						&& e.getButton() == MouseEvent.BUTTON1) {

					if (AdHocRailway.getInstance().isEditingMode()) {
						TurnoutHelper.addNewTurnoutDialog(turnoutGroup);
					}
				}
			}
		});

	}

	private void initTab() {
		for (final Turnout turnout : turnoutGroup.getTurnouts()) {
			addTurnout(turnout);
		}
	}

	public void addTurnout(final Turnout turnout) {
		final TurnoutWidget turnoutWidget = new TurnoutWidget(turnout, false);
		addWidget(turnoutWidget);
		turnoutToTurnoutWidget.put(turnout, turnoutWidget);
	}

	public void removeTurnout(final Turnout turnout) {
		remove(turnoutToTurnoutWidget.get(turnout));
	}

	public void updateTurnout(final Turnout turnout) {
		turnoutToTurnoutWidget.get(turnout).setTurnout(turnout);

	}

	public void updateTurnoutGroup(final TurnoutGroup group) {

	}

}
