package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class PastGamesActivityTest {
    @Rule
    public final IntentsTestRule<PastGamesActivity> intentsTestRule = new IntentsTestRule<>(
            PastGamesActivity.class);

    @Test
    public void testConfirmedGamesDisplaysCorrectTextView() {
        onView(withId(R.id.pastGamesTextView)).check(matches(withText(
                intentsTestRule.getActivity().getResources().getString(
                        R.string.pastGamesText))));
    }
}