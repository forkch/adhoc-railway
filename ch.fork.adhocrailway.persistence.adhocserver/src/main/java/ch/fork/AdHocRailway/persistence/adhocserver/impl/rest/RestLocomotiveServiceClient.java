package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestLocomotiveServiceClient {

    @GET("/locomotiveGroup")
    Call<SortedSet<LocomotiveGroup>> getAllLocomotivesGroups();

    @DELETE("/locomotiveGroup")
    Call<SortedSet<LocomotiveGroup>> deleteAllLocomotiveGroups();

    @GET("/locomotiveGroup/{id}")
    Call<LocomotiveGroup> getLocomotiveGroupById(@Path("id") String id);

    @POST("/locomotiveGroup")
    Call<LocomotiveGroup> addLocomotiveGroup(@Body LocomotiveGroup group);

    @PUT("/locomotiveGroup")
    Call<LocomotiveGroup> updateLocomotiveGroup(@Body LocomotiveGroup group);

    @DELETE("/locomotiveGroup/{id}")
    Call<LocomotiveGroup> deleteLocomotiveGroup(@Path("id") String id);

    @GET("/locomotive/{id}")
    Call<Locomotive> getLocomotiveById(@Path("id") String id);

    @POST("/locomotive")
    Call<Locomotive> addLocomotive(@Body Locomotive locomotive);

    @PUT("/locomotive")
    Call<Locomotive> updateLocomotive(@Body Locomotive locomotive);

    @DELETE("/locomotive/{id}")
    Call<Locomotive> deleteLocomotive(@Path("id") String id);
}
