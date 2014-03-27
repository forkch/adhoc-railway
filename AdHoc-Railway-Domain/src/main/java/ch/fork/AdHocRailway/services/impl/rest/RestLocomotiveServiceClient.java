package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import retrofit.http.GET;

import java.util.List;
import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestLocomotiveServiceClient {

    @GET("/locomotiveGroup")
    SortedSet<LocomotiveGroup> getAllLocomotivesGroups();
}
