package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import ch.epfl.sweng.erpa.R;

public class CreateGameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_game);
    }

    public void onCheckboxClicked(View view) {
        final CheckBox campaignCheckBox = findViewById(R.id.campaign);
        final CheckBox oneshotCheckBox = findViewById(R.id.oneshot);
        // Check which checkbox was clicked
        switch(view.getId()) {
            case R.id.campaign:
                if (campaignCheckBox.isChecked() && oneshotCheckBox.isChecked()) {
                    oneshotCheckBox.setChecked(false);
                }
            case R.id.oneshot:
                if (oneshotCheckBox.isChecked() && campaignCheckBox.isChecked()) {
                    campaignCheckBox.setChecked(false);
                }
            default:
        }
    }

    public void onSpinnerSelection(View view) {
        final Spinner feedbackSpinner = (Spinner) findViewById(R.id.universes_spinner);
        String feedbackType = feedbackSpinner.getSelectedItem().toString();
        if(feedbackType.equals("Other")){

        }
    }

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
            //Pop-Up * == oblig
        }
        else if(!(campaignCheckBox.isChecked() || oneshotCheckBox.isChecked())) {
            //Pop-Up you must choose either oneshot or campaign
        }

        else {
            Intent intent = new Intent(this, GameList.class);
            startActivity(intent);
        }
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
