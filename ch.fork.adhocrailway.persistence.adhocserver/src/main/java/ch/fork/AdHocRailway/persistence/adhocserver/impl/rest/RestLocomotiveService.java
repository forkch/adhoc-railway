package ch.fork.AdHocRailway.persistence.adhocserver.impl.rest;

import ch.fork.AdHocRailway.model.locomotives.Locomotive;
import ch.fork.AdHocRailway.model.locomotives.LocomotiveGroup;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.SIOService;
import ch.fork.AdHocRailway.persistence.adhocserver.impl.socketio.locomotives.SIOLocomotiveCallback;
import ch.fork.AdHocRailway.persistence.adhocserver.util.RestAdapterFactory;
import ch.fork.AdHocRailway.services.AdHocServiceException;
import ch.fork.AdHocRailway.services.LocomotiveService;
import ch.fork.AdHocRailway.services.LocomotiveServiceListener;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import java.io.IOException;
import java.util.SortedSet;

/**
 * Created by fork on 3/27/14.
 */
public class RestLocomotiveService implements LocomotiveService {
    private static final Logger LOGGER = Logger.getLogger(RestLocomotiveService.class);

    private final SIOLocomotiveCallback sioLocomotiveService;

    private final RestLocomotiveServiceClient locomotiveServiceClient;
    private LocomotiveServiceListener listener;

    public RestLocomotiveService(String endpointURL, SIOService sioService, String uuid) {
        final Retrofit retrofit = RestAdapterFactory.createRestAdapter(endpointURL, uuid);
        locomotiveServiceClient = retrofit.create(RestLocomotiveServiceClient.class);
        sioLocomotiveService = new SIOLocomotiveCallback(sioService);
    }

    @Override
    public void addLocomotive(final Locomotive locomotive) {

        try {
            final Response<Locomotive> response = locomotiveServiceClient.addLocomotive(locomotive).execute();
            if (response.isSuccessful()) {
                Locomotive addedLocomotive = response.body();
                locomotive.setId(addedLocomotive.getId());
                addedLocomotive.setGroup(null);
                if (listenerOk()) {
                    listener.locomotiveAdded(addedLocomotive);
                }
            } else {
                throw new AdHocServiceException("Failed to add locomotive: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }

    }

    @Override
    public void removeLocomotive(final Locomotive locomotive) {
        try {
            final Response<Locomotive> response = locomotiveServiceClient.deleteLocomotive(locomotive.getId()).execute();
            if (response.isSuccessful()) {
                Locomotive removedLocomotive = response.body();
                removedLocomotive.setGroup(null);
                if (listenerOk()) {
                    listener.locomotiveRemoved(removedLocomotive);
                }
            } else {
                throw new AdHocServiceException("Failed to remove locomotive: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }


    @Override
    public void updateLocomotive(final Locomotive locomotive) {
        try {
            final Response<Locomotive> response = locomotiveServiceClient.updateLocomotive(locomotive).execute();
            if (response.isSuccessful()) {
                Locomotive updatedLocomotive = response.body();
                locomotive.setId(updatedLocomotive.getId());
                updatedLocomotive.setGroup(null);
                if (listenerOk()) {
                    listener.locomotiveUpdated(updatedLocomotive);
                }
            } else {
                throw new AdHocServiceException("Failed to update locomotive: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void getAllLocomotiveGroups() {
        try {
            final Response<SortedSet<LocomotiveGroup>> response = locomotiveServiceClient.getAllLocomotivesGroups().execute();
            if (response.isSuccessful()) {
                SortedSet<LocomotiveGroup> updatedLocomotives = response.body();
                if (listenerOk()) {
                    listener.locomotivesUpdated(updatedLocomotives);
                }
            } else {
                throw new AdHocServiceException("Failed to get all locomotives: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void addLocomotiveGroup(final LocomotiveGroup group) {
        try {
            final Response<LocomotiveGroup> response = locomotiveServiceClient.addLocomotiveGroup(group).execute();
            if (response.isSuccessful()) {
                group.setId(response.body().getId());

                if (listenerOk()) {
                    listener.locomotiveGroupAdded(group);
                }

            } else {
                throw new AdHocServiceException("Failed to add locomotive group: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void removeLocomotiveGroup(final LocomotiveGroup group) {
        try {
            final Response<LocomotiveGroup> response =
                    locomotiveServiceClient.deleteLocomotiveGroup(group.getId()).execute();
            if (response.isSuccessful()) {

                if (listenerOk()) {
                    listener.locomotiveGroupRemoved(response.body());
                }
            } else {
                throw new AdHocServiceException("Failed to remove locomotive group: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }

    }

    @Override
    public void updateLocomotiveGroup(final LocomotiveGroup group) {
        try {
            final Response<LocomotiveGroup> response =
                    locomotiveServiceClient.updateLocomotiveGroup(group).execute();
            if (response.isSuccessful()) {

                if (listenerOk()) {
                    listener.locomotiveGroupUpdated(response.body());
                }
            } else {
                throw new AdHocServiceException("Failed to update locomotive group: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void clear() {

        try {
            final Response<SortedSet<LocomotiveGroup>> response =
                    locomotiveServiceClient.deleteAllLocomotiveGroups().execute();
            if (response.isSuccessful()) {

                if (listenerOk()) {
                    listener.locomotivesUpdated(response.body());
                }
            } else {
                throw new AdHocServiceException("Failed to clear locomotives: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }
    }

    @Override
    public void init(LocomotiveServiceListener listener) {
        this.listener = listener;
        sioLocomotiveService.init(listener);
    }

    @Override
    public void disconnect() {
        sioLocomotiveService.disconnect();

    }

    @Override
    public void addLocomotiveGroups(SortedSet<LocomotiveGroup> groups) {
        for (LocomotiveGroup group : groups) {
            try {
                final Response<LocomotiveGroup> response = locomotiveServiceClient.addLocomotiveGroup(group).execute();
                if (response.isSuccessful()) {
                    group.setId(response.body().getId());
                } else {
                    throw new AdHocServiceException("Failed to add locomotive groups: " + response.errorBody().string());
                }
            } catch (IOException e) {
                listener.failure(new AdHocServiceException(e));
            }
        }
    }

    private boolean listenerOk() {
        return listener != null;
    }

    static abstract class LocomotiveServiceCallback<T> implements Callback<T> {

        private final LocomotiveServiceListener listener;
        private final String failureMessage;

        LocomotiveServiceCallback(LocomotiveServiceListener listener, String failureMessage) {
            this.listener = listener;
            this.failureMessage = failureMessage;
        }

        @Override
        public void onResponse(Call<T> call, Response<T> response) {
            if (response.isSuccessful()) {
                handleSuccessfulResponse(response, listener);
            } else {
                handleUnsuccesfulResponse(response, failureMessage);
            }
        }

        protected abstract void handleSuccessfulResponse(Response<T> response, LocomotiveServiceListener listener);

        private void handleUnsuccesfulResponse(Response response, String message) {
            try {
                final JsonElement parse = new JsonParser().parse(response.errorBody().string());
                String messageFromServer = parse.getAsJsonObject().get("msg").getAsString();
                listener.failure(new AdHocServiceException(String.format("%s: %s", message, messageFromServer)));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onFailure(Call<T> call, Throwable t) {
            listener.failure(new AdHocServiceException(t));
        }
    }

}
