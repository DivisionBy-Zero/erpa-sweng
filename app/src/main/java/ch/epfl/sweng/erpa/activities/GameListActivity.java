package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.listeners.ListLikeOnClickListener;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.GameAdapter;
import ch.epfl.sweng.erpa.services.GameService;

public class GameListActivity extends DependencyConfigurationAgnosticActivity {

    public static final String GAME_LIST_ACTIVTIY_CLASS_KEY = "Game list activity class key";

    @Inject public GameService gameService;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    private List<Game> games;
    private Resources resources;
    private Toolbar toolbar;
    private GameList gameList = GameList.FIND_GAME;
    private Bundle bundle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        DataBindingUtil.setContentView(this, R.layout.activity_game_list);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;
        Intent myIntent = getIntent();
        bundle = myIntent.getExtras();

        if (bundle != null) {
            gameList = (GameList) bundle.getSerializable(GAME_LIST_ACTIVTIY_CLASS_KEY);
            toolbar = findViewById(R.id.game_list_toolbar);
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setToolbarText(gameList);
        }

        resources = this.getResources();
        games = new ArrayList<>(gameService.getAll());
        // TODO(@Roos) remove when FIXME is fixed
        createListData();

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        ListLikeOnClickListener listener = (view, position) -> {
            Intent intent = new Intent(this, GameViewerActivity.class);
            intent.putExtra(GameService.PROP_INTENT_GAME, games.get(position).getGameUuid());
            startActivity(intent);
        };

        RecyclerView.Adapter mAdapter = new GameAdapter(games, listener);
        mRecyclerView.setAdapter(mAdapter);

        // TODO(@Roos) uncomment when FIXME is fixed
//        createListData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        setToolbarText(gameList);
        return true;
    }

    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
        return onOptionItemViewSelected(item.getItemId());

    }

    public boolean onOptionItemViewSelected(int id) {
        // Handle action bar item clicks here.
        bundle = getIntent().getExtras();
        switch (id) {
            case R.id.actionSearch:
                Intent intent = new Intent(this, SortActivity.class);
                intent.putExtras(bundle);
                startActivityForResult(intent, 1);
                return true;
            default:
        }
        return super.onOptionsItemSelected(findViewById(id));
    }

    // FIXME(@Roos) list doesn't appear correctly the first time it's rendered
    private void createListData() {
        if (games.isEmpty()) {
            Game.GameBuilder gb = Game.builder()
                    .description("")
                    .gmUuid("")
                    .minPlayer(1)
                    .name("test")
                    .numberSessions(Optional.empty())
                    .oneshotOrCampaign(Game.OneshotOrCampaign.ONESHOT)
                    .playersUuid(new HashSet<>())
                    .sessionLengthInMinutes(Optional.empty())
                    .universe("DnD");
            for (int i = 0; i < new Random().nextInt(20) + 5; i++) {
                gb.difficulty(Game.Difficulty.values()[new Random().nextInt(3)])
                        .gameUuid(Integer.toString(i))
                        .maxPlayer(new Random().nextInt(6) + 1);
                games.add(gb.build());
                gameService.saveGame(gb.build());
            }
//            mAdapter.notifyDataSetChanged();
        }
    }

    protected void setToolbarText(GameList gameList) {
        @StringRes int id = R.string.title_example_for_toolbar_activity;;
        switch (gameList) {
            case FIND_GAME:
                id = R.string.titleListGamesActivity;
                break;
            case PENDING_REQUEST:
                id = R.string.pendingRequestText;
                break;
            case CONFIRMED_GAMES:
                id = R.string.confirmedGamesText;
                break;
            case PAST_GAMES:
                id = R.string.pastGamesText;
                break;
            case HOSTED_GAMES:
                id = R.string.hostedGamesText;
                break;
            case PAST_HOSTED_GAMES:
                id = R.string.pastHostedGamesText;
                break;
            default:
        }
        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        toolbarTitle.setText(id);
        getSupportActionBar().setTitle(id);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            //TODO(@Ryker) add sorting using the data from data
        }

    }

    public enum GameList {FIND_GAME, PENDING_REQUEST, CONFIRMED_GAMES, PAST_GAMES, HOSTED_GAMES, PAST_HOSTED_GAMES}
}
