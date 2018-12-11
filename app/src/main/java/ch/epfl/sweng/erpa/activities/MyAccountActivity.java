package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.MyAccountButtonAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.UserManagementService;
import ch.epfl.sweng.erpa.util.Pair;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject OptionalDependencyManager optionalDependency;
    @Inject Username username;
    @Inject UserProfile userProfile;
    @Inject UserManagementService ups;

    // TODO: (Anne) add Butterknife for better readability
    private ListView myListView;
    private ArrayAdapter myAdapter;

    private Context context;
    private Resources resources;

    private List<MyAccountButton> myAccountButtonList;
    private List<Drawable> myDrawablesList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_my_account);
    }

    @Override protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;

        myListView = findViewById(R.id.myAccountLayout);
        initializeArrays();

        Stream<MyAccountButton> buttonStream = Stream.of(myAccountButtonList).filter(
                b -> (b.getActiveForPlayer() == userProfile.getIsPlayer() ||
                        b.getActiveForGM() == userProfile.getIsGm()));
        Stream<Drawable> drawableStream = Stream.of(myDrawablesList);

        List<Pair<MyAccountButton, Drawable>> collect = Stream.zip(buttonStream, drawableStream,
                Pair::new).collect(Collectors.toList());

        Bundle profileBundle = new Bundle();
        profileBundle.putString(UserManagementService.PROP_INTENT_USER, username.getUserUuid());
        collect.add(new Pair<>(new MyAccountButton(resources.getString(R.string.profileText),
                UserProfileActivity.class, profileBundle, true, true),
                context.getDrawable(R.drawable.ic_action_name)));

        myAdapter = new MyAccountButtonAdapter(this, collect);
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener((l, v, position, id) -> {
            Intent intent = new Intent(this, collect.get(position).getFirst().getActivityClass());
            intent.putExtra(UserManagementService.PROP_INTENT_USER, username.getUserUuid());
            Bundle bundle = collect.get(position).getFirst().getBundle();
            intent.putExtras(bundle);
            startActivity(intent);
        });

        addNavigationMenu(this, findViewById(R.id.my_account_drawer_layout), findViewById(R.id.my_account_navigation_view), optionalDependency);
        setMenuInToolbar(this, findViewById(R.id.myAccountToolbar));
        getSupportActionBar().setTitle(R.string.titleMyAccountActivity);

    }

    //Handle toolbar items clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean found = onOptionItemSelectedUtils(item.getItemId(), findViewById(R.id.my_account_drawer_layout));
        return found || super.onOptionsItemSelected(item);
    }

    private void initializeArrays() {
        context = this.getApplicationContext();
        resources = context.getResources();
        Class activity = GameListActivity.class;
        Bundle empty = new Bundle();
        myAccountButtonList = Stream.of(
                new MyAccountButton(resources.getString(R.string.pendingRequestText), activity,
                        empty, true, false),
                new MyAccountButton(resources.getString(R.string.confirmedGamesText), activity,
                        empty, true, false),
                new MyAccountButton(resources.getString(R.string.pastGamesText), activity, empty,
                        true, false),
                new MyAccountButton(resources.getString(R.string.hostedGamesText), activity, empty,
                        false, true),
                new MyAccountButton(resources.getString(R.string.pastHostedGamesText), activity,
                        empty, false, true))
                .map(b -> {
                    Bundle bundle = new Bundle();
                    GameListActivity.GameListType targetList;
                    targetList = findTargetGameListType(b);
                    bundle.putSerializable(GAME_LIST_ACTIVITY_CLASS_KEY, targetList);
                    return new MyAccountButton(b.getText(), b.getActivityClass(),
                            bundle,
                            b.getActiveForPlayer(), b.getActiveForGM());
                })
                .collect(Collectors.toList());

        myDrawablesList = Stream.of(
                R.drawable.ic_dice1,
                R.drawable.ic_dice2,
                R.drawable.ic_dice3,
                R.drawable.ic_dice4,
                R.drawable.ic_dice5,
                R.drawable.ic_dice6)
                .map(context::getDrawable)
                .collect(Collectors.toList());
    }

    @android.support.annotation.NonNull
    private GameListActivity.GameListType findTargetGameListType(MyAccountButton b) {
        GameListActivity.GameListType targetList;
        if (b.getText().equals(resources.getString(R.string.pendingRequestText))) {
            targetList = GameListActivity.GameListType.PENDING_REQUEST;
        } else if (b.getText().equals(resources.getString(R.string.confirmedGamesText))) {
            targetList = GameListActivity.GameListType.CONFIRMED_GAMES;
        } else if (b.getText().equals(resources.getString(R.string.pastGamesText))) {
            targetList = GameListActivity.GameListType.PAST_GAMES;
        } else if (b.getText().equals(resources.getString(R.string.hostedGamesText))) {
            targetList = GameListActivity.GameListType.HOSTED_GAMES;
        } else {
            targetList = GameListActivity.GameListType.PAST_HOSTED_GAMES;
        }
        return targetList;
    }
}

