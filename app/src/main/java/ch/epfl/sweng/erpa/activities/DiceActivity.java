package ch.epfl.sweng.erpa.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.views.FlowLayout;

import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import processing.android.CompatUtils;
import processing.android.PFragment;

import static ch.epfl.sweng.erpa.util.ActivityUtils.installDefaultNavigationMenuHandler;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onNavigationItemMenuSelected;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setUsernameInMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class DiceActivity extends DependencyConfigurationAgnosticActivity {

    @BindView(R.id.dice_navigation_view) NavigationView navigationView;
    @BindView(R.id.dice_drawer_layout) DrawerLayout drawerLayout;

    @Inject OptionalDependencyManager optionalDependency;

    private final int MAX_DICE_NUMBER = 9;
    private final float ROTATION_SPEED = 3f;

    private FlowLayout flowLayout;
    private ArrayList<DieSketch> allDice = new ArrayList<>();

    /**
     * Map from die type to filename
     */
    private static final Map<Integer, String> compressedDiceResourcesPath =
            Collections.unmodifiableMap(new HashMap<Integer, String>() {{
                put(4, "shapes/d4.obj");
                put(6, "shapes/d6.obj");
                put(8, "shapes/d8.obj");
                put(10, "shapes/d10.obj");
                put(20, "shapes/d20.obj");
            }});

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_dice);
        ButterKnife.bind(this);

        installDefaultNavigationMenuHandler(navigationView, drawerLayout, this);
    }

    @Override protected void onResume() {
        super.onResume();
        setUsernameInMenu(navigationView, optionalDependency.get(Username.class));
        DataBindingUtil.setContentView(this, R.layout.activity_dice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Handle navigationMenu interactions
        DrawerLayout mDrawerLayout = findViewById(R.id.dice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.dice_navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> onNavigationItemMenuSelected(menuItem, mDrawerLayout, this));

        //Handle navigationMenu interactions
        addNavigationMenu(this, findViewById(R.id.dice_drawer_layout), findViewById(R.id.dice_navigation_view), up);
        setMenuInToolbar(this, findViewById(R.id.dice_toolbar));
        getSupportActionBar().setTitle(R.string.title_dice_activity);

        flowLayout = findViewById(R.id.dice_layout);
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
     * @param dieType The die type to show
     */
    private void addAndShowDie(int dieType) {
        DieSketch dieSketch = new DieSketch(compressedDiceResourcesPath.get(dieType), ROTATION_SPEED);
        allDice.add(dieSketch);

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        PFragment fragment = new PFragment(dieSketch);
        fragment.setView(frame, this);
    }
}
