package ch.epfl.sweng.erpa.activities;

import android.content.res.Resources;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.action.ViewActions;
import android.support.test.espresso.contrib.DrawerActions;
import android.support.test.espresso.contrib.NavigationViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;
import android.view.Gravity;
import android.view.WindowManager;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import org.hamcrest.core.StringContains;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.io.IOException;

import ch.epfl.sweng.erpa.ErpaApplication;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.operations.RemoteServicesProviderCoordinator;
import ch.epfl.sweng.erpa.services.GCP.ServerException;
import ch.epfl.sweng.erpa.services.dummy.DummyRemoteServicesProvider;
import toothpick.Scope;
import toothpick.Toothpick;
import toothpick.configuration.Configuration;
import toothpick.registries.FactoryRegistryLocator;
import toothpick.registries.MemberInjectorRegistryLocator;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.contrib.DrawerMatchers.isClosed;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static ch.epfl.sweng.erpa.util.TestUtils.getGame;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.core.StringContains.containsString;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class CreateGameActivityTest {
    @Rule
    public final IntentsTestRule<CreateGameActivity> intentsTestRule = new IntentsTestRule<>(
            CreateGameActivity.class);
    private Resources systemResources;
    private Game game;
    private String uuid;

    @Before
    public void setVariables() {
        systemResources = intentsTestRule.getActivity().getResources();
        game = getGame("Name");

    }

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
        final CreateGameActivity activity = intentsTestRule.getActivity();
        Runnable wakeUpDevice = new Runnable() {
            public void run() {
                activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON |
                        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED |
                        WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        };
        activity.runOnUiThread(wakeUpDevice);
    }

    @Test
    public void testCanParseAnySessionLength() {
        boolean q = Stream.of(systemResources.getStringArray(R.array.session_length_array))
                .filter(sl -> !"Undefined".equals(sl))
                .map(CreateGameActivity::findSessionLength)
                .allMatch(Optional::isPresent);
        assertTrue(q);
    }

    @Test
    public void testCanParseAnyDifficulty() {
        //noinspection Convert2MethodRef -- Objects::nonNull was introduced in API 24
        boolean q = Stream.of(systemResources.getStringArray(R.array.difficulties_array))
                .map(CreateGameActivity::findDifficulty)
                .allMatch(o -> o != null);
        assertTrue(q);
    }

    @Test
    public void testCanFillFormWithCorrectInputsAndNbSessions() {
        System.out.println("am here");
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(
                closeSoftKeyboard());

        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(
                matches(withSpinnerText(containsString("NOOB"))));

        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));

        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(
                matches(withSpinnerText(containsString("5h"))));

        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.num_session_field)).perform(typeText("2")).perform(closeSoftKeyboard());

        onView(withId(R.id.description_field)).perform(
                typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        // FIXME(@Roos): These tests are not working after bumping Gradle tools version
        // View gameForm = intentsTestRule.getActivity().findViewById(R.id.create_game_form);
        // gameForm.scrollBy(0, 500);
        // onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testCanFillFormWithCorrectInputsWithoutNbSessions() {
        onView(withId(R.id.create_game_name_field)).perform(typeText("Game Name")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.min_num_player_field)).perform(typeText("1")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.max_num_player_field)).perform(typeText("5")).perform(
                closeSoftKeyboard());
        onView(withId(R.id.difficulty_spinner)).perform(click());
        onData(hasToString(startsWith("N"))).perform(click());
        onView(withId(R.id.difficulty_spinner)).check(
                matches(withSpinnerText(containsString("NOOB"))));
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.session_length_spinner)).perform(click());
        onData(hasToString(startsWith("5"))).perform(click());
        onView(withId(R.id.session_length_spinner)).check(
                matches(withSpinnerText(containsString("5h"))));
        onView(withId(R.id.oneshot)).perform(click());
        onView(withId(R.id.campaign)).perform(click());
        onView(withId(R.id.description_field)).perform(
                typeText("Une petite description de partie")).perform(closeSoftKeyboard());
        // FIXME(@Roos): These tests are not working after bumping Gradle tools version
        // View gameForm = intentsTestRule.getActivity().findViewById(R.id.create_game_form);
        // gameForm.scrollBy(0, 500);
        // onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
    }

    @Test
    public void testEmptyFieldCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.emptyFieldMessage)).check(
                matches(isDisplayed())).perform(ViewActions.click());
    }

    @Test
    public void testEmptyCheckboxCreatesCorrectPopup() {
        onView(ViewMatchers.withId(R.id.create_game_name_field)).perform(typeText("lol")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.description_field)).perform(
                typeText("bla bla bla")).perform(closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.uncheckedCheckboxMessage)).check(
                matches(isDisplayed()));
    }

    @Test
    public void testCanFillUniverseFieldIfOtherIsPicked() {
        onView(withId(R.id.universes_spinner)).perform(click());
        onData(hasToString(startsWith("O"))).perform(click());
        onView(withId(R.id.universes_spinner)).check(
                matches(withSpinnerText(containsString("Other"))));
        onView(withId(R.id.universe_field)).perform(typeText("KazAdrok")).perform(
                closeSoftKeyboard());
    }

    @Test
    public void testCreatePopUpIfMaxSmallerThanMin() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("2")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.invalidPlayerNumber)).check(matches(isDisplayed()));
    }

    @Test
    public void test0PlayerCreatePopUp() {
        onView(ViewMatchers.withId(R.id.min_num_player_field)).perform(typeText("0")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.max_num_player_field)).perform(typeText("3")).perform(
                closeSoftKeyboard());
        onView(ViewMatchers.withId(R.id.submit_button)).perform(ViewActions.click());
        //check if the popup is displayed
        onView(ViewMatchers.withText(R.string.invalidPlayerNumber)).check(matches(isDisplayed()));
    }

    @Test
    public void testClickOnFindGameMenu() {
        testClickItemMenu(R.id.menu_findGame, GameListActivity.class.getName());
    }

    @Test
    public void testClickOnCreateGameMenu() {
        testClickItemMenu(R.id.menu_createGame, CreateGameActivity.class.getName());
    }

    //@Test
    public void testClickOnMyAccountMenu() {
        testClickItemMenu(R.id.menu_myAccount, MyAccountActivity.class.getName());
    }

    @Test
    public void testClickOnDiceMenu() {
        testClickItemMenu(R.id.menu_dice, DiceActivity.class.getName());
    }

    private void testClickItemMenu(int menuItemId, String className) {
        // Open Drawer to click on navigation.
        onView(withId(R.id.create_game_drawer_layout))
                .check(matches(isClosed(Gravity.LEFT))) // Left Drawer should be closed.
                .perform(DrawerActions.open()); // Open Drawer

        // Start the screen of your activity.
        onView(withId(R.id.create_game_navigation_view))
                .perform(NavigationViewActions.navigateTo(menuItemId));
        intended(hasComponent(className));
    }

    @Test
    public void testPopupOnIOException() {
        IOException e = new IOException("Connection problem");
        intentsTestRule.getActivity().runOnUiThread(()->intentsTestRule.getActivity().handleException(e));
        onView(ViewMatchers.withText(StringContains.containsString("Connection problem"))).check(matches(isDisplayed()));
    }
    @Test
    public void testPopupOnServerException() {
        ServerException e = new ServerException(404,"owo");
        intentsTestRule.getActivity().runOnUiThread(()->intentsTestRule.getActivity().handleException(e));
        onView(ViewMatchers.withText(StringContains.containsString("404"))).check(matches(isDisplayed()));
        onView(ViewMatchers.withText(StringContains.containsString("Server"))).check(matches(isDisplayed()));
    }

    @Test
    public void testPopupOnUnkownServErr() {
        ServerException e = new ServerException();
        intentsTestRule.getActivity().runOnUiThread(()->intentsTestRule.getActivity().handleException(e));
        onView(withText(containsString("Unknown server")));
    }

    @Test
    public void testUnknownError() {
        ServerException e = new ServerException();
        intentsTestRule.getActivity().runOnUiThread(()->intentsTestRule.getActivity().handleException(e));
        onView(withText(containsString("Unknown")));
    }
}
