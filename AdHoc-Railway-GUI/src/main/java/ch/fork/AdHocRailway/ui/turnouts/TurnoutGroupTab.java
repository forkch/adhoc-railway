package ch.fork.AdHocRailway.ui.turnouts;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.ui.context.TurnoutContext;
import ch.fork.AdHocRailway.ui.turnouts.configuration.TurnoutHelper;
import ch.fork.AdHocRailway.ui.widgets.WidgetTab;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;


public class TurnoutGroupTab extends WidgetTab {

    private final TurnoutGroup turnoutGroup;
    private final Map<Turnout, TurnoutWidget> turnoutToTurnoutWidget = new HashMap<Turnout, TurnoutWidget>();
    private final TurnoutContext ctx;

    public TurnoutGroupTab(final TurnoutContext ctx,
                           final TurnoutGroup turnoutGroup) {
        this.ctx = ctx;
        this.turnoutGroup = turnoutGroup;

        initTab();

        addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(final MouseEvent e) {
                if (e.getClickCount() == 2
                        && e.getButton() == MouseEvent.BUTTON1) {

                    if (ctx.isEditingMode()) {
                        TurnoutHelper.addNewTurnoutDialog(ctx, turnoutGroup);
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
        final TurnoutWidget turnoutWidget = new TurnoutWidget(ctx, turnout,
                false);
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

    public void revalidateTurnouts() {
        for (final TurnoutWidget widget : turnoutToTurnoutWidget.values()) {
            widget.revalidateTurnout();
        }
    }

}
