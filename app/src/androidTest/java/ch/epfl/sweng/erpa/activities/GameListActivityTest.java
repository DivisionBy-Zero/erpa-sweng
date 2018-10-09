package ch.epfl.sweng.erpa.activities;

import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ch.epfl.sweng.erpa.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class GameListActivityTest {
    @Rule
    public final IntentsTestRule<GameListActivity> intentsTestRule = new IntentsTestRule<>(GameListActivity.class);

    @Test
    public void testMinNumberOfCardsDisplayed() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        int itemCount = view.getLayoutManager().getItemCount();

        // magic number fits example in createListData in GameListActivity
        assertTrue(itemCount >= 5);
    }

    @Test
    public void testMaxNumberOfCardsDisplayed() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        int itemCount = view.getLayoutManager().getItemCount();

        // magic number fits example in createListData in GameListActivity
        assertTrue(itemCount <= 25);
    }

    @Test
    public void testFirstCardDisplayAllExpectedFields() {
        RecyclerView view = intentsTestRule.getActivity().findViewById(R.id.recyclerView);
        CardView firstCard = (CardView) view.getLayoutManager().getChildAt(0);
        List<View> vs = getViewChildrensRecursive(firstCard);
        Set<String> textFieldsText = Stream.of(vs)
                .filter(v -> TextView.class.isAssignableFrom(v.getClass()))
                .map(v -> (TextView) v)
                .map(TextView::getText)
                .map(Object::toString).collect(Collectors.toSet());

        Set<ImageView> imageViews = Stream.of(vs)
                .filter(v -> ImageView.class.isAssignableFrom(v.getClass()))
                .map(v -> (ImageView) v).collect(Collectors.toSet());

        Set<String> expectedTextFieldsText = Stream
                .of("DnD", "Lausanne", "test") // Values from createListData in GameListActivity .
                .collect(Collectors.toSet());
        assertEquals(expectedTextFieldsText, textFieldsText);
    }

    private List<View> getViewChildrensRecursive(ViewGroup parent) {
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
}
