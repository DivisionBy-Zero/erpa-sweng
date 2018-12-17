package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.PlayerJoinGameRequest;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.dummy.database.DummyGameService;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

class Utils {
    static void testClickItemMenu(int drawerLayoutId, int navigationViewId, int menuItemId, String className) {
        // Open Drawer to click on navigation.
        onView(withId(drawerLayoutId))
            .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
            .perform(DrawerActions.open()); // Open Drawer

        // Start the screen of your activity.
        onView(withId(navigationViewId))
            .perform(NavigationViewActions.navigateTo(menuItemId));
        intended(hasComponent(className));
    }

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

    static void joinUserToGame(String gameUuid, Username u2, GameService gameService,
                               PlayerJoinGameRequest.RequestStatus confirmed) throws IOException, ServerException {
        String previousUserUuid = DummyGameService.currentUserUuid;
        PlayerJoinGameRequest joinRequest;
        DummyGameService.currentUserUuid = u2.getUserUuid();
        joinRequest = gameService.joinGame(gameUuid);
        joinRequest.setRequestStatus(confirmed);
        gameService.updateGameJoinRequest(gameUuid, joinRequest);
        DummyGameService.currentUserUuid = previousUserUuid;
    }
}
