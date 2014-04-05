package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.locomotives.Locomotive;
import ch.fork.AdHocRailway.domain.locomotives.LocomotiveGroup;
import retrofit.http.*;

import java.util.List;
import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestLocomotiveServiceClient {

    @GET("/locomotiveGroup")
    SortedSet<LocomotiveGroup> getAllLocomotivesGroups();

    @DELETE("/locomotiveGroup")
    SortedSet<LocomotiveGroup> deleteAllLocomotiveGroups();

    @GET("/locomotiveGroup/{id}")
    LocomotiveGroup getLocomotiveGroupById(@Path("id") String id);

    @POST("/locomotiveGroup")
    LocomotiveGroup addLocomotiveGroup(@Body LocomotiveGroup group);

    @PUT("/locomotiveGroup")
    LocomotiveGroup updateLocomotiveGroup(@Body LocomotiveGroup group);

    @DELETE("/locomotiveGroup/{id}")
    LocomotiveGroup deleteLocomotiveGroup(@Path("id") String id);

    @GET("/locomotive/{id}")
    Locomotive getLocomotiveById(@Path("id") String id);

    @POST("/locomotive")
    Locomotive addLocomotive(@Body Locomotive locomotive);
    
    @PUT("/locomotive")
    Locomotive updateLocomotive(@Body Locomotive locomotive);
    
    @DELETE("/locomotive/{id}")
    Locomotive deleteLocomotive(@Path("id") String id);
    }
