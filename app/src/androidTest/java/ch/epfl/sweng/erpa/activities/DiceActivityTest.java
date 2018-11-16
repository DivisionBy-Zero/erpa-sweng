package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import ch.epfl.sweng.erpa.R;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class DiceActivityTest {
    @Rule
    public final IntentsTestRule<DiceActivity> intentsTestRule = new IntentsTestRule<>(
            DiceActivity.class);

    @Test
    public void testCorrectOutputForD4Die() {
        onView(withId(R.id.d4_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(4)));
        }
    }

    @Test
    public void testCorrectOutputForD6Die() {
        onView(withId(R.id.d6_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(6)));
        }
    }

    @Test
    public void testCorrectOutputForD8Die() {
        onView(withId(R.id.d8_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(8)));
        }
    }

    @Test
    public void testCorrectOutputForD10Die() {
        onView(withId(R.id.d6_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(10)));
        }
    }

    @Test
    public void testCorrectOutputForD12Die() {
        onView(withId(R.id.d12_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(12)));
        }
    }

    @Test
    public void testCorrectOutputForD20Die() {
        onView(withId(R.id.d20_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(20)));
        }
    }

    @Test
    public void testCorrectOutputForD100Die() {
        onView(withId(R.id.d100_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(100)));
        }
    }

    @Test
    public void testShowsOnAllResultView() {
        onView(withId(R.id.d100_number)).perform(typeText("15")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i){
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll2)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll3)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll4)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll5)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll6)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll7)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll8)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll9)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll10)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll11)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll12)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll13)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll14)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll15)).check(matches(textViewHasCorrectValue(100)));
        }
    }

    @Test
    public void testCanShowDifferentDice() {
        onView(withId(R.id.d12_number)).perform(typeText("5"));
        onView(withId(R.id.d100_number)).perform(typeText("2")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll2)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll3)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll4)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll5)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll6)).check(matches(textViewHasCorrectValue(100)));
            onView(withId(R.id.roll7)).check(matches(textViewHasCorrectValue(100)));
        }
    }

    @Test
    public void testCanShowAllDiceTypeAtOnce() {
        onView(withId(R.id.d4_number)).perform(typeText("1"));
        onView(withId(R.id.d6_number)).perform(typeText("1"));
        onView(withId(R.id.d8_number)).perform(typeText("1"));
        onView(withId(R.id.d10_number)).perform(typeText("1"));
        onView(withId(R.id.d12_number)).perform(typeText("1"));
        onView(withId(R.id.d20_number)).perform(typeText("1"));
        onView(withId(R.id.d100_number)).perform(typeText("1")).perform(closeSoftKeyboard());
        for (int i = 0; i < 10; ++i) {
            onView(withId(R.id.rollButton)).perform(click());
            onView(withId(R.id.roll1)).check(matches(textViewHasCorrectValue(4)));
            onView(withId(R.id.roll2)).check(matches(textViewHasCorrectValue(6)));
            onView(withId(R.id.roll3)).check(matches(textViewHasCorrectValue(8)));
            onView(withId(R.id.roll4)).check(matches(textViewHasCorrectValue(10)));
            onView(withId(R.id.roll5)).check(matches(textViewHasCorrectValue(12)));
            onView(withId(R.id.roll6)).check(matches(textViewHasCorrectValue(20)));
            onView(withId(R.id.roll7)).check(matches(textViewHasCorrectValue(100)));
        }
    }

    @Test
    public void testCreatePopupTooMuchDice() {
        onView(withId(R.id.d6_number)).perform(typeText("20")).perform(closeSoftKeyboard());
        onView(withId(R.id.rollButton)).perform(click());
        onView(ViewMatchers.withText("The number of dice must be less or equal to 15")).check(matches(isDisplayed()));
    }

    private Matcher<View> textViewHasCorrectValue(int diceSize) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("The TextView has value correct value and length");
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView)) {
                    return false;
                }

                if (view != null) {
                    String text = ((TextView) view).getText().toString();

                    Scanner scanner = new Scanner(text);
                    List<Integer> list = new ArrayList<Integer>();
                    while (scanner.hasNextInt()) {
                        list.add(scanner.nextInt());
                    }
                    
                    for (int i : list) {
                        if (i > diceSize) {
                            return false;
                        }
                    }
                    return true;
                }

                return false;
            }
        };
    }
}