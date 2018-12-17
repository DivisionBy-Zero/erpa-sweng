package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;

import com.annimon.stream.Stream;
import com.google.common.primitives.Ints;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSubstring;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule public ActivityTestRule<UserProfileActivity> activityTestRule =
        new ActivityTestRule<>(UserProfileActivity.class, false, false);

    @Inject LoggedUserCoordinator loggedUserCoordinator;
    @Inject UserManagementService userManagementService;
    @Inject UserProfile userProfile;
    @Inject Username username;

    private static boolean activityAsyncVisualElementsReady(UserProfileActivity activity, int... except) {
        return Stream.of(activity.usernameTV, activity.experienceTV)
            .filterNot(v1 -> Ints.contains(except, v1.getId()))
            .map(View::getVisibility)
            .allMatch(v -> v == View.VISIBLE);
    }

    @Test
    public void testIntent() throws Throwable {
        launchActivity("user", true, true);
        assertTrue(activityTestRule.getActivity().getIntent().hasExtra(UserManagementService.PROP_INTENT_USER));
    }

    @Test
    public void testUsername() throws Throwable {
        launchActivity("user", true, true);
        onView(withId(R.id.usernameTextView)).check(matches(withSubstring("@" + username.getUsername())));
    }

    @Test
    public void testGM() throws IOException, ServerException {
        launchActivity("-3", true, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_gm_only)));
    }

    @Test
    public void testPlayer() throws IOException, ServerException {
        launchActivity("-4", false, true);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_player_only)));
    }

    @Test
    public void testGmAndPlayer() throws IOException, ServerException {
        launchActivity("-5", true, true);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_player_and_gm)));
    }

    @Test
    public void testNeitherGmNorPlayer() throws IOException, ServerException {
        launchActivity("-6", false, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_neither_player_nor_gm)));
    }

    private void launchActivity(String usernameStr, boolean isGm, boolean isPlayer) throws IOException, ServerException {
        registerCurrentlyLoggedUser(loggedUserCoordinator, registerUsername(userManagementService, usernameStr));
        userProfile.setIsGm(isGm);
        userProfile.setIsPlayer(isPlayer);
        userManagementService.registerUserProfile(userProfile);
        Intent intent = new Intent().putExtra(UserManagementService.PROP_INTENT_USER, username.getUserUuid());
        activityTestRule.launchActivity(intent);

        while (!activityAsyncVisualElementsReady(activityTestRule.getActivity()))
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }
}
