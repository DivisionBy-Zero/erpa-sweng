package ch.epfl.sweng.erpa.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.OnClick;

import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.views.FlowLayout;

import com.annimon.stream.Stream;

import java.security.InvalidParameterException;
import java.util.ArrayList;

import javax.inject.Inject;

import processing.android.CompatUtils;
import processing.android.PFragment;

import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class DiceActivity extends DependencyConfigurationAgnosticActivity {

    @Inject UserProfile up;

    private final int MAX_DICE_NUMBER = 9;
    private final float ROTATION_SPEED = 3f;

    private FlowLayout flowLayout;
    private ArrayList<DieSketch> allDice = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dice);

        //Handle navigationMenu interactions
        addNavigationMenu(this, findViewById(R.id.dice_drawer_layout), findViewById(R.id.dice_navigation_view), up);
        setMenuInToolbar(this, findViewById(R.id.dice_toolbar));
        getSupportActionBar().setTitle(R.string.title_dice_activity);

        flowLayout = findViewById(R.id.dice_layout);
    }

    //Handle toolbar items clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean found = onOptionItemSelectedUtils(item.getItemId(), findViewById(R.id.dice_drawer_layout));
        return found || super.onOptionsItemSelected(item);
    }

    /**
     * Checks if any die is rolling else rolls all dice
     * @param view not used
     */
    @OnClick(R.id.rollButton)
    public void rollDices(View view) {
        if (Stream.of(allDice).allMatch(dice -> !dice.isRolling())) {
            for (DieSketch die : allDice) {
                die.roll();
            }
        }
    }

    /**
     * Removes the last die on the layout
     * @param view
     */
    @OnClick(R.id.dice_layout)
    public void removeDie(View view) {
        if (!allDice.isEmpty()) {
            int index = allDice.size() - 1;
            allDice.remove(index);
            flowLayout.removeViewAt(index);
        }
    }

    /**
     * Add a die on the FlowLayout if there is place depending on which button is pressed
     * @param view The button that has been pressed, needs to be cast to Button
     */
    public void addAndUpdateDie(View view) {
        if (allDice.size() < MAX_DICE_NUMBER) {
            Button button = (Button) view;
            addAndShowDie(Integer.parseInt(button.getText().toString().substring(1)));
        }
    }

    /**
     * Add a button of type dieType on the layout
     * @param dieType
     */
    private void addAndShowDie(int dieType) {
        DieSketch dieSketch = new DieSketch(getFileNameFromDieType(dieType), ROTATION_SPEED);
        allDice.add(dieSketch);

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        PFragment fragment = new PFragment(dieSketch);
        fragment.setView(frame, this);
    }

    /**
     * Return the file name of the die type given
     * @param dieType The type of the die. Has to be 4, 6, 8, 10 or 20
     * @return The name of the file to load
     * @throws InvalidParameterException if the die type is not 4, 6, 8, 10 or 20
     */
    private String getFileNameFromDieType(int dieType) {
        String name = "";
        switch (dieType) {
            case 4:
                name = "objs/d4R.obj";
                break;
            case 6:
                name = "objs/d6R.obj";
                break;
            case 8:
                name = "objs/d8R.obj";
                break;
            case 10:
                name = "objs/d10R.obj";
                break;
            case 20:
                name = "objs/d20_1R.obj";
                break;
            default:
                throw new InvalidParameterException("Not a valid die type");
        }
        return name;
    }
}
