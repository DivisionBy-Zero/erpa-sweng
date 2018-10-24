package ch.epfl.sweng.erpa.activities;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

import static android.content.ContentValues.TAG;

public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {
    @Inject RemoteServicesProvider rsp;

    @BindView(R.id.titleTextView) TextView title;
    @BindView(R.id.descriptionTextView) TextView description;
    @BindView(R.id.gmTextView) TextView gmName;
    @BindView(R.id.universeTextView) TextView universe;
    @BindView(R.id.difficultyTextView) TextView difficulty;
    @BindView(R.id.oneShotOrCampaignTextView) TextView type;
    @BindView(R.id.sessionNumberTextView) TextView numSessions;
    @BindView(R.id.sessionLength) TextView sessionLength;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_viewer);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Optional<Game> optGame;
        String gameId = getGameId();
        optGame = rsp.getGameService().getGame(gameId);

        if (optGame.isPresent()) {
            Game game = optGame.get();
            updateFields(game);
        } else {
            Log.d(TAG, "onResume: could not find game in database. Exiting", new NoSuchElementException());
            finish();
        }
    }

    private String getGameId() {
        String gameId = getIntent().getStringExtra(GameService.PROP_INTENT_GAMEUUID);
        if (gameId == null) {
            Exception thrown = new IllegalArgumentException("Game Id not found");
            Log.d(TAG, "GameViewerActivity: no game id passed with intent", thrown);
            finish();
            throw new IllegalArgumentException();
        } else {
            return gameId;
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateFields(Game game) {
        Log.d(TAG, "onResume: Successfully fetched game");

        title.setText(game.getName());
        description.setText(game.getDescription());
        gmName.setText(game.getGmUuid());
        universe.setText(game.getUniverse());
        difficulty.setText(game.getDifficulty().toString());
        type.setText(game.getOneshotOrCampaign().toString());
        numSessions.setText(game.getNumberSessions().toString());
        sessionLength.setText(game.getSessionLengthInMinutes().toString());
    }
}