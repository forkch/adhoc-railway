
package ch.fork.AdHocRailway.ui.switches;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.ArrayList;

import javax.swing.JPanel;

import ch.fork.AdHocRailway.domain.configuration.Preferences;
import ch.fork.AdHocRailway.domain.configuration.PreferencesKeys;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;

public class SwitchGroupTab extends JPanel {
    private static final long  serialVersionUID = 1L;
    private ArrayList          switchWidgets;
    private int                maxCols;
    private int                currentRow;
    private int                currentCol;
    private GridBagLayout      layout;
    private GridBagConstraints gbc;
    private SwitchGroup        switchGroup;

    public SwitchGroupTab(SwitchGroup sg) {
        this.switchWidgets = new ArrayList();
        switchGroup = sg;
        layout = new GridBagLayout();
        setLayout(layout);
        maxCols = Preferences.getInstance()
            .getIntValue(PreferencesKeys.SWITCH_CONTROLES);
        currentRow = 0;
        currentCol = 0;
        gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridx = currentRow;
        gbc.gridy = currentCol;
    }

    @SuppressWarnings("unchecked")
    public void addSwitchWidget(SwitchWidget aSwitchWidget) {
        add(aSwitchWidget);
        switchWidgets.add(aSwitchWidget);
        if (currentCol == maxCols) {
            currentRow++;
            currentCol = 0;
        }
        gbc.gridx = currentCol;
        gbc.gridy = currentRow;
        layout.setConstraints(aSwitchWidget, gbc);
        currentCol++;
        SwitchControl.getInstance().addSwitchChangeListener(aSwitchWidget);
    }

    public SwitchGroup getSwitchGroup() {
        return switchGroup;
    }
}
