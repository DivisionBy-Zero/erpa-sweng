package ch.epfl.sweng.erpa.activities;

import android.app.Instrumentation;
import android.content.Intent;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.UserSessionToken;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.LoggedUser;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;
import ch.epfl.sweng.erpa.util.Pair;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class MyAccountActivityTest extends DependencyConfigurationAgnosticTest {
    @Rule
    public final IntentsTestRule<MyAccountActivity> intentsTestRule = new IntentsTestRule<>(MyAccountActivity.class, false, false);
    private ListView listView;
    private Resources systemResources;
    private UserProfile userProfile;
    private Instrumentation instrumentation;

    @Before
    public void prepare() throws Throwable {
        super.prepare();
        String userUuid = "UserUuid";
        UserSessionToken userSessionToken = new UserSessionToken(userUuid, userUuid);
        userProfile = UserProfile.builder().uuid(userUuid).isGm(true).isPlayer(true).build();
        scope.getInstance(LoggedUserCoordinator.class).setCurrentLoggedUser(
            new LoggedUser(userSessionToken, userProfile, new Username(userUuid, userUuid)));

        intentsTestRule.launchActivity(new Intent());
        systemResources = intentsTestRule.getActivity().getResources();
        listView = intentsTestRule.getActivity().findViewById(R.id.myAccountLayout);
        instrumentation = InstrumentationRegistry.getInstrumentation();
    }

    @Test
    public void testFindRightNumberOfButtonsInTheList() {
        if (userProfile.getIsPlayer()) {
            if (userProfile.getIsGm())
                assertThat(listView.getCount(), is(6));
            else
                assertThat(listView.getCount(), is(4));
        } else
            assertThat(listView.getCount(), is(3));
    }

    @Test
    public void testButtonsNamesInTheList() {
        if (userProfile.getIsPlayer()) {
            checkCorrectName(0, R.string.pendingRequestText);
            checkCorrectName(1, R.string.confirmedGamesText);
            checkCorrectName(2, R.string.pastGamesText);
            if (userProfile.getIsGm()) {
                checkCorrectName(3, R.string.hostedGamesText);
                checkCorrectName(4, R.string.pastHostedGamesText);
                checkCorrectName(5, R.string.profileText);
            } else
                checkCorrectName(3, R.string.profileText);
        } else {
            checkCorrectName(0, R.string.hostedGamesText);
            checkCorrectName(1, R.string.pastHostedGamesText);
            checkCorrectName(2, R.string.profileText);
        }
    }

    public void checkCorrectName(int position, int ressourceID) {
        Pair pair = (Pair) listView.getItemAtPosition(position);
        assertThat(((MyAccountButton) pair.getFirst()).getText(),
            is(systemResources.getString(ressourceID)));
    }

    @Test
    public void testShouldShowPendingRequestOrHostedGamesWhenFirstButtonClicked() {

        performClickOnListviewButton(0);
        if (userProfile.getIsPlayer())
            intended(hasComponent(GameListActivity.class.getName()));
        else
            intended(hasComponent(GameListActivity.class.getName()));
    }

    @Test
    public void testShouldShowConfirmedGamesOrPastHostedGamesWhenSecondButtonClicked() {

        performClickOnListviewButton(1);
        if (userProfile.getIsPlayer())
            intended(hasComponent(GameListActivity.class.getName()));
        else
            intended(hasComponent(GameListActivity.class.getName()));
    }

    @Test
    public void testShouldShowPastGamesOrProfileWhenThirdButtonClicked() {

        performClickOnListviewButton(2);
        if (userProfile.getIsPlayer())
            intended(hasComponent(GameListActivity.class.getName()));
        else
            intended(hasComponent(GameListActivity.class.getName()));
    }

    @Test
    public void testShouldShowProfileWhenLastButtonClicked() {

        performClickOnListviewButton(listView.getChildCount() - 1);
        intended(hasComponent(UserProfileActivity.class.getName()));
    }

    public void performClickOnListviewButton(int myPosition) {
        instrumentation.runOnMainSync(() -> {
            int position = myPosition;
            listView.performItemClick(listView.getChildAt(position), position,
                listView.getAdapter().getItemId(position));
        });
    }
}