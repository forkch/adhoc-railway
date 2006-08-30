package ch.fork.AdHocRailway.ui.locomotives.configuration;

import java.util.List;
import java.util.SortedSet;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;

public class LocomotiveConfiguration {
    private SortedSet<Locomotive> locomotives;
    private List<LocomotiveGroup> locomotiveGroups;
    
    public LocomotiveConfiguration(List<LocomotiveGroup> locomotiveGroups, SortedSet<Locomotive> locomotives) {
        this.locomotiveGroups = locomotiveGroups;
        this.locomotives = locomotives;
    }

    public List<LocomotiveGroup> getLocomotiveGroups() {
        return locomotiveGroups;
    }

    public SortedSet<Locomotive> getLocomotives() {
        return locomotives;
    }

}
