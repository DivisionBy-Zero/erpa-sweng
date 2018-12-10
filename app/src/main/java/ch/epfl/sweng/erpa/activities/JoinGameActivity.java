package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.services.GameService;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

public class JoinGameActivity extends DependencyConfigurationAgnosticActivity {
    @Inject GameService gs;
    @Inject UserSessionToken sessionToken;

    public static final String GAME_UUID_KEY = "game_uuid";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        if (dependenciesNotReady()) return;

        AsyncTaskService asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);

        String gameUuid = getGameUuidFromIntent();

        asyncTaskService.run(() -> gs.joinGame(gameUuid), joinRequest -> {
            createPopup("Join Request sent!", this, this::finish);
        }, exc -> {
            createPopup("Could not submit join request: " + exc.getMessage(), this, this::finish);
        });
    }

    private String getGameUuidFromIntent() {
        String gameUuid = getIntent().getStringExtra(GAME_UUID_KEY);
        if (gameUuid == null) {
            RuntimeException thrown = new IllegalArgumentException("GameUuid property not found");
            Log.e("retrieveUuid", "Cannot join Game" , thrown);
            finish();
            throw thrown;
        }
        return gameUuid;
    }
}
