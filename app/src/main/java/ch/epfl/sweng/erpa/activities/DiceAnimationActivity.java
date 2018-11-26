package ch.epfl.sweng.erpa.activities;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.FrameLayout;

import javax.microedition.khronos.opengles.GL10;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.sketches.DieSketch;
import ch.epfl.sweng.erpa.views.FlowLayout;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;

public class DiceAnimationActivity extends AppCompatActivity {

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

        addNewDice("objs/d4R.obj", 1.5f);
        addNewDice("objs/d6R.obj", 0.5f);
        addNewDice("objs/d8R.obj", 0.5f);
        addNewDice("objs/d10R.obj", 0.5f);
        addNewDice("objs/d20_1R.obj", 0.5f);
    }

    public void addNewDice(String fileName, float factor) {
        frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        sketch = new DieSketch(fileName, factor);
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
    }

}
