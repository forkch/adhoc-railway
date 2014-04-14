package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import retrofit.http.*;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestTurnoutServiceClient {

    @GET("/turnoutGroup")
    SortedSet<TurnoutGroup> getAllTurnoutGroups();

    @DELETE("/turnoutGroup")
    SortedSet<TurnoutGroup> deleteAllTurnoutGroups();

    @GET("/turnoutGroup/{id}")
    TurnoutGroup getTurnoutGroupById(@Path("id") String id);

    @POST("/turnoutGroup")
    TurnoutGroup addTurnoutGroup(@Body TurnoutGroup group);

    @PUT("/turnoutGroup")
    TurnoutGroup updateTurnoutGroup(@Body TurnoutGroup group);

    @DELETE("/turnoutGroup/{id}")
    TurnoutGroup deleteTurnoutGroup(@Path("id") String id);

    @GET("/turnout/{id}")
    Turnout getTurnoutById(@Path("id") String id);

    @POST("/turnout")
    Turnout addTurnout(@Body Turnout turnout);

    @PUT("/turnout")
    Turnout updateTurnout(@Body Turnout turnout);

    @DELETE("/turnout/{id}")
    Turnout deleteTurnout(@Path("id") String id);
}
