package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.UiThread;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

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
import lombok.RequiredArgsConstructor;

import static android.content.ContentValues.TAG;
import static ch.epfl.sweng.erpa.operations.AsyncTaskService.failIfNotFound;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;

public class GameViewerActivity extends DependencyConfigurationAgnosticActivity {
    static List<PlayerJoinGameRequest.RequestStatus> userJoinRequestStatusThatShouldHideTheJoinButton = Stream.of(
            PlayerJoinGameRequest.RequestStatus.CONFIRMED,
            PlayerJoinGameRequest.RequestStatus.REJECTED,
            PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN
    ).collect(Collectors.toList());

    @BindView(R.id.descriptionTextView) TextView description;
    @BindView(R.id.difficultyTextView) TextView difficulty;
    @BindView(R.id.gameViewerPlayerListView) RecyclerView playerListView;
    @BindView(R.id.game_viewer_activity_content_panel) View contentPanel;
    @BindView(R.id.game_viewer_activity_loading_panel) View panelLoader;
    @BindView(R.id.game_viewer_drawer_layout) DrawerLayout myDrawerLayout;
    @BindView(R.id.game_viewer_navigation_view) NavigationView myNavigationView;
    @BindView(R.id.game_viewer_participants_loader) View participantsLoader;
    @BindView(R.id.game_list_swipe_refresh) SwipeRefreshLayout refreshLayout;
    @BindView(R.id.gmTextView) TextView gmName;
    @BindView(R.id.joinGameButton) Button joinGameButton;
    @BindView(R.id.oneShotOrCampaignTextView) TextView type;
    @BindView(R.id.sessionLengthTextView) TextView sessionLength;
    @BindView(R.id.sessionNumberTextView) TextView numSessions;
    @BindView(R.id.titleTextView) TextView title;
    @BindView(R.id.universeTextView) TextView universe;
    @Inject GameService gs;
    @Inject OptionalDependencyManager optionalDependency;
    @Inject UserManagementService ups;
    AsyncTaskService asyncTaskService;
    private Map<String, AsyncTask> asyncFetchThreads;
    private String gameUuid;
    private boolean currentUserIsGM = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        DataBindingUtil.setContentView(this, R.layout.activity_game_viewer);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);

        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            startActivity(getIntent());
            finish();
        });
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
        resetLoadersAndPanelsVisibilities();

        asyncTaskService.run(() -> gs.getGame(gameUuid), failIfNotFound(gameUuid, game -> {
            updateGameTVs(game);
            asyncTaskService.run(() -> gs.getGameJoinRequests(gameUuid), gameParticipantRequests -> {
                updateGameJoinRequests(gameParticipantRequests, game);
            });
        }));

        playerListView.setHasFixedSize(true);
        playerListView.setLayoutManager(new LinearLayoutManager(this));
        playerListView.setAdapter(new PlayerJoinGameRequestAdapter(new ArrayList<>()));


        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return true;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                PlayerJoinGameRequest request = ((PlayerJoinGameRequestAdapter.PlayerJoinGameRequestHolder) viewHolder).request;
                boolean currentUserIsRequestUser = optionalDependency.get(Username.class).map(Username::getUserUuid)
                        .filter(userUuid -> request.getUserUuid().equals(userUuid))
                        .isPresent();
                onSwipedDirection(direction, request, currentUserIsRequestUser);
            }
        };

        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(playerListView);
        playerListView.setTag(new ItemTouchHelper(itemTouchHelperCallback));
    }


    public void onSwipedDirection(int direction, PlayerJoinGameRequest request, Boolean currentUserIsRequestUser) {

        if (currentUserIsGM) {
            if (direction == ItemTouchHelper.LEFT)
                changePlayerStatus(request, PlayerJoinGameRequest.RequestStatus.REMOVED);
            else if (direction == ItemTouchHelper.RIGHT)
                changePlayerStatus(request, PlayerJoinGameRequest.RequestStatus.CONFIRMED);
        }
        if (currentUserIsRequestUser && direction == ItemTouchHelper.LEFT) {
            changePlayerStatus(request, PlayerJoinGameRequest.RequestStatus.HAS_QUIT);
        }

    }

    private void changePlayerStatus(PlayerJoinGameRequest request, PlayerJoinGameRequest.RequestStatus status) {
        request.setRequestStatus(status);
        asyncTaskService.run(() -> gs.updateGameJoinRequest(request.getGameUuid(), request), updatedRequest -> {
        }, exc -> {
        });
    }

    private void resetLoadersAndPanelsVisibilities() {
        contentPanel.setVisibility(View.GONE);
        joinGameButton.setVisibility(View.GONE);
        participantsLoader.setVisibility(View.VISIBLE);
        panelLoader.setVisibility(View.VISIBLE);
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
    private void updateGameJoinRequests(List<PlayerJoinGameRequest> gameParticipantRequests, Game game) {
        Log.d("GV FetchParticipants", "Updating game participants: " + gameParticipantRequests.toString());
        PlayerJoinGameRequestAdapter adapter = (PlayerJoinGameRequestAdapter) playerListView.getAdapter();

        participantsLoader.setVisibility(View.GONE);
        playerListView.setVisibility(View.VISIBLE);

        currentUserIsGM = optionalDependency.get(Username.class).map(Username::getUserUuid)
                .filter(userUuid -> game.getGmUserUuid().equals(userUuid))
                .isPresent();

        if (!currentUserIsGM) {
            joinGameButton.setVisibility(View.VISIBLE);
        } else {
            joinGameButton.setVisibility(View.GONE);
        }

        optionalDependency.get(Username.class).map(Username::getUserUuid).ifPresent(
                currentUserUuid -> Stream.of(gameParticipantRequests)
                        .filter(requests -> requests.getUserUuid().equals(currentUserUuid)).findFirst()
                        .map(PlayerJoinGameRequest::getRequestStatus)
                        .filter(userJoinRequestStatusThatShouldHideTheJoinButton::contains)
                        .executeIfPresent(s -> joinGameButton.setVisibility(View.GONE)));

        Optional.ofNullable(adapter)
                .executeIfPresent(a -> {
                    a.joinGameRequests.addAll(gameParticipantRequests);
                    a.notifyDataSetChanged();
                })
                .executeIfAbsent(() -> Log.e("GV FetchParticipants",
                        "Received participants response, but no adapter was found."));
    }

    @RequiredArgsConstructor
    class PlayerJoinGameRequestAdapter extends RecyclerView.Adapter<PlayerJoinGameRequestAdapter.PlayerJoinGameRequestHolder> {
        @NonNull List<PlayerJoinGameRequest> joinGameRequests;


        @NonNull
        @Override
        public PlayerJoinGameRequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
            Context context = parent.getContext();
            View view = LayoutInflater.from(context).inflate(R.layout.game_viewer_player, parent, false);
            return new PlayerJoinGameRequestHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PlayerJoinGameRequestHolder playerJoinGameRequestHolder, int i) {
            playerJoinGameRequestHolder.populateHolder(joinGameRequests.get(i));
        }

        @Override
        public int getItemCount() {
            return joinGameRequests.size();
        }

        class PlayerJoinGameRequestHolder extends RecyclerView.ViewHolder {
            @BindView(R.id.game_join_request_constraint_layout) ConstraintLayout gameJoinRequestConstraintLayout;
            @BindView(R.id.gameJoinRequestUsername) TextView gameJoinRequestUsernameTV;
            @BindView(R.id.gameJoinRequestStatus) TextView gameJoinRequestStatusTV;
            private PlayerJoinGameRequest request;

            public PlayerJoinGameRequestHolder(@NonNull View itemView) {
                super(itemView);
                ButterKnife.bind(this, itemView);
            }

            void populateHolder(PlayerJoinGameRequest request) {
                this.request = request;
                asyncTaskService.run(() -> ups.getUsernameFromUserUuid(request.getUserUuid()).get(),
                        participantUsername -> gameJoinRequestUsernameTV.setText(participantUsername.getUsername()),
                        exc -> handleErrorRetrievingUserData(exc, request));
                gameJoinRequestStatusTV.setText(Optional.ofNullable(request.getRequestStatus())
                        .map(Object::toString).orElse("Unknown state"));
            }
            void handleErrorRetrievingUserData(Throwable exc, PlayerJoinGameRequest request) {
                String errorMessage = "Could not retrieve User with UUID " + request.getUserUuid();
                gameJoinRequestUsernameTV.setText(errorMessage);
                Log.e("renderGameParticipant", errorMessage, exc);
            }
        }

    }
}
