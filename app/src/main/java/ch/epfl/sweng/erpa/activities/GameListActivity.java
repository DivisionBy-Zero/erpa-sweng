package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.os.Bundle;

import ch.epfl.sweng.erpa.R;

public class GameListActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_list);
    }
}
