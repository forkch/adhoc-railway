package ch.fork.RailControl.domain.configuration;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.fork.RailControl.domain.locomotives.DeltaLocomotive;
import ch.fork.RailControl.domain.locomotives.DigitalLocomotive;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.locomotives.NoneLocomotive;
import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;
import ch.fork.RailControl.domain.switches.Switch.SwitchOrientation;
import ch.fork.RailControl.domain.switches.Switch.SwitchState;
import ch.fork.RailControl.ui.ExceptionProcessor;

public class XMLImporter extends DefaultHandler implements ContentHandler {

    private Map<Integer, Switch> switchNumberToSwitch;

    private List<SwitchGroup> switchGroups;

    private Preferences preferences;

    private List<Locomotive> locomotives;

    private Switch actualSwitch;

    private SwitchGroup actualSwitchGroup;

    private Address actualAddress;

    private Locomotive actualLocomotive;

    public XMLImporter(Preferences preferences,
        Map<Integer, Switch> switchNumberToSwitch,
        List<SwitchGroup> switchGroups, List<Locomotive> locomotives,
        String filename) {

        this.preferences = preferences;
        this.switchGroups = switchGroups;
        this.switchNumberToSwitch = switchNumberToSwitch;
        this.locomotives = locomotives;
        switchGroups.clear();
        switchNumberToSwitch.clear();
        locomotives.clear();
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
            actualSwitchGroup = new SwitchGroup(attributes
                .getValue("name"));
        } else if (qName.equals("switch")) {
            parseSwitch(qName, attributes);
        } else if (qName.equals("address")) {
            actualAddress = new Address(Integer.parseInt(attributes
                .getValue("address1")), Integer.parseInt(attributes
                .getValue("address2")));
        } else if (qName.equals("locomotive")) {
            parseLocomotive(qName, attributes);
        } else if (qName.equals("guiconfigparameter")) {
            parseGuiConfig(qName, attributes);
        }
    }

    private void parseSwitch(String qName, Attributes attributes) {
        String type = attributes.getValue("type");
        String desc = attributes.getValue("desc");
        String defaultstate = attributes.getValue("defaultstate");
        String orientation = attributes.getValue("orientation");
        int bus = Integer.parseInt(attributes.getValue("bus"));
        int number = Integer.parseInt(attributes.getValue("number"));

        if (type.equals("DefaultSwitch")) {
            actualSwitch = new DefaultSwitch(number, desc);

        } else if (type.equals("DoubleCrossSwitch")) {
            actualSwitch = new DoubleCrossSwitch(number, desc);
        } else if (type.equals("ThreeWaySwitch")) {
            actualSwitch = new ThreeWaySwitch(number, desc);
        }
        actualSwitch.setBus(bus);
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
        if (attributes.getValue("type").equals("NoneLocomotive")) {
            actualLocomotive = new NoneLocomotive();
        } else if (attributes.getValue("type").equals("DeltaLocomotive")) {
            actualLocomotive = new DeltaLocomotive(name, bus, address,
                desc);
        } else if (attributes.getValue("type").equals("DigitalLocomotive")) {
            actualLocomotive = new DigitalLocomotive(name, bus, address,
                desc);
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
            switchGroups.add(actualSwitchGroup);
        } else if (qName.equals("switch")) {
            if (switchNumberToSwitch.get(actualSwitch.getNumber()) == null) {
                actualSwitch.setAddress(actualAddress);
                actualSwitchGroup.addSwitch(actualSwitch);
                switchNumberToSwitch.put(
                    actualSwitch.getNumber(), actualSwitch);
            }
            actualSwitch = null;
        } else if (qName.equals("locomotive")) {
            locomotives.add(actualLocomotive);
            actualLocomotive = null;
        }
    }
}
