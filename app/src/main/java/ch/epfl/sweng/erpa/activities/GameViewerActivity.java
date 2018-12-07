package ch.epfl.sweng.erpa.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.listeners.ListLikeOnClickListener;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserProfileService;

import static android.content.ContentValues.TAG;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GameList.HOSTED_GAMES;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onNavigationItemMenuSelected;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setUsernameInMenu;

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

    @BindView(R.id.gameViewerPlayerListView) ListView playerListView;

    @BindView(R.id.joinGameButton) Button joinGameButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_game_viewer);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateGame();

        Intent myIntent = getIntent();
        Bundle bundle = myIntent.getExtras();
        finishIfNull(bundle);

        GameListActivity.GameList gameList = (GameListActivity.GameList) bundle.getSerializable(
                GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY);

        if (!gameList.equals(GameListActivity.GameList.FIND_GAME))
            joinGameButton.setVisibility(View.INVISIBLE);

        if (!gameList.equals(GameListActivity.GameList.FIND_GAME) && !gameList.equals(GameListActivity.GameList.PENDING_REQUEST)) {
            ArrayList<String> uuidArray = new ArrayList<String>(game.getPlayersUuid());

            ListLikeOnClickListener mListener = ((v, position) -> {
                game = game.removePlayer(uuidArray.get(position));
                gs.saveGame(game);
                uuidArray.remove(position);
            });

            PlayerAdapter myPlayerAdapter = new PlayerAdapter(this, uuidArray,
                    gameList.equals(HOSTED_GAMES),
                    mListener);
            playerListView.setAdapter(myPlayerAdapter);
            setListViewHeightBasedOnChildren(playerListView);
        }

        addNavigationMenu(this, findViewById(R.id.game_viewer_drawer_layout), findViewById(R.id.game_viewer_navigation_view), up);

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

    }

    /*Method for Setting the Height of the ListView dynamically.
     Hack to fix the issue of not showing all the items of the ListView
     when placed inside a ScrollView  */
    public static void setListViewHeightBasedOnChildren(ListView listView) {
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null)
            return;

        int desiredWidth = View.MeasureSpec.makeMeasureSpec(listView.getWidth(), View.MeasureSpec.UNSPECIFIED);
        int totalHeight = 0;
        View view = null;
        for (int i = 0; i < listAdapter.getCount(); i++) {
            view = listAdapter.getView(i, view, listView);
            if (i == 0)
                view.setLayoutParams(new ViewGroup.LayoutParams(desiredWidth, ConstraintLayout.LayoutParams.WRAP_CONTENT));

            view.measure(desiredWidth, View.MeasureSpec.UNSPECIFIED);
            totalHeight += view.getMeasuredHeight();
        }
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        listView.setLayoutParams(params);
    }

    private void finishIfNull(Object obj) {
        if (obj == null) finish();
    }

}
