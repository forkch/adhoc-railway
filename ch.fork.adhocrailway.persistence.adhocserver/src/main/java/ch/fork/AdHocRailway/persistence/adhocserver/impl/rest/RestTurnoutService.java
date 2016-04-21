package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.turnouts.SIOTurnoutCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import org.apache.log4j.Logger;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Created by fork on 4/4/14.
 */
public class RestTurnoutService implements TurnoutService {

    private static final Logger LOGGER = Logger.getLogger(RestTurnoutService.class);

    private final SIOTurnoutCallback sioTurnoutService;

    private final RestTurnoutServiceClient restTurnoutServiceClient;
    private TurnoutServiceListener listener;

    public RestTurnoutService(String endpointUrl, SIOService sioService, String uuid) {
        Retrofit retrofit = RestAdapterFactory.createRestAdapter(endpointUrl, uuid);
        restTurnoutServiceClient = retrofit.create(RestTurnoutServiceClient.class);
        sioTurnoutService = new SIOTurnoutCallback(sioService);
    }

    @Override
    public void init(TurnoutServiceListener listener) {
        this.listener = listener;
        sioTurnoutService.init(listener);
    }

    @Override
    public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
        try {
            return restTurnoutServiceClient.getAllTurnoutGroups().execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addTurnoutGroup(TurnoutGroup group) {
        TurnoutGroup addTurnoutGroup = null;
        try {
            addTurnoutGroup = restTurnoutServiceClient.addTurnoutGroup(group).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        group.setId(addTurnoutGroup.getId());
        if (listenerOk()) {
            listener.turnoutGroupAdded(group);
        }
    }

    @Override
    public void removeTurnoutGroup(TurnoutGroup group) {
        try {
            TurnoutGroup deleteTurnoutGroup = restTurnoutServiceClient.deleteTurnoutGroup(group.getId()).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.turnoutGroupRemoved(group);
        }
    }

    @Override
    public void updateTurnoutGroup(TurnoutGroup group) {
        try {
            restTurnoutServiceClient.updateTurnoutGroup(group).execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.turnoutGroupUpdated(group);
        }
    }

    @Override
    public void addTurnout(Turnout turnout) {
        Turnout addTurnout = null;
        try {
            addTurnout = restTurnoutServiceClient.addTurnout(turnout).execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        turnout.setId(addTurnout.getId());
        if (listenerOk()) {
            listener.turnoutAdded(turnout);
        }
    }

    @Override
    public void removeTurnout(Turnout turnout) {
        try {
            Turnout deleteTurnout = restTurnoutServiceClient.deleteTurnout(turnout.getId()).execute().body();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.turnoutRemoved(turnout);
        }
    }

    @Override
    public void updateTurnout(Turnout turnout) {
        try {
            Turnout updateTurnout = restTurnoutServiceClient.updateTurnout(turnout).execute().body();
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (listenerOk()) {
            listener.turnoutUpdated(turnout);
        }
    }

    @Override
    public void clear() {
        SortedSet<TurnoutGroup> turnoutGroups = null;
        try {
            turnoutGroups = restTurnoutServiceClient.deleteAllTurnoutGroups().execute().body();
        } catch (IOException e) {

            throw new IllegalStateException(e);
        }
        if (listenerOk()) {
            listener.turnoutsUpdated(turnoutGroups);
        }
    }


    @Override
    public void disconnect() {
        sioTurnoutService.disconnect();
    }

    private boolean listenerOk() {
        return listener != null;
    }
}
