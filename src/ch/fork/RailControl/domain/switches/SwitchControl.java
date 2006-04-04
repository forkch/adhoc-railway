/*------------------------------------------------------------------------
 * 
 * <SwitchControl.java>  -  <Provides control over a switch>
 * 
 * begin     : j Tue Jan  3 21:25:16 CET 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : bm@fork.ch
 * language  : java
 * version   : $Id$
 * 
 *----------------------------------------------------------------------*/

/*------------------------------------------------------------------------
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 *----------------------------------------------------------------------*/

package ch.fork.RailControl.domain.switches;

import java.util.*;

public class SwitchControl {

    private static SwitchControl instance;
    private Map<String, Switch> nameToSwitches;
    private List<Map> busses;
    private Map<Integer, Switch> addressToSwitches;
    private List<SwitchChangedListener> listeners;

    private SwitchControl() {
        addressToSwitches = new HashMap<Integer, Switch>();
        listeners = new ArrayList<SwitchChangedListener>();
    }

    public static SwitchControl getInstance() {
        if(instance == null) {
            return new SwitchControl();
        } else {
            return instance;
        }
    }

    public void toggle(String name) throws SwitchException {
    }

    public void registerSwitch(Switch newSwitch) {
    }

    public Switch getSwitch(String name) {
    }
}
