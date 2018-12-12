package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.BiConsumer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserManagementService;
import lombok.AllArgsConstructor;

import static android.content.ContentValues.TAG;
import static ch.epfl.sweng.erpa.operations.AsyncTaskService.failIfNotFound;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;


public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {
    @BindView(R.id.descriptionTextView) TextView description;
    @BindView(R.id.difficultyTextView) TextView difficulty;
    @BindView(R.id.gameViewerPlayerListView) RecyclerView playerListView;
    @BindView(R.id.game_viewer_activity_content_panel) View contentPanel;
    @BindView(R.id.game_viewer_activity_loading_panel) View panelLoader;
    @BindView(R.id.game_viewer_drawer_layout) DrawerLayout myDrawerLayout;
    @BindView(R.id.game_viewer_navigation_view) NavigationView myNavigationView;
    @BindView(R.id.gmTextView) TextView gmName;
    // @BindView(R.id.gmLoader) View gmLoader;
    @BindView(R.id.joinGameButton) Button joinGameButton;
    @BindView(R.id.oneShotOrCampaignTextView) TextView type;
    @BindView(R.id.sessionLengthTextView) TextView sessionLength;
    @BindView(R.id.sessionNumberTextView) TextView numSessions;
    @BindView(R.id.titleTextView) TextView title;
    @BindView(R.id.universeTextView) TextView universe;

    // @BindView(R.id.playersContainer) View playersContainer;

    @Inject GameService gs;
    @Inject OptionalDependencyManager optionalDependency;
    @Inject UserManagementService ups;

    private String gameUuid;
    private Map<String, AsyncTask> asyncFetchThreads;
    private AsyncTaskService asyncTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        DataBindingUtil.setContentView(this, R.layout.activity_game_viewer);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;
        asyncFetchThreads = new HashMap<>();
        gameUuid = getIntent().getStringExtra(GameService.PROP_INTENT_GAME_UUID);
        if (gameUuid == null) {
            Log.e(TAG, "GameViewerActivity: No GameUuid passed with intent");
            finish();
        }

        addNavigationMenu(this, myDrawerLayout, myNavigationView, optionalDependency);

        contentPanel.setVisibility(View.GONE);
        asyncTaskService.run(() -> gs.getGame(gameUuid), failIfNotFound(gameUuid, this::updateGameTVs));
        asyncTaskService.run(() -> gs.getGameJoinRequests(gameUuid), this::updateGameJoinRequests);

        playerListView.setHasFixedSize(true);
        playerListView.setLayoutManager(new LinearLayoutManager(this));
        playerListView.setAdapter(new PlayerJoinGameRequestAdapter(
            new ArrayList<>(), (view, playerJoinGameRequest) -> {
            createPopup("Modifying Game Join requests is not yet implemented", this);
        }));
    }

    @Override protected void onStop() {
        super.onStop();
        Stream.of(asyncFetchThreads.values()).forEach(v -> v.cancel(true));
    }

    @OnClick(R.id.joinGameButton)
    void joinGame() {
        Intent joinGame = new Intent(this, JoinGameActivity.class);
        joinGame.putExtra(JoinGameActivity.GAME_UUID_KEY, gameUuid);
        startActivity(joinGame);
    }

    @UiThread
    private void updateGameTVs(Game game) {
        String gmUserUuid = game.getGmUserUuid();
        asyncFetchThreads.put(gmUserUuid, asyncTaskService.run(
            () -> ups.getUsernameFromUserUuid(gmUserUuid).map(Username::getUsername),
            failIfNotFound(gmUserUuid, gm -> {
                gmName.setVisibility(View.VISIBLE);
                gmName.setText(gm);
            })
        ));

        contentPanel.setVisibility(View.VISIBLE);
        panelLoader.setVisibility(View.GONE);

        title.setText(game.getTitle());
        description.setText(game.getDescription());
        universe.setText(game.getUniverse());
        difficulty.setText(game.getDifficulty().toString());
        type.setText(game.getOneshotOrCampaign());

        String numSessionsString = game.getNumberOfSessions().map(Object::toString).orElse("Unspecified");
        numSessions.setText(numSessionsString);

        String gameLength = game.getSessionLengthInMinutes().map(Object::toString).orElse("Unspecified");
        sessionLength.setText(gameLength);
    }

    @UiThread
    private void updateGameJoinRequests(List<PlayerJoinGameRequest> gameParticipantRequests) {
        Log.d("GameViewer", "Updating game participants: " + gameParticipantRequests.toString());
        PlayerJoinGameRequestAdapter adapter = (PlayerJoinGameRequestAdapter) playerListView.getAdapter();
        adapter.joinGameRequests.addAll(gameParticipantRequests);
        adapter.joinGameRequests.addAll(gameParticipantRequests);
        adapter.joinGameRequests.addAll(gameParticipantRequests);
        adapter.notifyDataSetChanged();
    }

    @AllArgsConstructor
    class PlayerJoinGameRequestAdapter extends RecyclerView.Adapter<PlayerJoinGameRequestAdapter.PlayerJoinGameRequestHolder> {
        List<PlayerJoinGameRequest> joinGameRequests;
        BiConsumer<View, PlayerJoinGameRequest> onViewElementClick;

        @NonNull
        @Override
        public PlayerJoinGameRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.game_viewer_player, parent, false);
            return new PlayerJoinGameRequestHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayerJoinGameRequestHolder playerJoinGameRequestHolder, int i) {
            playerJoinGameRequestHolder.populateHolder(joinGameRequests.get(i), onViewElementClick);
        }

        @Override
        public int getItemCount() {
            return joinGameRequests.size();
        }

        class PlayerJoinGameRequestHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.gameJoinRequestUsername) TextView gameJoinRequestUsernameTV;
            @BindView(R.id.gameJoinRequestStatus) TextView gameJoinRequestStatusTV;

            public PlayerJoinGameRequestHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void populateHolder(PlayerJoinGameRequest request, BiConsumer<View, PlayerJoinGameRequest> onItemViewClick) {
                itemView.setOnClickListener(view -> onItemViewClick.accept(view, request));
                asyncTaskService.run(() -> ups.getUsernameFromUserUuid(request.getUserUuid()).get(),
                    participantUsername -> gameJoinRequestUsernameTV.setText(participantUsername.getUsername()),
                    exc -> handleErrorRetrievingUserData(exc, request));
                // gameJoinRequestStatusTV.setText(request.getRequestStatus().toString());
            }

            void handleErrorRetrievingUserData(Throwable exc, PlayerJoinGameRequest request) {
                String errorMessage = "Could not retrieve User with UUID " + request.getUserUuid();
                gameJoinRequestUsernameTV.setText(errorMessage);
                Log.e("renderGameParticipant", errorMessage, exc);
            }
        }

    }
}
