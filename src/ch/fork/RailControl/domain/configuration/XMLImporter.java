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

import ch.fork.RailControl.domain.Preferences;
import ch.fork.RailControl.domain.locomotives.Locomotive;
import ch.fork.RailControl.domain.switches.Address;
import ch.fork.RailControl.domain.switches.DefaultSwitch;
import ch.fork.RailControl.domain.switches.DoubleCrossSwitch;
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;
import ch.fork.RailControl.domain.switches.ThreeWaySwitch;

public class XMLImporter extends DefaultHandler implements ContentHandler {

	private Map<Integer, Switch> switchNumberToSwitch;

	private List<SwitchGroup> switchGroups;

	private Preferences preferences;

	private List<Locomotive> locomotives;

	private Switch actualSwitch;

	private SwitchGroup actualSwitchGroup;

	private Address actualAddress;

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
			se.printStackTrace();
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
			if (attributes.getValue("type").equals("DefaultSwitch")) {
				actualSwitch = new DefaultSwitch(Integer.parseInt(attributes
						.getValue("number")), attributes.getValue("desc"));
				actualSwitch.setBus(Integer
						.parseInt(attributes.getValue("bus")));
			} else if(attributes.getValue("type").equals("DoubleCrossSwitch")) {
				actualSwitch = new DoubleCrossSwitch(Integer.parseInt(attributes
						.getValue("number")), attributes.getValue("desc"));
				actualSwitch.setBus(Integer
						.parseInt(attributes.getValue("bus")));
			} else if(attributes.getValue("type").equals("ThreeWaySwitch")) {
				actualSwitch = new ThreeWaySwitch(Integer.parseInt(attributes
						.getValue("number")), attributes.getValue("desc"));
				actualSwitch.setBus(Integer
						.parseInt(attributes.getValue("bus")));
			}
		} else if (qName.equals("address")) {
			actualAddress = new Address(Integer.parseInt(attributes
					.getValue("address1")), Integer.parseInt(attributes
					.getValue("address2")));
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		qName = qName.toLowerCase();
		if (qName.equals("switchgroup")) {
			switchGroups.add(actualSwitchGroup);
		} else if (qName.equals("switch")) {
			actualSwitch.setAddress(actualAddress);
			actualSwitchGroup.addSwitch(actualSwitch);
			switchNumberToSwitch.put(actualSwitch.getNumber(), actualSwitch);
			actualSwitch = null;
		}
	}
}
