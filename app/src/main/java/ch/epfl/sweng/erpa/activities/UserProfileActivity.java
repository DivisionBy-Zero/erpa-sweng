package ch.epfl.sweng.erpa.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.annimon.stream.Optional;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserProfileService;

import static android.content.ContentValues.TAG;

public class UserProfileActivity extends DependencyConfigurationAgnosticActivity {

    @Inject UserProfileService ups;

    @BindView(R.id.usernameTextView) TextView username;
    @BindView(R.id.experienceTextView) TextView experience;
    @BindView(R.id.playerOrGMTextView) TextView playerOrGm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
    }


    @Override
    protected void onResume() {
        super.onResume();

        String uuid = getUuidFromIntent();
        Optional<UserProfile> optUserProfile = ups.getUserProfile(uuid);
        if (optUserProfile.isPresent()) {
            updateFields(optUserProfile.get());
        } else {
            Log.d(TAG, "onResume: could not find UserProfile in database. Exiting", new NoSuchElementException());
            finish();
        }

    }

    @SuppressLint("SetTextI18n") private void updateFields(UserProfile userProfile) {
        username.setText(userProfile.getUsername());
        experience.setText(userProfile.getXp().toString());
        if (userProfile.getIsGm() && userProfile.getIsPlayer())
            playerOrGm.setText("Player and Game master");
        else if (userProfile.getIsPlayer())
            playerOrGm.setText("Player");
        else if (userProfile.getIsGm())
            playerOrGm.setText("Game master");
        else
            playerOrGm.setText("");
    }

    private String getUuidFromIntent() {
        String uuid = getIntent().getStringExtra(UserProfileService.PROP_INTENT_USER);
        if (uuid != null)
            return uuid;
        else {
            RuntimeException thrown = new IllegalArgumentException("User Id not found");
            Log.d(UserProfileActivity.class.getName(), "no user id passed with intent", thrown);
            finish();
            throw thrown;
        }
    }
}
