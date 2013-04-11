package ch.fork.AdHocRailway.services.impl.xml;

import java.io.File;
import java.io.IOException;
import java.util.SortedSet;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import ch.fork.AdHocRailway.domain.ApplicationContext;
import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveManager;
import ch.fork.AdHocRailway.domain.routes.Route;
import ch.fork.AdHocRailway.domain.routes.RouteGroup;
import ch.fork.AdHocRailway.domain.routes.RouteItem;
import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;

import com.thoughtworks.xstream.XStream;

public class XMLService {

	private static final AtomicInteger counter = new AtomicInteger();

	public static int nextValue() {
		return counter.getAndIncrement();
	}

	public XMLService() {
	}

	public void loadFile(final ApplicationContext appContext,
			final File xmlFile) {
		final XStream xstream = getXStream();

		final AdHocRailwayData data = (AdHocRailwayData) xstream
				.fromXML(xmlFile);
		addFunctionsIfNeccesaray(data);

		final XMLLocomotiveService locomotiveService = (XMLLocomotiveService) appContext
				.getLocomotiveManager().getService();
		final XMLTurnoutService turnoutService = (XMLTurnoutService) appContext
				.getTurnoutManager().getService();
		final XMLRouteService routeService = (XMLRouteService) appContext
				.getRouteManager().getService();

		locomotiveService.loadLocomotiveGroupsFromXML(data
				.getLocomotiveGroups());
		turnoutService.loadTurnoutGroupsFromXML(data.getTurnoutGroups());
		routeService.loadRouteGroupsFromXML(data.getRouteGroups());

	}

	public void saveFile(final ApplicationContext appContext,
			final File xmlFile) throws IOException {

		final SortedSet<LocomotiveGroup> locomotiveGroups = appContext
				.getLocomotiveManager().getAllLocomotiveGroups();

		final SortedSet<TurnoutGroup> turnoutGroups = appContext
				.getTurnoutManager().getAllTurnoutGroups();

		final SortedSet<RouteGroup> routeGroups = appContext.getRouteManager()
				.getAllRouteGroups();

		final AdHocRailwayData data = new AdHocRailwayData(locomotiveGroups,
				turnoutGroups, routeGroups);
		final XStream xstream = getXStream();

		final String xml = xstream.toXML(data);
		FileUtils.writeStringToFile(xmlFile, xml);

	}

	public void exportLocomotivesToFile(final File fileToExport,
			final LocomotiveManager locomotivePersistence) throws IOException {

		final AdHocRailwayData data = new AdHocRailwayData(
				locomotivePersistence.getAllLocomotiveGroups(), null, null);
		final XStream xstream = getXStream();
		final String xml = xstream.toXML(data);
		FileUtils.writeStringToFile(fileToExport, xml);
	}

	public void importLocomotivesFromFile(final File selectedFile,
			final LocomotiveManager locomotivePersistence) {

		final XStream xstream = getXStream();
		final AdHocRailwayData data = (AdHocRailwayData) xstream
				.fromXML(selectedFile);

		addFunctionsIfNeccesaray(data);
		new LocomotiveImporter().importLocomotives(locomotivePersistence,
				data.getLocomotiveGroups());
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
		xstream.omitField(Locomotive.class, "locomotiveGroup");
		xstream.omitField(Turnout.class, "turnoutGroup");
		xstream.omitField(Route.class, "routeGroup");
		xstream.omitField(RouteItem.class, "turnout");

		xstream.alias("AdHocRailwayData", AdHocRailwayData.class);
		xstream.alias("locomotive", Locomotive.class);
		xstream.alias("locomotiveGroup", LocomotiveGroup.class);
		xstream.alias("turnout", Turnout.class);
		xstream.alias("turnoutGroup", TurnoutGroup.class);
		xstream.alias("route", Route.class);
		xstream.alias("routeGroup", RouteGroup.class);
		xstream.alias("routeItem", RouteItem.class);

		xstream.useAttributeFor(LocomotiveGroup.class, "id");
		xstream.useAttributeFor(LocomotiveGroup.class, "name");
		xstream.useAttributeFor(Locomotive.class, "id");
		xstream.useAttributeFor(Locomotive.class, "name");
		xstream.useAttributeFor(Locomotive.class, "desc");
		xstream.useAttributeFor(Locomotive.class, "image");
		xstream.useAttributeFor(Locomotive.class, "bus");
		xstream.useAttributeFor(Locomotive.class, "address1");
		xstream.useAttributeFor(Locomotive.class, "address2");
		xstream.useAttributeFor(Locomotive.class, "type");

		xstream.addImplicitCollection(LocomotiveGroup.class, "locomotives");
		xstream.addImplicitCollection(TurnoutGroup.class, "turnouts");
		xstream.addImplicitCollection(RouteGroup.class, "routes");
		xstream.addImplicitCollection(Route.class, "routeItems");

		xstream.registerConverter(new LocomotiveTypeConverter());

		return xstream;
	}
}
