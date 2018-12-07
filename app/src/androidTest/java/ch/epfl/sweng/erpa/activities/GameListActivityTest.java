package ch.epfl.sweng.erpa.activities;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
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
import com.annimon.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering;
import ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_ACTIVTIY_CLASS_KEY;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.ASCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.DESCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DIFFICULTY;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DISTANCE;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.MAX_NUMBER_OF_PLAYERS;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GameListActivityTest {
    @Rule
    public final IntentsTestRule<GameListActivity> intentsTestRule = new IntentsTestRule<>(
            GameListActivity.class);

    private Toolbar toolbar;
    private Resources resources;

    @Before
    public void prepare() throws Throwable {
        Toothpick.setConfiguration(Configuration.forDevelopment().enableReflection());
        FactoryRegistryLocator.setRootRegistry(new ch.epfl.sweng.erpa.smoothie.FactoryRegistry());
        MemberInjectorRegistryLocator.setRootRegistry(
                new ch.epfl.sweng.erpa.smoothie.MemberInjectorRegistry());
        Scope scope = Toothpick.openScope(
                InstrumentationRegistry.getTargetContext().getApplicationContext());
        ErpaApplication application = scope.getInstance(ErpaApplication.class);

        Toothpick.reset(scope);
        application.installModules(scope);
        scope.getInstance(RemoteServicesProviderCoordinator.class).bindRemoteServicesProvider(
                DummyRemoteServicesProvider.class
        );

        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_ACTIVTIY_CLASS_KEY, GameListActivity.GameList.FIND_GAME);
        intentsTestRule.getActivity().getIntent().putExtras(bundle);
        intentsTestRule.runOnUiThread(() -> {
            intentsTestRule.getActivity().getIntent().putExtras(bundle);
            toolbar = intentsTestRule.getActivity().findViewById(R.id.game_list_toolbar);
            intentsTestRule.getActivity().setSupportActionBar(toolbar);
        });
        resources = intentsTestRule.getActivity().getResources();
    }

    private int getItemCount(@NonNull RecyclerView view) {
        return view.getLayoutManager().getItemCount();
    }

    @Test
    public void testMinNumberOfCardsDisplayed() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        int itemCount = getItemCount(view);

        // magic number fits example in createListData in GameListActivity
        assertTrue(itemCount >= 5);
    }

    @Test
    public void testMaxNumberOfCardsDisplayed() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        int itemCount = getItemCount(view);

        // magic number fits example in createListData in GameListActivity
        assertTrue(itemCount <= 25);
    }

    @Test
    public void testFirstCardDisplayAllExpectedFields() {
        RecyclerView recyclerView = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        View view = recyclerView.getLayoutManager().getChildAt(0);
        CardView firstCard = view.findViewById(R.id.cardview);
        List<View> vs = getViewChildrensRecursive(firstCard);
        vs.add(view.findViewById(R.id.difficultyBanner));
        Set<String> textFieldsText = Stream.of(vs)
                .filter(v -> TextView.class.isAssignableFrom(v.getClass()))
                .map(v -> (TextView) v)
                .map(TextView::getText)
                .map(Object::toString)
                .collect(Collectors.toSet());

        Set<ImageView> imageViews = Stream.of(vs)
                .filter(v -> ImageView.class.isAssignableFrom(v.getClass()))
                .map(v -> (ImageView) v).collect(Collectors.toSet());

        assertEquals(5, textFieldsText.size());
        assertEquals(2, imageViews.size());
    }

    private List<View> getViewChildrensRecursive(ViewGroup parent) {
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

    @Test
    public void testScrolling() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        assertTrue(view.getLayoutManager().canScrollVertically());
        assertFalse(view.getLayoutManager().canScrollHorizontally());
    }

    @Test
    public void testClick() {
        Instrumentation instrumentation = InstrumentationRegistry.getInstrumentation();
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);

        instrumentation.runOnMainSync(() -> {
            view.getLayoutManager().getChildAt(0).performClick();
        });
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
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayer() > 4;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
                .filterBy(gameFilter)
                .removeOneFilter(gameFilter)
                .build();
        assertEquals(0, sr.getGameFilters().size());
    }

    @Test
    public void removeAnAbsentGameFilter() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayer() > 4;
        GameService.StreamRefiner.GameFilter absGameFilter = g -> false;
        GameService.StreamRefiner sr = GameService.StreamRefiner.builder()
                .filterBy(gameFilter)
                .removeOneFilter(absGameFilter)
                .build();
        assertEquals(1, sr.getGameFilters().size());
    }

    @Test
    public void removeAllGameFilters() {
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayer() > 4;
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
        GameService.StreamRefiner.GameFilter gameFilter = g -> g.getMaxPlayer() > 4;
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
        testItemSelected(R.id.menu_actionSearch, SortActivity.class.getName());
    }

    @Test
    public void testMenuButtonSelected() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            DrawerLayout drawerLayout = intentsTestRule.getActivity().findViewById(R.id.game_list_drawer_layout);
            onOptionItemSelectedUtils(android.R.id.home, drawerLayout);
        });
    }

    private void testItemSelected(int id, String activityName) throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            MenuItem item = toolbar.getMenu().findItem(id);
            intentsTestRule.getActivity().onOptionItemViewSelected(item.getItemId());
        });
        intended(hasComponent(activityName));
    }

    @Test
    public void testToolbarSetText() throws Throwable {
        intentsTestRule.runOnUiThread(() -> {
            ActionBar actionBar = intentsTestRule.getActivity().getSupportActionBar();
            intentsTestRule.getActivity().setToolbarText(GameListActivity.GameList.HOSTED_GAMES);
            assertEquals(resources.getString(R.string.hostedGamesText), actionBar.getTitle());
            intentsTestRule.getActivity().setToolbarText(GameListActivity.GameList.PAST_HOSTED_GAMES);
            assertEquals(resources.getString(R.string.pastHostedGamesText), actionBar.getTitle());
        });
    }
}
