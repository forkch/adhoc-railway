package ch.fork.AdHocRailway.ui.switches.configuration;

import java.util.List;
import java.util.Map;

import com.sun.java.util.jar.pack.Instruction.Switch;

public class TurnoutConfiguration {

	private List<SwitchGroup> switchGroups;

	private Map<Integer, Switch> switchNumberToSwitch;

	public TurnoutConfiguration(List<SwitchGroup> switchGroups,
			Map<Integer, Switch> switchNumberToSwitch) {
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
