package ch.epfl.sweng.erpa.activities;

import android.content.res.Resources;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.swipeUp;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CreateGameTest {
    private Resources systemResources;

    @Rule
    public final IntentsTestRule<CreateGameActivity> intentsTestRule = new IntentsTestRule<>(
            CreateGameActivity.class);

    @Before
    public void setSystemResources() {
        systemResources = intentsTestRule.getActivity().getResources();
    }

    @Test
    public void testCanParseAnySessionLength() {
        boolean q = Stream.of(systemResources.getStringArray(R.array.session_length_array))
                .filter(sl -> !"Undefined".equals(sl))
                .map(CreateGameActivity::findSessionLength)
                .allMatch(Optional::isPresent);
        assertTrue(q);
    }

    @Test
    public void testCanParseAnyDifficulty() {
        //noinspection Convert2MethodRef -- Objects::nonNull was introduced in API 24
        boolean q = Stream.of(systemResources.getStringArray(R.array.difficulties_array))
                .map(CreateGameActivity::findDifficulty)
                .allMatch(o -> o != null);
        assertTrue(q);
    }

    @Test
    public void testCanFillFormWithCorrectInputsAndNbSessions() {
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(
                closeSoftKeyboard());

        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(
                matches(withSpinnerText(containsString("NOOB"))));

        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));

        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(
                matches(withSpinnerText(containsString("5h"))));

        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.num_session_field)).perform(typeText("2")).perform(closeSoftKeyboard());

        onView(withId(R.id.description_field)).perform(
                typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        onView(withId(R.id.create_game_form)).perform(swipeUp());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testCanFillFormWithCorrectInputsWithoutNbSessions() throws InterruptedException {
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(
                matches(withSpinnerText(containsString("NOOB"))));
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(
                matches(withSpinnerText(containsString("5h"))));
        onView(withId(R.id.oneshot)).perform(click());
        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.description_field)).perform(
                typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        onView(withId(R.id.create_game_form)).perform(swipeUp());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testEmptyFieldCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.emptyFieldMessage)).check(
                matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyCheckboxCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.create_game_name_field)).perform(typeText("lol")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.description_field)).perform(
                typeText("bla bla bla")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.uncheckedCheckboxMessage)).check(
                matches(isDisplayed()));
    }

    @Test
    public void testCanFillUniverseFieldIfOtherIsPicked() {
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.universe_field)).perform(typeText("KazAdrok")).perform(
                closeSoftKeyboard());
    }

    @Test
    public void testCreatePopUpIfMaxSmallerThanMin() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.invalidPlayerNumber)).check(matches(isDisplayed()));
    }

    @Test
    public void test0PlayerCreatePopUp() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("0")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.invalidPlayerNumber)).check(matches(isDisplayed()));
    }
}
