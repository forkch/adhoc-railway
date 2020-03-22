package ch.fork.adhocrailway.persistence.adhocserver.impl.rest;

import ch.fork.adhocrailway.model.turnouts.Route;
import ch.fork.adhocrailway.model.turnouts.RouteGroup;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestRouteServiceClient {

    @GET("/routeGroup")
    Call<SortedSet<RouteGroup>> getAllRouteGroups();

    @DELETE("/routeGroup")
    Call<SortedSet<RouteGroup>> deleteAllRouteGroups();

    @GET("/routeGroup/{id}")
    Call<RouteGroup> getRouteGroupById(@Path("id") String id);

    @POST("/routeGroup")
    Call<RouteGroup> addRouteGroup(@Body RouteGroup group);

    @PUT("/routeGroup")
    Call<RouteGroup> updateRouteGroup(@Body RouteGroup group);

    @DELETE("/routeGroup/{id}")
    Call<RouteGroup> deleteRouteGroup(@Path("id") String id);

    @GET("/route/{id}")
    Call<Route> getRouteById(@Path("id") String id);

    @POST("/route")
    Call<Route> addRoute(@Body Route route);

    @PUT("/route")
    Call<Route> updateRoute(@Body Route route);

    @DELETE("/route/{id}")
    Call<Route> deleteRoute(@Path("id") String id);
}
