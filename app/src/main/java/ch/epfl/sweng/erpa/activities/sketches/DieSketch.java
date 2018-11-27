package ch.epfl.sweng.erpa.activities.sketches;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import ch.epfl.sweng.erpa.util.Triplet;
import processing.core.PApplet;
import processing.core.PShape;

public class DieSketch extends PApplet {
    private static final Map<Integer, Triplet<Float, Float, Float>> d4rotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet(0.5f, 0f, 0f));
                put(2, new Triplet(1f, 0.2f, 0f));
                put(3, new Triplet(1.1f, 0.25f, 0.5f));
                put(4, new Triplet(1.8f, 2f, 0.3f));
            }});
    private static final Map<Integer, Triplet<Float, Float, Float>> d6rotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet(1.5f, 0.5f, -0.5f));
                put(2, new Triplet(1f, 0.5f, -0.5f));
                put(3, new Triplet(1f, 1f, -0.5f));
                put(4, new Triplet(1f, 0f, 0.5f));
                put(5, new Triplet(1f, 0.5f, 0.5f));
                put(6, new Triplet(1.5f, 0.5f, 0.5f));
            }});
    private static final Map<Integer, Triplet<Float, Float, Float>> d8rotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet(0.25f, 0f, -0.5));
                put(2, new Triplet(0.75f, 0f, 0.5f));
                put(3, new Triplet(0.25f, 0f, -1f));
                put(4, new Triplet(0.75f, 0f, -0.5f));
                put(5, new Triplet(0.25f, 0f, 0f));
                put(6, new Triplet(0.75f, 0f, 1f));
                put(7, new Triplet(0.25f, 0f, 0.5f));
                put(8, new Triplet(0.75f, 0f, 0f));
            }});
    private static final Map<Integer, Triplet<Float, Float, Float>> d10rotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet(0.25f, 0f, -1f));
                put(2, new Triplet(0.75f, 0f, 0.4f));
                put(3, new Triplet(0.25f, 0f, -0.2f));
                put(4, new Triplet(0.75f, 0f, -0.8f));
                put(5, new Triplet(0.25f, 0f, 0.2f));
                put(6, new Triplet(0.75f, 0f, -0.2f));
                put(7, new Triplet(0.25f, 0f, -0.6f));
                put(8, new Triplet(0.75f, 0f, 0f));
                put(9, new Triplet(0.25f, 0f, 0.6f));
                put(10, new Triplet(0.75f, 0f, -0.4f));
            }});
    private static final Map<Integer, Triplet<Float, Float, Float>> d20rotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Triplet<Float, Float, Float>>() {{
                put(1, new Triplet(1.125f, 0.5f, 0.2f));
                put(2, new Triplet(-0.125f, 0.7f, 0.4f));
                put(3, new Triplet(-0.313f, 0.3f, 1.25f));
                put(4, new Triplet(1f, 0.3f, 0.75f));
                put(5, new Triplet(0f, 0.3f, 1.25f));
                put(6, new Triplet(-0.125f, 0f, 1f));
                put(7, new Triplet(1.125f, 0.75f, 0.5f));
                put(8, new Triplet(-0.375f, 0.5f, -1f));
                put(9, new Triplet(0.81f, 0.3f, 0.5f));
                put(10, new Triplet(1.188f, 0.2f, -0.5f));
                put(11, new Triplet(0f, 0.1f, -0.5f));
                put(12, new Triplet(1f, 0.1f, -0.7f));
                put(13, new Triplet(1.2f, 0.3f, 0.75f));
                put(14, new Triplet(-0.3f, -0.1f, 0.8f));
                put(15, new Triplet(1f, -0.1f, -0.3f));
                put(16, new Triplet(-0.1f, 0.3f, 0.8f));
                put(17, new Triplet(0.4f, 1f, 0.45f));
                put(18, new Triplet(0.625f, 0.5f, -1f));
                put(19, new Triplet(-0.2f, -0.35f, 0.8f));
                put(20, new Triplet(0.375f, 0.5f, 0f));
            }});
    private static final Map<Integer, Map<Integer, Triplet<Float, Float, Float>>> diceRotationTranslationTable =
            Collections.unmodifiableMap(new HashMap<Integer, Map<Integer, Triplet<Float, Float, Float>>>() {{
                put(4, d4rotationTranslationTable);
                put(6, d6rotationTranslationTable);
                put(8, d8rotationTranslationTable);
                put(10, d10rotationTranslationTable);
                put(20, d20rotationTranslationTable);
            }});
    PShape die = null;
    float angleX = 0;
    float angleY = 0;
    float angleZ = 0.5f;
    float factor;
    float iX = 0;
    float iZ = 0;
    int dieValue;
    String fileName;

    public DieSketch(String fileName, int dieValue, float factor) {
        super();
        this.dieValue = dieValue;
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

        directionalLight(250, 200, 200, 1, 0, 0.5f);
        directionalLight(200, 250, 200, 0, 1, 0.5f);
        directionalLight(200, 200, 250, 0, -1, 2);

        pushMatrix();
        translate(250, 250, -50);
        scale(-100f);
        if (iX == 0)
            staticDie();
        else
            rotatingDie();
        stroke(255);
        shape(die);
        popMatrix();

        if (mousePressed) {
            if (iX == 0) {
                iX = 0.1f;
                iZ = 0.04f;
            } else {
                iX = 0;
            }
        }

    }

    private void rotatingDie() {
        rotateX(angleX * factor);
        rotateZ(angleZ * factor);

        angleX = checkAndIncrementAngle(angleX, iX);
        angleZ = checkAndIncrementAngle(angleZ, iZ);
    }

    private void staticDie() {
        pickNumber(4);

        rotateX(angleX);
        rotateY(angleY);
        rotateZ(angleZ);
    }

    private void pickNumber(int nb) {
        rotate(dieValue, nb);
    }

    private void rotate(int dieNb, int nb) {
        Triplet<Float, Float, Float> rotation = diceRotationTranslationTable.get(dieNb).get(nb);
        angleX = rotation.getFirst() * PI;
        angleY = rotation.getSecond() * PI;
        angleZ = rotation.getThird() * PI;
    }

    public float checkAndIncrementAngle(float angle, float increment) {
        float mAngle = angle + increment;
        if (mAngle > 4) {
            mAngle = 0;
        }
        return mAngle;
    }
}
