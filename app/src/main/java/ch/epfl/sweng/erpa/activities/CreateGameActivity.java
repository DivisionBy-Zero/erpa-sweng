package ch.epfl.sweng.erpa.activities;

import android.app.AlertDialog;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.annimon.stream.Optional;

import butterknife.OnClick;
import ch.epfl.sweng.erpa.CreateGameFormFragment;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign;

import static ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign.CAMPAIGN;
import static ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign.ONESHOT;

public class CreateGameActivity extends AppCompatActivity implements CreateGameFormFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_create_game);
        final EditText universeField = findViewById(R.id.universe_field);
        final Spinner universesSpinner = findViewById(R.id.universes_spinner);
        universesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String feedbackType = universesSpinner.getSelectedItem().toString();
                universeField.setVisibility((feedbackType.equals("Other")) ? View.VISIBLE : View.GONE);
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
        findViewById(R.id.layout_num_sessions).setVisibility((view.getId() == R.id.campaign) ? View.VISIBLE : View.GONE);
    }

    //call when the user submit a game and check if no requested field is empty
    @OnClick(R.id.submit_button)
    public void submitGame(View view) {
        EditText valueMin = findViewById(R.id.min_num_player_field);
        EditText valueMax = findViewById(R.id.max_num_player_field);
        String min = valueMin.getText().toString();
        String max = valueMax.getText().toString();
        int minPlayer = min.isEmpty() ? -1 : Integer.parseInt(min);
        int maxPlayer = max.isEmpty() ? -1 : Integer.parseInt(max);
        if (!playerNumberIsValid(minPlayer, maxPlayer)) {
            createPopup(getString(R.string.invalidPlayerNumber));
        } else if (!allObligFieldsFilled()) {
            createPopup(getString(R.string.emptyFieldMessage));
        } else if (!aRadioButtonIsChecked()) {
            createPopup(getString(R.string.uncheckedCheckboxMessage));
        } else {
            Spinner difficultySpinner = findViewById(R.id.difficulty_spinner);
            Spinner universesSpinner = findViewById(R.id.universes_spinner);
            Spinner sessionLengthSpinner = findViewById(R.id.session_length_spinner);
            RadioButton oneshotRadioButton = findViewById(R.id.oneshot);
            OneshotOrCampaign oneShotOrCampaign = oneshotRadioButton.isChecked() ? ONESHOT : CAMPAIGN;
            Game.Difficulty difficulty = findDifficulty(difficultySpinner.getSelectedItem().toString());
            Optional<Integer> sessionLength = findSessionLength(sessionLengthSpinner.getSelectedItem().toString());
            EditText numSess = findViewById(R.id.num_session_field);
            String nbSessionString = numSess.getText().toString();
            Optional<Integer> numbSession = nbSessionString.isEmpty() ? Optional.empty() : Optional.of(Integer.parseInt(nbSessionString));
            Game newGame = new Game("", findViewById(R.id.create_game_name_field).toString(),
                    minPlayer, maxPlayer, difficulty, universesSpinner.getSelectedItem().toString(),
                    oneShotOrCampaign, numbSession, sessionLength, findViewById(R.id.description_field).toString());
            Intent intent = new Intent(this, GameListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
    }

    private Optional<Integer> findSessionLength(String sessionLength) {
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

    private Game.Difficulty findDifficulty(String diff) {
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

    private boolean aRadioButtonIsChecked() {
        RadioButton campaignRadioButton = findViewById(R.id.campaign);
        RadioButton oneshotRadioButton = findViewById(R.id.oneshot);
        return (campaignRadioButton.isChecked() || oneshotRadioButton.isChecked());
    }

    private boolean allObligFieldsFilled() {
        return (checkFilledField(R.id.create_game_name_field)
                && checkFilledField(R.id.min_num_player_field)
                && checkFilledField(R.id.max_num_player_field)
                && checkFilledField(R.id.description_field));
    }

    private boolean playerNumberIsValid(int valueMin, int valueMax) {
        return (valueMin > 0 && valueMax >= valueMin);
    }

    // IdRes denotes that the integer parameter fieldId is expected to be an id resource reference
    private boolean checkFilledField(@IdRes int fieldId) {
        final EditText textField = findViewById(fieldId);
        String text = textField.getText().toString();
        return (!text.isEmpty());
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

}
