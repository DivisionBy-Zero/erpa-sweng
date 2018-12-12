package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.view.View;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.primitives.Ints;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserManagementService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.assertion.ViewAssertions.selectedDescendantsMatch;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static junit.framework.TestCase.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class GameViewerActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule public final ActivityTestRule<GameViewerActivity> activityTestRule =
        new ActivityTestRule<>(GameViewerActivity.class, false, false);
    @Inject GameService gameService;
    @Inject LoggedUserCoordinator loggedUserCoordinator;
    @Inject UserManagementService userManagementService;
    private Username u1, u2, gm, currentUser;
    private Game game, emptyOptGame;
    private GameViewerActivity activity;

    private static boolean gameViewerActivityAsyncVisualElementsReady(GameViewerActivity activity, int... except) {
        return Stream.of(activity.gmName, activity.playerListView, activity.joinGameButton)
            .filterNot(v1 -> Ints.contains(except, v1.getId()))
            .map(View::getVisibility)
            .allMatch(v -> v == View.VISIBLE);
    }

    @Before
    public void prepare() throws Throwable {
        super.prepare();

        gm = registerUsername(userManagementService, "Sapphie");
        u1 = registerUsername(userManagementService, "Ryker");
        u2 = registerUsername(userManagementService, "Ivan");
        currentUser = registerUsername(userManagementService, "Anne");

        game = getGame("Game" + System.currentTimeMillis());
        game.setGmUserUuid(gm.getUserUuid());

        emptyOptGame = getGame("empty");
        emptyOptGame.setGmUserUuid(gm.getUserUuid());
        emptyOptGame.setNumberOfSessions(Optional.empty());
        emptyOptGame.setSessionLengthInMinutes(Optional.empty());

        gameService.updateGame(game);
        gameService.updateGame(emptyOptGame);

        String gameUuid = game.getUuid();
        PlayerJoinGameRequest joinRequest;
        // Join Ryker
        DummyGameService.currentUserUuid = u1.getUserUuid();
        joinRequest = gameService.joinGame(gameUuid);
        joinRequest.setRequestStatus(PlayerJoinGameRequest.RequestStatus.REQUEST_TO_JOIN);
        gameService.updateGameJoinRequest(gameUuid, joinRequest);
        // Join Ivan
        DummyGameService.currentUserUuid = u2.getUserUuid();
        joinRequest = gameService.joinGame(gameUuid);
        joinRequest.setRequestStatus(PlayerJoinGameRequest.RequestStatus.CONFIRMED);
        gameService.updateGameJoinRequest(gameUuid, joinRequest);

        registerCurrentlyLoggedUser(loggedUserCoordinator, currentUser);

        Intents.init();
        launchActivityForGame(gameUuid);
    }

    @After
    public void teardown() {
        Intents.release();
    }

    private void launchActivityForGame(String gameUuid) {
        Intent gameViewerActivityLaunchIntent = new Intent();
        gameViewerActivityLaunchIntent.putExtra(GameService.PROP_INTENT_GAME_UUID, gameUuid);
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, GameListActivity.GameListType.HOSTED_GAMES);
        gameViewerActivityLaunchIntent.putExtras(bundle);
        activityTestRule.launchActivity(gameViewerActivityLaunchIntent);
        activity = activityTestRule.getActivity();
    }

    @Test
    public void activityHasGameUuidIntent() {
        assertTrue(activity.getIntent().hasExtra(GameService.PROP_INTENT_GAME_UUID));
    }

    @Test
    public void visibleTVsHaveCorrectText() {
        onView(withId(R.id.descriptionTextView)).check(matches(withText(game.getDescription())));
        onView(withId(R.id.universeTextView)).check(matches(withText(game.getUniverse())));
        onView(withId(R.id.titleTextView)).check(matches(withText(game.getTitle())));
        onView(withId(R.id.difficultyTextView)).check(matches(withText(game.getDifficulty().toString())));
        onView(withId(R.id.oneShotOrCampaignTextView)).check(matches(withText(game.getOneshotOrCampaign())));
        onView(withId(R.id.sessionLengthTextView)).check(matches(withText(game.getSessionLengthInMinutes().get().toString())));
        onView(withId(R.id.sessionNumberTextView)).check(matches(withText(game.getNumberOfSessions().get().toString())));

        while (!gameViewerActivityAsyncVisualElementsReady(activity))
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withId(R.id.gmTextView)).check(matches(withText(gm.getUsername())));
        onView(withId(R.id.gameViewerPlayerListView))
            .check(matches(isDisplayed()))
            .check(selectedDescendantsMatch(withText(u1.getUsername()), isDisplayed()))
            .check(selectedDescendantsMatch(withText(u2.getUsername()), isDisplayed()));
        onView(withId(R.id.joinGameButton)).check(matches(isDisplayed()));
    }

    @Test
    public void visibleTVsOnEmptySessionsOrLength() {
        activityTestRule.finishActivity();
        launchActivityForGame(emptyOptGame.getUuid());
        onView(withId(R.id.sessionNumberTextView)).check(matches(withText("Unspecified")));
        onView(withId(R.id.sessionLengthTextView)).check(matches(withText("Unspecified")));
    }

    @Test
    public void testClickOnJoinGameButton() {
        while (!gameViewerActivityAsyncVisualElementsReady(activity))
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withId(R.id.joinGameButton)).perform(click());
        intended(hasComponent(JoinGameActivity.class.getName()));
        intended(hasExtra(JoinGameActivity.GAME_UUID_KEY, game.getUuid()));
    }

    @Test
    public void testJoinButtonHiddenIfGM() {
        registerCurrentlyLoggedUser(loggedUserCoordinator, gm);
        restartActivityAndCheckJoinButtonIsHidden();
    }

    @Test
    public void testJoinButtonHiddenIfJoinRequestSent() {
        registerCurrentlyLoggedUser(loggedUserCoordinator, u1);
        restartActivityAndCheckJoinButtonIsHidden();
    }

    private void restartActivityAndCheckJoinButtonIsHidden() {
        activityTestRule.finishActivity();
        launchActivityForGame(game.getUuid());
        while (!gameViewerActivityAsyncVisualElementsReady(activity, R.id.joinGameButton))
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withId(R.id.joinGameButton))
            .check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    }
}
