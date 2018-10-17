package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.annimon.stream.function.Predicate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.MyAccountButtonAdapter;
import ch.epfl.sweng.erpa.model.UserProfile;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserProfile userProfile;

    // TODO: (Anne) add Butterknife for better readability
    private ListView myListView;
    private ArrayAdapter myAdapter;

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

        ArrayList<MyAccountButton> myFilteredAccountButtons = filterButtonList(
                myAccountButtonList, (b -> (b.isActiveForPlayer() == userProfile.getIsPlayer() ||
                        b.isActiveForGM() == userProfile.getIsGm())));
        List<Drawable> myFilteredDrawables = cutDownImagesList();

        myAdapter = new MyAccountButtonAdapter(this, myFilteredAccountButtons, myFilteredDrawables);
        myListView.setAdapter(myAdapter);
        myListView.setOnItemClickListener((l, v, position, id) -> startActivity(
                new Intent(this, myFilteredAccountButtons.get(position).getActivityClass())));

    }

    private void initializeArrays() {
        myAccountButtonList = Arrays.asList(
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.pendingRequestText), PendingRequestActivity.class, true, false),
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.confirmedGamesText), ConfirmedGamesActivity.class, true, false),
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.pastGamesText), PastGamesActivity.class, true, false),
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.hostedGamesText), HostedGamesActivity.class, false, true),
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.pastHostedGamesText), PastHostedGamesActivity.class, false, true),
                new MyAccountButton(this.getApplicationContext().getResources().getString(
                        R.string.profileText), ProfileActivity.class, true, true)
        );
        myDrawablesList = Arrays.asList(
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp),
                this.getApplicationContext().getDrawable(R.drawable.drawable_temp)
        );
    }

    private List<Drawable> cutDownImagesList() {
        ArrayList<Drawable> myFilterDrawableList = new ArrayList<>(myDrawablesList);
        for (int i = myDrawablesList.size() - 2; i >= myAccountButtonList.size() - 1; --i) {
            myFilterDrawableList.remove(i);
        }
        return myFilterDrawableList;
    }

    private ArrayList<MyAccountButton> filterButtonList(List<MyAccountButton> l,
                                                        Predicate<MyAccountButton> predicate) {
        ArrayList<MyAccountButton> newL = new ArrayList<>();
        for (int i = 0; i < l.size(); ++i) {
            if (predicate.test(l.get(i)))
                newL.add(l.get(i));
        }
        return newL;
    }
}
