package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.annimon.stream.IntStream;

import butterknife.OnClick;

import java.util.Random;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserProfileService;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onNavigationItemMenuSelected;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setUsernameInMenu;

public class DiceActivity extends DependencyConfigurationAgnosticActivity {

    @Inject UserProfile up;

    Random rng = new Random();    //used as a RNG
    private final int[] dice = {4, 6, 8, 10, 12, 20, 100};
    private final int MAX_DICE_NUMBER = 15;
    private TextView[] allResultViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        allResultViews = getAllResultViews();

        //Handle navigationMenu interactions
        DrawerLayout mDrawerLayout = findViewById(R.id.dice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.dice_navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> onNavigationItemMenuSelected(menuItem, mDrawerLayout, this));
        setUsernameInMenu(navigationView, up);
    }

    @OnClick(R.id.rollButton)
    public void rollDices(View view) {
        int[] rolls = getNumberOfRolls();
        if (IntStream.of(rolls).sum() > 15) {
            createPopup("The number of dice must be less or equal to 15", this);
        } else {
            clearAllResults();
            rollAndShowAllDice(rolls);
        }
    }

    /**
     * Rolls and shows all dice
     * @param rolls An array containing all the dice to roll
     */
    private void rollAndShowAllDice(int[] rolls) {
        int n = 0;
        for (int i = 0; i < 7; ++i) {
            String dieType = "D" + dice[i];
            for (int j = 0; j < rolls[i]; ++j) {
                allResultViews[n].setText(dieType + ": " + rollDie(dieType));
                ++n;
            }
        }
    }

    /**
     * Rolls one die
     * @param dieType The type of die to roll
     * @return The result of the roll
     */
    private int rollDie(String dieType) {
        int result = -1;
        switch (dieType) {
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
            default:
                throw new AssertionError("Die type is incorrect");
        }
        return ++result;
    }

    /**
     * Gets all the TextViews where we write the results of the rolls
     * @return An array containing all the TextViews
     */
    private TextView[] getAllResultViews() {
        TextView[] list = new TextView[MAX_DICE_NUMBER];
        for (int i = 1; i < list.length + 1; ++i) {
            String textId = "roll" + i;
            int id = getResources().getIdentifier(textId, "id", getBaseContext().getPackageName());
            list[i - 1] = findViewById(id);
        }
        return list;
    }

    /**
     * Gets all the dice to be rolled
     * @return An array containing all the number of rolls of each die
     */
    private int[] getNumberOfRolls() {
        int[] rolls = new int[dice.length];
        for (int i = 0; i < dice.length; ++i) {
            String textId = "d" + dice[i] + "_number";
            int id = getResources().getIdentifier(textId, "id", getBaseContext().getPackageName());
            String text = ((EditText)findViewById(id)).getText().toString();
            if (text.isEmpty())
                rolls[i] = 0;
            else
                rolls[i] = Integer.parseInt(text);
        }
        return rolls;
    }

    /**
     * Clears all the dice results
     */
    private void clearAllResults() {
        for (int i = 0; i < allResultViews.length; ++i) {
            allResultViews[i].setText("");
        }
    }
}
