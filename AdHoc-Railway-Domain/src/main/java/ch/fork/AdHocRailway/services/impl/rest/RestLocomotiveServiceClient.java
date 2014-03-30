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
    SortedSet<LocomotiveGroup> getAllLocomotivesGroups(@Header("adhoc-railway-appid") String appId);

    @DELETE("/locomotiveGroup")
    SortedSet<LocomotiveGroup> deleteAllLocomotiveGroups(@Header("adhoc-railway-appid") String appId);

    @GET("/locomotiveGroup/{id}")
    LocomotiveGroup getLocomotiveGroupById(@Path("id") String id, @Header("adhoc-railway-appid") String appId);

    @POST("/locomotiveGroup")
    LocomotiveGroup addLocomotiveGroup(@Body LocomotiveGroup group, @Header("adhoc-railway-appid") String appId);

    @PUT("/locomotiveGroup")
    LocomotiveGroup updateLocomotiveGroup(@Body LocomotiveGroup group, @Header("adhoc-railway-appid") String appId);

    @DELETE("/locomotiveGroup/{id}")
    LocomotiveGroup deleteLocomotiveGroup(@Path("id") String id, @Header("adhoc-railway-appid") String appId);

    @GET("/locomotive/{id}")
    Locomotive getLocomotiveById(@Path("id") String id, @Header("adhoc-railway-appid") String appId);

    @POST("/locomotive")
    LocomotiveGroup addLocomotive(@Body Locomotive locomotive, @Header("adhoc-railway-appid") String appId);

    @PUT("/locomotive")
    LocomotiveGroup updateLocomotive(@Body Locomotive locomotive, @Header("adhoc-railway-appid") String appId);

    @DELETE("/locomotive/{id}")
    LocomotiveGroup deleteLocomotive(@Path("id") String id, @Header("adhoc-railway-appid") String appId);
}
