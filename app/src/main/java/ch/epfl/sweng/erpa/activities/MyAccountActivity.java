package ch.epfl.sweng.erpa.activities;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.Collector;
import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.MyAccountButtonAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;
import lombok.Data;
import lombok.NonNull;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserProfile userProfile;

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

        collect.add(new Pair<>(new MyAccountButton(resources.getString(R.string.profileText),
                ProfileActivity.class, true, true),
                context.getDrawable(R.drawable.ic_action_name)));

        myAdapter = new MyAccountButtonAdapter(this, collect);
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener((l, v, position, id) -> startActivity(
                new Intent(this, collect.get(position).getFirst().getActivityClass())));

    }

    private void initializeArrays() {
        context = this.getApplicationContext();
        resources = context.getResources();
        myAccountButtonList = Arrays.asList(
                new MyAccountButton(resources.getString(R.string.pendingRequestText),
                        PendingRequestActivity.class, true, false),
                new MyAccountButton(resources.getString(R.string.confirmedGamesText),
                        ConfirmedGamesActivity.class, true, false),
                new MyAccountButton(resources.getString(R.string.pastGamesText),
                        PastGamesActivity.class, true, false),
                new MyAccountButton(resources.getString(R.string.hostedGamesText),
                        HostedGamesActivity.class, false, true),
                new MyAccountButton(resources.getString(R.string.pastHostedGamesText),
                        PastHostedGamesActivity.class, false, true)
        );
        myDrawablesList = Stream.rangeClosed(1, 8).map(i -> context.getDrawable(R.drawable.ic_action_name)).collect(
                Collectors.toList());
    }

    @Data
    public class Pair<T1, T2> {
        @NonNull T1 first;
        @NonNull T2 second;
    }
}
