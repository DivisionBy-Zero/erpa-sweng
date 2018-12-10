package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering;
import ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.ASCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.DESCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DIFFICULTY;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DISTANCE;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.MAX_NUMBER_OF_PLAYERS;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GameListActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule
    public final ActivityTestRule<GameListActivity> intentsTestRule =
        new ActivityTestRule<>(GameListActivity.class, false, false);

    @Inject GameService gameService;
    private Toolbar toolbar;
    private Resources resources;
    private GameListActivity activity;

    static List<View> getViewChildrensRecursive(ViewGroup parent) {
        if (parent == null) return new ArrayList<>();
        List<View> ret = new ArrayList<>();
        if (parent.getChildCount() > 0) {
            for (int i = 0; i < parent.getChildCount(); ++i) {
                View v = parent.getChildAt(i);
                if (ViewGroup.class.isAssignableFrom(v.getClass())) {
                    ViewGroup vg = (ViewGroup) v;
                    ret.addAll(getViewChildrensRecursive(vg));
                } else {
                    ret.add(v);
                }
            }
        }
        return ret;
    }

    static Intent intentForGameListType(GameListActivity.GameListType type) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, type);
        Intent intent = new Intent();
        intent.putExtras(bundle);
        return intent;
    }

    private static void populateGameList(GameService gameService, int nbGames) {
        for (int i = 0; i < nbGames; i++) {
            Game g = Game.builder()
                .description("Lorem ipsum")
                .difficulty(Game.Difficulty.CHILL)
                .uuid(String.valueOf(i))
                .maxPlayers(5)
                .gmUserUuid("Empress Sapphie")
                .title("The lost pepsi " + Integer.toString(i))
                .minPlayers(1)
                .universe("DnD")
                .isCampaign(true)
                .numberOfSessions(Optional.of(3))
                .sessionLengthInMinutes(Optional.of(550))
                .locationLat(0.0)
                .locationLon(0.0)
                .gameStatus(Game.GameStatus.CREATED)
                .build();
            Exceptional.of(() -> {
                gameService.createGame(g);
                return null;
            }).get();
        }
        assertEquals(nbGames, gameService.getAllGames(new GameService.StreamRefiner()).size());
    }

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        intentsTestRule.launchActivity(intentForGameListType(GameListActivity.GameListType.FIND_GAME));
        activity = intentsTestRule.getActivity();
        gameService.removeGames();
        populateGameList(gameService, 10);
        intentsTestRule.runOnUiThread(() -> {
            toolbar = activity.findViewById(R.id.game_list_toolbar);
            activity.setSupportActionBar(toolbar);
            resources = activity.getResources();
        });
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    private int getItemCount(@NonNull RecyclerView view) {
        return view.getLayoutManager().getItemCount();
    }

    @Test
    public void testMinNumberOfCardsDisplayed() {
        RecyclerView view = activity.findViewById(R.id.game_list_recycler_view);
        // magic number fits example in createListData in GameListActivity
        assertThat(5, lessThan(getItemCount(view)));
    }

    @Test
    public void testMaxNumberOfCardsDisplayed() {
        RecyclerView view = activity.findViewById(R.id.game_list_recycler_view);
        int itemCount = getItemCount(view);

        // magic number fits example in createListData in GameListActivity
        assertTrue(itemCount <= 25);
    }

    @Test
    public void testFirstCardDisplayAllExpectedFields() {
        RecyclerView recyclerView = activity.findViewById(R.id.game_list_recycler_view);
        View view = recyclerView.getLayoutManager().getChildAt(0);
        CardView firstCard = view.findViewById(R.id.cardview);
        List<View> vs = getViewChildrensRecursive(firstCard);
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
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        activity.<RecyclerView>findViewById(R.id.game_list_recycler_view)
            .getLayoutManager()
            .getChildAt(0).callOnClick();
        intended(hasComponent(GameViewerActivity.class.getName()));
    }

    @Test
    public void StreamRefinerToBuilder() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, DESCENDING)
            .sortBy(DISTANCE, ASCENDING)
            .build();
        assertEquals(sr, sr.toBuilder().build());
    }

    @Test
    public void modifyStreamRefinerWithToBuilder() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, DESCENDING)
            .sortBy(DISTANCE, ASCENDING)
            .build();
        sr = sr.toBuilder().clearCriteria().build();
        assertEquals(0, sr.getSortCriterias().size());
    }

    @Test
    public void sortByWithNoCriterias() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder().build();
        assertEquals(0, sr.getSortCriterias().size());
    }

    @Test
    public void sortByWithOneCriteria() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .build();
        assertEquals(1, sr.getSortCriterias().size());
        assertEquals(new TreeMap<SortCriteria, Ordering>() {{
            put(DIFFICULTY, ASCENDING);
        }}, sr.getSortCriterias());
    }

    @Test
    public void sortByWithAllCriterias() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, DESCENDING)
            .sortBy(DISTANCE, ASCENDING)
            .build();
        assertEquals(sr.getSortCriterias().size(), 3);
        assertEquals(new TreeMap<SortCriteria, Ordering>() {{
            put(DIFFICULTY, ASCENDING);
            put(MAX_NUMBER_OF_PLAYERS, DESCENDING);
            put(DISTANCE, ASCENDING);
        }}, sr.getSortCriterias());
    }

    @Test
    public void conflictingSortByCriteriaTakesLast() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DISTANCE, DESCENDING)
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(DISTANCE, ASCENDING)
            .build();
        assertEquals(2, sr.getSortCriterias().size());
        assertEquals(new TreeMap<SortCriteria, Ordering>() {{
            put(DIFFICULTY, ASCENDING);
            put(DISTANCE, ASCENDING);
        }}, sr.getSortCriterias());
    }

    @Test
    public void removeASortCriteria() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, DESCENDING)
            .sortBy(DISTANCE, ASCENDING)
            .removeOneCriteria(DIFFICULTY)
            .build();
        assertEquals(sr.getSortCriterias().size(), 2);
        assertEquals(new TreeMap<SortCriteria, Ordering>() {{
            put(MAX_NUMBER_OF_PLAYERS, DESCENDING);
            put(DISTANCE, ASCENDING);
        }}, sr.getSortCriterias());
    }

    @Test
    public void removeAnAbsentSortCriteria() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .removeOneCriteria(DISTANCE)
            .build();
        assertEquals(1, sr.getSortCriterias().size());
    }

    @Test
    public void removeAllSortCriteria() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .sortBy(DIFFICULTY, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, DESCENDING)
            .clearCriteria()
            .build();
        assertEquals(0, sr.getSortCriterias().size());
    }

    @Test
    public void filterByWithNoGameFilter() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder().build();
        assertEquals(0, sr.getGameFilters().size());
    }

    @Test
    public void filterByWithOneGameFilter() {
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(g -> true)
            .build();
        assertEquals(1, sr.getGameFilters().size());
    }

    @Test
    public void filterByWithRedundantGameFilter() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> true;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(gameFilter)
            .filterBy(gameFilter)
            .build();
        assertEquals("Failed", 1, sr.getGameFilters().size());
    }

    @Test
    public void removeAGameFilter() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayers() > 4;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(gameFilter)
            .removeOneFilter(gameFilter)
            .build();
        assertEquals(0, sr.getGameFilters().size());
    }

    @Test
    public void removeAnAbsentGameFilter() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayers() > 4;
        GameService.StreamRefiner.GameFilter absGameFilter = g -> false;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(gameFilter)
            .removeOneFilter(absGameFilter)
            .build();
        assertEquals(1, sr.getGameFilters().size());
    }

    @Test
    public void removeAllGameFilters() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayers() > 4;
        GameService.StreamRefiner.GameFilter absGameFilter = g -> false;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(gameFilter)
            .filterBy(absGameFilter)
            .clearFilters()
            .build();
        assertEquals(0, sr.getGameFilters().size());
    }

    @Test
    public void removeAllRefinements() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayers() > 4;
        GameService.StreamRefiner.GameFilter absGameFilter = g -> false;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
            .filterBy(gameFilter)
            .sortBy(DIFFICULTY, DESCENDING)
            .filterBy(absGameFilter)
            .sortBy(DISTANCE, ASCENDING)
            .sortBy(MAX_NUMBER_OF_PLAYERS, ASCENDING)
            .clearRefinements()
            .build();
        assertEquals(0, sr.getGameFilters().size());
        assertEquals(0, sr.getSortCriterias().size());
    }

    @Test
    public void testSearchItemSelected() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            MenuItem item = toolbar.getMenu().findItem(R.id.menu_actionSearch);
            activity.onOptionsItemSelected(item);
        });
        intended(hasComponent(SortActivity.class.getName()));
    }

    @Test
    public void testMenuButtonSelected() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            DrawerLayout drawerLayout = activity.findViewById(R.id.game_list_drawer_layout);
            onOptionItemSelectedUtils(android.R.id.home, drawerLayout);
        });
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
}
