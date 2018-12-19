package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.services.GameService;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

/**
 * An activity that is launched when a user wants to join a game
 * it takes an extra that is the uuid of the game we want to
 * attempt to join
 */
public class JoinGameActivity extends DependencyConfigurationAgnosticActivity {
    public static final String GAME_UUID_KEY = "game_uuid";
    @Inject GameService gs;
    @Inject UserSessionToken sessionToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_game);
        if (dependenciesNotReady()) return;

        AsyncTaskService asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);

        String gameUuid = getGameUuidFromIntent(getIntent());

        asyncTaskService.run(() -> gs.joinGame(gameUuid), joinRequest -> {
            createPopup("Join Request sent!", this, this::finish);
        }, this::handleException);
    }

    void handleException(Throwable exc) {
        createPopup("Could not submit join request: " + exc.getMessage(), this, this::finish);
    }

    static String getGameUuidFromIntent(Intent intent) {
        String gameUuid = intent.getStringExtra(GAME_UUID_KEY);
        if (gameUuid == null) {
            RuntimeException thrown = new IllegalArgumentException("GameUuid property not found");
            Log.e("retrieveUuid", "Cannot join Game", thrown);
            throw thrown;
        }
        return gameUuid;
    }
}
