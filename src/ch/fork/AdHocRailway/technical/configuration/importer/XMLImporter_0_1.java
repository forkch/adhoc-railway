/*------------------------------------------------------------------------
 * 
 * <./domain/configuration/importer/XMLImporter_0_1.java>  -  <desc>
 * 
 * begin     : Wed Aug 23 16:58:19 BST 2006
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


package ch.fork.AdHocRailway.technical.configuration.importer;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.fork.AdHocRailway.domain.Address;
import ch.fork.AdHocRailway.domain.locomotives.DeltaLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.DigitalLocomotive;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveControl;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.NoneLocomotive;
import ch.fork.AdHocRailway.domain.switches.DefaultSwitch;
import ch.fork.AdHocRailway.domain.switches.DoubleCrossSwitch;
import ch.fork.AdHocRailway.domain.switches.Switch;
import ch.fork.AdHocRailway.domain.switches.SwitchControl;
import ch.fork.AdHocRailway.domain.switches.SwitchGroup;
import ch.fork.AdHocRailway.domain.switches.SwitchState;
import ch.fork.AdHocRailway.domain.switches.ThreeWaySwitch;
import ch.fork.AdHocRailway.domain.switches.Switch.SwitchOrientation;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class XMLImporter_0_1 extends DefaultHandler implements ContentHandler {
    private Preferences       preferences;
    private Switch            actualSwitch;
    private SwitchGroup       actualSwitchGroup;
    private Address[]         actualAddresses;
    private int               actualBus;
    private SwitchControl     switchControl;
    private Locomotive        actualLocomotive;
    private LocomotiveGroup   actualLocomotiveGroup;
    private LocomotiveControl locomotiveControl;

    public XMLImporter_0_1(String filename) {
        this.switchControl = SwitchControl.getInstance();
        this.preferences = Preferences.getInstance();
        this.locomotiveControl = LocomotiveControl.getInstance();
        switchControl.unregisterAllSwitchGroups();
        switchControl.unregisterAllSwitches();
        locomotiveControl = LocomotiveControl.getInstance();
        locomotiveControl.clear();
        parseDocument(filename);
    }

    private void parseDocument(String filename) {
        // get a factory
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            // get a new instance of parser
            SAXParser sp = spf.newSAXParser();
            // parse the file and also register this class for call backs
            sp.parse(filename, this);
        } catch (SAXException se) {
            ExceptionProcessor.getInstance().processException(
                "Error opening file", se);
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        } catch (IOException ie) {
            ie.printStackTrace();
        }
    }

    // Event Handlers
    public void startElement(String uri, String localName, String qName,
        Attributes attributes) throws SAXException {
        qName = qName.toLowerCase();
        if (qName.equals("switchgroup")) {
            actualSwitchGroup = new SwitchGroup(attributes.getValue("name"));
        } else if (qName.equals("switch")) {
            parseSwitch(qName, attributes);
        } else if (qName.equals("address")) {
            int address1 = Integer.parseInt(attributes.getValue("address1"));
            int address2 = Integer.parseInt(attributes.getValue("address2"));
            Address actualAddress1 = new Address(actualBus, address1);
            Address actualAddress2 = new Address(actualBus, address2);
            actualAddresses[0] = actualAddress1;
            if (actualAddresses.length == 2) {
                actualAddresses[1] = actualAddress2;
            }
        } else if (qName.equals("locomotivegroup")) {
            actualLocomotiveGroup = new LocomotiveGroup(attributes
                .getValue("name"));
        } else if (qName.equals("locomotive")) {
            parseLocomotive(qName, attributes);
        } else if (qName.equals("guiconfigparameter")) {
            parseGuiConfig(qName, attributes);
        }
    }

    private void parseSwitch(String qName, Attributes attributes) {
        String type = attributes.getValue("type");
        String desc = attributes.getValue("desc");
        actualBus = Integer.parseInt(attributes.getValue("bus"));
        String defaultstate = attributes.getValue("defaultstate");
        String orientation = attributes.getValue("orientation");
        int number = Integer.parseInt(attributes.getValue("number"));
        if (type.equals("DefaultSwitch")) {
            actualSwitch = new DefaultSwitch(number, desc);
            actualAddresses = new Address[1];
        } else if (type.equals("DoubleCrossSwitch")) {
            actualSwitch = new DoubleCrossSwitch(number, desc);
            actualAddresses = new Address[1];
        } else if (type.equals("ThreeWaySwitch")) {
            actualSwitch = new ThreeWaySwitch(number, desc);
            actualAddresses = new Address[2];
        }
        if (defaultstate.equals("straight")) {
            actualSwitch.setDefaultState(SwitchState.STRAIGHT);
        } else if (defaultstate.equals("curved")) {
            actualSwitch.setDefaultState(SwitchState.LEFT);
        } else {
            actualSwitch.setDefaultState(SwitchState.STRAIGHT);
        }
        if (orientation.toLowerCase().equals("north")) {
            actualSwitch.setSwitchOrientation(SwitchOrientation.NORTH);
        } else if (orientation.toLowerCase().equals("east")) {
            actualSwitch.setSwitchOrientation(SwitchOrientation.EAST);
        } else if (orientation.toLowerCase().equals("south")) {
            actualSwitch.setSwitchOrientation(SwitchOrientation.SOUTH);
        } else if (orientation.toLowerCase().equals("west")) {
            actualSwitch.setSwitchOrientation(SwitchOrientation.WEST);
        } else {
            actualSwitch.setSwitchOrientation(SwitchOrientation.EAST);
        }
    }

    private void parseLocomotive(String qName, Attributes attributes) {
        String name = attributes.getValue("name");
        String desc = attributes.getValue("desc");
        String type = attributes.getValue("type");
        int bus = Integer.parseInt(attributes.getValue("bus"));
        int address = Integer.parseInt(attributes.getValue("address"));
        if (type.equals("NoneLocomotive")) {
            actualLocomotive = new NoneLocomotive();
        } else if (type.equals("DeltaLocomotive")) {
            actualLocomotive = new DeltaLocomotive(name, new Address(bus,
                address), desc);
        } else if (type.equals("DigitalLocomotive")) {
            actualLocomotive = new DigitalLocomotive(name, new Address(bus,
                address), desc);
        }
    }

    private void parseGuiConfig(String gName, Attributes attributes) {
        preferences.setStringValue(attributes.getValue("name"), attributes
            .getValue("value"));
    }

    public void endElement(String uri, String localName, String qName)
        throws SAXException {
        qName = qName.toLowerCase();
        if (qName.equals("switchgroup")) {
            switchControl.registerSwitchGroup(actualSwitchGroup);
        } else if (qName.equals("locomotivegroup")) {
            locomotiveControl.registerLocomotiveGroup(actualLocomotiveGroup);
        } else if (qName.equals("switch")) {
            actualSwitch.setAddresses(actualAddresses);
            actualSwitchGroup.addSwitch(actualSwitch);
            switchControl.registerSwitch(actualSwitch);
            actualSwitch = null;
        } else if (qName.equals("locomotive")) {
            actualLocomotiveGroup.addLocomotive(actualLocomotive);
            locomotiveControl.registerLocomotive(actualLocomotive);
            actualLocomotive = null;
        }
    }
}
