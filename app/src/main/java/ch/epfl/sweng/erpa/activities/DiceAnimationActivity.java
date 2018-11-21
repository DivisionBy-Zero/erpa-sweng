package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.Toast;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.views.FlowLayout;
import processing.android.CompatUtils;
import processing.android.PFragment;
import processing.core.PApplet;
import processing.core.PShape;

public class DiceAnimationActivity extends AppCompatActivity {

    private View img;
    private FrameLayout frame;
    private PApplet sketch;
    private FlowLayout flowLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataBindingUtil.setContentView(this, R.layout.activity_dice_animation);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        //fab.setOnClickListener(new View.OnClickListener() {
        //    @Override
        //    public void onClick(View view) {

        //    }
        //});

        img = findViewById(R.id.d6);
        flowLayout = findViewById(R.id.dice_animation_flowLayout);

        addNewDice("objs/d4R.obj", 2f);
    }

    public void addNewDice(String fileName, float factor) {
        frame = new FrameLayout(this);
        frame.setId(CompatUtils.getUniqueViewId());
        flowLayout.addView(frame);

        sketch = new Sketch(fileName, factor);
        PFragment fragment = new PFragment(sketch);
        fragment.setView(frame, this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (sketch != null) {
            sketch.onRequestPermissionsResult(
                    requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (sketch != null) {
            sketch.onNewIntent(intent);
        }
    }

}

class Sketch extends PApplet {

    PShape die = null;
    float angleX = 0;
    float angleY = 0;
    float angleZ = 0.5f;
    float factor;
    float iX = 0;
    float iZ = 0;
    String fileName;

    Sketch(String fileName, float factor) {
        super();
        this.fileName = fileName;
        this.factor = factor;
    }

    public void settings() {
        size(500, 500, P3D);
    }

    @Override
    public void setup() {

        die = loadShape(fileName);

        if (die == null)
            throw new RuntimeException("Cannot get object");

    }

    public void draw() {
        //secondary color
        background(246, 236, 211);

        camera(width / 2, height / 2, 300, 0, 0, 0, 0, 1, 0);


        directionalLight(250, 200, 200, 1, 0, 0);
        directionalLight(200, 250, 200, 0, 1, 0);
        directionalLight(200, 200, 250, 0, 0, 1);

        pushMatrix();
        translate(-50, 0, 0);
        scale(-150f);
        rotateX(angleX * factor);
        rotateZ(angleZ * factor);
        stroke(255);
        shape(die);
        popMatrix();


        angleX = checkAndIncrementAngle(angleX, iX);
        angleZ = checkAndIncrementAngle(angleZ, iZ);

        if (mousePressed) {
            if(iX == 0) {
                iX = 0.1f;
                iZ = 0.04f;
            }
            else {
                iX = 0;
                iZ = 0;
                angleX = PI/2;
                angleY = 0;
                angleZ = 0;
                rotateY(angleY);
            }
        }
    }

    public float checkAndIncrementAngle(float angle, float increment) {
        float mAngle = angle + increment;
        if (mAngle >= 4 * PI) {
            mAngle = 0;
        }
        return mAngle;
    }
}
