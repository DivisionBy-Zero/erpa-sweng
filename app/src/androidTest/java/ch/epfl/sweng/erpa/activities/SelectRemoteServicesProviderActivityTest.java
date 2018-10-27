package ch.epfl.sweng.erpa.activities;

import android.app.Instrumentation;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.RadioGroup;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import ch.epfl.sweng.erpa.R;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class SelectRemoteServicesProviderActivityTest {

    private RadioGroup radioGroup;
    private Instrumentation instrumentation;

    @Rule
    public final IntentsTestRule<SelectRemoteServicesProviderActivity> intentsTestRule = new IntentsTestRule<>(
            SelectRemoteServicesProviderActivity.class);

    @Before
    public void setVariables() {
        instrumentation = InstrumentationRegistry.getInstrumentation();
        radioGroup = intentsTestRule.getActivity().findViewById(R.id.rspSelectionRadioGroup);
    }

    @Test
    public void canSelectFirstServiceProvider() {
        selectAndSubmitRSP(0);
    }

    @Test
    public void canSelectLastServiceProvider() {
        selectAndSubmitRSP(radioGroup.getChildCount() - 1);
    }

    public void selectAndSubmitRSP(int myPosition) {
        instrumentation.runOnMainSync(() -> {
            radioGroup.check(myPosition);
        });
        onView(withId(R.id.rspSelectionSubmit)).perform(click());
    }
}