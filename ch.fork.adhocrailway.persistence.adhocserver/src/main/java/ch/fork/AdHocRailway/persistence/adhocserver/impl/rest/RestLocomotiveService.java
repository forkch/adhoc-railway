package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.locomotives.SIOLocomotiveCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import org.apache.log4j.Logger;
import retrofit2.Retrofit;

import java.io.IOException;
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


        final Retrofit retrofit = RestAdapterFactory.createRestAdapter(endpointURL, uuid);
        locomotiveServiceClient = retrofit.create(RestLocomotiveServiceClient.class);
        sioLocomotiveService = new SIOLocomotiveCallback(sioService);
    }

    @Override
    public void addLocomotive(Locomotive locomotive) {
        Locomotive addLocomotive = null;
        try {
            addLocomotive = locomotiveServiceClient.addLocomotive(locomotive).execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        locomotive.setId(addLocomotive.getId());
        LOGGER.debug("addLocomotive(): " + locomotive);

        if (listenerOk()) {
            listener.locomotiveAdded(locomotive);
        }
    }

    @Override
    public void removeLocomotive(Locomotive locomotive) {
        try {
            locomotiveServiceClient.deleteLocomotive(locomotive.getId()).execute();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.debug("removeLocomotive(): " + locomotive);

        if (listenerOk()) {
            listener.locomotiveRemoved(locomotive);
        }

    }

    @Override
    public void updateLocomotive(Locomotive locomotive) {
        try {
            locomotiveServiceClient.updateLocomotive(locomotive).execute();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.debug("updateLocomotive(): " + locomotive);
        if (listenerOk()) {
            listener.locomotiveUpdated(locomotive);
        }
    }

    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        SortedSet<LocomotiveGroup> allLocomotivesGroups = null;
        try {
            allLocomotivesGroups = locomotiveServiceClient.getAllLocomotivesGroups().execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.debug("getAllLocomotiveGroups(): " + allLocomotivesGroups);
        return allLocomotivesGroups;
    }

    @Override
    public void addLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup addLocomotiveGroup = null;
        try {
            addLocomotiveGroup = locomotiveServiceClient.addLocomotiveGroup(group).execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.debug("addLocomotiveGroup(): " + addLocomotiveGroup);
        group.setId(addLocomotiveGroup.getId());

        if (listenerOk()) {
            listener.locomotiveGroupAdded(group);
        }
    }

    @Override
    public void removeLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup deleteLocomotiveGroup = null;
        try {
            deleteLocomotiveGroup = locomotiveServiceClient.deleteLocomotiveGroup(group.getId()).execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        LOGGER.debug("removeLocomotiveGroup(): " + deleteLocomotiveGroup);

        if (listenerOk()) {
            listener.locomotiveGroupRemoved(group);
        }
    }

    @Override
    public void updateLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup updateLocomotiveGroup = null;
        try {
            updateLocomotiveGroup = locomotiveServiceClient.updateLocomotiveGroup(group).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        LOGGER.debug("updateLocomotiveGroup(): " + updateLocomotiveGroup);
        if (listenerOk()) {
            listener.locomotiveGroupUpdated(group);
        }
    }

    @Override
    public void clear() {
        SortedSet<LocomotiveGroup> locomotiveGroups = null;
        try {
            locomotiveGroups = locomotiveServiceClient.deleteAllLocomotiveGroups().execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
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
