package ch.epfl.sweng.erpa.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
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

import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.CreateGameFormFragment;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign;

import static ch.epfl.sweng.erpa.model.Game.OneshotOrCampaign.*;

public class CreateGameActivity extends AppCompatActivity implements CreateGameFormFragment.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        ButterKnife.bind(this);
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
    public void onOneShotOrCampaignSelected(View view) {
        ButterKnife.bind(this, view);
        findViewById(R.id.layout_num_sessions).setVisibility((view.getId() == R.id.campaign) ? View.VISIBLE : View.GONE);
    }

    //call when the user submit a game and check if no requested field is empty
    public void submitGame(View view) {
        EditText minPlayer = findViewById(R.id.min_num_player_field);
        EditText maxPlayer = findViewById(R.id.max_num_player_field);
        int valueMin = Integer.parseInt(minPlayer.getText().toString());
        int valueMax = Integer.parseInt(maxPlayer.getText().toString());
        if (!playerNumberIsValid(valueMin, valueMax)) {
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
            Game.SessionLength sessionLength = findSessionLength(sessionLengthSpinner.getSelectedItem().toString());
            Game newGame = new Game("", findViewById(R.id.create_game_name_field).toString(),
                    valueMin, valueMax, difficulty,
                    universesSpinner.getSelectedItem().toString(),
                    oneShotOrCampaign, Integer.parseInt(findViewById(R.id.num_session_field).toString()),
                    sessionLength,
                    findViewById(R.id.description_field).toString());
            Intent intent = new Intent(this, GameListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
    }

    private Game.SessionLength findSessionLength(String sessionLength) {
        switch (sessionLength) {
            case "Undefined": return Game.SessionLength.UNDEFINED;
            case "-1h": return Game.SessionLength.LESSH1;
            case "1h": return Game.SessionLength.H1;
            case "2h": return Game.SessionLength.H2;
            case "3h": return Game.SessionLength.H3;
            case "4h": return Game.SessionLength.H4;
            case "5h": return Game.SessionLength.H5;
            case "6h": return Game.SessionLength.H6;
            case "+6h": return Game.SessionLength.MOREH6;
            default: return null;
        }
    }

    private Game.Difficulty findDifficulty(String diff) {
        switch (diff){
            case "NOOB": return Game.Difficulty.NOOB;
            case "Chill": return Game.Difficulty.CHILL;
            case "Hard": return Game.Difficulty.HARD;
            default: return null;
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
        final EditText textField = (EditText) findViewById(fieldId);
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
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });
        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }

}
