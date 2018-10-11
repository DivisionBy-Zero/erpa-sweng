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
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;

@RunWith(AndroidJUnit4.class)
public class LoginActivityTest {
    @Rule
    public final IntentsTestRule<LoginActivity> intentsTestRule = new IntentsTestRule<>(LoginActivity.class);

    @Test
    public void testEmptyUsernameCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noNameMessage)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyPasswordCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noPassMessage)).check(matches(isDisplayed()));
    }

    @Test
    public void testIncorrectLoginCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.password)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.incorrectLogin)).check(matches(isDisplayed()));
    }

    @Test
    public void testCorrectLogin()
    {
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.password)).perform(typeText("admin")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the correct activity is displayed
        intended(hasComponent(ch.epfl.sweng.erpa.activities.MainActivity.class.getName()));
    }

    @Test
    public void testWithoutLogin()
    {
        onView(ViewMatchers.withId(R.id.no_login_button)).perform(ViewActions.click());
        // Check if the correct activity is displayed
        intended(hasComponent(ch.epfl.sweng.erpa.activities.MainActivity.class.getName()));
    }
}
