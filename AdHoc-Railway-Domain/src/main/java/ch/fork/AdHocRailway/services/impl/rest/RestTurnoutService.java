package ch.fork.AdHocRailway.services.impl.rest;

import ch.fork.AdHocRailway.domain.turnouts.Turnout;
import ch.fork.AdHocRailway.domain.turnouts.TurnoutGroup;
import ch.fork.AdHocRailway.services.TurnoutService;
import ch.fork.AdHocRailway.services.TurnoutServiceListener;
import ch.fork.AdHocRailway.services.impl.socketio.turnouts.SIOTurnoutCallback;
import ch.fork.AdHocRailway.utils.RestAdapterFactory;
import org.apache.log4j.Logger;
import retrofit.RestAdapter;

import java.util.SortedSet;

/**
 * Created by fork on 4/4/14.
 */
public class RestTurnoutService implements TurnoutService {

    private static final Logger LOGGER = Logger.getLogger(RestTurnoutService.class);

    private final SIOTurnoutCallback sioTurnoutService;

    private final RestTurnoutServiceClient restTurnoutServiceClient;
    private TurnoutServiceListener listener;

    public RestTurnoutService(String endpointUrl, String uuid) {
        RestAdapter restAdapter = RestAdapterFactory.createRestAdapter(endpointUrl, uuid);
        restTurnoutServiceClient = restAdapter.create(RestTurnoutServiceClient.class);
        sioTurnoutService = new SIOTurnoutCallback();
    }

    @Override
    public void init(TurnoutServiceListener listener) {
        this.listener = listener;
        sioTurnoutService.init(listener);
    }

    @Override
    public SortedSet<TurnoutGroup> getAllTurnoutGroups() {
        return restTurnoutServiceClient.getAllTurnoutGroups();
    }

    @Override
    public void addTurnoutGroup(TurnoutGroup group) {
        TurnoutGroup addTurnoutGroup = restTurnoutServiceClient.addTurnoutGroup(group);
        group.setId(addTurnoutGroup.getId());
        if (listenerOk()) {
            listener.turnoutGroupAdded(group);
        }
    }

    @Override
    public void removeTurnoutGroup(TurnoutGroup group) {
        TurnoutGroup deleteTurnoutGroup = restTurnoutServiceClient.deleteTurnoutGroup(group.getId());
        if (listenerOk()) {
            listener.turnoutGroupRemoved(group);
        }
    }

    @Override
    public void updateTurnoutGroup(TurnoutGroup group) {
        restTurnoutServiceClient.updateTurnoutGroup(group);
        if (listenerOk()) {
            listener.turnoutGroupUpdated(group);
        }
    }

    @Override
    public void addTurnout(Turnout turnout) {
        Turnout addTurnout = restTurnoutServiceClient.addTurnout(turnout);
        turnout.setId(addTurnout.getId());
        if (listenerOk()) {
            listener.turnoutAdded(turnout);
        }
    }

    @Override
    public void removeTurnout(Turnout turnout) {
        Turnout deleteTurnout = restTurnoutServiceClient.deleteTurnout(turnout.getId());
        if (listenerOk()) {
            listener.turnoutRemoved(turnout);
        }
    }

    @Override
    public void updateTurnout(Turnout turnout) {
        Turnout updateTurnout = restTurnoutServiceClient.updateTurnout(turnout);
        if (listenerOk()) {
            listener.turnoutUpdated(turnout);
        }
    }

    @Override
    public void clear() {
        SortedSet<TurnoutGroup> turnoutGroups = restTurnoutServiceClient.deleteAllTurnoutGroups();
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
