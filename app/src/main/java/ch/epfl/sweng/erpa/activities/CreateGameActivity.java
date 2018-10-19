package ch.epfl.sweng.erpa.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.HashSet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.CreateGameFormFragment;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign;

import static ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign.CAMPAIGN;
import static ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign.ONESHOT;

public class CreateGameActivity extends AppCompatActivity implements CreateGameFormFragment.OnFragmentInteractionListener {
    @BindView(R.id.campaign) RadioButton campaignRadioButton;
    @BindView(R.id.create_game_name_field) EditText gameName;
    @BindView(R.id.description_field) EditText gameDescription;
    @BindView(R.id.difficulty_spinner) Spinner difficultySpinner;
    @BindView(R.id.layout_num_sessions) View numSessionsView;
    @BindView(R.id.max_num_player_field) EditText valueMax;
    @BindView(R.id.min_num_player_field) EditText valueMin;
    @BindView(R.id.num_session_field) EditText numSess;
    @BindView(R.id.oneshot) RadioButton oneshotRadioButton;
    @BindView(R.id.session_length_spinner) Spinner sessionLengthSpinner;
    @BindView(R.id.universe_field) EditText universeField;
    @BindView(R.id.universes_spinner) Spinner universesSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_create_game);
        ButterKnife.bind(this);
        universesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView,
                                       int position, long id) {
                String feedbackType = universesSpinner.getSelectedItem().toString();
                universeField.setVisibility((feedbackType.equals(
                        getString(R.string.univOther))) ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
            }
        });
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
    }

    // When Oneshot or Campaign checkboxes are checked, uncheck the other one
    @OnClick({R.id.oneshot, R.id.campaign})
    public void onOneShotOrCampaignSelected(View view) {
        numSessionsView.setVisibility((view.getId() == R.id.campaign) ? View.VISIBLE : View.GONE);
    }

    //call when the user submit a game and check if no requested field is empty
    @OnClick(R.id.submit_button)
    public void submitGame(View view) {
        if (!playerNumberIsValid()) {
            createPopup(getString(R.string.invalidPlayerNumber));
        } else if (!allObligFieldsFilled()) {
            createPopup(getString(R.string.emptyFieldMessage));
        } else if (!aRadioButtonIsChecked()) {
            createPopup(getString(R.string.uncheckedCheckboxMessage));
        } else {
            createAndPublishGame();

            Intent intent = new Intent(this, GameListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
    }

    // Only call this method after activity elements have been verified!
    private void createAndPublishGame() {
        Integer minPlayers = intValueFromEditTextOrMinusOne(valueMin);
        Integer maxPlayers = intValueFromEditTextOrMinusOne(valueMax);

        OneshotOrCampaign oneShotOrCampaign = oneshotRadioButton.isChecked() ? ONESHOT : CAMPAIGN;
        Game.Difficulty difficulty = findDifficulty(
                difficultySpinner.getSelectedItem().toString());
        Optional<Integer> numbSession = Optional.of(numSess.getText().toString())
                .filter(s -> !s.isEmpty()).map(Integer::parseInt);
        Optional<Integer> sessionLength = findSessionLength(
                sessionLengthSpinner.getSelectedItem().toString());
        String universe = universesSpinner.getSelectedItem().toString();

        String gameUUID = "";

        Game newGame = new Game(gameUUID, new HashSet<>(), gameName.toString(),
                minPlayers, maxPlayers, difficulty, universe, oneShotOrCampaign, numbSession,
                sessionLength, gameDescription.toString());
        // TODO(@Roos): Generate a valid gameUUID and send the new game to the gameService.
    }

    private boolean aRadioButtonIsChecked() {
        return (campaignRadioButton.isChecked() || oneshotRadioButton.isChecked());
    }

    private boolean allObligFieldsFilled() {
        return Stream.of(gameName, valueMin, valueMax, gameDescription)
                .allMatch(f -> !f.getText().toString().isEmpty());
    }

    private boolean playerNumberIsValid() {
        int minPlayers = intValueFromEditTextOrMinusOne(valueMin);
        int maxPlayers = intValueFromEditTextOrMinusOne(valueMax);
        return (minPlayers > 0 && maxPlayers >= minPlayers);
    }

    private void createPopup(String text) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        final TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);
        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(tv);
        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", (dialog, id) -> {
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

    private static int intValueFromEditTextOrMinusOne(EditText editText) {
        return Optional.of(editText.getText().toString())
                .filter(t -> !t.isEmpty()).map(Integer::parseInt).orElse(-1);
    }

    static Optional<Integer> findSessionLength(String sessionLength) {
        switch (sessionLength) {
            case "Undefined":
                return Optional.empty();
            case "-1h":
                return Optional.of(30);
            case "1h":
                return Optional.of(60);
            case "2h":
                return Optional.of(120);
            case "3h":
                return Optional.of(180);
            case "4h":
                return Optional.of(240);
            case "5h":
                return Optional.of(300);
            case "6h":
                return Optional.of(360);
            case "+6h":
                return Optional.of(Integer.MAX_VALUE);
            default:
                return Optional.empty();
        }
    }

    static Game.Difficulty findDifficulty(String diff) {
        switch (diff) {
            case "NOOB":
                return Game.Difficulty.NOOB;
            case "Chill":
                return Game.Difficulty.CHILL;
            case "Hard":
                return Game.Difficulty.HARD;
            default:
                return null;
        }
    }
}
