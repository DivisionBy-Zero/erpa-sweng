package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.views.FlowLayout;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class DiceActivityTest {
    @Rule
    public final IntentsTestRule<DiceActivity> intentsTestRule = new IntentsTestRule<>(
            DiceActivity.class);

    @Test
    public void testCanShowAllDice() {
        createTestForDie(R.id.d4_button);
        createTestForDie(R.id.d6_button);
        createTestForDie(R.id.d8_button);
        createTestForDie(R.id.d10_button);
        createTestForDie(R.id.d20_button);
    }

    @Test
    public void testCanRemoveDie() {
        onView(withId(R.id.d4_button)).perform(click());
        onView(withId(R.id.dice_layout)).perform(click()).check(matches(flowLayoutIsEmpty()));
    }

    private void createTestForDie(int dieButtonId) {
        onView(withId(dieButtonId)).perform(click());
        onView(withId(R.id.dice_layout)).check(matches(dieIsOnFlowLayout()));

    }

    private Matcher<View> dieIsOnFlowLayout() {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("The FlowLayout shows the die");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!FlowLayout.class.isAssignableFrom(view.getClass())) {
                    return false;
                }

                if (view != null) {
                    int count = ((FlowLayout) view).getChildCount();
                    ((FlowLayout) view).removeAllViewsInLayout();
                    return  count == 1;
                }

                return false;
            }
        };
    }

    private Matcher<View> flowLayoutIsEmpty() {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("The FlowLayout shows no die");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!FlowLayout.class.isAssignableFrom(view.getClass())) {
                    return false;
                }

                if (view != null) {
                    int count = ((FlowLayout) view).getChildCount();
                    ((FlowLayout) view).removeAllViewsInLayout();
                    return  count == 0;
                }

                return false;
            }
        };
    }
}
