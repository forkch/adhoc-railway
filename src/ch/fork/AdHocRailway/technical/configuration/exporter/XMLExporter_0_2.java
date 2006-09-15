/*------------------------------------------------------------------------
 * 
 * <./domain/configuration/exporter/XMLExporter_0_2.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:28 BST 2006
 * copyright : (C) by Benjamin Mueller 
 * email     : news@fork.ch
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


package ch.fork.AdHocRailway.technical.configuration.exporter;

import java.util.Map;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.technical.configuration.Preferences;

public class XMLExporter_0_2 {

    private static StringBuffer sb;

    public static String export() {
        sb = new StringBuffer();
        sb.append("<?xml version=\"1.0\"?>\n");
        sb.append("<RailControl xmlns=\"http://www.fork.ch/RailControl\" "
            + "ExporterVersion=\"0.2\">\n");

        exportSwitchConfiguration();
        exportLocomotiveConfiguration();
        exportPreferences();

        sb.append("</RailControl>");
        return sb.toString();
    }

    private static void exportSwitchConfiguration() {
        sb.append("<SwitchConfiguration>\n");
        for (SwitchGroup sg : SwitchControl.getInstance().getSwitchGroups()) {

            sb.append("<SwitchGroup name=\"" + sg.getName() + "\">\n");
            exportSwitches(sg);
            sb.append("</SwitchGroup>\n");
        }
        sb.append("</SwitchConfiguration>\n");
    }

    private static void exportSwitches(SwitchGroup sg) {
        for (Switch s : sg.getSwitches()) {
            sb.append("<Switch ");
            sb.append(" desc=\"" + s.getDesc() + "\" ");
            sb.append(" number=\"" + s.getNumber() + "\" ");
            sb.append(" type=\"" + s.getType() + "\" ");
            sb.append(" defaultstate=\"" + s.getDefaultState() + "\" ");
            sb.append(" orientation=\"" + s.getSwitchOrientation() + "\" >\n");
            for (Address address : s.getAddresses()) {
                sb.append("<Address bus=\"" + address.getBus()
                    + "\" address=\"" + address.getAddress() + "\" switched=\""
                    + address.isAddressSwitched() + "\" />\n");
            }
            sb.append("</Switch>\n");
        }
    }

    private static void exportLocomotiveConfiguration() {
        sb.append("<LocomotiveConfiguration>\n");
        for (LocomotiveGroup lg : LocomotiveControl.getInstance()
            .getLocomotiveGroups()) {

            sb.append("<LocomotiveGroup name=\"" + lg.getName() + "\">\n");
            for (Locomotive l : lg.getLocomotives()) {

                sb.append("<Locomotive name=\"" + l.getName() + "\" type=\""
                    + l.getType() + "\" bus=\"" + l.getAddress().getBus()
                    + "\" address=\"" + l.getAddress().getAddress()
                    + "\" desc=\"" + l.getDesc() + "\" />\n");
            }
            sb.append("</LocomotiveGroup>\n");

        }
        sb.append("</LocomotiveConfiguration>\n");
    }

    private static void exportPreferences() {
        sb.append("<GuiConfiguration>\n");
        Map<String, String> preferences = Preferences.getInstance()
            .getPreferences();
        for (String key : preferences.keySet()) {
            sb.append("<GuiConfigParameter ");
            sb.append(" name=\"" + key + "\"");
            sb.append(" value=\"" + preferences.get(key) + "\"");
            sb.append("/>\n");
        }
        sb.append("</GuiConfiguration>\n");
    }
}
