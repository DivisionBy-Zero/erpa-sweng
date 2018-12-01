package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.GameListActivity.GameList;
import ch.epfl.sweng.erpa.listeners.ListLikeOnClickListener;
import ch.epfl.sweng.erpa.model.GameViewerAdapter;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GameList.FIND_GAME;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GameList.HOSTED_GAMES;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GameList.PENDING_REQUEST;

public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {
    @Inject GameService gs;
    // TODO(@Sapphie) change this once proper login is implemented
    @Inject UserProfile up;
    @BindView(R.id.gameViewerExpandableList) ExpandableListView expListView;
    @BindView(R.id.joinGameButton) Button joinGameButton;
    private Game game;
    private List<String> mListDataHeader = new ArrayList<>();
    private HashMap<String, LinearLayout> mListDataChild = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_viewer);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Intent myIntent = getIntent();
        Bundle bundle = myIntent.getExtras();
        finishIfNull(bundle);

        updateGame();

        GameList gameList = (GameList) bundle.getSerializable(
                GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY);
        finishIfNull(gameList);
        setJoinButton(gameList);

        prepareListData(gameList);

        GameViewerAdapter mListAdapter = new GameViewerAdapter(this, game, mListDataHeader,
                mListDataChild);
        expListView.setAdapter(mListAdapter);
    }

    public void setJoinButton(GameList gameList) {
        int joinGameButtonVisibility = gameList == FIND_GAME ? VISIBLE : GONE;
        joinGameButton.setVisibility(joinGameButtonVisibility);
    }

    private void prepareListData(GameList gameList) {
        View view = LayoutInflater.from(this).inflate(R.layout.game_viewer_expandable_list_item,
                null);

        mListDataHeader.add("Game Information");
        mListDataChild.put(mListDataHeader.get(0),
                view.findViewById(R.id.gameViewerGameDescription));

        if (!gameList.equals(FIND_GAME) && !gameList.equals(PENDING_REQUEST)) {
            ArrayList<String> uuidArray = new ArrayList<>(game.getPlayersUuid());

            mListDataHeader.add("List of Players");
            mListDataChild.put(mListDataHeader.get(1),
                    view.findViewById(R.id.gameViewerPlayerList));

            ListLikeOnClickListener mListener = ((v, position) -> {
                game = game.removePlayer(uuidArray.get(position));
                gs.saveGame(game);
                uuidArray.remove(position);
            });

            ListView playerListView = view.findViewById(R.id.gameViewerPlayerListView);
            PlayerAdapter myPlayerAdapter = new PlayerAdapter(this, uuidArray,
                    gameList.equals(HOSTED_GAMES),
                    mListener);
            playerListView.setAdapter(myPlayerAdapter);
        }
    }

    private void getGameOrFinish(Optional<Game> optGame) {
        if (optGame.isPresent()) {
            game = optGame.get();
        } else {
            Log.d(TAG, "onResume: could not find game in database. Exiting",
                    new NoSuchElementException());
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

    @butterknife.Optional @OnClick(R.id.joinGameButton)
    public void joinGame() {
        game = game.withPlayer(up.getUuid());
        gs.saveGame(game);
        updateGame();
    }

    private void updateGame() {
        getGameOrFinish(gs.getGame(getGameId()));
    }

    private void finishIfNull(Object obj) {
        if (obj == null) finish();
    }
}

