package ch.epfl.sweng.erpa.activities;


import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.BundleMatchers.hasKey;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtraWithKey;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.GameViewerActivity;
import ch.epfl.sweng.erpa.model.Game;

@RunWith(AndroidJUnit4.class)
public class GameViewerActivityTest {
    @Rule
    public final ActivityTestRule<GameViewerActivity> activityTestRule = new ActivityTestRule<>(GameViewerActivity.class, false, false);

    private final Game g = new Game("John Smith", "Lord of the Smith", "0", "5", "HELL", "Earth", "Oneshot", "1", "30 minutes", "Loremp impums lollol");

    private final Intent intent = initIntent();

    private Intent initIntent()
    {
        Intent intent = new Intent();
       // intent.putExtra(GameViewerActivity.EXTRA_GAME_KEY, g);
        return intent;
    }

    @Test
    public void testIntent() {
        assert (activityTestRule.getActivity().getIntent().getSerializableExtra(GameViewerActivity.EXTRA_GAME_KEY) instanceof Game);
    }


    @Test
    public void testDescription()
    {
        activityTestRule.launchActivity(intent);
       // onView(withId(R.id.gmTextView)).check(matches(withText(g.getGmName())));
    }

    @Test
    public void testTitle()
    {
        activityTestRule.launchActivity(intent);
        onView(ViewMatchers.withId(R.id.titleTextView)).check(matches(withText(g.getName())));
    }

}
