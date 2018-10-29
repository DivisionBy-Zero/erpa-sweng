package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.UserProfileService;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sweng.erpa.util.TestUtils.getUserProfile;
import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.core.AllOf.allOf;

@RunWith(AndroidJUnit4.class)
public class UserProfileActivityTest {
    @Rule public ActivityTestRule<UserProfileActivity> activityTestRule =
            new ActivityTestRule<>(UserProfileActivity.class, false, false);

    private UserProfileService ups;
    private UserProfile userProfile = getUserProfile("hey");

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        Scope scope = Toothpick.openScope(InstrumentationRegistry.getTargetContext().getApplicationContext());
        ErpaApplication application = scope.getInstance(ErpaApplication.class);

        Toothpick.reset(scope);
        application.installModules(scope);
        scope.getInstance(RemoteServicesProviderCoordinator.class).bindRemoteServicesProvider(
                DummyRemoteServicesProvider.class
        );
        ups = scope.getInstance(UserProfileService.class);

        Intent i = new Intent();
        i.putExtra(UserProfileService.PROP_INTENT_USER, userProfile.getUuid());
        ups.saveUserProfile(userProfile);
        activityTestRule.launchActivity(i);
    }

    @Test
    public void testIntent() {
        assertTrue(activityTestRule.getActivity().getIntent().hasExtra(UserProfileService.PROP_INTENT_USER));
    }

    @Test
    public void testUsername() {
        onView(withId(R.id.usernameTextView)).check(matches(withText(userProfile.getUsername())));
    }

    @Test
    public void testExperience() {
        onView(withId(R.id.experienceTextView)).check(matches(withText(userProfile.getXp().toString())));
    }

    @Test
    public void testGM() {
        String testUuid = "-3";
        relaunchActivity(testUuid, true, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText("Game master")));
    }

    @Test
    public void testPlayer() {
        String testUuid = "-4";
        relaunchActivity(testUuid, false, true);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText("Player")));
    }

    @Test
    public void testGmAndPlayer() {
        String testUuid = "-5";
        relaunchActivity(testUuid, true, true);
        onView(withId(R.id.playerOrGMTextView)).check(
                matches(allOf(
                        withText(containsString("Player")),
                        withText(containsString("Game master")))));
    }

    @Test
    public void testNeitherGmNorPlayer() {
        String testUuid = "-6";
        relaunchActivity(testUuid, false, false);
        onView(withId(R.id.playerOrGMTextView)).check(matches(withText("")));
    }

    private void relaunchActivity(String id, boolean isGm, boolean isPlayer) {
        activityTestRule.finishActivity();
        UserProfile gmProfile = new UserProfile(id, "Sapphie", "", UserProfile.Experience.Casual, isGm, isPlayer);
        ups.saveUserProfile(gmProfile);
        activityTestRule.launchActivity((new Intent()).putExtra(UserProfileService.PROP_INTENT_USER, id));
    }
}
