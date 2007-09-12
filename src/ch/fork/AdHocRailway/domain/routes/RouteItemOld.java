package ch.fork.AdHocRailway.domain.routes;

import ch.fork.AdHocRailway.domain.turnouts.TurnoutControl;

import com.sun.java.util.jar.pack.Instruction.Switch;

public class RouteItemOld implements Comparable {

    private int         routedSwitchNumber;
    private SwitchState routedSwitchState;
    private SwitchState previousSwitchState;

    public RouteItemOld(int routedSwitchNumber) {
        this(routedSwitchNumber, SwitchState.STRAIGHT);
    }
    public RouteItemOld(int routedSwitchNumber, SwitchState switchState) {
        this.routedSwitchNumber = routedSwitchNumber;
        this.routedSwitchState = switchState;
    }

    public Switch getRoutedSwitch() {
        return TurnoutControl.getInstance().getNumberToSwitch().get(
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
        RouteItemOld newItem =
            new RouteItemOld(routedSwitchNumber, routedSwitchState);
        return newItem;
    }
    public String toString() {
        return routedSwitchNumber + " : " + getRoutedSwitchState();
    }
    public int compareTo(Object o) {
        if (o instanceof RouteItemOld) {
            RouteItemOld routeItem = (RouteItemOld) o;
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
