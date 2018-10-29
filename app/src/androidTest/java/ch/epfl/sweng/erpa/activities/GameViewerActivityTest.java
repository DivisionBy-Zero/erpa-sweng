package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.annimon.stream.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
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
    private Game game = getGame("hewwo");
    private Game emptyOptGame = getGame("empty");

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
        emptyOptGame.setNumberSessions(Optional.empty());
        emptyOptGame.setSessionLengthInMinutes(Optional.empty());
        scope.getInstance(GameService.class).saveGame(game);
        scope.getInstance(GameService.class).saveGame(emptyOptGame);

        Intent i = new Intent();
        i.putExtra(GameService.PROP_INTENT_GAME, game.getGameUuid());
        activityTestRule.launchActivity(i);

    }

    @Test
    public void testHasIntent() {
        assert (activityTestRule.getActivity().getIntent().hasExtra(GameService.PROP_INTENT_GAME));
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
        onView(withId(R.id.sessionLengthTextView)).check(matches(withText(game.getSessionLengthInMinutes().get().toString())));
    }

    @Test
    public void testNumSessions() {
        onView(withId(R.id.sessionNumberTextView)).check(matches(withText(game.getNumberSessions().get().toString())));
    }

    @Test
    public void testEmptyNumSessions() {
        Intent iOld = activityTestRule.getActivity().getIntent();
        activityTestRule.finishActivity();
        Intent i = new Intent();
        i.putExtra(GameService.PROP_INTENT_GAME, emptyOptGame.getGameUuid());
        activityTestRule.launchActivity(i);
        onView(withId(R.id.sessionNumberTextView)).check(matches(withText("Unspecified")));
        onView(withId(R.id.sessionLengthTextView)).check(matches(withText("Unspecified")));
        activityTestRule.finishActivity();
        activityTestRule.launchActivity(iOld);
    }
}
