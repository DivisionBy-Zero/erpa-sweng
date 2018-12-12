package ch.epfl.sweng.erpa.services.GCP;

import android.support.annotation.NonNull;
import android.support.v4.util.Consumer;
import android.util.Log;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.util.NoSuchElementException;

import javax.inject.Inject;
import javax.inject.Singleton;

import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import lombok.Getter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@Singleton
public class GCPRemoteServicesProvider implements RemoteServicesProvider {
    private static final String API_URL = "https://erpa-sweng.appspot.com";
    // Public for debugging purposes :)
    public static Gson gson = new GsonBuilder()
        .registerTypeAdapterFactory(Adapters.GameDifficultyTypeAdapter.FACTORY)
        .registerTypeAdapterFactory(Adapters.GameStatusTypeAdapter.FACTORY)
        .registerTypeAdapterFactory(Adapters.OptionalTypeAdapter.FACTORY)
        .registerTypeAdapterFactory(Adapters.PlayerJoinRequestStatusTypeAdapter.FACTORY)
        .create();
    @Getter @NonNull private static Retrofit retrofit = new Retrofit.Builder()
        .baseUrl(API_URL)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build();
    @Getter @Inject GCPGameService gameService;
    @Getter @Inject GCPUserManagementService userProfileService;


    @Inject public GCPRemoteServicesProvider() {
    }

    static <T> void executeRequest(Call<T> call, Consumer<T> callback, Consumer<Throwable> exceptionConsumer) {
        call.enqueue(new Callback<T>() {
            @Override
            public void onResponse(@NonNull Call<T> call0, @NonNull Response<T> response) {
                Optional.of(response)
                    .filter(Response::isSuccessful)
                    .executeIfAbsent(() -> {
                        ServerException exception = new ServerException(response.code(),
                            Exceptional.of(response::errorBody).map(ResponseBody::string).get());
                        Log.w("executeRequest", exception);
                        exceptionConsumer.accept(exception);
                    })
                    .map(Response::body)
                    .executeIfPresent(callback::accept);
            }

            @Override
            public void onFailure(@NonNull Call<T> call0, Throwable t) {
                exceptionConsumer.accept(t);
            }
        });
    }

    static <T> Response<T> executeAndThrowOnError(Call<T> call) throws IOException, ServerException {
        assert call != null;
        Response<T> response = call.execute();
        int responseCode = response.code();
        if (responseCode == 404) {
            throw new NoSuchElementException("Could not find requested resource in the server.");
        } else if (!response.isSuccessful()) {
            String responseError = Exceptional.of(response::errorBody).map(ResponseBody::string).get();
            Log.e("executeRequest", String.format("HTTP Error %d: %s. Request: %s",
                responseCode, responseError, response.raw().request().url().toString()));
            throw new ServerException(responseCode, responseError);
        }
        return response;
    }

    /**
     * Wraps the call in an optional and discards any errors.
     */
    static <T> Optional<T> callAndReturnOptional(Call<T> call) {
        return Exceptional.of(() -> executeAndThrowOnError(call))
            .getOptional()
            .filter(Response::isSuccessful)
            .map(Response::body);
    }

    @Override public String getFriendlyProviderName() {
        return "Google Cloud Platform";
    }

    @Override public String getFriendlyProviderDescription() {
        return "Queries the Google Cloud Platform applet ERPA";
    }

    @Override public void terminate() {
    }
}
