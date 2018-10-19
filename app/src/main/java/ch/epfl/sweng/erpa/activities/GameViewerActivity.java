package ch.epfl.sweng.erpa.activities;


import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;

import static android.content.ContentValues.TAG;

public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {

    public static final String EXTRA_GAME_KEY = "game";
    @Inject
    RemoteServicesProvider rsp;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_viewer);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        Optional<Game> optGame;
        String gameId = getGameId();
        optGame = rsp.getGameService().getGame(gameId);

        if (optGame.isPresent())
        {
            Game game = optGame.get();
            updateFields(game);
        }
        else
        {
            Log.d(TAG, "onResume: could not find game in database. Exiting", new NoSuchElementException());
            finish();
        }
    }


    private String getGameId()
    {

       String gameId = getIntent().getStringExtra(GameViewerActivity.EXTRA_GAME_KEY);
       if(gameId == null)
       {
           Exception thrown = new IllegalArgumentException("Game Id not found");
           Log.d(TAG, "GameViewerActivity: no game id passed with intent", thrown);
           finish();
           throw new IllegalArgumentException();
       }
       else
       {
           return gameId;
       }
    }
    private void updateFields(Game game)
    {
        setTextViewText(R.id.titleTextView,game.getName());
        setTextViewText(R.id.descriptionTextView,game.getDescription());
        setTextViewText(R.id.gmTextView,game.getGmUid());
        setTextViewText(R.id.universeTextView,game.getUniverse());
        Log.d(TAG, "onResume: Successfully fetched game");

        String miscInfo = String.format("Difficulty: %s\n" +
                        "%s\n" +
                        "Number of sessions: %s\n" +
                        "Session length: %s",
                game.getDifficulty(), game.getType(), game.getNumSessions(), game.getSessionLength());

        setTextViewText(R.id.generalInfoTextView, miscInfo);

    }



    private void setTextViewText(int id, String text)
    {
        ((TextView)findViewById(id)).setText(text);
    }
}