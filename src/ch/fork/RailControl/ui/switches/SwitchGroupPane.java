package ch.fork.RailControl.ui.switches;

import java.util.List;
import java.util.Map;

import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;

import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;

public class SwitchGroupPane extends JTabbedPane {

    private Map<Integer, Switch> switchNumberToSwitch;

    private List<SwitchGroup> switchGroups;

    public SwitchGroupPane(Map<Integer, Switch> switchNumberToSwitch) {
        super(JTabbedPane.BOTTOM);
        this.switchNumberToSwitch = switchNumberToSwitch;
    }

    public void update(List<SwitchGroup> switchGroups) {
        this.switchGroups = switchGroups;
        this.removeAll();
        for (SwitchGroup switchGroup : switchGroups) {
            SwitchGroupTab switchGroupTab = new SwitchGroupTab(switchGroup);
            JScrollPane switchGroupPane = new JScrollPane(switchGroupTab);
            add(switchGroupPane, switchGroup.getName());

            for (Switch aSwitch : switchGroup.getSwitches()) {
                SwitchWidget switchWidget = new SwitchWidget(aSwitch,
                    switchGroup, switchNumberToSwitch);
                switchGroupTab.addSwitchWidget(switchWidget);
            }
        }
        revalidate();
        repaint();
    }
}
