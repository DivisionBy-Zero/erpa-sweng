package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.listeners.RecyclerViewClickListener;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameHolder> {
    private List<Game> games;
    private RecyclerViewClickListener mListener;

    public GameAdapter(List<Game> games, RecyclerViewClickListener listener) {
        this.games = new ArrayList<>(games);
        mListener = listener;
    }

    @NonNull
    @Override
    public GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_games_row,
                parent, false);
        return new GameHolder(view, mListener, parent.getContext());
    }

    @Override
    public void onBindViewHolder(@NonNull GameHolder gameHolder, int i) {
        gameHolder.setDetails(games.get(i));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class GameHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.difficultyBanner) TextView difficulty;
        @BindView(R.id.gameTitle) TextView title;
        @BindView(R.id.location) TextView location;
        @BindView(R.id.universeName) TextView universe;
        @BindView(R.id.nbPlayersInfo) TextView players;
        private RecyclerViewClickListener mListener;
        private Context context;

        public GameHolder(View itemView, RecyclerViewClickListener listener, Context context) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
            this.context = context;
            itemView.setOnClickListener(this);
        }

        public void setDetails(Game game) {
            difficulty.setText(game.getDifficulty().toString());
            difficulty.setBackgroundColor(getColor(game.getDifficulty()));
            title.setText(game.getName());
            location.setText("Lausanne");
            universe.setText(game.getUniverse());
            players.setText(getPlayersText(game));
        }

        private int getColor(@NonNull Game.Difficulty diff) {
            int id = 0;
            switch (diff) {
                case NOOB:
                    id = R.color.noobDifficultyColor;
                    break;
                case CHILL:
                    id = R.color.chillDifficultyColor;
                    break;
                case HARD:
                    id = R.color.hardDifficultyColor;
                    break;
            }
            return context.getResources().getColor(id);
        }

        private String getPlayersText(Game game) {
            String str = (new StringBuilder())
                    .append(game.getPlayersUuid().size())
                    .append("/")
                    .append((game.getMinPlayer().equals(game.getMaxPlayer())) ?
                            game.getMaxPlayer() : game.getMinPlayer() + "-" + game.getMaxPlayer())
                    .toString();
            return str;
        }

        @Override public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
