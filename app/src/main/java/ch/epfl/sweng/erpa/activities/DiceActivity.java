package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.OnClick;

import java.util.Random;

import ch.epfl.sweng.erpa.R;

public class DiceActivity extends DependencyConfigurationAgnosticActivity {

    Random rng = new Random();    //used as a RNG

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        TextView textView = findViewById(R.id.diceRollResult);
        textView.setMovementMethod(new ScrollingMovementMethod());

        EditText nbOfRolls = findViewById(R.id.nbOfDice);
        nbOfRolls.setText("1");
    }

    @OnClick(R.id.rollButton)
    public void rollDices(View view) {
        String diceType = ((Spinner) findViewById(R.id.diceTypeSpinner)).getSelectedItem().toString();
        int nbOfRolls = Integer.parseInt(((EditText) findViewById(R.id.nbOfDice)).getText().toString());
        TextView textView = findViewById(R.id.diceRollResult);

        textView.setText(rollAllDice(nbOfRolls, diceType));
    }

    /**
     * Rolls dices
     * @param nbOfRolls Number of rolls to do
     * @param diceType Type of dice to use
     * @return a string containing all the results separated by spaces
     */
    private String rollAllDice(int nbOfRolls, String diceType) {

        StringBuilder strBuild = new StringBuilder();
        for (int i = 0; i < nbOfRolls; ++i) {
            strBuild.append(rollDie(diceType) + "       ");
        }
        return strBuild.toString();
    }

    /**
     * Rolls one die
     * @param diceType The type of die to roll
     * @return The result of the roll
     */
    private int rollDie(String diceType) {
        int result = -1;
        switch(diceType) {
            case "D4":
                result = rng.nextInt(4);
                break;
            case "D6":
                result = rng.nextInt(6);
                break;
            case "D8":
                result = rng.nextInt(8);
                break;
            case "D10":
                result = rng.nextInt(10);
                break;
            case "D12":
                result = rng.nextInt(12);
                break;
            case "D20":
                result = rng.nextInt(20);
                break;
            case "D100":
                result = rng.nextInt(100);
                break;
        }
        return ++result;
    }
}
