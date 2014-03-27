package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import retrofit.RestAdapter;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public class RestLocomotiveService implements LocomotiveService {

    private final RestLocomotiveServiceClient locomotiveServiceClient;

    public RestLocomotiveService() {
        RestAdapter restAdapter = new RestAdapter.Builder()
                .setEndpoint("http://localhost:3000")
                .setLogLevel(RestAdapter.LogLevel.FULL)
                .build();

        locomotiveServiceClient = restAdapter.create(RestLocomotiveServiceClient.class);
    }

    @Override
    public void addLocomotive(Locomotive locomotive) {

    }

    @Override
    public void removeLocomotive(Locomotive locomotive) {

    }

    @Override
    public void updateLocomotive(Locomotive locomotive) {

    }

    @Override
    public SortedSet<LocomotiveGroup> getAllLocomotiveGroups() {
        return locomotiveServiceClient.getAllLocomotivesGroups();
    }

    @Override
    public void addLocomotiveGroup(LocomotiveGroup group) {

    }

    @Override
    public void removeLocomotiveGroup(LocomotiveGroup group) {

    }

    @Override
    public void updateLocomotiveGroup(LocomotiveGroup group) {

    }

    @Override
    public void clear() {

    }

    @Override
    public void init(LocomotiveServiceListener listener) {

    }

    @Override
    public void disconnect() {

    }

    public static void main(String[] args) {
        RestLocomotiveService s = new RestLocomotiveService();
        SortedSet<LocomotiveGroup> allLocomotiveGroups = s.getAllLocomotiveGroups();
        for (LocomotiveGroup allLocomotiveGroup : allLocomotiveGroups) {
            System.out.println(allLocomotiveGroup);
        }

    }
}
