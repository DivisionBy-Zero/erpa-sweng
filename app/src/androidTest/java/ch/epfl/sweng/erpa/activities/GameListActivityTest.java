package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.BiConsumer;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static ch.epfl.sweng.erpa.activities.Utils.testClickItemMenu;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GameListActivityTest extends DependencyConfigurationAgnosticTest {
    private final static BiConsumer<Integer, Class> testMenuItem = (id, cls) ->
        testClickItemMenu(R.id.game_list_drawer_layout, R.id.game_list_navigation_view, id, cls.getName());
    @Rule public final ActivityTestRule<GameListActivity> intentsTestRule =
        new ActivityTestRule<>(GameListActivity.class, false, false);

    @Inject GameService gameService;
    @Inject LoggedUserCoordinator loggedUserCoordinator;
    @Inject UserManagementService userManagementService;
    private Resources resources;
    private GameListActivity activity;

    static Intent intentForGameListType(GameListActivity.GameListType type) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, type);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        return intent;
    }

    private static void populateGameList(GameService gameService, int nbGames) {
        Exceptional.of(() -> {
            for (int i = 0; i < nbGames; i++) {
                gameService.createGame(getGame("The lost pepsi " + Integer.toString(i)));
            }
            return null;
        }).getOrThrowRuntimeException();
        assertEquals(nbGames, gameService.getAllGames(new GameService.StreamRefiner()).size());
    }

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        Username currentUser = registerUsername(userManagementService, "Isa");
        registerCurrentlyLoggedUser(loggedUserCoordinator, currentUser);

        gameService.removeGames();
        populateGameList(gameService, 10);

        intentsTestRule.launchActivity(intentForGameListType(GameListActivity.GameListType.FIND_GAME));
        activity = intentsTestRule.getActivity();
        resources = activity.getResources();
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    @Test
    public void testCardsDisplayed() {
        RecyclerView view = activity.findViewById(R.id.game_list_recycler_view);
        assertTrue(view.getLayoutManager().getItemCount() > 1);
    }

    @Test
    public void testFirstCardDisplayAllExpectedFields() {
        RecyclerView recyclerView = activity.findViewById(R.id.game_list_recycler_view);
        View view = recyclerView.getLayoutManager().getChildAt(0);
        CardView firstCard = view.findViewById(R.id.cardview);
        List<View> vs = Utils.getViewChildrensRecursive(firstCard);
        vs.add(view.findViewById(R.id.difficultyBanner));
        Set<String> textFieldsText = Stream.of(vs)
            .filter(v -> TextView.class.isAssignableFrom(v.getClass()))
            .map(v -> (TextView) v)
            .filter(v -> v.getVisibility() == View.VISIBLE)
            .map(TextView::getText)
            .map(Object::toString)
            .collect(Collectors.toSet());

        Set<ImageView> imageViews = Stream.of(vs)
            .filter(v -> ImageView.class.isAssignableFrom(v.getClass()))
            .filter(v -> v.getVisibility() == View.VISIBLE)
            .map(v -> (ImageView) v).collect(Collectors.toSet());

        // Difficulty, title, location, universe, currentNbPlayers, "/", maxNbPlayers
        assertEquals(7, textFieldsText.size());
        // Location, Universe
        assertEquals(2, imageViews.size());
    }

    @Test
    public void testScrolling() {
        RecyclerView view = activity.findViewById(R.id.game_list_recycler_view);
        assertTrue(view.getLayoutManager().canScrollVertically());
        assertFalse(view.getLayoutManager().canScrollHorizontally());
    }

    @Test
    public void testClick() {
        View cardView;
        do {
            cardView = activity.<RecyclerView>findViewById(R.id.game_list_recycler_view)
                .getLayoutManager().getChildAt(0);
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        } while (cardView == null);

        cardView.callOnClick();
        intended(hasComponent(GameViewerActivity.class.getName()));
    }

    @Test
    public void testToolbarSetText() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            ActionBar actionBar = activity.getSupportActionBar();
            activity.setToolbarText(GameListActivity.GameListType.HOSTED_GAMES);
            assertEquals(resources.getString(R.string.hostedGamesText), actionBar.getTitle());
            activity.setToolbarText(GameListActivity.GameListType.PAST_HOSTED_GAMES);
            assertEquals(resources.getString(R.string.pastHostedGamesText), actionBar.getTitle());
        });
    }

    @Test
    public void testSearchItemSelected() {
        onView(withId(R.id.menu_actionSearch))
            .check(matches(isDisplayed()))
            .perform(click());
        intended(hasComponent(SortActivity.class.getName()));
    }

    @Test
    public void testClickOnFindGameMenu() {
        testMenuItem.accept(R.id.menu_findGame, GameListActivity.class);
    }

    @Test
    public void testClickOnCreateGameMenu() {
        testMenuItem.accept(R.id.menu_createGame, CreateGameActivity.class);
    }

    @Test
    public void testClickOnMyAccountMenu() {
        testMenuItem.accept(R.id.menu_myAccount, MyAccountActivity.class);
    }

    @Test
    public void testClickOnDiceMenu() {
        testMenuItem.accept(R.id.menu_dice, DiceActivity.class);
    }
}
