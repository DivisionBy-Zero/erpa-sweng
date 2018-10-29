package ch.epfl.sweng.erpa.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

import static android.content.ContentValues.TAG;

public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {
    @Inject GameService gs;
    // TODO(@Sapphie) change this once proper login is implemented
    @Inject UserProfile up;

    private Game game;

    @BindView(R.id.titleTextView) TextView title;
    @BindView(R.id.descriptionTextView) TextView description;
    @BindView(R.id.gmTextView) TextView gmName;
    @BindView(R.id.universeTextView) TextView universe;
    @BindView(R.id.difficultyTextView) TextView difficulty;
    @BindView(R.id.oneShotOrCampaignTextView) TextView type;
    @BindView(R.id.sessionNumberTextView) TextView numSessions;
    @BindView(R.id.sessionLengthTextView) TextView sessionLength;
    @BindView(R.id.participatingPlayersTextView) TextView playersInGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_viewer);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGame();
    }

    private void getGameOrFinish(Optional<Game> optGame) {
        if (optGame.isPresent()) {
            game = optGame.get();
        } else {
            Log.d(TAG, "onResume: could not find game in database. Exiting", new NoSuchElementException());
            finish();
        }
    }
    private String getGameId() {

        String gameId = getIntent().getStringExtra(GameService.PROP_INTENT_GAME);
        if (gameId == null) {
            Exception thrown = new IllegalArgumentException("Game Id not found");
            Log.d(TAG, "GameViewerActivity: no game id passed with intent", thrown);
            finish();
            throw new IllegalArgumentException();
        } else {
            return gameId;
        }
    }

    @OnClick(R.id.joinGameButton)
    void joinGame() {
        game = game.withPlayer(up.getUuid());
        gs.saveGame(game);
        updateGame();
    }

    private void updateGame() {
        getGameOrFinish(gs.getGame(getGameId()));
        updateFields();
    }
    @SuppressLint("SetTextI18n")
    private void updateFields() {
        Log.d(TAG, "onResume: Successfully fetched game");

        title.setText(game.getName());
        description.setText(game.getDescription());
        gmName.setText(game.getGmUuid());
        universe.setText(game.getUniverse());
        difficulty.setText(game.getDifficulty().toString());
        type.setText(game.getOneshotOrCampaign().toString());

        String numSessionsString = game.getNumberSessions().map(Object::toString).orElse("Unspecified");
        numSessions.setText(numSessionsString);

        String gameLength = game.getSessionLengthInMinutes().map(Object::toString).orElse("Unspecified");
        sessionLength.setText(gameLength);

        String playerInfo = Stream.of(game.getPlayersUuid()).reduce("", (elem, acc)->acc + ", " + elem);
        playersInGame.setText(playerInfo);

    }
}
