package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;

import com.annimon.stream.Optional;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.services.GameService;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_STREAM_REFINER_KEY;

/**
 * The activity that's launched in order to pick the filters
 * and the order of a list of games
 */
public class SortActivity extends DependencyConfigurationAgnosticActivity {
    private GameService.StreamRefinerBuilder streamRefinerBuilder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_sort);
        ButterKnife.bind(this);
    }

    @Override protected void onResume() {
        super.onResume();
        streamRefinerBuilder = Optional.ofNullable(getIntent().getExtras())
            .map(bundle -> bundle.getSerializable(GAME_LIST_VIEWER_STREAM_REFINER_KEY))
            .map(s -> (GameService.StreamRefiner) s)
            .orElse(new GameService.StreamRefiner()).toBuilder();
    }

    @OnClick({R.id.diffAsc, R.id.diffDesc, R.id.maxNumPlayerAsc, R.id.maxNumPlayerDesc,
        R.id.distAsc, R.id.distDesc, R.id.dateAsc, R.id.dateDesc})
    public void onCheckBoxClicked(CheckBox checkBox) {
        CheckBox checkBox1 = findViewById(R.id.dateAsc);
        GameService.StreamRefiner.SortCriteria criteria = GameService.StreamRefiner.SortCriteria.DATE;
        GameService.StreamRefiner.Ordering ordering = GameService.StreamRefiner.Ordering.DESCENDING;
        switch (checkBox.getId()) {
            case (R.id.diffAsc):
                checkBox1 = findViewById(R.id.diffDesc);
                criteria = GameService.StreamRefiner.SortCriteria.DIFFICULTY;
                ordering = GameService.StreamRefiner.Ordering.ASCENDING;
                break;
            case (R.id.diffDesc):
                checkBox1 = findViewById(R.id.diffAsc);
                criteria = GameService.StreamRefiner.SortCriteria.DIFFICULTY;
                ordering = GameService.StreamRefiner.Ordering.DESCENDING;
                break;
            case (R.id.maxNumPlayerAsc):
                checkBox1 = findViewById(R.id.maxNumPlayerDesc);
                criteria = GameService.StreamRefiner.SortCriteria.MAX_NUMBER_OF_PLAYERS;
                ordering = GameService.StreamRefiner.Ordering.ASCENDING;
                break;
            case (R.id.maxNumPlayerDesc):
                checkBox1 = findViewById(R.id.maxNumPlayerAsc);
                criteria = GameService.StreamRefiner.SortCriteria.MAX_NUMBER_OF_PLAYERS;
                ordering = GameService.StreamRefiner.Ordering.ASCENDING;
                break;
            case (R.id.distAsc):
                checkBox1 = findViewById(R.id.distDesc);
                criteria = GameService.StreamRefiner.SortCriteria.DISTANCE;
                ordering = GameService.StreamRefiner.Ordering.ASCENDING;
                break;
            case (R.id.distDesc):
                checkBox1 = findViewById(R.id.distAsc);
                criteria = GameService.StreamRefiner.SortCriteria.DISTANCE;
                ordering = GameService.StreamRefiner.Ordering.DESCENDING;
                break;
            case (R.id.dateAsc):
                checkBox1 = findViewById(R.id.dateDesc);
                criteria = GameService.StreamRefiner.SortCriteria.DATE;
                ordering = GameService.StreamRefiner.Ordering.ASCENDING;
                break;
            case (R.id.dateDesc):
                checkBox1 = findViewById(R.id.dateAsc);
                criteria = GameService.StreamRefiner.SortCriteria.DATE;
                ordering = GameService.StreamRefiner.Ordering.DESCENDING;
                break;
            default:
        }

        if (checkBox1.isChecked()) {
            checkBox1.setChecked(false);
            streamRefinerBuilder.removeOneCriteria(criteria);
        } else {
            streamRefinerBuilder.sortBy(criteria, ordering);
        }
    }

    @OnClick(R.id.sortButton)
    public void onClickSortButton(View view) {
        Optional.ofNullable(getIntent().getParcelableExtra(REQUESTING_ACTIVITY_INTENT_KEY))
            .map(x -> (Intent) x).executeIfPresent(parentIntent -> {
            parentIntent.putExtra(GAME_LIST_VIEWER_STREAM_REFINER_KEY, streamRefinerBuilder.build());
            startActivity(parentIntent);
        }).executeIfAbsent(() -> Log.e("Sort click", "Could not find parent activity intent"));
        finish();
    }
}

