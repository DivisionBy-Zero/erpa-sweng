package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.CreateGameActivity;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;

@RunWith(AndroidJUnit4.class)
public class CreateGameTest {
    @Rule
    public final IntentsTestRule<CreateGameActivity> intentsTestRule = new IntentsTestRule<>(CreateGameActivity.class);

    @Test
    public void testCanFillFormWithCorrectInputsAndNbSessions() {
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(closeSoftKeyboard());
        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(matches(withSpinnerText(containsString("NOOB"))));
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.oneshot)).perform(click());
        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.numb_session_field)).perform(typeText("2")).perform(closeSoftKeyboard());
        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(matches(withSpinnerText(containsString("5h"))));
        onView(withId(R.id.description_field)).perform(typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testCanFillFormWithCorrectInputsWithoutNbSessions() {
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(closeSoftKeyboard());
        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(matches(withSpinnerText(containsString("NOOB"))));
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.oneshot)).perform(click());
        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(matches(withSpinnerText(containsString("5h"))));
        onView(withId(R.id.description_field)).perform(typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testEmptyFieldCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.emptyFieldMessage)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyCheckboxCreatesCorrectPopup()
    {
        onView(ViewMatchers.withId(R.id.create_game_name_field)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("2")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.description_field)).perform(typeText("bla bla bla")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.uncheckedCheckboxMessage)).check(matches(isDisplayed()));
    }
}
