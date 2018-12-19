package ch.epfl.sweng.erpa.activities;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

/**
 * An activity that communicates with the server in order to
 * create a new account
 */
public class SignupActivity extends DependencyConfigurationAgnosticActivity {
    @BindView(R.id.signup_activity_loading_panel) View progressLoader;
    @BindView(R.id.user_input_panel) View inputPanel;
    @BindView(R.id.isGM) CheckBox isGmV;
    @BindView(R.id.isPlayer) CheckBox isPlayerV;
    @BindView(R.id.levelSelect) Spinner levelTextV;
    @BindView(R.id.nameText) EditText usernameTextV;
    @BindView(R.id.passText) EditText passwordTextV;
    @BindView(R.id.passTextConfirm) EditText confirmTextV;

    @Inject LoggedUserCoordinator loggedUserCoordinator;

    AsyncTaskService asyncTaskService;
    AsyncTask<Void, Void, Exceptional<String>> signUpTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);
    }

    @OnClick(R.id.signupButton)
    public void signUp(View view) {
        String username = usernameTextV.getText().toString();
        String password = passwordTextV.getText().toString();
        boolean isGm = isGmV.isChecked();
        boolean isPlayer = isPlayerV.isChecked();

        if (getErrorMessage(username, password, isGm, isPlayer)
            .executeIfPresent(m -> createPopup(m, this)).isPresent())
            return;

        inputPanel.setVisibility(View.GONE);
        progressLoader.setVisibility(View.VISIBLE);

        UserProfile newUser = new UserProfile("", isGm, isPlayer);
        signUpTask = loggedUserCoordinator.trySignUp(asyncTaskService, username, password, newUser, this::finish,
            exception -> {
                inputPanel.setVisibility(View.VISIBLE);
                progressLoader.setVisibility(View.GONE);
                Log.e("tryLogin", "Could not create account", exception);
                createPopup("Could not create account: " + exception.getMessage(), this);
            });
    }

    @Override protected void onStop() {
        super.onStop();
        if (signUpTask != null)
            signUpTask.cancel(true);
    }

    Optional<String> getErrorMessage(String username, String password, boolean isGm, boolean isPlayer) {
        if (username.isEmpty()) return Optional.of(getString(R.string.noNameMessage));
        if (password.isEmpty()) return Optional.of(getString(R.string.noPassMessage));
        if (!password.equals(confirmTextV.getText().toString()))
            return Optional.of(getString(R.string.passwords_not_match));
        if (!isGm && !isPlayer) return Optional.of(getString(R.string.not_select_GM_or_player));
        return Optional.empty();
    }
}
