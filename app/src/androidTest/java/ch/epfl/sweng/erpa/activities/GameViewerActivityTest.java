package ch.epfl.sweng.erpa.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static org.junit.Assert.assertEquals;

@RunWith(AndroidJUnit4.class)
public class GameViewerActivityTest {
    @Rule public final ActivityTestRule<GameViewerActivity> activityTestRule = new ActivityTestRule<>(
            GameViewerActivity.class, false, false);
    private Game game = getGame("hewwo");
    private Game emptyOptGame = getGame("empty");
    private ExpandableListView expandableListView;
    private ExpandableListAdapter expandableListAdapter;
    private Instrumentation instrumentation;

    @Before
    public void prepare() {
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
        emptyOptGame.setNumberSessions(Optional.empty());
        emptyOptGame.setSessionLengthInMinutes(Optional.empty());
        scope.getInstance(GameService.class).saveGame(game);
        scope.getInstance(GameService.class).saveGame(emptyOptGame);

        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, GameListActivity.GameList.FIND_GAME);
        Intent i = new Intent();
        i.putExtra(GameService.PROP_INTENT_GAME, game.getGameUuid());
        i.putExtras(bundle);
        activityTestRule.launchActivity(i);

        expandableListView = activityTestRule.getActivity().findViewById(R.id.gameViewerExpandableList);
        expandableListAdapter =  expandableListView.getExpandableListAdapter();
        instrumentation = InstrumentationRegistry.getInstrumentation();

    }

    @Test
    public void testExpandableTextViewIsExpandable() {
        instrumentation.runOnMainSync(() -> {
            for(int i = 0; i < expandableListView.getChildCount(); ++i) {
                expandableListView.expandGroup(i);
                expandableListView.isGroupExpanded(i);
            }
        });
    }

    @Test
    public void testGameInfoDisplayAllExpectedFields() {
        LinearLayout firstChild = (LinearLayout) expandableListAdapter.getChild(0, 0);
        ConstraintLayout constraintLayout = (ConstraintLayout) firstChild.getChildAt(0);
        List<View> vs = getViewChildrensRecursive(constraintLayout);
        Set<String> textFieldsText = Stream.of(vs)
                .filter(v -> TextView.class.isAssignableFrom(v.getClass()))
                .map(v -> (TextView) v)
                .map(TextView::getText)
                .map(Object::toString).collect(Collectors.toSet());

        Set<ImageView> imageViews = Stream.of(vs)
                .filter(v -> ImageView.class.isAssignableFrom(v.getClass()))
                .map(v -> (ImageView) v).collect(Collectors.toSet());

        Set<String> expectedTextFieldsText = Stream
                .of("The land of the Sapphie", "", "Sapphie", "bepsi is gud", "Sapphtopia",
                        "Difficulty:", "CHILL", "ONESHOT", "Number of Sessions:",
                        Integer.toString(-73), "Session Length:", Integer.toString(Integer.MAX_VALUE))
                .collect(Collectors.toSet());
        assertEquals(expectedTextFieldsText, textFieldsText);
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


}
