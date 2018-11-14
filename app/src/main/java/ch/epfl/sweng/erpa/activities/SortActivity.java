package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.services.GameService;

public class SortActivity extends DependencyConfigurationAgnosticActivity {

    private GameService.StreamRefinerBuilder streamRefinerBuilder = GameService.StreamRefiner.builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_sort);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.diffAsc, R.id.diffDesc, R.id.maxNumPlayerAsc, R.id.maxNumPlayerDesc,
              R.id.distAsc, R.id.distDesc, R.id.dateAsc, R.id.dateDesc})
    public void onCheckBoxClicked(CheckBox checkBox) {
        CheckBox checkBox1 = checkBox;
        GameService.StreamRefiner.SortCriteria criteria;
        GameService.StreamRefiner.Ordering ordering;
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
                checkBox1 = findViewById(R.id.dateAsc);
                criteria = GameService.StreamRefiner.SortCriteria.DATE;
                ordering = GameService.StreamRefiner.Ordering.DESCENDING;
        }
        if (checkBox1.isChecked()) {
            checkBox1.setChecked(false);
            streamRefinerBuilder.removeOneCriteria(criteria);
        } else
            streamRefinerBuilder.sortBy(criteria, ordering);

    }

    @OnClick(R.id.sortButton)
    public void onClickSortButton(View view) {
        Bundle bundle = getIntent().getExtras();
        Intent intent = new Intent(this, GameListActivity.class);
        intent.putExtras(bundle);
        intent.putExtra("result", streamRefinerBuilder);
        setResult(RESULT_OK, intent);
        startActivity(intent);
    }
}

