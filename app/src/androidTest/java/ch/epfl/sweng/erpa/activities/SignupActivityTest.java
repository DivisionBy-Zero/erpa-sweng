package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class SignupActivityTest {
    @Rule
    public final IntentsTestRule<SignupActivity> intentsTestRule = new IntentsTestRule<>(SignupActivity.class);

    @Test
    public void testEmptyUsernameCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noNameMessage)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyPasswordCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noPassMessage)).check(matches(isDisplayed()));
    }

    @Test
    public void testIncorrectPasswordConfirmCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.passwordsNotMatch)).check(matches(isDisplayed()));
    }

    @Test
    public void testUsernameAlreadyInUse()
    {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("admin")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.isGM)).perform(ViewActions.click());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the correct popup is displayed
        onView(ViewMatchers.withText(R.string.username_in_use)).check(matches(isDisplayed()));
    }

    @Test
    public void testCorrectSignup()
    {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.isGM)).perform(ViewActions.click());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the activity is closed
        assertTrue(intentsTestRule.getActivity().isFinishing());
    }

    @Test
    public void testGMOrPlayerNotCheckedCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the activity is closed
        onView(ViewMatchers.withText(R.string.notSelectGmOrPlayer)).check(matches(isDisplayed()));
    }
}
