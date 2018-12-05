package ch.epfl.sweng.erpa.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.OnClick;


import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.views.FlowLayout;

import java.util.ArrayList;

import javax.inject.Inject;

import processing.android.CompatUtils;
import processing.android.PFragment;

import static ch.epfl.sweng.erpa.util.ActivityUtils.onNavigationItemMenuSelected;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setUsernameInMenu;

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
        DataBindingUtil.setContentView(this, R.layout.activity_dice);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Handle navigationMenu interactions
        DrawerLayout mDrawerLayout = findViewById(R.id.dice_drawer_layout);

        NavigationView navigationView = findViewById(R.id.dice_navigation_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> onNavigationItemMenuSelected(menuItem, mDrawerLayout, this));
        setUsernameInMenu(navigationView, up);

        flowLayout = findViewById(R.id.dice_layout);
    }

    @OnClick(R.id.rollButton)
    public void rollDices(View view) {
        for (DieSketch die : allDice) {
            die.roll();
        }
    }

    @OnClick(R.id.dice_layout)
    public void removeDie(View view) {
        if (!allDice.isEmpty()) {
            int index = allDice.size() - 1;
            allDice.remove(index);
            flowLayout.removeViewAt(index);
        }
    }

    public void addAndUpdateDie(View view) {
        if (allDice.size() < MAX_DICE_NUMBER) {
            Button button = (Button) view;
            addAndShowDie(Integer.parseInt(button.getText().toString().substring(1)));
        }
    }

    private void addAndShowDie(int dieType) {
        DieSketch dieSketch = new DieSketch(getFileNameFromDieType(dieType), ROTATION_SPEED);
        allDice.add(dieSketch);

        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        PFragment fragment = new PFragment(dieSketch);
        fragment.setView(frame, this);
    }

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
        }
        return name;
    }
}
