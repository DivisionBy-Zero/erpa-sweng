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
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

@RunWith(MockitoJUnitRunner.class)
public class LoginActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule
    public final IntentsTestRule<LoginActivity> intentsTestRule = new IntentsTestRule<>(LoginActivity.class);

    @Mock LoggedUserCoordinator luc;

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        intentsTestRule.getActivity().loggedUserCoordinator = luc;
        doAnswer(invocation -> {
            invocation.<Runnable>getArgument(3).run();
            return null;
        }).when(luc).tryLogin(any(), any(), any(), any(), any());
    }

    @Test
    public void testEmptyUsernameCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noNameMessage)).check(matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyPasswordCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withText(R.string.noPassMessage)).check(matches(isDisplayed()));
    }

    @Test
    public void testIncorrectLoginCreatesCorrectPopup() {
        String errorString = "Bad auth";
        doAnswer(invocation -> {
            invocation.<Consumer<Throwable>>getArgument(4).accept(new IllegalArgumentException(errorString));
            return null;
        }).when(luc).tryLogin(any(), any(), any(), any(), any());
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("lol"));
        onView(ViewMatchers.withId(R.id.password)).perform(typeText("lol")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the popup is displayed
        onView(ViewMatchers.withSubstring(errorString)).check(matches(isDisplayed()));
    }

    @Test
    public void testCorrectLogin() {
        onView(ViewMatchers.withId(R.id.username)).perform(typeText("admin"));
        onView(ViewMatchers.withId(R.id.password)).perform(typeText("admin")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.login_button)).perform(ViewActions.click());
        // Check if the correct activity is displayed
        assertTrue(intentsTestRule.getActivity().isFinishing());
    }

    @Test
    public void testWithoutLogin() {
        onView(ViewMatchers.withId(R.id.no_login_button)).perform(ViewActions.click());
        // Check if the correct activity is displayed
        intended(hasComponent(ch.epfl.sweng.erpa.activities.MainActivity.class.getName()));
    }
}
