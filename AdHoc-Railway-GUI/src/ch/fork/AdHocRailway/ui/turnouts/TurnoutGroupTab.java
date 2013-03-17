package ch.fork.AdHocRailway.ui.turnouts;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.ui.WidgetTab;

public class TurnoutGroupTab extends WidgetTab {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1877319981355821341L;
	private final TurnoutGroup turnoutGroup;
	private final Map<Turnout, TurnoutWidget> turnoutToTurnoutWidget = new HashMap<Turnout, TurnoutWidget>();

	public TurnoutGroupTab(TurnoutGroup turnoutGroup) {
		this.turnoutGroup = turnoutGroup;

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
		for (Turnout turnout : turnoutGroup.getTurnouts()) {
			addTurnout(turnout);
		}
	}

	public void addTurnout(Turnout turnout) {
		TurnoutWidget turnoutWidget = new TurnoutWidget(turnout);
		addWidget(turnoutWidget);
		turnoutToTurnoutWidget.put(turnout, turnoutWidget);
	}

	public void removeTurnout(Turnout turnout) {
		remove(turnoutToTurnoutWidget.get(turnout));
	}

	public void updateTurnout(Turnout turnout) {
		turnoutToTurnoutWidget.get(turnout).setTurnout(turnout);

	}

	public void updateTurnoutGroup(TurnoutGroup group) {

	}

}
