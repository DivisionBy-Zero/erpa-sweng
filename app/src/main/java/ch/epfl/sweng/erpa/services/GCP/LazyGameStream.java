package ch.epfl.sweng.erpa.services.GCP;

import android.util.Log;

import java.util.List;
import java.util.Map;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.LazyAsyncStream;
import retrofit2.Call;

public class LazyGameStream extends LazyAsyncStream<Game> {
    private final GCPApi.GameInterface gi;
    private final Map<String, String> refinements;

    public LazyGameStream(int chunks, Map<String, String> refinements) {
        super(chunks);
        this.gi = GCPRemoteServicesProvider.getRetrofit().create(GCPApi.GameInterface.class);
        this.refinements = refinements;
        loadAhead(0);
    }

    @Override public void loadAhead(int from) {
        Log.d("GCP Games look ahead", "Loading " + chunks + " elements starting at " + from);
        Call<List<Game>> call = gi.getGames(refinements, from, chunks);
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
