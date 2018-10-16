package ch.epfl.sweng.erpa.activities;


import static android.support.constraint.Constraints.TAG;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasKey;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.arch.persistence.room.Room;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.annimon.stream.Optional;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.RuleChain;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.GameViewerActivity;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.RemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import ch.epfl.sweng.erpa.services.dummy.database.DummyDao;
import ch.epfl.sweng.erpa.services.dummy.database.DummyDatabase;
import toothpick.Scope;
import toothpick.Toothpick;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator.*;

@RunWith(AndroidJUnit4.class)
public class GameViewerActivityTest {
    private final ActivityTestRule<GameViewerActivity> activityTestRule = new ActivityTestRule<>(GameViewerActivity.class);

    private final Game g =
            new Game("John Smith", "Lord of the Smith", 0, 5, Game.Difficulty.CHILL, "hello", Game.OneshotOrCampaign.CAMPAIGN, Optional.empty(), Optional.empty(), "Loremp impums lollol", "hewwo");
    @Rule
    public final TestRule chain = RuleChain.outerRule(
            new ExternalResource()
            {
                @Override public void before()
                {
                    application = (ErpaApplication) InstrumentationRegistry.getTargetContext().getApplicationContext();
                    appScope = Toothpick.openScope(application);
                    RemoteServicesProviderCoordinator rspc = appScope.getInstance(RemoteServicesProviderCoordinator.class);
                    rspc.rspClassFromFullyQualifiedName(DummyRemoteServicesProvider.class.getName()).ifPresentOrElse(
                            rspc::bindRemoteServicesProvider,() -> {throw new RuntimeException("couldn't find rsp");}
                    );

                    RemoteServicesProvider rsp = appScope.getInstance(RemoteServicesProvider.class);
                    rsp.getGameService().saveGame(g);

                    

                }
                @Override public void after()
                {
                    Toothpick.reset(appScope);
                    application.initToothpick(appScope);
                }
            }
    ).around(activityTestRule);

    @Before
    public void initAppAndLaunch()
    {
        Intent i = new Intent();
        Log.d(TAG,"Initiating test");
        i.putExtra(GameViewerActivity.EXTRA_GAME_KEY, g.getGid());
        activityTestRule.launchActivity(i);
    }

    @Test
    public void testHasIntent()
    {
        assert(activityTestRule.getActivity().getIntent().hasExtra(GameViewerActivity.EXTRA_GAME_KEY));
    }
    private Scope appScope;
    private ErpaApplication application;


    @Test
    public void testDescription()
    {
        onView(withId(R.id.gmTextView)).check(matches(withText(g.getGmUniqueID())));
    }

    @Test
    public void testTitle()
    {
        onView(ViewMatchers.withId(R.id.titleTextView)).check(matches(withText(g.getName())));
    }

}
