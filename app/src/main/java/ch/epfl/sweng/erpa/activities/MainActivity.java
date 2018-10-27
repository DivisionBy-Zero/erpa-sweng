package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_ACTIVTIY_CLASS_KEY;

public class MainActivity extends DependencyConfigurationAgnosticActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.launch_storage_provider_greet)
    public void launchStorageProviderGreet(View view) {
        startActivity(new Intent(this, RemoteServiceGreeterActivity.class));
    }

    @OnClick(R.id.launch_game_list_button)
    public void launchGameList(View view) {
        Intent intent = new Intent(this, GameListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_ACTIVTIY_CLASS_KEY, GameListActivity.GameList.FIND_GAME);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @OnClick(R.id.launch_create_game_button)
    public void launchCreateGame(View view) {
        startActivity(new Intent(this, CreateGameActivity.class));
    }

    @OnClick(R.id.launch_my_account_button)
    public void launchMyAccount(View view) {
        startActivity(new Intent(this, MyAccountActivity.class));
    }
}
