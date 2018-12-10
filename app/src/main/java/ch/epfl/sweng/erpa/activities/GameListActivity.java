package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.function.BiConsumer;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.ObservableAsyncList;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.GameService;
import lombok.AllArgsConstructor;
import toothpick.Scope;

import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class GameListActivity extends DependencyConfigurationAgnosticActivity {
    public static final String GAME_LIST_ACTIVITY_CLASS_KEY = "Game list activity class key";
    public static final String GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY = "Game list viewer activity class key";
    private static final Map<GameListType, Integer> stringIdForGameListType =
        Collections.unmodifiableMap(new HashMap<GameListType, Integer>() {{
            put(GameListType.FIND_GAME, R.string.titleListGamesActivity);
            put(GameListType.PENDING_REQUEST, R.string.pendingRequestText);
            put(GameListType.CONFIRMED_GAMES, R.string.confirmedGamesText);
            put(GameListType.PAST_GAMES, R.string.pastGamesText);
            put(GameListType.HOSTED_GAMES, R.string.hostedGamesText);
            put(GameListType.PAST_HOSTED_GAMES, R.string.pastHostedGamesText);
        }});
    private static final Map<Game.Difficulty, Integer> colorIdForDifficulty =
        Collections.unmodifiableMap(new HashMap<Game.Difficulty, Integer>() {{
            put(Game.Difficulty.NOOB, R.color.noobDifficultyColor);
            put(Game.Difficulty.CHILL, R.color.chillDifficultyColor);
            put(Game.Difficulty.HARD, R.color.hardDifficultyColor);
        }});

    @BindView(R.id.game_list_activity_loading_panel) View loader;
    @BindView(R.id.game_list_drawer_layout) DrawerLayout myDrawerLayout;
    @BindView(R.id.game_list_navigation_view) NavigationView myNavigationView;
    @BindView(R.id.game_list_recycler_view) RecyclerView mRecyclerView;
    @BindView(R.id.game_list_swipe_refresh) SwipeRefreshLayout swipeRefreshLayout;
    @BindView(R.id.game_list_toolbar) Toolbar myToolbar;

    @Inject GameService gameService;
    @Inject OptionalDependencyManager optionalDependency;
    @Inject Scope scope;

    private Map<String, AsyncTask> asyncFetchThreads = new HashMap<>();
    private AsyncTaskService asyncTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_game_list);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;
        loader.setVisibility(View.VISIBLE);

        Bundle bundle = getIntent().getExtras();
        if (bundle == null) {
            finish();
            return;
        }

        addNavigationMenu(this, myDrawerLayout, myNavigationView, optionalDependency);
        setMenuInToolbar(this, myToolbar);
        setToolbarText((GameListType) bundle.getSerializable(GAME_LIST_ACTIVITY_CLASS_KEY));

        ObservableAsyncList<Game> games = gameService.getAllGames(new GameService.StreamRefiner());
        games.addObserver(this::updateGames);
        games.refreshDataAndReset();

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new GameListViewAdapter(gameService, games, (view, game) -> {
            Intent intent = new Intent(this, GameViewerActivity.class);
            intent.putExtra(GameService.PROP_INTENT_GAME_UUID, game.getUuid());
            putExtraOnGameViewer(intent);
            startActivity(intent);
        }));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_appbar, menu);
        // setToolbarText();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_actionSearch:
                Intent intent = new Intent(this, SortActivity.class);
                Optional.ofNullable(getIntent().getExtras()).ifPresent(intent::putExtras);
                startActivityForResult(intent, 1);
                return true;
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    protected void setToolbarText(GameListType gameListType) {
        @StringRes int toolbarTextId = Optional.ofNullable(stringIdForGameListType.get(gameListType))
            .orElse(R.string.title_example_for_toolbar_activity);
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(toolbarTextId);
        Optional.ofNullable(getSupportActionBar()).ifPresent(b -> b.setTitle(toolbarTextId));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            //TODO(@Ryker) add sorting using the data from data
        }
    }

    private void putExtraOnGameViewer(Intent intent) {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            GameListType gameListType = (GameListType) bundle.getSerializable(GAME_LIST_ACTIVITY_CLASS_KEY);
            intent.putExtra(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, gameListType);
        }
    }

    public void updateGames(ObservableAsyncList updatedGames) {
        Log.d("GameListType", "Player List update; Size: " + updatedGames.size());
        if (updatedGames.isLoading()) {
            loader.setVisibility(View.VISIBLE);
        } else {
            loader.setVisibility(View.GONE);
        }
        Optional.ofNullable(mRecyclerView.getAdapter()).ifPresent(RecyclerView.Adapter::notifyDataSetChanged);
    }

    public enum GameListType {FIND_GAME, PENDING_REQUEST, CONFIRMED_GAMES, PAST_GAMES, HOSTED_GAMES, PAST_HOSTED_GAMES}

    @AllArgsConstructor
    class GameListViewAdapter extends RecyclerView.Adapter<GameListViewAdapter.GameListViewElementHolder> {
        private GameService gameService;
        private ObservableAsyncList<Game> games;
        private BiConsumer<View, Game> onItemClickListener;

        @NonNull
        @Override
        public GameListViewElementHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.list_games_row, parent, false);
            return new GameListViewElementHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull GameListViewElementHolder gameHolder, int i) {
            gameHolder.configureItemViewport(games.get(i), onItemClickListener);
        }

        @Override
        public int getItemCount() {
            return games.size();
        }

        class GameListViewElementHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.difficultyBanner) TextView difficultyTV;
            @BindView(R.id.gameTitle) TextView titleTV;
            @BindView(R.id.location) TextView locationTV;
            @BindView(R.id.universeName) TextView universeTV;
            @BindView(R.id.currentNbPlayersProgressBar) View nbPlayersProgress;
            @BindView(R.id.currentNbPlayersInfo) TextView nbPlayersTV;
            @BindView(R.id.maxNbPlayersInfo) TextView maxPlayersTV;

            GameListViewElementHolder(View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void configureItemViewport(Game game, BiConsumer<View, Game> onItemViewClick) {
                difficultyTV.setText(game.getDifficulty().toString());
                int colorId = colorIdForDifficulty.get(game.getDifficulty());
                difficultyTV.setBackgroundColor(getResources().getColor(colorId));
                titleTV.setText(game.getTitle());
                locationTV.setText("Lausanne"); // TODO(@Roos): Get location from location services
                universeTV.setText(game.getUniverse());
                int maxPlayers = game.getMaxPlayers();
                int minPlayers = game.getMinPlayers();
                String maxPlayersString = (maxPlayers == minPlayers) ?
                    Integer.toString(maxPlayers) : minPlayers + "-" + maxPlayers;
                maxPlayersTV.setText(maxPlayersString);

                itemView.setOnClickListener(view -> onItemViewClick.accept(view, game));

                String gameUuid = game.getUuid();
                if (asyncFetchThreads.containsKey(gameUuid))
                    return;
                asyncFetchThreads.put(gameUuid, asyncTaskService.run(
                    () -> gameService.getGameJoinRequests(gameUuid),
                    joinGameRequests -> {
                        Log.d("postFetchGamePlayers", joinGameRequests.toString());
                        nbPlayersTV.setText(Integer.toString(joinGameRequests.size()));
                        nbPlayersTV.setVisibility(View.VISIBLE);
                        nbPlayersProgress.setVisibility(View.GONE);
                        asyncFetchThreads.remove(gameUuid);
                    }, exc -> this.handleErrorsRetrievingPlayerJoinGameRequests(exc, game)));
            }

            void handleErrorsRetrievingPlayerJoinGameRequests(Throwable exc, Game game) {
                String errorMessage = "Could not retrieve Game with UUID " + game.getUuid();
                nbPlayersTV.setText(errorMessage);
                nbPlayersProgress.setVisibility(View.GONE);
                Log.e("renderGameParticipant", errorMessage, exc);
            }
        }
    }
}
