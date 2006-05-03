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
import ch.fork.RailControl.domain.switches.Switch;
import ch.fork.RailControl.domain.switches.SwitchGroup;

public class XMLImporter extends DefaultHandler implements ContentHandler {

	private Map<Integer, Switch> switchNumberToSwitch;

	private List<SwitchGroup> switchGroups;

	private Preferences preferences;

	private List<Locomotive> locomotives;

	public XMLImporter(Preferences preferences,
			Map<Integer, Switch> switchNumberToSwitch,
			List<SwitchGroup> switchGroups, List<Locomotive> locomotives,
			String filename) {

		this.preferences = preferences;
		this.switchGroups = switchGroups;
		this.switchNumberToSwitch = switchNumberToSwitch;
		this.locomotives = locomotives;
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
		
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		
	}

}
