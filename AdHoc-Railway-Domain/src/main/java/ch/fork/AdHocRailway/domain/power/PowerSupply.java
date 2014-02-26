package ch.fork.AdHocRailway.domain.power;

import java.util.ArrayList;
import java.util.List;

public class PowerSupply {

    private final int bus;
    private List<Booster> boosters = new ArrayList<Booster>();

    public PowerSupply(final int bus) {
        this.bus = bus;
        for (int i = 0; i < 8; i++) {
            final Booster booster = new Booster(this, i);
            boosters.add(booster);
        }
    }

    public int getBus() {
        return bus;
    }

    public List<Booster> getBoosters() {
        return boosters;
    }

    public void setBoosters(final List<Booster> boosters) {
        this.boosters = boosters;
    }

    public Booster getBooster(final int idx) {
        return boosters.get(idx);
    }

}
