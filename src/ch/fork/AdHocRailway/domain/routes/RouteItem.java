package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchState;

public class RouteItem implements Comparable {

    private int         routedSwitchNumber;
    private SwitchState routedSwitchState;
    private SwitchState previousSwitchState;

    public RouteItem(int routedSwitchNumber) {
        this(routedSwitchNumber, SwitchState.STRAIGHT);
    }
    public RouteItem(int routedSwitchNumber, SwitchState switchState) {
        this.routedSwitchNumber = routedSwitchNumber;
        this.routedSwitchState = switchState;
    }

    public Switch getRoutedSwitch() {
        return SwitchControl.getInstance().getNumberToSwitch().get(
            routedSwitchNumber);
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
        RouteItem newItem =
            new RouteItem(routedSwitchNumber, routedSwitchState);
        return newItem;
    }
    public String toString() {
        return routedSwitchNumber + " : " + getRoutedSwitchState();
    }
    public int compareTo(Object o) {
        if (o instanceof RouteItem) {
            RouteItem routeItem = (RouteItem) o;
            if (routedSwitchNumber < routeItem.routedSwitchNumber)
                return -1;
            else if (routedSwitchNumber > routeItem.routedSwitchNumber)
                return 1;

            return 0;
        }
        return -1;
    }

    public int getRoutedSwitchNumber() {
        return routedSwitchNumber;
    }

}
