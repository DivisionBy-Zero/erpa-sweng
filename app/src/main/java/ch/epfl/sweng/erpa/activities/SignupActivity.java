package ch.epfl.sweng.erpa.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import butterknife.OnClick;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserSignupService;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import toothpick.Scope;
import toothpick.Toothpick;

import static ch.epfl.sweng.erpa.util.ActivityUtils.createPopup;

public class SignupActivity extends DependencyConfigurationAgnosticActivity {

    @Inject Scope scope;
    @Inject UserSignupService uss;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        Toothpick.inject(this, scope);
    }

    @OnClick(R.id.signupButton)
    public void signUp(View view) {
        String usernameText = ((EditText) findViewById(R.id.nameText)).getText().toString();
        String passwordText = ((EditText) findViewById(R.id.passText)).getText().toString();
        String confirmText = ((EditText) findViewById(R.id.passTextConfirm)).getText().toString();
        String levelText = ((Spinner) findViewById(R.id.levelSelect)).getSelectedItem().toString();
        boolean isGm = ((CheckBox) findViewById(R.id.isGM)).isChecked();
        boolean isPlayer = ((CheckBox) findViewById(R.id.isPlayer)).isChecked();

        if (usernameText.isEmpty()) {
            createPopup(getString(R.string.noNameMessage), this);
            return;
        }

        if (passwordText.isEmpty()) {
            createPopup(getString(R.string.noPassMessage), this);
            return;
        }

        if (!passwordText.equals(confirmText)) {
            createPopup(getString(R.string.passwords_not_match), this);
            return;
        }

        if (!isGm && !isPlayer) {
            createPopup(getString(R.string.not_select_GM_or_player), this);
            return;
        }

        Optional<UserProfile> newUser = uss.storeNewUser(usernameText, passwordText, levelText, isGm, isPlayer);
        newUser.ifPresent(u -> finish());

        createPopup(getString(R.string.username_in_use), this);
    }
}
