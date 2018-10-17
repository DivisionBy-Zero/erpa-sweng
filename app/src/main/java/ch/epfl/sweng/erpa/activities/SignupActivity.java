package ch.epfl.sweng.erpa.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.OnClick;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserSignupService;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import toothpick.Scope;
import toothpick.Toothpick;

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
            createPopup(getString(R.string.noNameMessage));
            return;
        }

        if (passwordText.isEmpty()) {
            createPopup(getString(R.string.noPassMessage));
            return;
        }

        if (!passwordText.equals(confirmText)) {
            createPopup(getString(R.string.passwords_not_match));
            return;
        }

        if (!isGm && !isPlayer) {
            createPopup(getString(R.string.not_select_GM_or_player));
            return;
        }

        Optional<UserProfile> newUser = uss.storeNewUser(usernameText, passwordText, levelText, isGm, isPlayer);
        newUser.ifPresent(u -> finish());

        createPopup(getString(R.string.username_in_use));
    }

    private void createPopup(String text) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        final TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(tv);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
            }
        });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();
        // show it
        alertDialog.show();
    }
}
