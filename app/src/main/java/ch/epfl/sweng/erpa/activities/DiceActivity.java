package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.views.FlowLayout;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import processing.android.CompatUtils;
import processing.android.PFragment;

import static ch.epfl.sweng.erpa.util.ActivityUtils.installDefaultNavigationMenuHandler;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class DiceActivity extends DependencyConfigurationAgnosticActivity {

    @BindView(R.id.dice_navigation_view) NavigationView navigationView;
    @BindView(R.id.dice_drawer_layout) DrawerLayout drawerLayout;

    @Inject OptionalDependencyManager optionalDependency;

    private final int MAX_DICE_NUMBER = 9;
    private final float ROTATION_SPEED = 3f;

    private FlowLayout flowLayout;
    private ArrayList<DieSketch> allDice = new ArrayList<>();

    private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;

    /**
     * Map from number of faces to filename
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

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(c -> rollDices(findViewById(R.id.rollButton)));

        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;
        Toolbar toolbar = findViewById(R.id.dice_toolbar);
        setSupportActionBar(toolbar);

        //Handle navigationMenu interactions
        addNavigationMenu(this, findViewById(R.id.dice_drawer_layout), findViewById(R.id.dice_navigation_view), optionalDependency);
        setMenuInToolbar(this, findViewById(R.id.dice_toolbar));
        Optional.ofNullable(getSupportActionBar()).ifPresent(b -> b.setTitle(R.string.title_dice_activity));

        flowLayout = findViewById(R.id.dice_layout);

        for (DieSketch die : allDice)
            die.draw();

        mSensorManager.registerListener(mShakeDetector,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(mShakeDetector);
    }

    //Handle toolbar items clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean found = onOptionItemSelectedUtils(item.getItemId(), findViewById(R.id.dice_drawer_layout));
        return found || super.onOptionsItemSelected(item);
    }

    /**
     * If no die is rolling, rolls all dice
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
     * @param view Not used
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
     * If there is place left for another die, add the die corresponding to the pressed button
     * @param button The button that has been pressed
     */
    //I need to cast the view to button because if I don't do it android doesn't recognize it as an onClick function
    public void addAndUpdateDie(View button) {
        if (allDice.size() < MAX_DICE_NUMBER) {
            addAndShowDie(Integer.parseInt(((Button) button).getText().toString().substring(1)));
        }
    }

    /**
     * Add a die of with numberOfFaces faces on the layout
     * @param numberOfFaces The number of faces of the die to show
     */
    private void addAndShowDie(int numberOfFaces) {
        DieSketch dieSketch = new DieSketch(compressedDiceResourcesPath.get(numberOfFaces), ROTATION_SPEED);
        allDice.add(dieSketch);
        showDie(dieSketch);
    }

    /**
     * Shows a dieSketch
     * @param die the dieSketch to show
     */
    private void showDie(DieSketch die) {
        FrameLayout frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        PFragment fragment = new PFragment(die);
        fragment.setView(frame, this);
    }

    // Thank to https://stackoverflow.com/questions/2317428/how-to-refresh-app-upon-shaking-the-device
    private static class ShakeDetector implements SensorEventListener {

        // The gForce that is necessary to register as shake. Must be greater than 1G (one earth gravity unit)
        private static final float SHAKE_THRESHOLD_GRAVITY = 2F;
        private static final int SHAKE_SLOP_TIME_MS = 400;
        private static final int SHAKE_COUNT_RESET_TIME_MS = 2000;

        private OnShakeListener mListener = sh -> {};
        private long mShakeTimestamp = 0;
        private int mShakeCount = 0;

        private void setOnShakeListener(OnShakeListener listener) {
            this.mListener = listener;
        }

        private interface OnShakeListener {
            void onShake(int count);
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // ignored
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            if (mListener != null) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];

                float gX = x / SensorManager.GRAVITY_EARTH;
                float gY = y / SensorManager.GRAVITY_EARTH;
                float gZ = z / SensorManager.GRAVITY_EARTH;

                // gForce will be close to 1 when there is no movement.
                float gForce = (float) Math.sqrt(gX * gX + gY * gY + gZ * gZ);

                if (gForce > SHAKE_THRESHOLD_GRAVITY) {
                    final long now = System.currentTimeMillis();
                    // ignore shake events too close to each other
                    if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now ) {
                        return;
                    }

                    // reset the shake count after 3 seconds of no shakes
                    if (mShakeTimestamp + SHAKE_COUNT_RESET_TIME_MS < now ) {
                        mShakeCount = 0;
                    }

                    mShakeTimestamp = now;
                    mShakeCount++;

                    mListener.onShake(mShakeCount);
                }
            }
        }
    }
}
