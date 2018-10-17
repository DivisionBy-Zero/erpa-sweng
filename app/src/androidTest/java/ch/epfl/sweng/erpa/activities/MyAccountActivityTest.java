package ch.epfl.sweng.erpa.activities;

import android.content.ComponentName;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;

import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.erpa.R;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.junit.Assert.*;

public class MyAccountActivityTest {
    @Rule
    public final IntentsTestRule<MyAccountActivity> intentsTestRule = new IntentsTestRule<>(MyAccountActivity.class);

    // TODO : (Anne) rewrite tests to match with refactoring
//    @Test
//    public void testCanLaunchPendingRequestActivity() {
//        onView(ViewMatchers.withId(R.id.launch_pending_request_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(new ComponentName(getTargetContext(), PendingRequestActivity.class)));
//    }
//
//    @Test
//    public void testCanLaunchConfirmedGamesActivity() {
//        onView(ViewMatchers.withId(R.id.launch_confirmed_games_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(ConfirmedGamesActivity.class.getName()));
//    }
//
//    @Test
//    public void testCanLaunchPastGamesActivity() {
//        onView(ViewMatchers.withId(R.id.launch_past_games_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(PastGamesActivity.class.getName()));
//    }
//
//    @Test
//    public void testCanLaunchHostedGamesActivity() {
//        onView(ViewMatchers.withId(R.id.launch_hosted_games_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(HostedGamesActivity.class.getName()));
//    }
//
//    @Test
//    public void testCanLaunchPastHostedGamesActivity() {
//        onView(ViewMatchers.withId(R.id.launch_past_hosted_games_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(PastHostedGamesActivity.class.getName()));
//    }
//
//    @Test
//    public void testCanLaunchProfileActivity() {
//        onView(ViewMatchers.withId(R.id.launch_profile_activity_layout)).perform(ViewActions.click());
//        intended(hasComponent(ProfileActivity.class.getName()));
//    }
}