package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.turnouts.Turnout;
import ch.fork.AdHocRailway.model.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.turnouts.SIOTurnoutCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import com.google.common.collect.Sets;
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
            listener.failure(new AdHocServiceException(e));
            return Sets.newTreeSet();
        }
    }

    @Override
    public void addTurnoutGroup(TurnoutGroup group) {
        TurnoutGroup addTurnoutGroup;
        try {
            addTurnoutGroup = restTurnoutServiceClient.addTurnoutGroup(group).execute().body();
            group.setId(addTurnoutGroup.getId());
            if (listenerOk()) {
                listener.turnoutGroupAdded(addTurnoutGroup);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void removeTurnoutGroup(TurnoutGroup group) {
        try {
            TurnoutGroup deleteTurnoutGroup = restTurnoutServiceClient.deleteTurnoutGroup(group.getId()).execute().body();
            if (listenerOk()) {
                listener.turnoutGroupRemoved(deleteTurnoutGroup);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void updateTurnoutGroup(TurnoutGroup group) {
        try {
            TurnoutGroup updatedGroup = restTurnoutServiceClient.updateTurnoutGroup(group).execute().body();
            if (listenerOk()) {
                listener.turnoutGroupUpdated(updatedGroup);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void addTurnout(Turnout turnout) {
        Turnout addTurnout = null;
        try {
            addTurnout = restTurnoutServiceClient.addTurnout(turnout).execute().body();
            addTurnout.setTurnoutGroup(null);
            turnout.setId(addTurnout.getId());
            if (listenerOk()) {
                listener.turnoutAdded(addTurnout);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void removeTurnout(Turnout turnout) {
        try {
            Turnout deleteTurnout = restTurnoutServiceClient.deleteTurnout(turnout.getId()).execute().body();

            deleteTurnout.setTurnoutGroup(null);
            if (listenerOk()) {
                listener.turnoutRemoved(deleteTurnout);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void updateTurnout(Turnout turnout) {
        try {
            Turnout updateTurnout = restTurnoutServiceClient.updateTurnout(turnout).execute().body();
            updateTurnout.setTurnoutGroup(null);
            if (listenerOk()) {
                listener.turnoutUpdated(updateTurnout);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void clear() {
        SortedSet<TurnoutGroup> turnoutGroups = null;
        try {
            turnoutGroups = restTurnoutServiceClient.deleteAllTurnoutGroups().execute().body();
            if (listenerOk()) {
                listener.turnoutsUpdated(turnoutGroups);
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
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
