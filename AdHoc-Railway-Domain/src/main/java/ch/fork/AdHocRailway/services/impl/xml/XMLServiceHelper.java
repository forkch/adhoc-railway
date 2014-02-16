package ch.fork.AdHocRailway.services.impl.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import ch.fork.AdHocRailway.domain.turnouts.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.manager.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.manager.turnouts.RouteManager;
import ch.fork.AdHocRailway.manager.turnouts.TurnoutManager;

import com.thoughtworks.xstream.XStream;

public class XMLServiceHelper {

	private static final Logger LOGGER = Logger
			.getLogger(XMLServiceHelper.class);
	private static final AtomicInteger counter = new AtomicInteger();

	public static int nextValue() {
		return counter.getAndIncrement();
	}

	public XMLServiceHelper() {
	}

	public void loadFile(final XMLLocomotiveService locomotiveService,
			final XMLTurnoutService turnoutService,
			final XMLRouteService routeService, final File xmlFile) {

		AdHocRailwayData data;
		try {
			LOGGER.info("start loading locomotives, turnout and routes  from file: "
					+ xmlFile);
			final XStream xstream = getXStream();

			data = (AdHocRailwayData) xstream.fromXML(new FileReader(xmlFile));
			addFunctionsIfNeccesaray(data);

			locomotiveService.loadLocomotiveGroupsFromXML(data
					.getLocomotiveGroups());
			turnoutService.loadTurnoutGroupsFromXML(data.getTurnoutGroups());
			routeService.loadRouteGroupsFromXML(data.getRouteGroups());

			LOGGER.info("finished loading locomotives, turnout and routes  from file: "
					+ xmlFile);
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void saveFile(final LocomotiveManager locomotiveManager,
			final TurnoutManager turnoutManager,
			final RouteManager routeManager, final File xmlFile)
			throws IOException {

		LOGGER.info("start saving/exporting locomotives, turnout and routes to file: "
				+ xmlFile);
		final SortedSet<LocomotiveGroup> locomotiveGroups = locomotiveManager
				.getAllLocomotiveGroups();

		final SortedSet<TurnoutGroup> turnoutGroups = turnoutManager
				.getAllTurnoutGroups();

		final SortedSet<RouteGroup> routeGroups = routeManager
				.getAllRouteGroups();

		final AdHocRailwayData data = new AdHocRailwayData(locomotiveGroups,
				turnoutGroups, routeGroups);
		final XStream xstream = getXStream();

		final String xml = xstream.toXML(data);
		FileUtils.writeStringToFile(xmlFile, xml);

		LOGGER.info("finished saving locomotives, turnout and routes  to file: "
				+ xmlFile);

	}

	public void exportLocomotivesToFile(final File fileToExport,
			final LocomotiveManager locomotivePersistence) throws IOException {
		LOGGER.info("start exporting locomotives to file: " + fileToExport);

		final AdHocRailwayData data = new AdHocRailwayData(
				locomotivePersistence.getAllLocomotiveGroups(), null, null);
		final XStream xstream = getXStream();
		final String xml = xstream.toXML(data);
		FileUtils.writeStringToFile(fileToExport, xml);
		LOGGER.info("finished exporting locomotives to file: " + fileToExport);
	}

	public void importLocomotivesFromFile(final File fileToImport,
			final LocomotiveManager locomotivePersistence) {
		AdHocRailwayData data;
		try {
			LOGGER.info("start importing locomotives from file: "
					+ fileToImport);

			final XStream xstream = getXStream();
			data = (AdHocRailwayData) xstream.fromXML(new FileReader(
					fileToImport));

			addFunctionsIfNeccesaray(data);
			new LocomotiveImporter().importLocomotives(locomotivePersistence,
					data.getLocomotiveGroups());
			LOGGER.info("finished importing locomotives from file: "
					+ fileToImport);
		} catch (final FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void addFunctionsIfNeccesaray(final AdHocRailwayData data) {
		for (final LocomotiveGroup group : data.getLocomotiveGroups()) {
			if (group.getLocomotives() == null) {
				continue;
			}
			for (final Locomotive locomotive : group.getLocomotives()) {
				if (locomotive.getFunctions() == null) {

					switch (locomotive.getType()) {
					case DELTA:
						locomotive.setFunctions(LocomotiveFunction
								.getDeltaFunctions());
						break;
					case DIGITAL:
						locomotive.setFunctions(LocomotiveFunction
								.getDigitalFunctions());
						break;
					case SIMULATED_MFX:
						locomotive.setFunctions(LocomotiveFunction
								.getSimulatedMfxFunctions());
						break;
					default:
						break;

					}

				}
			}
		}
	}

	private XStream getXStream() {
		final XStream xstream = new XStream();

		// old configurations
		xstream.omitField(TurnoutGroup.class, "turnoutNumberOffset");
		xstream.omitField(TurnoutGroup.class, "turnoutNumberAmount");
		xstream.omitField(RouteGroup.class, "routeNumberOffset");
		xstream.omitField(RouteGroup.class, "routeNumberAmount");

		xstream.alias("AdHocRailwayData", AdHocRailwayData.class);
		xstream.alias("locomotive", Locomotive.class);
		xstream.alias("locomotiveGroup", LocomotiveGroup.class);
		xstream.alias("turnout", Turnout.class);
		xstream.alias("turnoutGroup", TurnoutGroup.class);
		xstream.alias("route", Route.class);
		xstream.alias("routeGroup", RouteGroup.class);
		xstream.alias("routeItem", RouteItem.class);

		xstream.addImplicitCollection(LocomotiveGroup.class, "locomotives");
		xstream.addImplicitCollection(TurnoutGroup.class, "turnouts");
		xstream.addImplicitCollection(RouteGroup.class, "routes");
		xstream.addImplicitCollection(Route.class, "routeItems");

		xstream.registerConverter(new LocomotiveTypeConverter());
		xstream.autodetectAnnotations(true);

		return xstream;
	}
}
