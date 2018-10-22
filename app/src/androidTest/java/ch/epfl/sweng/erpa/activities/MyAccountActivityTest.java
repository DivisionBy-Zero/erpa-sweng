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
            assertThat(((MyAccountButton) listView.getItemAtPosition(0)).getText(),
                    is(systemResources.getString(R.string.pendingRequestText)));
            assertThat(((MyAccountButton) listView.getItemAtPosition(1)).getText(),
                    is(systemResources.getString(R.string.confirmedGamesText)));
            assertThat(((MyAccountButton) listView.getItemAtPosition(2)).getText(),
                    is(systemResources.getString(R.string.pastGamesText)));
            if (userProfile.getIsGm()) {
                assertThat(((MyAccountButton) listView.getItemAtPosition(3)).getText(),
                        is(systemResources.getString(R.string.hostedGamesText)));
                assertThat(((MyAccountButton) listView.getItemAtPosition(4)).getText(),
                        is(systemResources.getString(R.string.pastHostedGamesText)));
                assertThat(((MyAccountButton) listView.getItemAtPosition(5)).getText(),
                        is(systemResources.getString(R.string.profileText)));
            } else
                assertThat(((MyAccountButton) listView.getItemAtPosition(3)).getText(),
                        is(systemResources.getString(R.string.profileText)));
        } else {
            assertThat(((MyAccountButton) listView.getItemAtPosition(0)).getText(),
                    is(systemResources.getString(R.string.hostedGamesText)));
            assertThat(((MyAccountButton) listView.getItemAtPosition(1)).getText(),
                    is(systemResources.getString(R.string.pastHostedGamesText)));
            assertThat(((MyAccountButton) listView.getItemAtPosition(2)).getText(),
                    is(systemResources.getString(R.string.profileText)));
        }
    }

    @Test
    public void testShouldShowPendingRequestOrHostedGamesWhenFirstButtonClicked() {

        instrumentation.runOnMainSync(() -> {
            int position = 0;
            listView.performItemClick(listView.getChildAt(position), position,
                    listView.getAdapter().getItemId(position));
        });

        if (userProfile.getIsPlayer())
            intended(hasComponent(PendingRequestActivity.class.getName()));
        else
            intended(hasComponent(HostedGamesActivity.class.getName()));
    }

    @Test
    public void testShouldShowConfirmedGamesOrPastHostedGamesWhenSecondButtonClicked() {

        instrumentation.runOnMainSync(() -> {
            int position = 1;
            listView.performItemClick(listView.getChildAt(position), position,
                    listView.getAdapter().getItemId(position));
        });

        if (userProfile.getIsPlayer())
            intended(hasComponent(ConfirmedGamesActivity.class.getName()));
        else
            intended(hasComponent(PastHostedGamesActivity.class.getName()));
    }

    @Test
    public void testShouldShowPastGamesOrProfileWhenThirdButtonClicked() {

        instrumentation.runOnMainSync(() -> {
            int position = 2;
            listView.performItemClick(listView.getChildAt(position), position,
                    listView.getAdapter().getItemId(position));
        });

        if (userProfile.getIsPlayer())
            intended(hasComponent(PastGamesActivity.class.getName()));
        else
            intended(hasComponent(ProfileActivity.class.getName()));
    }

    @Test
    public void testShouldShowProfileWhenLastButtonClicked() {

        instrumentation.runOnMainSync(() -> {
            int position = listView.getChildCount() - 1;
            listView.performItemClick(listView.getChildAt(position), position,
                    listView.getAdapter().getItemId(position));
        });

        intended(hasComponent(ProfileActivity.class.getName()));
    }
}