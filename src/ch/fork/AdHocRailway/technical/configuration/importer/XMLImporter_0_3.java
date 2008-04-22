/*------------------------------------------------------------------------
 * 
 * copyright : (C) 2008 by Benjamin Mueller 
 * email     : news@fork.ch
 * website   : http://sourceforge.net/projects/adhocrailway
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
import ch.fork.AdHocRailway.domain.locomotives.FileLocomotivePersistence;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceException;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveType;
import ch.fork.AdHocRailway.domain.routes.FileRoutePersistence;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceException;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.FileTurnoutPersistence;
import ch.fork.AdHocRailway.domain.turnouts.SRCPTurnoutTypes;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutType;
import ch.fork.AdHocRailway.technical.configuration.Preferences;
import ch.fork.AdHocRailway.ui.ExceptionProcessor;

public class XMLImporter_0_3 extends DefaultHandler implements ContentHandler {
	private Preferences					preferences;
	private Turnout						actualTurnout;
	private TurnoutGroup				actualTurnoutGroup;
	private Address						actualAddress;
	private Address[]					actualAddresses;
	private int							actualAddressCounter	= 0;
	private TurnoutPersistenceIface		turnoutPersistence;
	private Locomotive					actualLocomotive;
	private LocomotiveGroup				actualLocomotiveGroup;
	private LocomotivePersistenceIface	locomotivePersistence;
	private RoutePersistenceIface		routePersistence;
	private RouteGroup					actualRouteGroup;
	private Route						actualRoute;
	private RouteItem					actualRouteItem;

	public XMLImporter_0_3(String filename) {
		this(filename, FileTurnoutPersistence.getInstance(),
				FileLocomotivePersistence.getInstance(),
				FileRoutePersistence.getInstance());
	}

	public XMLImporter_0_3(String filename,
			TurnoutPersistenceIface turnoutPersistence,
			LocomotivePersistenceIface locomotivePersistence,
			RoutePersistenceIface routePersistence) {
		this.preferences = Preferences.getInstance();
		this.turnoutPersistence = turnoutPersistence;
		this.locomotivePersistence = locomotivePersistence;
		this.routePersistence = routePersistence;

		try {
			routePersistence.clear();
		} catch (RoutePersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		turnoutPersistence.clear();

		try {
			locomotivePersistence.clear();
		} catch (LocomotivePersistenceException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		TurnoutType defaultType = new TurnoutType(0, "DEFAULT");
		TurnoutType doublecrossType = new TurnoutType(0, "DOUBLECROSS");
		TurnoutType threewayType = new TurnoutType(0, "THREEWAY");

		turnoutPersistence.addTurnoutType(defaultType);
		turnoutPersistence.addTurnoutType(doublecrossType);
		turnoutPersistence.addTurnoutType(threewayType);

		LocomotiveType deltaType = new LocomotiveType(0, "DELTA");
		deltaType.setDrivingSteps(14);
		deltaType.setStepping(4);
		deltaType.setFunctionCount(4);
		LocomotiveType digitalType = new LocomotiveType(0, "DIGITAL");
		digitalType.setDrivingSteps(28);
		digitalType.setStepping(2);
		digitalType.setFunctionCount(5);
		locomotivePersistence.addLocomotiveType(deltaType);
		locomotivePersistence.addLocomotiveType(digitalType);

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
			actualTurnoutGroup = new TurnoutGroup(0, attributes
					.getValue("name"), 0, 0);

			turnoutPersistence.addTurnoutGroup(actualTurnoutGroup);

		} else if (qName.equals("switch")) {
			parseTurnout(qName, attributes);
		} else if (qName.equals("address")) {
			int address = Integer.parseInt(attributes.getValue("address"));
			int bus = Integer.parseInt(attributes.getValue("bus"));
			actualAddress = new Address(bus, address);
			actualAddresses[actualAddressCounter] = actualAddress;
			actualAddressCounter++;
		} else if (qName.equals("routegroup")) {
			actualRouteGroup = new RouteGroup(0, attributes.getValue("name"),
					0, 0);
			routePersistence.addRouteGroup(actualRouteGroup);
		} else if (qName.equals("route")) {
			parseRoute(qName, attributes);
		} else if (qName.equals("routedswitch")) {
			parseRoutedSwitch(qName, attributes);
		} else if (qName.equals("locomotivegroup")) {
			actualLocomotiveGroup = new LocomotiveGroup(0, attributes
					.getValue("name"));
			locomotivePersistence.addLocomotiveGroup(actualLocomotiveGroup);
		} else if (qName.equals("locomotive")) {
			parseLocomotive(qName, attributes);
		} else if (qName.equals("guiconfigparameter")) {
			parseGuiConfig(qName, attributes);
		}
	}

	private void parseTurnout(String qName, Attributes attributes) {
		String type = attributes.getValue("type");
		String desc = attributes.getValue("desc");
		String defaultstate = attributes.getValue("defaultstate");
		String orientation = attributes.getValue("orientation");
		int number = Integer.parseInt(attributes.getValue("number"));

		if (type.toUpperCase().equals("DEFAULT") || type.toUpperCase().equals("DEFAULTSWITCH")) {
			TurnoutType turnoutType = turnoutPersistence
					.getTurnoutType(SRCPTurnoutTypes.DEFAULT);
			actualTurnout = new Turnout(0, turnoutType, actualTurnoutGroup,
					number, desc, defaultstate.toUpperCase(), orientation
							.toUpperCase(), 0, 0, false);
			actualAddresses = new Address[1];
			turnoutType.getTurnouts().add(actualTurnout);
		} else if (type.toUpperCase().equals("DOUBLECROSS") || type.toUpperCase().equals("DOUBLECROSSSWITCH")) {
			TurnoutType turnoutType = turnoutPersistence
					.getTurnoutType(SRCPTurnoutTypes.DOUBLECROSS);
			actualTurnout = new Turnout(0, turnoutType, actualTurnoutGroup,
					number, desc, defaultstate.toUpperCase(), orientation
							.toUpperCase(), 0, 0, false);
			actualAddresses = new Address[1];
			turnoutType.getTurnouts().add(actualTurnout);
		} else if (type.toUpperCase().equals("THREEWAY") || type.toUpperCase().equals("THREEWAYSWITCH")) {
			TurnoutType turnoutType = turnoutPersistence
					.getTurnoutType(SRCPTurnoutTypes.THREEWAY);
			actualTurnout = new Turnout(0, turnoutType, actualTurnoutGroup,
					number, desc, defaultstate.toUpperCase(), orientation
							.toUpperCase(), 0, 0, false);
			actualAddresses = new Address[2];
			turnoutType.getTurnouts().add(actualTurnout);
		}
	}

	private void parseRoute(String qName, Attributes attributes) {
		String name = attributes.getValue("name");
		int number = Integer.parseInt(attributes.getValue("number"));
		actualRoute = new Route(0, actualRouteGroup, number, name);
		actualRouteGroup.getRoutes().add(actualRoute);
		routePersistence.addRoute(actualRoute);
	}

	private void parseRoutedSwitch(String qName, Attributes attributes) {
		int switchNumber = Integer
				.parseInt(attributes.getValue("switchNumber"));
		String switchStateRouted = attributes.getValue("switchStateRouted");
		Turnout turnout = null;
		turnout = turnoutPersistence.getTurnoutByNumber(switchNumber);
		if (turnout != null) {

			actualRouteItem = new RouteItem(0, turnout, actualRoute,
					switchStateRouted);
			actualRoute.getRouteItems().add(actualRouteItem);
			turnout.getRouteItems().add(actualRouteItem);
			routePersistence.addRouteItem(actualRouteItem);
		}
	}

	private void parseLocomotive(String qName, Attributes attributes) {
		String name = attributes.getValue("name");
		String desc = attributes.getValue("desc");
		String type = attributes.getValue("type");
		int bus = Integer.parseInt(attributes.getValue("bus"));
		int address = Integer.parseInt(attributes.getValue("address"));

		if (type.toUpperCase().equals("DELTA") ||type.toUpperCase().equals("DELTALOCOMOTIVE") ) {
			LocomotiveType locomotiveType = locomotivePersistence
					.getLocomotiveTypeByName("DELTA");
			actualLocomotive = new Locomotive(0, actualLocomotiveGroup,
					locomotiveType, name, desc, "", address, bus);
			locomotiveType.getLocomotives().add(actualLocomotive);
		} else if (type.toUpperCase().equals("DIGITAL") ||type.toUpperCase().equals("DIGITALLOCOMOTIVE")) {
			LocomotiveType locomotiveType = locomotivePersistence
					.getLocomotiveTypeByName("DIGITAL");
			actualLocomotive = new Locomotive(0, actualLocomotiveGroup,
					locomotiveType, name, desc, "", address, bus);
			locomotiveType.getLocomotives().add(actualLocomotive);
		}
	}

	private void parseGuiConfig(String gName, Attributes attributes) {
		// preferences.setStringValue(attributes.getValue("name"), attributes
		// .getValue("value"));
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		qName = qName.toLowerCase();
		if (qName.equals("switchgroup")) {
		} else if (qName.equals("locomotivegroup")) {
		} else if (qName.equals("switch")) {
			actualTurnout.setBus1(actualAddresses[0].getBus());
			actualTurnout.setAddress1(actualAddresses[0].getAddress());
			if (actualAddresses.length == 2) {
				actualTurnout.setBus2(actualAddresses[1].getBus());
				actualTurnout.setAddress2(actualAddresses[1].getAddress());
			}
			actualTurnout.setTurnoutGroup(actualTurnoutGroup);
			actualTurnoutGroup.getTurnouts().add(actualTurnout);

			turnoutPersistence.addTurnout(actualTurnout);

			actualTurnout = null;
			actualAddressCounter = 0;
		} else if (qName.equals("route")) {
			actualRoute = null;
		} else if (qName.equals("locomotive")) {
			actualLocomotive.setLocomotiveGroup(actualLocomotiveGroup);
			actualLocomotiveGroup.getLocomotives().add(actualLocomotive);

			locomotivePersistence.addLocomotive(actualLocomotive);
			actualLocomotive = null;
		}
	}
}
