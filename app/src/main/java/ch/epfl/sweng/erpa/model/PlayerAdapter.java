package ch.epfl.sweng.erpa.model;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.listeners.ListLikeOnClickListener;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class PlayerAdapter extends ArrayAdapter<String> {

    private Boolean isHostedGame;
    private ListLikeOnClickListener mListener;

    public PlayerAdapter(Context context,
                         List<String> myPlayersUuIdsList, Boolean isHostedGame,
                         ListLikeOnClickListener listener) {
        super(context, -1, myPlayersUuIdsList);
        this.isHostedGame = isHostedGame;
        mListener = listener;
    }

    @SuppressLint("ViewHolder") @NonNull @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Log.i("BONJOUR", "Element " + position);
        int mResources = R.layout.game_viewer_player;
        View rowView = LayoutInflater.from(getContext()).inflate(mResources, parent,
                false);

        PlayerHolder playerHolder = new PlayerHolder(rowView, position, mListener);
        playerHolder.setDetails(getItem(position), isHostedGame);
        rowView.setTag(playerHolder);

        return rowView;
    }

    public class PlayerHolder {
        @BindView(R.id.playerTextView) TextView playerX;
        @BindView(R.id.playerGameViewer) TextView playerName;
        @BindView(R.id.buttonRemovePlayer) Button button;
        private ListLikeOnClickListener mListener;
        private int position;

        public PlayerHolder(View v, int position, ListLikeOnClickListener listener) {
            ButterKnife.bind(this, v);
            mListener = listener;
            this.position = position;
        }

        public void setDetails(String uuid, Boolean isHostedGame) {
            playerName.setText(uuid);
            if (isHostedGame) {
                button.setVisibility(VISIBLE);
                button.setOnClickListener(v -> mListener.onClick(v, position));
            } else
                button.setVisibility(GONE);
            Resources res = getContext().getResources();
            String text = String.format(res.getString(R.string.playerX), position + 1);
            playerX.setText(text);
        }
    }
}
