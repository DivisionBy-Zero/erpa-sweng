package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ArrayRes;
import android.support.annotation.RequiresApi;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

import butterknife.BindView;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.MyAccountButtons;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserProfileProvider;

public class MyAccountActivity extends DependencyConfigurationAgnosticActivity {
    @Inject @Named("User Profile Provider") Provider<UserProfile> userProfileProvider;

//    @BindView(R.id.myAccountLayout) ConstraintLayout myAccountLayout;

    // List<Images> images = {}
    // List display_text, where to go, isPlayer, isGM

    private List<MyAccountButtons> maListe = Arrays.asList(
            new MyAccountButtons("@string/pendingRequestText", PendingRequestActivity.class, "@mipmap/ic_launcher", true, false),
            new MyAccountButtons("1", ConfirmedGamesActivity.class, "@mipmap/ic_launcher",true, false),
            new MyAccountButtons("2", PastGamesActivity.class, "@mipmap/ic_launcher",true, false),
            new MyAccountButtons("3", HostedGamesActivity.class,"@mipmap/ic_launcher", false, true),
            new MyAccountButtons("4", PastHostedGamesActivity.class,"@mipmap/ic_launcher", false, true),
            new MyAccountButtons("5", ProfileActivity.class,"@mipmap/ic_launcher", true, true)
    );
            //new ArrayList<MyAccountButtons>();

    // maListe.filter(selon_indicateur)

    public LinearLayout createButton(MyAccountButtons myAccountButtons) {
        LinearLayout linearLayout = new LinearLayout(this);
        ConstraintLayout constraintLayout = new ConstraintLayout(this);
        TextView textView = new TextView(this);
        ImageView imageView = new ImageView(this);
        textView.setText(myAccountButtons.getText());
        constraintLayout.addView(textView);
        constraintLayout.addView(imageView);
        linearLayout.addView(constraintLayout);
        linearLayout.setOnClickListener(view -> startActivity(new Intent(this, myAccountButtons.getActivityClass())));
        return linearLayout;
    }

    @RequiresApi(api = Build.VERSION_CODES.N) @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

//        userProfileProvider.get();
        
        maListe.forEach(myAccountButtons -> {
            if(myAccountButtons.isActiveForPlayer() == true ||
                    myAccountButtons.isActiveForGM() == true){
                LinearLayout myAccountLayout = findViewById(R.id.myAccountLayout);
                LinearLayout newButton = createButton(myAccountButtons);
                View lastChild = myAccountLayout.getChildAt(
                        myAccountLayout.getChildCount()-1);
                if(lastChild != null){
                    int lastBottom = lastChild.getBottom();
                    newButton.setTop(lastBottom);
                }
                myAccountLayout.addView(newButton);
            }
        });


    }

}
