package ch.fork.adhocrailway.persistence.adhocserver.impl.rest;

import ch.fork.adhocrailway.model.turnouts.Turnout;
import ch.fork.adhocrailway.model.turnouts.TurnoutGroup;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public interface RestTurnoutServiceClient {

    @GET("/turnoutGroup")
    Call<SortedSet<TurnoutGroup>> getAllTurnoutGroups();

    @DELETE("/turnoutGroup")
    Call<SortedSet<TurnoutGroup>> deleteAllTurnoutGroups();

    @GET("/turnoutGroup/{id}")
    Call<TurnoutGroup> getTurnoutGroupById(@Path("id") String id);

    @POST("/turnoutGroup")
    Call<TurnoutGroup> addTurnoutGroup(@Body TurnoutGroup group);

    @PUT("/turnoutGroup")
    Call<TurnoutGroup> updateTurnoutGroup(@Body TurnoutGroup group);

    @DELETE("/turnoutGroup/{id}")
    Call<TurnoutGroup> deleteTurnoutGroup(@Path("id") String id);

    @GET("/turnout/{id}")
    Call<Turnout> getTurnoutById(@Path("id") String id);

    @POST("/turnout")
    Call<Turnout> addTurnout(@Body Turnout turnout);

    @PUT("/turnout")
    Call<Turnout> updateTurnout(@Body Turnout turnout);

    @DELETE("/turnout/{id}")
    Call<Turnout> deleteTurnout(@Path("id") String id);
}
