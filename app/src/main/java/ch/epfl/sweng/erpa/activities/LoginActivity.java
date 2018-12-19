package ch.epfl.sweng.erpa.activities;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.operations.AsyncTaskService;
import ch.epfl.sweng.erpa.operations.LoggedUserCoordinator;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

/**
 * An activity for logging in.
 * Communicates with the server, using information given in fields
 * The resulting user profile is then injectable
 */
public class LoginActivity extends DependencyConfigurationAgnosticActivity {
    @BindView(R.id.login_activity_loading_panel) View progressLoader;
    @BindView(R.id.login_activity_input_panel) View inputPanel;
    @BindView(R.id.username) EditText usernameET;
    @BindView(R.id.password) EditText passwordET;

    @Inject LoggedUserCoordinator loggedUserCoordinator;

    AsyncTaskService asyncTaskService;
    AsyncTask<Void, Void, Exceptional<String>> loginTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (dependenciesNotReady()) return;
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        asyncTaskService = new AsyncTaskService();
        asyncTaskService.setResultConsumerContext(this::runOnUiThread);
    }

    @OnClick(R.id.login_button)
    public void login(View view) {
        String username = usernameET.getText().toString();
        String password = passwordET.getText().toString();

        if (getErrorMessage(username, password)
            .executeIfPresent(m -> createPopup(m, this)).isPresent())
            return;

        inputPanel.setVisibility(View.GONE);
        progressLoader.setVisibility(View.VISIBLE);

        loginTask = loggedUserCoordinator.tryLogin(asyncTaskService, username, password, this::finish,
            exception -> {
                inputPanel.setVisibility(View.VISIBLE);
                progressLoader.setVisibility(View.GONE);
                Log.e("tryLogin", "Could not login", exception);
                createPopup("Could not login: " + exception.getMessage(), this);
            });
    }

    @Override protected void onStop() {
        super.onStop();
        if (loginTask != null)
            loginTask.cancel(true);
    }

    @OnClick(R.id.no_login_button)
    public void continueWithoutLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_TASK_ON_HOME);
        startActivity(intent);
        finish();
    }

    Optional<String> getErrorMessage(String username, String password) {
        if (username.isEmpty()) return Optional.of(getString(R.string.noNameMessage));
        if (password.isEmpty()) return Optional.of(getString(R.string.noPassMessage));
        return Optional.empty();
    }
}
