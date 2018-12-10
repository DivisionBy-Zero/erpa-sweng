package ch.epfl.sweng.erpa.services.GCP;

import android.util.Log;

import java.util.List;
import java.util.Map;

import ch.epfl.sweng.erpa.model.Game;
import retrofit2.Call;

public class LazyGameStream extends LazyAsyncStream<Game> {
    private final GCPApi.GameInterface gi;
    private final Map<String, String> queries;

    public LazyGameStream(int chunks, Map<String, String> queries) {
        super(chunks);
        this.gi = GCPRemoteServicesProvider.getRetrofit().create(GCPApi.GameInterface.class);
        this.queries = queries;
        loadAhead(0);
    }

    @Override protected void loadAhead(int from) {
        Log.d("GCP Games look ahead", "Loading " + chunks + " elements starting at " + from);
        Call<List<Game>> call = gi.getGames(queries, from, chunks);
        this.loading = true;
        this.updateObservers();

        GCPRemoteServicesProvider.executeRequest(call, res -> {
            if (res != null) {
                this.elements.addAll(res);
                Log.d("loadAhead", "Successfully fetched elements from server; result size: " + res.size());
            }
            this.loading = false;
            this.updateObservers();
        }, throwable -> {
            this.loading = false;
            this.updateObservers();
            Log.d("loadAhead", "Error: something went wrong loading games", throwable);
        });
    }

}
