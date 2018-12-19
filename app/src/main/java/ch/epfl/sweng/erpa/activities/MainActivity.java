package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.view.View;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setUsernameInMenu;

public class MainActivity extends DependencyConfigurationAgnosticActivity {
    @BindView(R.id.main_drawer_layout) DrawerLayout drawerLayout;
    @BindView(R.id.main_navigation_view) NavigationView navigationView;

    @Inject OptionalDependencyManager optionalDependency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        addNavigationMenu(this, drawerLayout, navigationView, optionalDependency);
    }

    @Override protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;
        setUsernameInMenu(navigationView, optionalDependency.get(Username.class));
    }

    @OnClick(R.id.launch_storage_provider_greet)
    public void launchStorageProviderGreet(View view) {
        startActivity(new Intent(this, RemoteServiceGreeterActivity.class));
    }

    @OnClick(R.id.launch_game_list_button)
    public void launchGameList(View view) {
        Intent intent = new Intent(this, GameListActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, GameListActivity.GameListType.FIND_GAME);
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

    @OnClick(R.id.launch_login_button)
    public void launchLoginPage(View view) {
        startActivity(new Intent(this, LoginActivity.class));
    }

    @OnClick(R.id.launch_signup_button)
    public void launchSignupPage(View view) {
        startActivity(new Intent(this, SignupActivity.class));
    }

    @OnClick(R.id.launch_dice_button)
    public void launchDicePage(View view) {
        startActivity(new Intent(this, DiceActivity.class)); // $COVERAGE-IGNORE$
    }
}
