package ch.epfl.sweng.erpa.activities;

import android.support.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;

import static ch.epfl.sweng.erpa.activities.GameListActivityTest.intentForGameListType;
import static org.junit.Assert.assertEquals;

public class GameListActivityTypeTest extends DependencyConfigurationAgnosticTest {
    @Rule public final ActivityTestRule<GameListActivity> intentsTestRule =
        new ActivityTestRule<>(GameListActivity.class, false, false);

    @Test
    public void allGameTypesShowToolbarName() {
        for (GameListActivity.GameListType type : GameListActivity.GameListType.values()) {
            intentsTestRule.launchActivity(intentForGameListType(type));
            verifyToolbarInActivityWithType(type);
            intentsTestRule.getActivity().finish();
        }
    }

    private void verifyToolbarInActivityWithType(GameListActivity.GameListType listType) {
        GameListActivity activity = intentsTestRule.getActivity();
        String expectedText = activity.getString(GameListActivity.stringIdForGameListType.get(listType));
        assertEquals(expectedText, activity.getSupportActionBar().getTitle());
    }
}
