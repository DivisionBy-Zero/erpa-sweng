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
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.R;

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
                if (feedbackType.equals("Other")) {
                    universeField.setVisibility(View.VISIBLE);
                } else {
                    universeField.setVisibility(View.GONE);
                }
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
        if (view.getId() == R.id.campaign) {
            findViewById(R.id.num_sessions).setVisibility(View.VISIBLE);
            findViewById(R.id.numb_session_field).setVisibility(View.VISIBLE);

        } else {
            findViewById(R.id.num_sessions).setVisibility(View.GONE);
            findViewById(R.id.numb_session_field).setVisibility(View.GONE);
        }
    }

    //call when the user submit a game and check if no requested field is empty
    public void submitGame(View view) {
        Spinner difficultySpinner = findViewById(R.id.difficulty_spinner);
        Spinner universesSpinner = findViewById(R.id.universes_spinner);
        Spinner sessionLengthSpinner = findViewById(R.id.session_length_spinner);
        EditText minPlayer = findViewById(R.id.min_num_player_field);
        EditText maxPlayer = findViewById(R.id.max_num_player_field);
        String valueMin = minPlayer.getText().toString();
        String valueMax = maxPlayer.getText().toString();
        if (!playerNumberIsValid(valueMin, valueMax)) {
            createPopup(getString(R.string.invalidPlayerNumber));
        } else if (!allObligFieldsFilled()) {
            createPopup(getString(R.string.emptyFieldMessage));
        } else if (!aRadioButtonIsChecked()) {
            createPopup(getString(R.string.uncheckedCheckboxMessage));
        } else {
            RadioButton oneshotRadioButton = findViewById(R.id.oneshot);
            String oneShotOrCampaign = oneshotRadioButton.isChecked() ? "Oneshot" : "Campaign";
            Game newGame = new Game("", findViewById(R.id.create_game_name_field).toString(),
                    valueMin, valueMax, difficultySpinner.getSelectedItem().toString(),
                    universesSpinner.getSelectedItem().toString(),
                    oneShotOrCampaign, findViewById(R.id.numb_session_field).toString(),
                    sessionLengthSpinner.getSelectedItem().toString(),
                    findViewById(R.id.description_field).toString());
            Intent intent = new Intent(this, GameListActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
    }

    private boolean aRadioButtonIsChecked(){
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

    private boolean playerNumberIsValid(String valueMin, String valueMax) {
        int min = Integer.parseInt(valueMin);
        int max = Integer.parseInt(valueMax);
        return (min > 0 && max >= min);
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
