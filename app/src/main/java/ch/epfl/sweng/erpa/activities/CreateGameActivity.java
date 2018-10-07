package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import ch.epfl.sweng.erpa.model.Game;

import ch.epfl.sweng.erpa.R;

public class CreateGameActivity extends Activity {

    EditText universeField;
    Spinner universesSpinner;
    TextView numbSession;
    EditText numbSessionField;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
        universeField = findViewById(R.id.universe_field);
        universesSpinner = findViewById(R.id.universes_spinner);
        numbSession = findViewById(R.id.num_sessions);
        numbSessionField = findViewById(R.id.numb_session_field);
        numbSession.setVisibility(View.GONE);
        numbSessionField.setVisibility(View.GONE);
        universesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String feedbackType = universesSpinner.getSelectedItem().toString();
                if(feedbackType.equals("Other"))
                    universeField.setVisibility(View.VISIBLE);
                else
                    universeField.setVisibility(View.GONE);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parentView) {}
        });
    }

    //When Oneshot or Campaign checkboxes are checked, uncheck the other one
    public void onCheckboxClicked(View view) {
        final CheckBox campaignCheckBox = findViewById(R.id.campaign);
        final CheckBox oneshotCheckBox = findViewById(R.id.oneshot);
        // Check which checkbox was clicked
        if (view.getId() == R.id.campaign) {
            if (campaignCheckBox.isChecked() && oneshotCheckBox.isChecked())
                oneshotCheckBox.setChecked(false);
            numbSession.setVisibility(View.VISIBLE);
            numbSessionField.setVisibility(View.VISIBLE);
        } else {
            if (oneshotCheckBox.isChecked() && campaignCheckBox.isChecked())
                campaignCheckBox.setChecked(false);
            numbSession.setVisibility(View.GONE);
            numbSessionField.setVisibility(View.GONE);

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
            Intent intent = new Intent(this, GameList.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            startActivity(intent);
        }
    }

    private boolean aRadioButtonIsChecked() {
        RadioButton campaignRadioButton = findViewById(R.id.campaign);
        RadioButton oneshotRadioButton = findViewById(R.id.oneshot);
        return (campaignRadioButton.isChecked() || oneshotRadioButton.isChecked());
    }

    private boolean allObligFieldsFilled(){
        return (checkFilledField(R.id.create_game_name_field)
>>>>>>> ce67a11... fixup! Add create_game activity and corresponding testclass
                && checkFilledField(R.id.min_num_player_field)
                && checkFilledField(R.id.max_num_player_field)
                && checkFilledField(R.id.description_field))) {
            createPopup(getString(R.string.emptyFieldMessage));
        }
        else if(!(campaignCheckBox.isChecked() || oneshotCheckBox.isChecked())) {
            createPopup(getString(R.string.uncheckedCheckboxMessage));
        }
        else {
            Spinner difficultySpinner = findViewById(R.id.difficulty_spinner);
            Spinner sessionLengthSpinner = findViewById(R.id.session_length_spinner);
            String oneShotOrCampaign = oneshotCheckBox.isChecked() ? "Oneshot" : "Campaign";
            if(checkFilledField(R.id.numb_session_field)){
                Game newGame = new Game("", findViewById(R.id.create_game_name_field).toString(),
                                        findViewById(R.id.min_num_player_field).toString(),
                                        findViewById(R.id.max_num_player_field).toString(),
                                        difficultySpinner.getSelectedItem().toString(),
                                        universesSpinner.getSelectedItem().toString(),
                                        oneShotOrCampaign,
                                        findViewById(R.id.numb_session_field).toString(),
                                        sessionLengthSpinner.getSelectedItem().toString(),
                                        findViewById(R.id.description_field).toString());
            }
            else{
                Game newGame = new Game("", findViewById(R.id.create_game_name_field).toString(),
                        findViewById(R.id.min_num_player_field).toString(),
                        findViewById(R.id.max_num_player_field).toString(),
                        difficultySpinner.getSelectedItem().toString(),
                        universesSpinner.getSelectedItem().toString(),
                        oneShotOrCampaign,
                        sessionLengthSpinner.getSelectedItem().toString(),
                        findViewById(R.id.description_field).toString());
            }
            Intent intent = new Intent(this, GameList.class);
            startActivity(intent);
        }
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

    private boolean checkFilledField(@IdRes int myId) {
        final EditText textField = (EditText) findViewById(myId);
        String text = textField.getText().toString();
        return (!text.isEmpty());
    }


//    Spinner spinner = (Spinner) findViewById(R.id.difficulty_spinner);
//    // Create an ArrayAdapter using the string array and a default spinner layout
//    ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
//            R.array.difficulties_array, android.R.layout.simple_spinner_item);
//    // Specify the layout to use when the list of choices appears
//    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//    // Apply the adapter to the spinner
//    spinner.setAdapter(adapter);

}
