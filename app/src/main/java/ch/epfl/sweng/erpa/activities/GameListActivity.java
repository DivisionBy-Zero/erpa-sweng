package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.annimon.stream.Optional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.GameAdapter;

public class GameListActivity extends Activity {

    private ArrayList<Game> games = new ArrayList<>();

    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new GameAdapter(this, games);
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