package ch.epfl.sweng.erpa.model;

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
        return new GameHolder(view, mListener);
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
        @BindView(R.id.gameTitle) TextView title;
        @BindView(R.id.location) TextView location;
        @BindView(R.id.universeName) TextView universe;
        private RecyclerViewClickListener mListener;

        public GameHolder(View itemView, RecyclerViewClickListener listener) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            mListener = listener;
            itemView.setOnClickListener(this);
        }

        public void setDetails(Game game) {
            title.setText(game.getName());
            location.setText("Lausanne");
            universe.setText(game.getUniverse());
        }

        @Override public void onClick(View v) {
            mListener.onClick(v, getAdapterPosition());
        }
    }
}
