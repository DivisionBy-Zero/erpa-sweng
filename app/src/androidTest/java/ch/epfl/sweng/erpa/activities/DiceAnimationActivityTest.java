package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class DiceAnimationActivityTest {
    @Rule
    public final IntentsTestRule<DiceAnimationActivity> intentsTestRule = new IntentsTestRule<>(
            DiceAnimationActivity.class);

    @Test
    public void testCanClickOnFlowLayout(){
        onView(withId(R.id.dice_animation_flowLayout)).perform(click());
    }

}