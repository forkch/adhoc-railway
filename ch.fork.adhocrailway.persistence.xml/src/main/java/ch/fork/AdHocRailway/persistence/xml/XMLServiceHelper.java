package ch.fork.AdHocRailway.persistence.xml;

import ch.fork.AdHocRailway.manager.LocomotiveManager;
import ch.fork.AdHocRailway.manager.RouteManager;
import ch.fork.AdHocRailway.manager.TurnoutManager;
import ch.fork.AdHocRailway.model.AdHocRailwayException;
import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveFunction;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.model.turnouts.*;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLLocomotiveService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLRouteService;
import ch.fork.AdHocRailway.persistence.xml.impl.XMLTurnoutService;
import ch.fork.AdHocRailway.utils.DataImporter;
import com.thoughtworks.xstream.XStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.SortedSet;
import java.util.UUID;

public class XMLServiceHelper {

    private static final Logger LOGGER = Logger
            .getLogger(XMLServiceHelper.class);

    public XMLServiceHelper() {
    }

    public static String nextValue() {
        return UUID.randomUUID().toString();
    }

    public void loadFile(final XMLLocomotiveService locomotiveService,
                         final XMLTurnoutService turnoutService,
                         final XMLRouteService routeService, final File xmlFile) {
        try {
            loadFile(locomotiveService, turnoutService, routeService, FileUtils.openInputStream(xmlFile));
        } catch (IOException e) {
            throw new AdHocRailwayException("could not find file " + xmlFile);
        }

    }

    public void loadFile(final XMLLocomotiveService locomotiveService,
                         final XMLTurnoutService turnoutService,
                         final XMLRouteService routeService, final InputStream xmlFile) {

        AdHocRailwayData data;
        LOGGER.info("start loading locomotives, turnout and routes  from file: "
                + xmlFile);
        final XStream xstream = getXStream();

        data = (AdHocRailwayData) xstream.fromXML(xmlFile);

        addFunctionsIfNeccesaray(data);

        locomotiveService.loadLocomotiveGroupsFromXML(data
                .getLocomotiveGroups());
        turnoutService.loadTurnoutGroupsFromXML(data.getTurnoutGroups());
        routeService.loadRouteGroupsFromXML(data.getRouteGroups());

        LOGGER.info("finished loading locomotives, turnout and routes  from file: "
                + xmlFile);

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
            new DataImporter().importLocomotives(locomotivePersistence,
                    data.getLocomotiveGroups());
            LOGGER.info("finished importing locomotives from file: "
                    + fileToImport);
        } catch (final FileNotFoundException e) {
            throw new AdHocRailwayException("could not find file " + fileToImport);
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
        xstream.alias("locomotiveFunction", LocomotiveFunction.class);
        xstream.alias("locomotiveGroup", LocomotiveGroup.class);
        xstream.alias("turnout", Turnout.class);
        xstream.alias("turnoutGroup", TurnoutGroup.class);
        xstream.alias("route", Route.class);
        xstream.alias("routeGroup", RouteGroup.class);
        xstream.alias("routeItem", RouteItem.class);

        xstream.addImplicitCollection(LocomotiveGroup.class, "locomotives");
        xstream.addImplicitCollection(TurnoutGroup.class, "turnouts");
        xstream.addImplicitCollection(RouteGroup.class, "routes");
        xstream.addImplicitCollection(Route.class, "routedTurnouts");

        xstream.registerConverter(new LocomotiveTypeConverter());
        xstream.autodetectAnnotations(true);

        return xstream;
    }

    public void importAllFromFile(final File fileToImport,
                                  final LocomotiveManager locomotivePersistence, final TurnoutManager turnoutManager, final RouteManager routeManager) {
        AdHocRailwayData data;
        try {
            LOGGER.info("start importing locomotives, turnouts and routes from file: "
                    + fileToImport);

            final XStream xstream = getXStream();
            data = (AdHocRailwayData) xstream.fromXML(new FileReader(
                    fileToImport));

            addFunctionsIfNeccesaray(data);
            new DataImporter().importLocomotives(locomotivePersistence,
                    data.getLocomotiveGroups()
            );
            LOGGER.info("finished importing locomotives from file: " + fileToImport);

            new DataImporter().importTurnouts(turnoutManager,
                    data.getTurnoutGroups());
            LOGGER.info("finished importing turnouts from file: " + fileToImport);

            new DataImporter().importRoutes(routeManager,
                    data.getRouteGroups());
            LOGGER.info("finished importing routes from file: "
                    + fileToImport);
        } catch (final FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Turnout getTurnoutById(AdHocRailwayData data, String turnoutId) {

        for(TurnoutGroup turnoutGroup : data.getTurnoutGroups()) {
            for (Turnout turnout : turnoutGroup.getTurnouts()) {
                if (turnout.getId().equals(turnoutId))
                    return turnout;
            }
        }
        return null;
    }
}
