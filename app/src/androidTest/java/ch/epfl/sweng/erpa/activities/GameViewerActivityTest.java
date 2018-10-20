package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAssertion;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;
import toothpick.Scope;
import toothpick.Toothpick;

import static org.hamcrest.core.StringContains.containsString;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class GameViewerActivityTest
{
    private final ActivityTestRule<GameViewerActivity> activityTestRule = new ActivityTestRule<>(GameViewerActivity.class, false, false);
    private Scope scope;
    private ErpaApplication app;
    private Game game = getGame("hewwo");
    private DummyGameService gs;
    @Rule public final TestRule chain = RuleChain.outerRule(
            new ExternalResource()
            {
                @Override
                protected void before() throws Throwable
                {
                    app = (ErpaApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
                    scope = Toothpick.openScope(app);
                    RemoteServicesProviderCoordinator rsp = scope.getInstance(RemoteServicesProviderCoordinator.class);
                    rsp.rspClassFromFullyQualifiedName(DummyRemoteServicesProvider.class.getName()).ifPresentOrElse(
                            rsp::bindRemoteServicesProvider, () -> { throw new RuntimeException("Couldn't find RSP");}
                    );
                    gs = new DummyGameService(InstrumentationRegistry.getTargetContext().getApplicationContext());
                }

                @Override
                protected void after()
                {
                    Toothpick.reset(scope);
                    app.initToothpick(scope);
                }
            }

    ).around(activityTestRule);

    @Before
    public void initIntent()
    {
        Intent i = new Intent();
        i.putExtra(GameViewerActivity.EXTRA_GAME_KEY, game.getGid());
        activityTestRule.launchActivity(i);
        gs.saveGame(game);
    }


    @Test
    public void testHasIntent()
    {
        assert(activityTestRule.getActivity().getIntent().hasExtra(GameViewerActivity.EXTRA_GAME_KEY));
    }

    @Test
    public void testDescription()
    {
        onView(withId(R.id.descriptionTextView)).check(matches(withText(game.getDescription())));
    }


    @Test
    public void testGmName()
    {
        onView(withId(R.id.gmTextView)).check(matches(withText(game.getGmUid())));
    }

    @Test
    public void testUniverse()
    {
        onView(withId(R.id.universeTextView)).check(matches(withText(game.getUniverse())));
    }

    @Test
    public void testTitle()
    {
        onView(withId(R.id.titleTextView)).check(matches(withText(game.getName())));
    }

    @Test
    public void testMisc()
    {
        onView(withId(R.id.generalInfoTextView)).check(containsText(game.getNumSessions()));
    }


    private <T> ViewAssertion containsText(T text)
    {
        return matches(withText(containsString(String.valueOf(text))));
    }
    private Game getGame(String gid)
    {
        return new Game(
                gid,
                "Sapphie",
                "The land of the Sapphie",
                "Bepsi is gud",
                "Sapphtopia",
                "E X T R E M E",
                "Campaign",
                -73,
                Integer.MAX_VALUE
        );
    }
}
