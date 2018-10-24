package ch.epfl.sweng.erpa.activities;

import android.app.Instrumentation;
import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.widget.ListView;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.UserProfile;

import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class MyAccountActivityTest {

    private ListView listView;
    private Resources systemResources;
    private UserProfile userProfile;
    private Instrumentation instrumentation;

    @Rule
    public final IntentsTestRule<MyAccountActivity> intentsTestRule = new IntentsTestRule<>(
            MyAccountActivity.class);

    @Before
    public void setTestVariables() {
        systemResources = intentsTestRule.getActivity().getResources();
        listView = intentsTestRule.getActivity().findViewById(R.id.myAccountLayout);
        userProfile = intentsTestRule.getActivity().userProfile;
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
        MyAccountActivity.Pair pair = (MyAccountActivity.Pair) listView.getItemAtPosition(position);
        assertThat(((MyAccountButton) pair.getFirst()).getText(),
                is(systemResources.getString(ressourceID)));
    }

    @Test
    public void testShouldShowPendingRequestOrHostedGamesWhenFirstButtonClicked() {

        performClickOnListviewButton(0);
        if (userProfile.getIsPlayer())
            intended(hasComponent(PendingRequestActivity.class.getName()));
        else
            intended(hasComponent(HostedGamesActivity.class.getName()));
    }

    @Test
    public void testShouldShowConfirmedGamesOrPastHostedGamesWhenSecondButtonClicked() {

        performClickOnListviewButton(1);
        if (userProfile.getIsPlayer())
            intended(hasComponent(ConfirmedGamesActivity.class.getName()));
        else
            intended(hasComponent(PastHostedGamesActivity.class.getName()));
    }

    @Test
    public void testShouldShowPastGamesOrProfileWhenThirdButtonClicked() {

        performClickOnListviewButton(2);
        if (userProfile.getIsPlayer())
            intended(hasComponent(PastGamesActivity.class.getName()));
        else
            intended(hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void testShouldShowProfileWhenLastButtonClicked() {

        performClickOnListviewButton(listView.getChildCount() - 1);
        intended(hasComponent(ProfileActivity.class.getName()));
    }

    public void performClickOnListviewButton(int myPosition) {
        instrumentation.runOnMainSync(() -> {
            int position = myPosition;
            listView.performItemClick(listView.getChildAt(position), position,
                    listView.getAdapter().getItemId(position));
        });
    }
}