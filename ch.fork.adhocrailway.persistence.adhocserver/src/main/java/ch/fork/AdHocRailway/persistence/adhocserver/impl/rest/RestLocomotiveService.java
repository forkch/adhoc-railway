package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.locomotives.SIOLocomotiveCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import org.apache.log4j.Logger;
import retrofit.RestAdapter;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public class RestLocomotiveService implements LocomotiveService {
    private static final Logger LOGGER = Logger.getLogger(RestLocomotiveService.class);

    private final SIOLocomotiveCallback sioLocomotiveService;

    private final RestLocomotiveServiceClient locomotiveServiceClient;
    private LocomotiveServiceListener listener;

    public RestLocomotiveService(String endpointURL,SIOService sioService, String uuid) {
        RestAdapter restAdapter = RestAdapterFactory.createRestAdapter(endpointURL, uuid);
        locomotiveServiceClient = restAdapter.create(RestLocomotiveServiceClient.class);
        sioLocomotiveService = new SIOLocomotiveCallback(sioService);
    }

    @Override
    public void addLocomotive(Locomotive locomotive) {
        Locomotive addLocomotive = locomotiveServiceClient.addLocomotive(locomotive);
        locomotive.setId(addLocomotive.getId());
        LOGGER.info("addLocomotive(): " + locomotive);

        if (listenerOk()) {
            listener.locomotiveAdded(locomotive);
        }
    }

    @Override
    public void removeLocomotive(Locomotive locomotive) {
        locomotiveServiceClient.deleteLocomotive(locomotive.getId());
        LOGGER.info("removeLocomotive(): " + locomotive);

        if (listenerOk()) {
            listener.locomotiveRemoved(locomotive);
        }

    }

    @Override
    public void updateLocomotive(Locomotive locomotive) {
        locomotiveServiceClient.updateLocomotive(locomotive);
        LOGGER.info("updateLocomotive(): " + locomotive);
        if (listenerOk()) {
            listener.locomotiveUpdated(locomotive);
        }
    }

    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        SortedSet<LocomotiveGroup> allLocomotivesGroups = locomotiveServiceClient.getAllLocomotivesGroups();
        LOGGER.info("getAllLocomotiveGroups(): " + allLocomotivesGroups);
        return allLocomotivesGroups;
    }

    @Override
    public void addLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup addLocomotiveGroup = locomotiveServiceClient.addLocomotiveGroup(group);
        LOGGER.info("addLocomotiveGroup(): " + addLocomotiveGroup);
        group.setId(addLocomotiveGroup.getId());

        if (listenerOk()) {
            listener.locomotiveGroupAdded(group);
        }
    }

    @Override
    public void removeLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup deleteLocomotiveGroup = locomotiveServiceClient.deleteLocomotiveGroup(group.getId());
        LOGGER.info("removeLocomotiveGroup(): " + deleteLocomotiveGroup);

        if (listenerOk()) {
            listener.locomotiveGroupRemoved(group);
        }
    }

    @Override
    public void updateLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup updateLocomotiveGroup = locomotiveServiceClient.updateLocomotiveGroup(group);
        LOGGER.info("updateLocomotiveGroup(): " + updateLocomotiveGroup);
        if (listenerOk()) {
            listener.locomotiveGroupUpdated(group);
        }
    }

    @Override
    public void clear() {
        SortedSet<LocomotiveGroup> locomotiveGroups = locomotiveServiceClient.deleteAllLocomotiveGroups();
        if (listenerOk()) {
            listener.locomotivesUpdated(locomotiveGroups);
        }
    }

    @Override
    public void init(LocomotiveServiceListener listener) {
        this.listener = listener;
        sioLocomotiveService.init(listener);
    }

    @Override
    public void disconnect() {
        sioLocomotiveService.disconnect();

    }

    private boolean listenerOk() {
        return listener != null;
    }

}
