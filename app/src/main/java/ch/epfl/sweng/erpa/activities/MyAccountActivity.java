package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.MyAccountButtonAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserAuthService;
import ch.epfl.sweng.erpa.services.UserProfileService;
import ch.epfl.sweng.erpa.util.Pair;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_ACTIVTIY_CLASS_KEY;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserProfile userProfile;
    @Inject UserProfileService ups;

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
        onResume();
    }

    @Override protected void onResume() {
        super.onResume();

        myListView = findViewById(R.id.myAccountLayout);
        initializeArrays();

        Stream<MyAccountButton> buttonStream = Stream.of(myAccountButtonList).filter(
                b -> (b.getActiveForPlayer() == userProfile.getIsPlayer() ||
                        b.getActiveForGM() == userProfile.getIsGm()));
        Stream<Drawable> drawableStream = Stream.of(myDrawablesList);

        List<Pair<MyAccountButton, Drawable>> collect = Stream.zip(buttonStream, drawableStream,
                Pair::new).collect(Collectors.toList());

        Bundle profileBundle = new Bundle();
        profileBundle.putString(UserProfileService.PROP_INTENT_USER, userProfile.getUuid());
        collect.add(new Pair<>(new MyAccountButton(resources.getString(R.string.profileText),
                UserProfileActivity.class, profileBundle, true, true),
                context.getDrawable(R.drawable.ic_action_name)));
        // TODO (@Anne) remove when proper authentication is implemented
        ups.saveUserProfile(userProfile);

        myAdapter = new MyAccountButtonAdapter(this, collect);
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener((l, v, position, id) -> {
            Intent intent = new Intent(this, collect.get(position).getFirst().getActivityClass());
            Bundle bundle = collect.get(position).getFirst().getBundle();
            intent.putExtras(bundle);
            startActivity(intent);
        });
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
                    GameListActivity.GameList targetList;
                    targetList = findTargetGameListType(b);
                    bundle.putSerializable(GAME_LIST_ACTIVTIY_CLASS_KEY, targetList);
                    return new MyAccountButton(b.getText(), b.getActivityClass(),
                            bundle,
                            b.getActiveForPlayer(), b.getActiveForGM());
                })
                .collect(Collectors.toList());

        myDrawablesList = Stream.rangeClosed(1, 8).map(
                i -> context.getDrawable(R.drawable.ic_action_name)).collect(
                Collectors.toList());
    }

    @android.support.annotation.NonNull
    private GameListActivity.GameList findTargetGameListType(MyAccountButton b) {
        GameListActivity.GameList targetList;
        if (b.getText().equals(resources.getString(R.string.pendingRequestText))) {
            targetList = GameListActivity.GameList.PENDING_REQUEST;
        } else if (b.getText().equals(resources.getString(R.string.confirmedGamesText))) {
            targetList = GameListActivity.GameList.CONFIRMED_GAMES;
        } else if (b.getText().equals(resources.getString(R.string.pastGamesText))) {
            targetList = GameListActivity.GameList.PAST_GAMES;
        } else if (b.getText().equals(resources.getString(R.string.hostedGamesText))) {
            targetList = GameListActivity.GameList.HOSTED_GAMES;
        } else {
            targetList = GameListActivity.GameList.PAST_HOSTED_GAMES;
        }
        return targetList;
    }
}

