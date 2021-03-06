package ch.epfl.sweng.erpa.activities;

import android.content.ComponentName;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUser;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule
    public final IntentsTestRule<MainActivity> intentsTestRule = new IntentsTestRule<>(MainActivity.class);

    @Test
    public void testCanLaunchGameList() {
        onView(ViewMatchers.withId(R.id.launch_game_list_button)).perform(ViewActions.click());
        intended(hasComponent(new ComponentName(getTargetContext(), GameListActivity.class)));
    }

    @Test
    public void testCanLaunchCreateGame() {
        onView(ViewMatchers.withId(R.id.launch_create_game_button)).perform(ViewActions.click());
        intended(hasComponent(CreateGameActivity.class.getName()));
    }

    @Test
    public void testCanLaunchMyAccount() {
        String userUuid = "UserUuid";
        UserSessionToken userSessionToken = new UserSessionToken(userUuid, userUuid);
        UserProfile userProfile = UserProfile.builder().uuid(userUuid).isGm(true).isPlayer(true).build();
        scope.getInstance(LoggedUserCoordinator.class).setCurrentLoggedUser(
            new LoggedUser(userSessionToken, userProfile, new Username(userUuid, userUuid)));

        onView(ViewMatchers.withId(R.id.launch_my_account_button)).perform(ViewActions.click());
        intended(hasComponent(MyAccountActivity.class.getName()));
    }

    @Test
    public void testCanLaunchLogin() {
        onView(ViewMatchers.withId(R.id.launch_login_button)).perform(ViewActions.click());
        intended(hasComponent(LoginActivity.class.getName()));
    }

    @Test
    public void testCanLaunchSignup() {
        onView(ViewMatchers.withId(R.id.launch_signup_button)).perform(ViewActions.click());
        intended(hasComponent(SignupActivity.class.getName()));
    }

    @Test
    public void testCanLaunchDiceInterface() {
        onView(ViewMatchers.withId(R.id.launch_dice_button)).perform(ViewActions.click());
        intended(hasComponent(DiceActivity.class.getName()));
    }
}
