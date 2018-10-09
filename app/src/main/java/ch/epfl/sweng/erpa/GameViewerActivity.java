package ch.epfl.sweng.erpa;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.io.Serializable;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;

public class GameViewerActivity extends Activity {

    public static final String EXTRA_GAME_KEY = "game";
    @Override
    @Inject
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_viewer);

        //this should get injected
        RemoteServicesProvider rsp = new DummyRemoteServicesProvider();
        Optional<Game> optGame;
        String gameId = getIntent().getStringExtra(GameViewerActivity.EXTRA_GAME_KEY);
        if(gameId != null && (optGame = rsp.getGameService().getGame(gameId)).isPresent())
        {

            Game game = optGame.get();
            //very uninteresting code
            setTextViewText(R.id.titleTextView,game.getName());
            setTextViewText(R.id.descriptionTextView,game.getDescription());
            setTextViewText(R.id.gmTextView,game.getGmName());
            setTextViewText(R.id.universeTextView,game.getUniverse());

            String miscInfo = String.format("Difficulty: %s\n" +
                    "%s\n" +
                    "Number of sessions: %s\n" +
                    "Session length: %s",
                    game.getDifficulty(), game.getOneshotOrCampaign(), game.getNumberSessions(), game.getSessionLength());

            setTextViewText(R.id.generalInfoTextView, miscInfo);

            String playerInfo = String.format("At least %s players required. At most %s allowed. %s registered", game.getMinPlayer(),game.getMaxPayer(),game.getPlayers().size());
        }
        else
        {
            finish();
        }
    }



    private void setTextViewText(int id, String text)
    {
        ((TextView)findViewById(id)).setText(text);
    }
}
