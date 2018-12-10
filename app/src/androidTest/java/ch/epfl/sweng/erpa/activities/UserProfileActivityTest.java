package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Collection;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUser;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSubstring;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule public IntentsTestRule<UserProfileActivity> intentsTestRule =
            new IntentsTestRule<>(UserProfileActivity.class, false, false);

    private UserManagementService ups;
    private UserProfile userProfile;

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        ups = scope.getInstance(UserManagementService.class);

        relaunchActivity("0", true, true);
        checkStage();
    }

    @Test
    public void testIntent() {
        assertTrue(intentsTestRule.getActivity().getIntent().hasExtra(UserManagementService.PROP_INTENT_USER));
    }

    @Test
    public void testUsername() throws Throwable {
        checkStage();
        onView(withId(R.id.usernameTextView)).check(matches(withSubstring("@"+userProfile.getUsername())));
    }

    private void checkStage() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            Collection<Activity> activitiesInStage = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
            activitiesInStage.size();
        });
    }

    @Test
    public void testGM() throws IOException, ServerException {
        String testUuid = "-3";
        relaunchActivity(testUuid, true, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_gm_only)));
    }

    @Test
    public void testPlayer() throws IOException, ServerException {
        String testUuid = "-4";
        relaunchActivity(testUuid, false, true);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_player_only)));
    }

    @Test
    public void testGmAndPlayer() throws IOException, ServerException {
        String testUuid = "-5";
        relaunchActivity(testUuid, true, true);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_player_and_gm)));
    }

    @Test
    public void testNeitherGmNorPlayer() throws IOException, ServerException {
        String testUuid = "-6";
        relaunchActivity(testUuid, false, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText(R.string.user_profile_neither_player_nor_gm)));
    }

    private void relaunchActivity(String username, boolean isGm, boolean isPlayer) throws IOException, ServerException {
        String userUuid = ups.registerNewUsername(username);
        UserSessionToken userSessionToken = new UserSessionToken(userUuid, userUuid);
        userProfile = UserProfile.builder().uuid(userUuid).isGm(true).isPlayer(true).username(userUuid).build();
        scope.getInstance(LoggedUserCoordinator.class).setCurrentLoggedUser(
            new LoggedUser(userSessionToken, userProfile, new Username(userUuid, userUuid)));

        intentsTestRule.finishActivity();
        userProfile = new UserProfile(username, username, "", UserProfile.Experience.Casual, isGm, isPlayer);
        ups.saveUserProfile(userProfile);
        intentsTestRule.launchActivity(new Intent().putExtra(UserManagementService.PROP_INTENT_USER, userUuid));
    }
}
