package ch.fork.AdHocRailway.technical.configuration.exporter;

import ch.fork.AdHocRailway.domain.Constants;
import ch.fork.AdHocRailway.domain.locomotives.LocomotivePersistenceIface;
import ch.fork.AdHocRailway.domain.routes.RoutePersistenceIface;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutPersistenceIface;
import ch.fork.AdHocRailway.technical.configuration.ConfigurationException;

public class XMLExporter {

    public String export(double version, TurnoutPersistenceIface turnoutPersistence,
			LocomotivePersistenceIface locomotivePersistence,
			RoutePersistenceIface routePersistence) throws ConfigurationException {
        if (version == 0.2) {
            return new XMLExporter_0_2(turnoutPersistence, locomotivePersistence, routePersistence).export();
        } else if(version == 0.3) {
            return new XMLExporter_0_3(turnoutPersistence, locomotivePersistence, routePersistence).export();
        }
        throw new ConfigurationException(Constants.ERR_VERSION_NOT_SUPPORTED);
    }
}
