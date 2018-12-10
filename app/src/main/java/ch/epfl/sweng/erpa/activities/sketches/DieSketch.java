package ch.epfl.sweng.erpa.activities.sketches;

import ch.epfl.sweng.erpa.util.Triplet;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import processing.core.PApplet;
import processing.core.PShape;

public class DieSketch extends PApplet {

    private float angleX = 0;
    private float angleY = 0;
    private float angleZ = 0.5f;
    private PShape die = null;
    private float factor;
    private int dieValue;
    private String fileName;
    private float iX = 0.1f;
    private float iZ = 0.1f;
    private boolean rolling  = false;
    private int rollLength = Integer.MAX_VALUE;
    private int rollStarted  = millis();
    private Random rng = new Random();

    private final int MAX_EXTENDED_ROLL_TIME = 2000;
    private final int MINIMUM_ROLL_TIME = 1000;

    /**
     * Map from die type to filename
     */
    private static final Map<String, Integer> compressedDiceResourcesPath =
            Collections.unmodifiableMap(new HashMap<String, Integer>() {{
                put("shapes/d4.obj", 4);
                put("shapes/d6.obj", 6);
                put("shapes/d8.obj", 8);
                put("shapes/d10.obj", 10);
                put("shapes/d20.obj", 20);
            }});

    public DieSketch(String fileName, float factor) {
        super();
        this.dieValue = compressedDiceResourcesPath.get(fileName);
        this.fileName = fileName;
        this.factor = factor;
    }

    public void settings() {
        size(340, 340, P3D);
    }

    @Override
    public void setup() {
        die = loadShape(fileName);

        if (die == null)
            throw new RuntimeException("Cannot get object");
        iX = 0.1f;
    }

    public void draw() {
        //secondary color
        background(246, 236, 211);

        directionalLight(250, 200, 200, 1, 0, 0.5f);
        directionalLight(200, 250, 200, 0, 1, 0.5f);
        directionalLight(200, 200, 250, 0, -1, 2);

        pushMatrix();
        translate(170, 170, -50);
        scale(-100f);

        if (millis() - rollStarted > rollLength && rolling) {
            setResult();
            rolling = false;
        }

        if (rolling) {
            iZ = iX;
            rotatingDie();
        } else
            staticDie();

        stroke(255);
        shape(die);
        popMatrix();
    }

    private void rotatingDie() {
        rotateX(angleX * factor);
        rotateZ(angleZ * factor);

        angleX = checkAndIncrementAngle(angleX, iX);
        angleZ = checkAndIncrementAngle(angleZ, iZ);
    }

    private void staticDie() {
        rotateX(angleX);
        rotateY(angleY);
        rotateZ(angleZ);
    }

    private void setResult() {
        Triplet<Float, Float, Float> rotation = diceRotationTranslationTable.get(dieValue).get(rng.nextInt(dieValue) + 1);
        angleX = rotation.getFirst() * PI;
        angleY = rotation.getSecond() * PI;
        angleZ = rotation.getThird() * PI;
    }

    public void roll() {
        rollStarted = millis();
        rolling = true;
        rollLength = MINIMUM_ROLL_TIME + rng.nextInt(MAX_EXTENDED_ROLL_TIME);
    }

    public boolean isRolling() {
        return rolling;
    }

    private float checkAndIncrementAngle(float angle, float increment) {
        return (angle + increment) % 4;
    }

