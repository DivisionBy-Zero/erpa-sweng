package ch.epfl.sweng.erpa.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.TreeMap;

import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.ASCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.Ordering.DESCENDING;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DIFFICULTY;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.DISTANCE;
import static ch.epfl.sweng.erpa.services.GameService.StreamRefiner.SortCriteria.MAX_NUMBER_OF_PLAYERS;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class GameServiceStreamRefinerTest {
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
        assertEquals(new TreeMap<GameService.StreamRefiner.SortCriteria, GameService.StreamRefiner.Ordering>() {{
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
        assertEquals(new TreeMap<GameService.StreamRefiner.SortCriteria, GameService.StreamRefiner.Ordering>() {{
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
        assertEquals(new TreeMap<GameService.StreamRefiner.SortCriteria, GameService.StreamRefiner.Ordering>() {{
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
        assertEquals(new TreeMap<GameService.StreamRefiner.SortCriteria, GameService.StreamRefiner.Ordering>() {{
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
        assertEquals(1, sr.getGameFilters().size());
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

}
