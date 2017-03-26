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
                locomotive.setId(response.body().getId());

                if (listenerOk()) {
                    listener.locomotiveAdded(locomotive);
                }
            } else {
                throw new AdHocServiceException("Failed to add locomotive: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }

//        locomotiveServiceClient.addLocomotive(locomotive).enqueue(new LocomotiveServiceCallback<Locomotive>(listener, "Failed to add locomotive") {
//
//            @Override
//            protected void handleSuccessfulResponse(Response<Locomotive> response, LocomotiveServiceListener listener) {
//                locomotive.setId(response.body().getId());
//                LOGGER.debug("addLocomotive(): " + locomotive);
//
//                if (listenerOk()) {
//                    listener.locomotiveAdded(locomotive);
//                }
//            }
//        });

    }

    @Override
    public void removeLocomotive(final Locomotive locomotive) {
        locomotiveServiceClient.deleteLocomotive(locomotive.getId()).enqueue(new LocomotiveServiceCallback<Locomotive>(listener, "Failed to remove locomotive") {

            @Override
            protected void handleSuccessfulResponse(Response<Locomotive> response, LocomotiveServiceListener listener) {
                LOGGER.debug("removeLocomotive(): " + locomotive);

                if (listenerOk()) {
                    listener.locomotiveRemoved(locomotive);
                }
            }
        });
    }


    @Override
    public void updateLocomotive(final Locomotive locomotive) {
        locomotiveServiceClient.updateLocomotive(locomotive).enqueue(new LocomotiveServiceCallback<Locomotive>(listener, "Failed to update locomotive") {
            @Override
            protected void handleSuccessfulResponse(Response<Locomotive> response, LocomotiveServiceListener listener) {
                LOGGER.debug("updateLocomotive(): " + locomotive);
                if (listenerOk()) {
                    listener.locomotiveUpdated(locomotive);
                }
            }
        });
    }

    @Override
    public void getAllLocomotiveGroups() {
        locomotiveServiceClient.getAllLocomotivesGroups().enqueue(new LocomotiveServiceCallback<SortedSet<LocomotiveGroup>>(listener, "Failed to load all locomotive groups") {

            @Override
            protected void handleSuccessfulResponse(Response<SortedSet<LocomotiveGroup>> response, LocomotiveServiceListener listener) {
                final SortedSet<LocomotiveGroup> body = response.body();
                LOGGER.debug("getAllLocomotiveGroups(): " + body);
                listener.locomotivesUpdated(body);
            }
        });
    }

    @Override
    public void addLocomotiveGroup(final LocomotiveGroup group) {

        try {
            final Response<LocomotiveGroup> response = locomotiveServiceClient.addLocomotiveGroup(group).execute();
            if (response.isSuccessful()) {
                group.setId(response.body().getId());
            } else {
                throw new AdHocServiceException("Failed to add locomotive group: " + response.errorBody().string());
            }
        } catch (IOException e) {
            listener.failure(new AdHocServiceException(e));
        }

//        locomotiveServiceClient.addLocomotiveGroup(group).enqueue(new LocomotiveServiceCallback<LocomotiveGroup>(listener, "Failed to add locomotive group") {
//            @Override
//            protected void handleSuccessfulResponse(Response<LocomotiveGroup> response, LocomotiveServiceListener listener) {
//                final LocomotiveGroup addedLocomotiveGroup = response.body();
//                LOGGER.debug("addLocomotiveGroup(): " + addedLocomotiveGroup);
//                group.setId(addedLocomotiveGroup.getId());
//
//                if (listenerOk()) {
//                    listener.locomotiveGroupAdded(group);
//                }
//            }
//        });


    }

    @Override
    public void removeLocomotiveGroup(final LocomotiveGroup group) {
        locomotiveServiceClient.deleteLocomotiveGroup(group.getId()).enqueue(new LocomotiveServiceCallback<LocomotiveGroup>(listener, "Failed to remove locomotive group") {
            @Override
            protected void handleSuccessfulResponse(Response<LocomotiveGroup> response, LocomotiveServiceListener listener) {
                LOGGER.debug("removeLocomotiveGroup(): " + response.body());
                if (listenerOk()) {
                    listener.locomotiveGroupRemoved(group);
                }
            }
        });
    }

    @Override
    public void updateLocomotiveGroup(final LocomotiveGroup group) {
        locomotiveServiceClient.updateLocomotiveGroup(group).enqueue(new LocomotiveServiceCallback<LocomotiveGroup>(listener, "Failed to update locomotive group") {
            @Override
            protected void handleSuccessfulResponse(Response<LocomotiveGroup> response, LocomotiveServiceListener listener) {
                LOGGER.debug("updateLocomotiveGroup(): " + response.body());
                if (listenerOk()) {
                    listener.locomotiveGroupUpdated(group);
                }
            }
        });
    }

    @Override
    public void clear() {
        locomotiveServiceClient.deleteAllLocomotiveGroups().enqueue(new LocomotiveServiceCallback<SortedSet<LocomotiveGroup>>(listener, "Failed to clear locomotives") {
            @Override
            protected void handleSuccessfulResponse(Response<SortedSet<LocomotiveGroup>> response, LocomotiveServiceListener listener) {
                if (listenerOk()) {
                    listener.locomotivesUpdated(response.body());
                }

            }
        });

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

}
