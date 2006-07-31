package ch.fork.AdHocRailway.domain.locomotives;

import java.util.SortedSet;
import java.util.TreeSet;

public class LocomotiveGroup implements Comparable {

    private String name;

    private SortedSet<Locomotive> locomotives;

    public LocomotiveGroup(String name) {
        this.name = name;
        locomotives = new TreeSet<Locomotive>();
    }

    public void addLocomotive(Locomotive locomotive) {
        locomotives.add(locomotive);
    }

    public void removeLocomotive(Locomotive locomotive) {
        locomotives.remove(locomotive);
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

    public SortedSet<Locomotive> getLocomotives() {
        return locomotives;
    }

    public LocomotiveGroup clone() {
        LocomotiveGroup newLocomotiveGroup = new LocomotiveGroup(name);
        return newLocomotiveGroup;
    }
    
    
    public String toXML() {
        StringBuffer sb = new StringBuffer();
        sb.append("<LocomotiveGroup name=\""
            + name + "\">\n");
        for (Locomotive l : locomotives) {
            sb.append(l.toXML());
        }
        sb.append("</LocomotiveGroup>\n");
        return sb.toString();
    }

    public int compareTo(Object o) {
        if (o instanceof LocomotiveGroup) {
            LocomotiveGroup anotherLocomotiveGroup = (LocomotiveGroup) o;
            return (name.compareTo(anotherLocomotiveGroup.getName()));
        }
        return 0;
    }
}
