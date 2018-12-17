package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.support.test.espresso.intent.rule.IntentsTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withSubstring;
import static ch.epfl.sweng.erpa.activities.JoinGameActivity.GAME_UUID_KEY;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;

public class JoinGameActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule public final IntentsTestRule<JoinGameActivity> intentsTestRule =
        new IntentsTestRule<>(JoinGameActivity.class, false, false);
    @Inject UserManagementService userManagementService;
    Game game;
    Intent intent;
    Username gm, user;

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        gm = registerUsername(userManagementService, "Sapphie");
        user = registerUsername(userManagementService, "Ryker");

        game = getGame("Game" + System.currentTimeMillis());
        game.setGmUserUuid(gm.getUserUuid());
        intent = new Intent().putExtra(GAME_UUID_KEY, game.getUuid());
    }

    @Test(expected = IllegalArgumentException.class)
    public void callingWithoutUuidShouldThrow() {
        JoinGameActivity.getGameUuidFromIntent(new Intent());
    }

    @Test
    public void handleExceptionCreatesPopup() throws Throwable {
        intentsTestRule.launchActivity(intent);
        String text = "Example test";
        intentsTestRule.runOnUiThread(() ->
            intentsTestRule.getActivity().handleException(new ServerException(500, text)));
        onView(withSubstring(text)).check(matches(isDisplayed()));
    }
}