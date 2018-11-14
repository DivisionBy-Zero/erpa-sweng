package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.CheckBox;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SortActivityTest {
    @Rule
    public final IntentsTestRule<SortActivity> intentsTestRule = new IntentsTestRule<>(
            SortActivity.class);

    @Test
    public void testClickOnCheckBoxActivates() {
        onView(withId(R.id.dateAsc)).perform(click());
        assertTrue(((CheckBox) intentsTestRule.getActivity().findViewById(R.id.dateAsc)).isChecked());
    }

    @Test
    public void testDoubleClickOnCheckBoxesDesactivates() {
        onView(withId(R.id.dateAsc)).perform(click());
        onView(withId(R.id.dateAsc)).perform(click());
        assertFalse(((CheckBox) intentsTestRule.getActivity().findViewById(R.id.dateAsc)).isChecked());
    }

    @Test
    public void testAllCheckBoxes() {
        onView(withId(R.id.diffAsc)).perform(click());
        onView(withId(R.id.diffDesc)).perform(click());
        onView(withId(R.id.maxNumPlayerAsc)).perform(click());
        onView(withId(R.id.maxNumPlayerDesc)).perform(click());
        onView(withId(R.id.distAsc)).perform(click());
        onView(withId(R.id.distDesc)).perform(click());
        onView(withId(R.id.dateAsc)).perform(click());
        onView(withId(R.id.dateDesc)).perform(click());
        assertFalse(((CheckBox) intentsTestRule.getActivity().findViewById(R.id.dateAsc)).isChecked());
        assertTrue(((CheckBox) intentsTestRule.getActivity().findViewById(R.id.dateDesc)).isChecked());

    }

}