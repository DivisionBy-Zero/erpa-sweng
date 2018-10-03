package ch.epfl.sweng.erpa;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void launch_game_list(View view)
    {
        Intent intent = new Intent(this, GameList.class);
        startActivity(intent);
    }

    public void launch_create_game(View view)
    {
        Intent intent = new Intent(this, CreateGame.class);
        startActivity(intent);
    }
}
