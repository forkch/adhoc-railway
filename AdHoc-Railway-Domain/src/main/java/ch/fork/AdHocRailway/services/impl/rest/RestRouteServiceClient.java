package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.turnouts.Route;
import ch.fork.AdHocRailway.domain.turnouts.RouteGroup;
import retrofit.http.*;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestRouteServiceClient {

    @GET("/routeGroup")
    SortedSet<RouteGroup> getAllRouteGroups();

    @DELETE("/routeGroup")
    SortedSet<RouteGroup> deleteAllRouteGroups();

    @GET("/routeGroup/{id}")
    RouteGroup getRouteGroupById(@Path("id") String id);

    @POST("/routeGroup")
    RouteGroup addRouteGroup(@Body RouteGroup group);

    @PUT("/routeGroup")
    RouteGroup updateRouteGroup(@Body RouteGroup group);

    @DELETE("/routeGroup/{id}")
    RouteGroup deleteRouteGroup(@Path("id") String id);

    @GET("/route/{id}")
    Route getRouteById(@Path("id") String id);

    @POST("/route")
    Route addRoute(@Body Route route);

    @PUT("/route")
    Route updateRoute(@Body Route route);

    @DELETE("/route/{id}")
    Route deleteRoute(@Path("id") String id);
}
