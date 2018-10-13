package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.annimon.stream.Optional;

import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.inject.Inject;
import javax.inject.Named;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.listeners.RecyclerViewClickListener;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.GameAdapter;
import ch.epfl.sweng.erpa.services.GameService;

public class GameListActivity extends DependencyConfigurationAgnosticActivity {

    @Inject @Named("List of games") List<Game> games;

    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);

        ButterKnife.bind(this);

        mRecyclerView.setHasFixedSize(true);

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);


        RecyclerViewClickListener listener = (view, position) -> {
            Intent intent = new Intent(this, GameViewerActivity.class);
            intent.putExtra(GameService.PROP_INTENT_GAMEUUID, games.get(position).getGameUuid());
            startActivity(intent);
        };

        mAdapter = new GameAdapter(games, listener);
        mRecyclerView.setAdapter(mAdapter);

        createListData();
    }

    private void createListData() {
        if (games.isEmpty()) {
            Game game = Game.builder()
                    .gameUuid("")
                    .gmUuid("")
                    .playersUuid(new HashSet<>())
                    .name("test")
                    .minPlayer(1)
                    .maxPlayer(3)
                    .difficulty(Game.Difficulty.NOOB)
                    .universe("DnD")
                    .oneshotOrCampaign(Game.OneshotOrCampaign.ONESHOT)
                    .numberSessions(Optional.empty())
                    .sessionLengthInMinutes(Optional.empty())
                    .description("")
                    .build();
            for (int i = 0; i < new Random().nextInt(20) + 5; i++) games.add(game);
            mAdapter.notifyDataSetChanged();
        }
    }
}
