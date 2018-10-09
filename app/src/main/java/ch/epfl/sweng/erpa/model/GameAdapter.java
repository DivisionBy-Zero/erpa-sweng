package ch.epfl.sweng.erpa.model;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import ch.epfl.sweng.erpa.R;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameHolder> {
    private Context context;
    private ArrayList<Game> games;

    public GameAdapter(Context context, ArrayList<Game> games) {
        this.context = context;
        this.games = games;
    }

    @NonNull
    @Override
    public GameHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_row, parent, false);
        return new GameHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameHolder gameHolder, int i) {
        gameHolder.setDetails(games.get(i));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    public static class GameHolder extends RecyclerView.ViewHolder {
        private TextView title, location, universe;

        public GameHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.gameTitle);
            location = itemView.findViewById(R.id.location);
            universe = itemView.findViewById(R.id.universeName);
        }

        public void setDetails(Game game) {
            title.setText(game.title);
            location.setText(game.location);
            universe.setText(game.universe);
        }

    }

}
