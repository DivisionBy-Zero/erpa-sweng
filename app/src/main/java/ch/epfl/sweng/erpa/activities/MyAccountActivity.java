package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.Game;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.GameService;
import ch.epfl.sweng.erpa.services.UserManagementService;
import ch.epfl.sweng.erpa.util.Pair;
import ch.epfl.sweng.erpa.util.Triplet;
import lombok.Data;

import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_VIEWER_STREAM_REFINER_KEY;
import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;
import static ch.epfl.sweng.erpa.util.ActivityUtils.onOptionItemSelectedUtils;
import static ch.epfl.sweng.erpa.util.ActivityUtils.setMenuInToolbar;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    private static Map<GameListActivity.GameListType, Integer> strIdForGameListType =
        Collections.unmodifiableMap(new HashMap<GameListActivity.GameListType, Integer>() {{
            put(GameListActivity.GameListType.PENDING_REQUEST, R.string.pendingRequestText);
            put(GameListActivity.GameListType.CONFIRMED_GAMES, R.string.confirmedGamesText);
            put(GameListActivity.GameListType.PAST_GAMES, R.string.pastGamesText);
            put(GameListActivity.GameListType.HOSTED_GAMES, R.string.hostedGamesText);
            put(GameListActivity.GameListType.PAST_HOSTED_GAMES, R.string.pastHostedGamesText);
        }});
    @BindView(R.id.myAccountLayout) ListView myListView;
    @BindView(R.id.myAccountToolbar) Toolbar toolbar;
    @BindView(R.id.my_account_drawer_layout) DrawerLayout myDrawerLayout;
    @BindView(R.id.my_account_navigation_view) NavigationView myNavigationView;
    @Inject OptionalDependencyManager optionalDependency;
    @Inject UserProfile userProfile;
    @Inject Username username;

    private static MyAccountButtonData getMyProfileButtonData(String userUuid) {
        Bundle bundle = new Bundle();
        bundle.putString(UserManagementService.PROP_INTENT_USER, userUuid);
        return new MyAccountButtonData(R.string.profileText, UserProfileActivity.class, bundle, R.drawable.ic_action_name);
    }

    private List<MyAccountButtonData> getAvailableButtonsData(boolean userIsPlayer, boolean userIsGm) {
        Stream<GameListActivity.GameListType> targetListType = Stream.of(
            new Triplet<>(GameListActivity.GameListType.PENDING_REQUEST, true, false),
            new Triplet<>(GameListActivity.GameListType.CONFIRMED_GAMES, true, false),
            new Triplet<>(GameListActivity.GameListType.PAST_GAMES, true, false),
            new Triplet<>(GameListActivity.GameListType.HOSTED_GAMES, false, true),
            new Triplet<>(GameListActivity.GameListType.PAST_HOSTED_GAMES, false, true))
            .filter(t -> t.getSecond() == userIsPlayer || t.getThird() == userIsGm)
            .map(Triplet::getFirst);

        Stream<Integer> images = Stream.of(
            R.drawable.ic_dice1,
            R.drawable.ic_dice2,
            R.drawable.ic_dice3,
            R.drawable.ic_dice4,
            R.drawable.ic_dice5,
            R.drawable.ic_dice6
        );

        return Stream.zip(targetListType, images, Pair::new).map(p -> {
            Bundle bundle = new Bundle();
            GameListActivity.GameListType listType = p.getFirst();
            // Add the activity class
            bundle.putSerializable(GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY, listType);
            // Add the base stream refiner associated to the activity class, if any present
            getRefinerFromGameListType(listType).ifPresent(sr ->
                bundle.putSerializable(GAME_LIST_VIEWER_STREAM_REFINER_KEY, sr)
            );
            return new MyAccountButtonData(strIdForGameListType.get(listType),
                GameListActivity.class, bundle, p.getSecond());
        }).collect(Collectors.toList());
    }

    private Optional<GameService.StreamRefiner> getRefinerFromGameListType(GameListActivity.GameListType listType) {
        Map<GameListActivity.GameListType, GameService.StreamRefiner> gameListTypeStreamRefinerMap =
            Collections.unmodifiableMap(new HashMap<GameListActivity.GameListType, GameService.StreamRefiner>() {{
                put(GameListActivity.GameListType.PENDING_REQUEST,
                    new GameService.StreamRefinerBuilder()
                        .filterBy(new GameService.StreamRefiner.WithPlayerPending(username.getUserUuid())).build());
                put(GameListActivity.GameListType.CONFIRMED_GAMES,
                    new GameService.StreamRefinerBuilder()
                        .filterBy(new GameService.StreamRefiner.WithPlayerConfirmed(username.getUserUuid())).build());
                put(GameListActivity.GameListType.PAST_GAMES,
                    new GameService.StreamRefinerBuilder()
                        .filterBy(new GameService.StreamRefiner.WithPlayerConfirmed(username.getUserUuid()))
                        .filterBy(new GameService.StreamRefiner.WithGameStatus(Game.GameStatus.FINISHED)).build());
                put(GameListActivity.GameListType.HOSTED_GAMES,
                    new GameService.StreamRefinerBuilder()
                        .filterBy(new GameService.StreamRefiner.WithGameMaster(username.getUserUuid())).build());
                put(GameListActivity.GameListType.PAST_HOSTED_GAMES,
                    new GameService.StreamRefinerBuilder()
                        .filterBy(new GameService.StreamRefiner.WithGameMaster(username.getUserUuid()))
                        .filterBy(new GameService.StreamRefiner.WithGameStatus(Game.GameStatus.FINISHED)).build());
            }});
        return Optional.ofNullable(gameListTypeStreamRefinerMap.get(listType));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_my_account);
        ButterKnife.bind(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;

        List<MyAccountButtonData> collect = getAvailableButtonsData(userProfile.getIsPlayer(), userProfile.getIsGm());
        collect.add(getMyProfileButtonData(userProfile.getUuid()));
        ArrayAdapter myAdapter = new MyAccountButtonAdapter(this, collect);
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener((pv, view, pos, idx) -> {
            MyAccountButtonData source = ((MyAccountButtonHolder) view.getTag()).getSource();
            Intent intent = new Intent(this, source.activityClass);
            intent.putExtra(UserManagementService.PROP_INTENT_USER, username.getUserUuid());
            intent.putExtras(source.getBundle());
            startActivity(intent);
        });

        addNavigationMenu(this, myDrawerLayout, myNavigationView, optionalDependency);
        setMenuInToolbar(this, toolbar);
        Optional.ofNullable(getSupportActionBar()).ifPresent(b -> b.setTitle(R.string.titleMyAccountActivity));
    }

    // Handle the toolbar items clicks
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Boolean found = onOptionItemSelectedUtils(item.getItemId(), myDrawerLayout);
        return found || super.onOptionsItemSelected(item);
    }

    static class MyAccountButtonAdapter extends ArrayAdapter<MyAccountButtonData> {
        MyAccountButtonAdapter(Context context, List<MyAccountButtonData> buttonInformation) {
            super(context, -1, buttonInformation);
        }

        @NonNull @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.my_account_button, parent, false);
                convertView.setTag(new MyAccountButtonHolder(convertView, position, getItem(position)));
            }
            ((MyAccountButtonHolder) convertView.getTag()).populate();
            return convertView;
        }
    }

    @Data
    static class MyAccountButtonData {
        @NonNull private Integer textId;
        @NonNull private Class activityClass;
        @NonNull private Bundle bundle;
        @NonNull private Integer drawableResourceId;
    }

    @Data
    static class MyAccountButtonHolder {
        @BindView(R.id.buttonName) TextView text;
        @BindView(R.id.buttonImage) ImageView image;
        @NonNull View view;
        @NonNull Integer position;
        @NonNull MyAccountButtonData source;

        void populate() {
            ButterKnife.bind(this, view);
            text.setText(source.getTextId());
            image.setImageDrawable(view.getContext().getDrawable(source.getDrawableResourceId()));
        }
    }
}
