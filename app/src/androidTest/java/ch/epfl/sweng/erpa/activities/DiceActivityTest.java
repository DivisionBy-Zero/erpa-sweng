package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
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
import java.util.Scanner;

import ch.epfl.sweng.erpa.R;
import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;

@RunWith(AndroidJUnit4.class)
public class DiceActivityTest {
    @Rule
    public final IntentsTestRule<DiceActivity> intentsTestRule = new IntentsTestRule<>(
            DiceActivity.class);

    @Test
    public void testCorrectOutputForAllDice() {
        String[] strList = {"D4", "D6", "D8", "D10", "D12", "D20", "D100"};
        for (String s : strList ) {
            onView(withId(R.id.diceTypeSpinner)).perform(click());
            onData(hasToString(is(s))).perform(click());
            onView(withId(R.id.nbOfDice)).perform(clearText()).perform(typeText("100")).perform(
                    closeSoftKeyboard());
            onView(withId(R.id.rollButton)).perform(click());
            int n = Integer.parseInt(s.substring(1));
            onView(withId(R.id.diceRollResult)).check(matches(textViewHasCorrectValue(n)));
        }
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
                    if (list.size() != 10)
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
