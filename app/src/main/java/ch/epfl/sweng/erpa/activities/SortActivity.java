package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;

import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;

public class SortActivity extends DependencyConfigurationAgnosticActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_sort);
        ButterKnife.bind(this);
    }
}

