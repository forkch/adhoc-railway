
package ch.fork.AdHocRailway.domain.switches;

import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class SwitchGroup {
    private SortedSet<Switch> switches;
    private String            name;

    public SwitchGroup(String name) {
        this.name = name;
        switches = new TreeSet<Switch>();
    }

    public void addSwitch(Switch aSwitch) {
        switches.add(aSwitch);
    }

    public void removeSwitch(Switch aSwitch) {
        switches.remove(aSwitch);
    }

    public void replaceSwitch(Switch oldSwitch, Switch newSwitch) {
        switches.remove(oldSwitch);
        switches.add(newSwitch);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public Set<Switch> getSwitches() {
        return switches;
    }

    public SwitchGroup clone() {
        SwitchGroup newSwitchGroup = new SwitchGroup(name);
        return newSwitchGroup;
    }

    public boolean equals(Object o) {
        if (o instanceof SwitchGroup) {
            SwitchGroup sg = (SwitchGroup) o;
            return sg.getName().equals(name);
        }
        return false;
    }

    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<SwitchGroup name=\"" + name + "\">\n");
        for (Switch s : switches) {
            sb.append(s.toXML());
        }
        sb.append("</SwitchGroup>\n");
        return sb.toString();
    }
}
