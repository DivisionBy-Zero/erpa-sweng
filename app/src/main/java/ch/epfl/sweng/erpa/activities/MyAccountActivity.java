package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import ch.epfl.sweng.erpa.R;

public class MyAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        //listener to start pendingRequestActivity
        LinearLayout pending_request = findViewById(R.id.launch_pending_request_activity_layout);
        pending_request.setOnClickListener(view -> startActivity(new Intent(this, PendingRequestActivity.class)));
        //listener to start confirmedGamesActivity
        LinearLayout confirmed_games = findViewById(R.id.launch_confirmed_games_activity_layout);
        confirmed_games.setOnClickListener(view -> startActivity(new Intent(this, ConfirmedGamesActivity.class)));
        //listener to start pastGamesActivity
        LinearLayout past_games = findViewById(R.id.launch_past_games_activity_layout);
        past_games.setOnClickListener(view -> startActivity(new Intent(this, PastGamesActivity.class)));
        //listener to start hostedGamesActivity
        LinearLayout hosted_games = findViewById(R.id.launch_hosted_games_activity_layout);
        hosted_games.setOnClickListener(view -> startActivity(new Intent(this, HostedGamesActivity.class)));
        //listener to start pastHostedGamesActivity
        LinearLayout past_hosted_games = findViewById(R.id.launch_past_hosted_games_activity_layout);
        past_hosted_games.setOnClickListener(view -> startActivity(new Intent(this, PastHostedGamesActivity.class)));
        //listener to start profileActivity
        LinearLayout profile= findViewById(R.id.launch_profile_activity_layout);
        profile.setOnClickListener(view -> startActivity(new Intent(this, ProfileActivity.class)));
    }

}
