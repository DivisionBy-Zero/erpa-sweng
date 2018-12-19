package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.CreateGameFormFragment;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.GameService;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

/**
 * Activity class for the creation of a game
 * Sends the game to the server
 * Sanitizes the input before sending it
 */
public class CreateGameActivity extends DependencyConfigurationAgnosticActivity implements CreateGameFormFragment.OnFragmentInteractionListener {
    private static final HashMap<String, Integer> sessionLengthTranslationTable = new HashMap<String, Integer>() {{
        put("Undefined", null);
        put("-1h", 30);
        put("1h", 60);
        put("2h", 120);
        put("3h", 180);
        put("4h", 240);
        put("5h", 300);
        put("6h", 360);
        put("+6h", Integer.MAX_VALUE);
    }};

    private static final HashMap<String, Game.Difficulty> difficultyTranslationTable = new HashMap<String, Game.Difficulty>() {{
        Stream.of(Game.Difficulty.values())
            .forEach(difficulty -> put(difficulty.toString().toUpperCase(), difficulty));
    }};

    @BindView(R.id.campaign) RadioButton campaignRadioButton;
    @BindView(R.id.create_game_name_field) EditText gameName;
    @BindView(R.id.description_field) EditText gameDescription;
    @BindView(R.id.create_game_drawer_layout) DrawerLayout myDrawerLayout;
    @BindView(R.id.create_game_navigation_view) NavigationView myNavigationView;
    @BindView(R.id.create_game_toolbar) Toolbar activityToolbar;
    @BindView(R.id.difficulty_spinner) Spinner difficultySpinner;
    @BindView(R.id.layout_num_sessions) View numSessionsView;
    @BindView(R.id.loading_panel_create_game) View loader;
    @BindView(R.id.max_num_player_field) EditText valueMax;
    @BindView(R.id.min_num_player_field) EditText valueMin;
    @BindView(R.id.num_session_field) EditText numSess;
    @BindView(R.id.oneshot) RadioButton oneshotRadioButton;
    @BindView(R.id.session_length_spinner) Spinner sessionLengthSpinner;
    @BindView(R.id.universe_field) EditText universeField;
    @BindView(R.id.universes_spinner) Spinner universesSpinner;

    @Inject GameService gameService;
    @Inject OptionalDependencyManager optionalDependency;
    @Inject Username username;

    private AsyncTaskService asyncTaskService;

    private static Optional<Integer> intValueFromEditText(EditText editText) {
        return Optional.of(editText.getText().toString())
            .filter(t -> !t.isEmpty()).map(Integer::parseInt);
    }

    static Optional<Integer> findSessionLength(String sessionLength) {
        return Optional.ofNullable(sessionLengthTranslationTable.get(sessionLength));
    }

    static Game.Difficulty findDifficulty(String difficulty) {
        return difficultyTranslationTable.get(String.valueOf(difficulty).toUpperCase());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_create_game);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);

        universesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String feedbackType = universesSpinner.getSelectedItem().toString();
                boolean shouldDisplay = feedbackType.equals(getString(R.string.univOther));
                universeField.setVisibility(shouldDisplay ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override protected void onResume() {
        super.onResume();
        addNavigationMenu(this, myDrawerLayout, myNavigationView, optionalDependency);
        setMenuInToolbar(this, activityToolbar);
        Optional.ofNullable(getSupportActionBar()).ifPresent(b -> b.setTitle("Create Game"));
    }

    // Handle myToolbar items clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean found = onOptionItemSelectedUtils(item.getItemId(), myDrawerLayout);
        return found || super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.oneshot, R.id.campaign})
    public void onOneShotOrCampaignSelected(View view) {
        numSessionsView.setVisibility((view.getId() == R.id.campaign) ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.submit_button)
    public void submitGame(View view) {
        if (getErrorMessage().executeIfPresent(m -> createPopup(m, this)).isPresent())
            return;

        Optional<Integer> optionalNbSessions = Optional.of(numSess.getText().toString())
            .filter(s -> !s.isEmpty()).map(Integer::parseInt);

        Game newGame = new Game(Game.genGameUuid(),
            username.getUserUuid(),
            gameName.getText().toString(),
            intValueFromEditText(valueMin).get(),
            intValueFromEditText(valueMax).get(),
            findDifficulty(difficultySpinner.getSelectedItem().toString()),
            universesSpinner.getSelectedItem().toString(),
            !oneshotRadioButton.isChecked(),
            optionalNbSessions,
            findSessionLength(sessionLengthSpinner.getSelectedItem().toString()),
            gameDescription.getText().toString(),
            0.0, 0.0,
            Game.GameStatus.CREATED);

        loader.setVisibility(View.VISIBLE);

        asyncTaskService.run(() -> gameService.createGame(newGame), game -> {
            loader.setVisibility(View.GONE);
            launchGameViewer(game);
        }, this::handleException);
    }

    private void launchGameViewer(Game g) {
        Intent intent = new Intent(this, GameListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, GameListActivity.GameListType.FIND_GAME);
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
    }

    void handleException(Throwable exc) {
        Log.e("createGamePost", "Could not create game", exc);
        createPopup("Could not create game: " + exc.getMessage(), this);
    }

    private Optional<String> getErrorMessage() {
        if (!playerNumberIsValid()) return Optional.of(getString(R.string.invalidPlayerNumber));
        if (!allObligFieldsFilled()) return Optional.of(getString(R.string.emptyFieldMessage));
        if (!aRadioButtonIsChecked())
            return Optional.of(getString(R.string.uncheckedCheckboxMessage));
        return Optional.empty();
    }

    private boolean aRadioButtonIsChecked() {
        return (campaignRadioButton.isChecked() || oneshotRadioButton.isChecked());
    }

    private boolean allObligFieldsFilled() {
        return Stream.of(gameName, valueMin, valueMax, gameDescription)
            .allMatch(f -> !f.getText().toString().isEmpty());
    }

    private boolean playerNumberIsValid() {
        int minPlayers = intValueFromEditText(valueMin).orElse(-1);
        int maxPlayers = intValueFromEditText(valueMax).orElse(-1);
        return (minPlayers > 0 && maxPlayers >= minPlayers);
    }

    @Override public void onFragmentInteraction(Uri uri) {
        // Method stub required to inflate the fragment on this activity. Not used.
    }
}
