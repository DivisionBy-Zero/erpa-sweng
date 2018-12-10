package ch.epfl.sweng.erpa.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.annimon.stream.function.Supplier;

import java.util.NoSuchElementException;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;
import ch.epfl.sweng.erpa.services.UserManagementService;

import static ch.epfl.sweng.erpa.util.ActivityUtils.addNavigationMenu;

public class UserProfileActivity extends DependencyConfigurationAgnosticActivity {
    @Inject OptionalDependencyManager optionalDependency;
    @Inject UserManagementService ups;

    @BindView(R.id.usernameTextView) TextView usernameTV;
    @BindView(R.id.experienceTextView) TextView experienceTV;
    @BindView(R.id.playerOrGMTextView) TextView playerOrGmTV;

    private AsyncTaskService asyncTaskService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_user_profile);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (dependenciesNotReady()) return;

        String userUuid = getUuidFromIntent();

        Supplier<Throwable> notFoundException = () ->
            new NoSuchElementException("Could not find User Profile with UUID " + userUuid);

        asyncTaskService.run(() -> ups.getUserProfile(userUuid).orElseThrow(notFoundException),
            this::updateUserProfile, this::asyncDataFetchErrorHandler);

        asyncTaskService.run(() -> ups.getUsernameFromUserUuid(userUuid).orElseThrow(notFoundException),
            username -> usernameTV.setText("@" + username.getUsername()), this::asyncDataFetchErrorHandler);

        addNavigationMenu(this, findViewById(R.id.user_profile_drawer_layout),
            findViewById(R.id.user_profile_navigation_view), optionalDependency);
    }

    @SuppressLint("SetTextI18n") private void updateUserProfile(UserProfile userProfile) {
        experienceTV.setText(UserProfile.Experience.Casual.toString());
        if (userProfile.getIsGm() && userProfile.getIsPlayer())
            playerOrGmTV.setText(getString(R.string.user_profile_player_and_gm));
        else if (userProfile.getIsPlayer())
            playerOrGmTV.setText(getString(R.string.user_profile_player_only));
        else if (userProfile.getIsGm())
            playerOrGmTV.setText(getString(R.string.user_profile_gm_only));
        else
            playerOrGmTV.setText(getString(R.string.user_profile_neither_player_nor_gm));
    }

    private String getUuidFromIntent() {
        String userUuid = getIntent().getStringExtra(UserManagementService.PROP_INTENT_USER);
        if (userUuid == null) {
            RuntimeException thrown = new IllegalArgumentException("UserUuid property not found");
            Log.e("retrieveUuid", "Cannot show User" , thrown);
            finish();
            throw thrown;
        }
        return userUuid;
    }

    private void asyncDataFetchErrorHandler(Throwable exc) {
        Log.e("asyncErrorHandler", "Cannot populate UserProfileActivity", exc);
        finish();
    }
}
