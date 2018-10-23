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

import com.annimon.stream.Exceptional;
import com.annimon.stream.Optional;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.services.UserAuthService;
import toothpick.Scope;

public class SignupActivity extends DependencyConfigurationAgnosticActivity {
    @Inject Scope scope;
    @Inject UserAuthService uap;

    @BindView(R.id.nameText) EditText usernameField;
    @BindView(R.id.passText) EditText passwordField;
    @BindView(R.id.passTextConfirm) EditText passwordConfirmField;
    @BindView(R.id.levelSelect) Spinner levelSpinner;
    @BindView(R.id.isGM) CheckBox gmCheckBox;
    @BindView(R.id.isPlayer) CheckBox playerCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.signupButton)
    public void signUp(View view) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        if (new VerifyConditionAndPrintMessageIfFailed()
                .and(username.isEmpty(), R.string.noNameMessage)
                .and(password.isEmpty(), R.string.noPassMessage)
                .and(!passwordConfirmField.getText().toString().equals(password), R.string.passwordsNotMatch)
                .and(!gmCheckBox.isChecked() && !playerCheckBox.isChecked(), R.string.notSelectGmOrPlayer)
                .coalesce())
            return;

        Exceptional.of(() -> uap.signUpUser(username, password))
                .ifException(e -> createPopup(e.getMessage()))
                .getOrElse(Optional.empty())
                .ifPresent(u -> finish());
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

    private class VerifyConditionAndPrintMessageIfFailed {
        String errorMessage;

        VerifyConditionAndPrintMessageIfFailed and(Boolean condition, int resId) {
            if (errorMessage == null && condition) errorMessage = getString(resId);
            return this;
        }

        boolean coalesce() {
            if (errorMessage != null) createPopup(errorMessage);
            return errorMessage != null;
        }
    }
}
