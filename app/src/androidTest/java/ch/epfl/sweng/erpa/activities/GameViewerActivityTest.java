package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
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
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static org.hamcrest.core.StringContains.containsString;

@RunWith(AndroidJUnit4.class)
public class GameViewerActivityTest {
    @Rule public final ActivityTestRule<GameViewerActivity> activityTestRule = new ActivityTestRule<>(GameViewerActivity.class, false, false);
    private Scope scope;
    private Game game = getGame("hewwo");

    @Before
    public void prepare() {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        scope = Toothpick.openScope(InstrumentationRegistry.getTargetContext().getApplicationContext());
        ErpaApplication application = scope.getInstance(ErpaApplication.class);

        Toothpick.reset(scope);
        application.installModules(scope);
        scope.getInstance(RemoteServicesProviderCoordinator.class).bindRemoteServicesProvider(
                DummyRemoteServicesProvider.class
        );

        scope.getInstance(GameService.class).saveGame(game);

        Intent i = new Intent();
        i.putExtra(GameService.PROP_INTENT_GAMEUUID, game.getGameUuid());
        activityTestRule.launchActivity(i);
    }

    @Test
    public void testHasIntent() {
        assert (activityTestRule.getActivity().getIntent().hasExtra(GameService.PROP_INTENT_GAMEUUID));
    }

    @Test
    public void testDescription() {
        onView(withId(R.id.descriptionTextView)).check(matches(withText(game.getDescription())));
    }


    @Test
    public void testGmName() {
        onView(withId(R.id.gmTextView)).check(matches(withText(game.getGmUuid())));
    }

    @Test
    public void testUniverse() {
        onView(withId(R.id.universeTextView)).check(matches(withText(game.getUniverse())));
    }

    @Test
    public void testTitle() {
        onView(withId(R.id.titleTextView)).check(matches(withText(game.getName())));
    }

    @Test
    public void testDifficulty() {
        onView(withId(R.id.difficultyTextView)).check(matches(withText(game.getDifficulty().toString())));
    }

    @Test
    public void testType() {
        onView(withId(R.id.oneShotOrCampaignTextView)).check(matches(withText(game.getOneshotOrCampaign().toString())));
    }

    @Test
    public void testSessionLength() {
        onView(withId(R.id.sessionLength)).check(matches(withText(game.getSessionLengthInMinutes().toString())));
    }

    @Test
    public void testNumSessions() {
        onView(withId(R.id.sessionNumberTextView)).check(matches(withText(game.getNumberSessions().toString())));
    }

    private <T> ViewAssertion containsText(T text) {
        return matches(withText(containsString(String.valueOf(text))));
    }

}
