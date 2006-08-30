package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchState;


public class RouteItem {

    private Switch routedSwitch;
    private SwitchState routedSwitchState;
    private SwitchState previousSwitchState;
    
    public RouteItem(Switch routedSwitch, SwitchState switchState) {
        this.routedSwitch = routedSwitch;
        this.routedSwitchState = switchState;
    }

    public Switch getRoutedSwitch() {
        return routedSwitch;
    }

    public SwitchState getSwitchState() {
        return routedSwitchState;
    }

    public SwitchState getPreviousSwitchState() {
        return previousSwitchState;
    }

    public void setPreviousSwitchState(SwitchState previousSwitchState) {
        this.previousSwitchState = previousSwitchState;
    }

    public SwitchState getRoutedSwitchState() {
        return routedSwitchState;
    }

    public void setRoutedSwitchState(SwitchState routedSwitchState) {
        this.routedSwitchState = routedSwitchState;
    }
    
    public Object clone() {
        RouteItem newItem = new RouteItem(routedSwitch, routedSwitchState);
        return newItem;
    }
    public String toString() {
        return getRoutedSwitch().getNumber() + " : "
        + getRoutedSwitchState();
    }

}
