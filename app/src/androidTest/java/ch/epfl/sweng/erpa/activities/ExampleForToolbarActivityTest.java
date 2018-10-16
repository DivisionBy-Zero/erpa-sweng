package ch.epfl.sweng.erpa.activities;

import android.support.design.widget.CoordinatorLayout;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.NestedScrollView;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class ExampleForToolbarActivityTest {
    @Rule
    public final IntentsTestRule<ExampleForToolbarActivity> intentsTestRule = new IntentsTestRule<>(ExampleForToolbarActivity.class);

    @Test
    public void testScrolling() {
        NestedScrollView view = intentsTestRule.getActivity().findViewById(R.id.NestedScrollView1);
        assertTrue(view.canScrollVertically(1));
        view.scrollTo(0, 10);
        assertTrue(view.canScrollVertically(-1));
    }

    @Test
    public void testHideWhenScrollDown() {
        CoordinatorLayout view = intentsTestRule.getActivity().findViewById(R.id.CoordinatorLayout);
        view.scrollTo(0, 1000);
        onView(withId(R.id.toolbar_main)).check(matches(not(isCompletelyDisplayed())));
    }

    @Test
    public void testShowWhenScrollUp() {
        CoordinatorLayout view = intentsTestRule.getActivity().findViewById(R.id.CoordinatorLayout);
        view.scrollTo(0, 1000);
        view.scrollTo(0, -500);
        onView(withId(R.id.toolbar_main)).check(matches(isDisplayed()));
        onView(withText(R.string.title_example_for_toolbar_activity)).check(matches(withParent(withId(R.id.toolbar_main))));
    }

}
