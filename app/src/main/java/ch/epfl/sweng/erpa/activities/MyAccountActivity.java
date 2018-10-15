package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButton;
import ch.epfl.sweng.erpa.model.UserProfile;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserProfile userProfile;

//    @BindView(R.id.myAccountLayout) ConstraintLayout myAccountLayout;

    private List<MyAccountButton> maListe = Arrays.asList(
            new MyAccountButton("@string/pendingRequestText", PendingRequestActivity.class, "@mipmap/ic_launcher", true, false),
            new MyAccountButton("1", ConfirmedGamesActivity.class, "@mipmap/ic_launcher",true, false),
            new MyAccountButton("2", PastGamesActivity.class, "@mipmap/ic_launcher",true, false),
            new MyAccountButton("3", HostedGamesActivity.class,"@mipmap/ic_launcher", false, true),
            new MyAccountButton("4", PastHostedGamesActivity.class,"@mipmap/ic_launcher", false, true),
            new MyAccountButton("5", ProfileActivity.class,"@mipmap/ic_launcher", true, true)
    );

    public LinearLayout createButton(MyAccountButton myAccountButton) {
        LinearLayout linearLayout = new LinearLayout(this);
        ConstraintLayout constraintLayout = new ConstraintLayout(this);
        TextView textView = new TextView(this);
        ImageView imageView = new ImageView(this);
        textView.setText(myAccountButton.getText());
        constraintLayout.addView(textView);
        constraintLayout.addView(imageView);
        linearLayout.addView(constraintLayout);
        linearLayout.setOnClickListener(view -> startActivity(new Intent(this, myAccountButton.getActivityClass())));
        return linearLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.N) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_my_account);

        maListe.forEach(myAccountButton -> {
            if(myAccountButton.isActiveForPlayer() == userProfile.isPlayer() ||
                    myAccountButton.isActiveForGM() == userProfile.isGM()){
                LinearLayout myAccountLayout = findViewById(R.id.myAccountLayout);
                LinearLayout newButton = createButton(myAccountButton);
                View lastChild = myAccountLayout.getChildAt(
                        myAccountLayout.getChildCount()-1);
                if(lastChild != null){
                    int lastBottom = lastChild.getBottom();
                    newButton.setTop(lastBottom);
                }
                myAccountLayout.addView(newButton);
            }
        });

    // onResume
    }

}

//    private RecyclerView recyclerView;
//    private MyAccountButtonAdapter adapter;
//    private ArrayList<MyAccountButton> myAccountButtonArrayList;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_my_account);
//
//        recyclerView = findViewById(R.id.recyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//
//        myAccountButtonArrayList = new ArrayList<>();
//        adapter = new MyAccountButtonAdapter(this, myAccountButtonArrayList);
//        recyclerView.setAdapter(adapter);
//
//        createListData();
//
//    }
//
//    private void createListData() {
//        MyAccountButton myAccountButton = new MyAccountButton("@string/pendingRequestText", PendingRequestActivity.class, "@mipmap/ic_launcher", true, false);
//        myAccountButtonArrayList.add(myAccountButton);
//        myAccountButton = new MyAccountButton("1", ConfirmedGamesActivity.class, "@mipmap/ic_launcher",true, false);
//        myAccountButtonArrayList.add(myAccountButton);
//        myAccountButton = new MyAccountButton("2", PastGamesActivity.class, "@mipmap/ic_launcher",true, false);
//        myAccountButtonArrayList.add(myAccountButton);
//        myAccountButton = new MyAccountButton("3", HostedGamesActivity.class,"@mipmap/ic_launcher", false, true);
//        myAccountButtonArrayList.add(myAccountButton);
//        myAccountButton = new MyAccountButton("4", PastHostedGamesActivity.class,"@mipmap/ic_launcher", false, true);
//        myAccountButtonArrayList.add(myAccountButton);
//        myAccountButton = new MyAccountButton("5", ProfileActivity.class,"@mipmap/ic_launcher", true, true);
//        myAccountButtonArrayList.add(myAccountButton);
//        adapter.notifyDataSetChanged();
//    }

