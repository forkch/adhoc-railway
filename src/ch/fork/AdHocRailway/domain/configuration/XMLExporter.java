package ch.fork.AdHocRailway.domain.configuration;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.configuration.exception.ConfigurationException;
import ch.fork.AdHocRailway.domain.configuration.exporter.XMLExporter_0_2;

public class XMLExporter {

    public static String export(double version) throws ConfigurationException {
        if (version == 0.2) {
            return XMLExporter_0_2.export();
        }
        throw new ConfigurationException(Constants.ERR_VERSION_NOT_SUPPORTED);
    }
}
