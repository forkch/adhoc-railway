package ch.fork.AdHocRailway.ui.switches.configuration;

import java.util.List;
import java.util.Map;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;

public class SwitchConfiguration {

    private List<SwitchGroup>          switchGroups;
    private Map<Integer, Switch>       switchNumberToSwitch;
    
    public SwitchConfiguration(List<SwitchGroup> switchGroups, Map<Integer, Switch> switchNumberToSwitch) {
        this.switchGroups = switchGroups;
        this.switchNumberToSwitch = switchNumberToSwitch;
    }

    public List<SwitchGroup> getSwitchGroups() {
        return switchGroups;
    }

    public Map<Integer, Switch> getSwitchNumberToSwitch() {
        return switchNumberToSwitch;
    }
}
