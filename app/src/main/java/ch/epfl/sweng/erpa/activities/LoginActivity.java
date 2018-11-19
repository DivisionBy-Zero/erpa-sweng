package ch.epfl.sweng.erpa.activities;

import android.app.Application;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.services.UserAuthService;
import toothpick.Scope;
import toothpick.Toothpick;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

public class LoginActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserAuthService uas;
    @Inject Scope scope;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Application application = getApplication();
        Toothpick.inject(this, scope);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        String usernameText = ((EditText) findViewById(R.id.username)).getText().toString();
        String passwordText = ((EditText) findViewById(R.id.password)).getText().toString();

        if (usernameText.isEmpty()) {
            createPopup(getString(R.string.noNameMessage), this);
            return;
        }

        if (passwordText.isEmpty()) {
            createPopup(getString(R.string.noPassMessage), this);
            return;
        }

        Optional<UserAuth> ua = uas.getUserAuth(usernameText, passwordText);

        ua.ifPresent(u -> finish());

        createPopup(getString(R.string.incorrectLogin), this);
    }

    public void continueWithoutLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