    private final HashMap<Integer, Triplet<Float, Float, Float>> d4rotationTranslationTable =
            new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet<>(0.5f, 0f, 0f));
                put(2, new Triplet<>(1f, 0.2f, 0f));
                put(3, new Triplet<>(1.1f, 0.25f, 0.5f));
                put(4, new Triplet<>(1.8f, 2f, 0.3f));
            }};

    private final HashMap<Integer, Triplet<Float, Float, Float>> d6rotationTranslationTable =
            new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet<>(1.5f, 0.5f, -0.5f));
                put(2, new Triplet<>(1f, 0.5f, -0.5f));
                put(3, new Triplet<>(1f, 1f, -0.5f));
                put(4, new Triplet<>(1f, 0f, 0.5f));
                put(5, new Triplet<>(1f, 0.5f, 0.5f));
                put(6, new Triplet<>(1.5f, 0.5f, 0.5f));
            }};

    private final HashMap<Integer, Triplet<Float, Float, Float>> d8rotationTranslationTable =
            new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet<>(0.25f, 0f, -0.5f));
                put(2, new Triplet<>(0.75f, 0f, 0.5f));
                put(3, new Triplet<>(0.25f, 0f, -1f));
                put(4, new Triplet<>(0.75f, 0f, -0.5f));
                put(5, new Triplet<>(0.25f, 0f, 0f));
                put(6, new Triplet<>(0.75f, 0f, 1f));
                put(7, new Triplet<>(0.25f, 0f, 0.5f));
                put(8, new Triplet<>(0.75f, 0f, 0f));
            }};

    private final HashMap<Integer, Triplet<Float, Float, Float>> d10rotationTranslationTable =
            new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet<>(0.25f, 0f, -1f));
                put(2, new Triplet<>(0.75f, 0f, 0.4f));
                put(3, new Triplet<>(0.25f, 0f, -0.2f));
                put(4, new Triplet<>(0.75f, 0f, -0.8f));
                put(5, new Triplet<>(0.25f, 0f, 0.2f));
                put(6, new Triplet<>(0.75f, 0f, -0.2f));
                put(7, new Triplet<>(0.25f, 0f, -0.6f));
                put(8, new Triplet<>(0.75f, 0f, 0f));
                put(9, new Triplet<>(0.25f, 0f, 0.6f));
                put(10, new Triplet<>(0.75f, 0f, -0.4f));
            }};

    private final HashMap<Integer, Triplet<Float, Float, Float>> d20rotationTranslationTable =
            new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet<>(1.125f, 0.5f, 0.2f));
                put(2, new Triplet<>(-0.125f, 0.7f, 0.4f));
                put(3, new Triplet<>(-0.313f, 0.3f, 1.25f));
                put(4, new Triplet<>(1f, 0.3f, 0.75f));
                put(5, new Triplet<>(0f, 0.3f, 1.25f));
                put(6, new Triplet<>(-0.125f, 0f, 1f));
                put(7, new Triplet<>(1.125f, 0.75f, 0.5f));
                put(8, new Triplet<>(-0.375f, 0.5f, -1f));
                put(9, new Triplet<>(0.81f, 0.3f, 0.5f));
                put(10, new Triplet<>(1.188f, 0.2f, -0.5f));
                put(11, new Triplet<>(0f, 0.1f, -0.5f));
                put(12, new Triplet<>(1f, 0.1f, -0.7f));
                put(13, new Triplet<>(1.2f, 0.3f, 0.75f));
                put(14, new Triplet<>(-0.3f, -0.1f, 0.8f));
                put(15, new Triplet<>(1f, -0.1f, -0.3f));
                put(16, new Triplet<>(-0.1f, 0.3f, 0.8f));
                put(17, new Triplet<>(0.4f, 1f, 0.45f));
                put(18, new Triplet<>(0.625f, 0.5f, -1f));
                put(19, new Triplet<>(-0.2f, -0.35f, 0.8f));
                put(20, new Triplet<>(0.375f, 0.5f, 0f));
            }};

    private final HashMap<Integer, HashMap<Integer, Triplet<Float, Float, Float>>> diceRotationTranslationTable =
            new HashMap<Integer, HashMap<Integer, Triplet<Float, Float, Float>>>() {{
                put(4, d4rotationTranslationTable);
                put(6, d6rotationTranslationTable);
                put(8, d8rotationTranslationTable);
                put(10, d10rotationTranslationTable);
                put(20, d20rotationTranslationTable);
            }};
}
