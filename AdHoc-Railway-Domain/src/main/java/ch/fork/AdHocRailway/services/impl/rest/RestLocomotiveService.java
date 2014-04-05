package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.SIOService;
import ch.fork.AdHocRailway.services.impl.socketio.locomotives.SIOLocomotiveService;
import org.apache.log4j.Logger;
import retrofit.RestAdapter;

import java.util.SortedSet;
import java.util.UUID;

/**
 * Created by fork on 3/27/14.
 */
public class RestLocomotiveService implements LocomotiveService {
    private static final Logger LOGGER = Logger.getLogger(RestLocomotiveService.class);

    private final SIOLocomotiveService sioLocomotiveService;

    private final RestLocomotiveServiceClient locomotiveServiceClient;
    private final String uuid;
    private LocomotiveServiceListener listener;

    public RestLocomotiveService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://localhost:3000")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();
        uuid = UUID.randomUUID().toString();
        SIOService.setUUID(uuid);

        locomotiveServiceClient = restAdapter.create(RestLocomotiveServiceClient.class);
        sioLocomotiveService = new SIOLocomotiveService();

    }

    @Override
    public void addLocomotive(Locomotive locomotive) {
        locomotiveServiceClient.addLocomotive(locomotive, uuid);
        LOGGER.info("addLocomotive(): " + locomotive);

        if (listenerOk()) listener.locomotiveAdded(locomotive);
    }

    @Override
    public void removeLocomotive(Locomotive locomotive) {
        locomotiveServiceClient.deleteLocomotive(locomotive.getId(), uuid);
        LOGGER.info("removeLocomotive(): " + locomotive);

        if (listenerOk()) listener.locomotiveRemoved(locomotive);

    }

    @Override
    public void updateLocomotive(Locomotive locomotive) {
        locomotiveServiceClient.updateLocomotive(locomotive, uuid);
        LOGGER.info("updateLocomotive(): " + locomotive);
        if (listenerOk())
            listener.locomotiveUpdated(locomotive);
    }


    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        SortedSet<LocomotiveGroup> allLocomotivesGroups = locomotiveServiceClient.getAllLocomotivesGroups(uuid);
        LOGGER.info("getAllLocomotiveGroups(): " + allLocomotivesGroups);

        for (LocomotiveGroup allLocomotiveGroup : allLocomotivesGroups) {
            System.out.println(allLocomotiveGroup);
            for (Locomotive locomotive : allLocomotiveGroup.getLocomotives()) {
                System.out.println(locomotive);
            }
        }
        return allLocomotivesGroups;
    }

    @Override
    public void addLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup locomotiveGroup = locomotiveServiceClient.addLocomotiveGroup(group, uuid);
        LOGGER.info("addLocomotiveGroup(): " + locomotiveGroup);

        if (listenerOk()) listener.locomotiveGroupAdded(locomotiveGroup);

    }

    @Override
    public void removeLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup locomotiveGroup = locomotiveServiceClient.deleteLocomotiveGroup(group.getId(), uuid);
        LOGGER.info("removeLocomotiveGroup(): " + locomotiveGroup);

        if (listenerOk()) listener.locomotiveGroupRemoved(locomotiveGroup);
    }

    @Override
    public void updateLocomotiveGroup(LocomotiveGroup group) {
        LocomotiveGroup locomotiveGroup = locomotiveServiceClient.updateLocomotiveGroup(group, uuid);
        LOGGER.info("updateLocomotiveGroup(): " + locomotiveGroup);
        if (listenerOk()) listener.locomotiveGroupUpdated(locomotiveGroup);
    }

    @Override
    public void clear() {
        SortedSet<LocomotiveGroup> locomotiveGroups = locomotiveServiceClient.deleteAllLocomotiveGroups(uuid);
        if (listenerOk()) listener.locomotivesUpdated(locomotiveGroups);
    }

    @Override
    public void init(LocomotiveServiceListener listener) {
        this.listener = listener;
        sioLocomotiveService.init(listener);
    }

    @Override
    public void disconnect() {

    }

    private boolean listenerOk() {
        return listener != null;
    }

    public static void main(String[] args) {
        RestLocomotiveService s = new RestLocomotiveService();
        SortedSet<LocomotiveGroup> allLocomotiveGroups = s.getAllLocomotiveGroups();

        s.addLocomotiveGroup(new LocomotiveGroup("", "some name"));
        s.getAllLocomotiveGroups();
    }
}
