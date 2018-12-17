package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;

import com.annimon.stream.function.Consumer;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class SignupActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule
    public final IntentsTestRule<SignupActivity> intentsTestRule = new IntentsTestRule<>(SignupActivity.class);

    @Mock LoggedUserCoordinator luc;

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        intentsTestRule.getActivity().loggedUserCoordinator = luc;
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(4).run();
            return null;
        }).when(luc).trySignUp(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void testEmptyUsernameCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noNameMessage)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyPasswordCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noPassMessage)).check(matches(isDisplayed()));
    }

    @Test
    public void testIncorrectPasswordConfirmCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.passwords_not_match)).check(matches(isDisplayed()));
    }

    @Test
    public void testUsernameAlreadyInUse() {
        String errorString = "User Already exists";
        doAnswer(invocation -> {
            invocation.<Consumer<Throwable>>getArgument(5).accept(new IllegalArgumentException(errorString));
            return null;
        }).when(luc).trySignUp(any(), any(), any(), any(), any(), any());
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("admin")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.isGM)).perform(ViewActions.click());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the correct popup is displayed
        onView(ViewMatchers.withSubstring(errorString)).check(matches(isDisplayed()));
    }

    @Test
    public void testCorrectSignup() {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.isGM)).perform(ViewActions.click());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the activity is closed
        assertTrue(intentsTestRule.getActivity().isFinishing());
    }

    @Test
    public void testGMOrPlayerNotCheckedCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.nameText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passText)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.passTextConfirm)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.signupButton)).perform(ViewActions.click());
        // Check if the activity is closed
        onView(ViewMatchers.withText(R.string.not_select_GM_or_player)).check(matches(isDisplayed()));
    }
}
