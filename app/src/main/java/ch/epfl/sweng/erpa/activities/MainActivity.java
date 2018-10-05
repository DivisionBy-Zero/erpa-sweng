package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.CreateGameActivity;
import ch.epfl.sweng.erpa.activities.GameListActivity;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launch_game_list(View view)
    {
        Intent intent = new Intent(this, GameListActivity.class);
        startActivity(intent);
    }

    public void launch_create_game(View view)
    {
        Intent intent = new Intent(this, CreateGameActivity.class);
        startActivity(intent);
    }
}
