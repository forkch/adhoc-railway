
package ch.fork.AdHocRailway.domain.configuration;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.configuration.exception.ConfigurationException;
import ch.fork.AdHocRailway.domain.configuration.importer.XMLImporter_0_1;
import ch.fork.AdHocRailway.domain.configuration.importer.XMLImporter_0_2;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class XMLImporter extends DefaultHandler implements ContentHandler {

    private String filename;
    private boolean supported = true;
    
    public XMLImporter(String filename) throws ConfigurationException {
        this.filename = filename;
        parseDocument(filename);
        if(!supported ) {
            throw new ConfigurationException(Constants.ERR_VERSION_NOT_SUPPORTED);
        }
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
        if (qName.equals("railcontrol")) {
            double version = Double.parseDouble(attributes
                .getValue("ExporterVersion"));
            if (version == 0.1) {
                XMLImporter_0_1 importer = new XMLImporter_0_1(filename);
                return;
            } else if (version == 0.2) {
                XMLImporter_0_2 importer = new XMLImporter_0_2(filename);
                return;
            }
            supported = false;
            return;
        }
    }
}
