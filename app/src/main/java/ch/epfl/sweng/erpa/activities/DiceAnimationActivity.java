package ch.epfl.sweng.erpa.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.FrameLayout;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.views.FlowLayout;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class DiceAnimationActivity extends AppCompatActivity {
    private static final Map<Integer, String> compressedDiceResourcesPath =
            Collections.unmodifiableMap(new HashMap<Integer, String>() {{
                put(4, "shapes/d4.obj");
                put(6, "shapes/d6.obj");
                put(8, "shapes/d8.obj");
                put(10, "shapes/d10.obj");
                put(20, "shapes/d20.obj");
            }});
    private FrameLayout frame;
    private PApplet sketch;
    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_dice_animation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        flowLayout = findViewById(R.id.dice_animation_flowLayout);

        addNewDice(4, 1.5f);
        addNewDice(6, 0.5f);
        addNewDice(8, 0.5f);
        addNewDice(10, 0.5f);
        addNewDice(20, 0.5f);
    }

    public void addNewDice(int dieValue, float factor) {
        frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        String shapeResourcePath = Objects.requireNonNull(
                compressedDiceResourcesPath.get(dieValue),
                "Could not find resource file for die of value " + dieValue);

        sketch = new DieSketch(shapeResourcePath, dieValue, factor);
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
    }
}
