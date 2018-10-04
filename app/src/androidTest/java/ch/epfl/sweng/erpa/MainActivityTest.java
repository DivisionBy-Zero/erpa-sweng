package ch.epfl.sweng.erpa;

import android.content.ComponentName;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {
    @Rule
    public final IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void testCanLaunchGameList()
    {
        onView(ViewMatchers.withId(R.id.launch_game_list_button)).perform(ViewActions.click());
        //Intents.init();
        intended(hasComponent(new ComponentName(getTargetContext(), GameList.class)));
        //Intents.release();
    }
    @Test
    public void testCanLaunchCreateGame()
    {
        onView(ViewMatchers.withId(R.id.launch_create_game_button)).perform(ViewActions.click());
        //Intents.init();
        intended(hasComponent(CreateGame.class.getName()));
        //Intents.release();
    }
}
