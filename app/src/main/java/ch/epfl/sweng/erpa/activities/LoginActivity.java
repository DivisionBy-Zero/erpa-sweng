package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.services.UserAuthService;
import toothpick.Scope;
import toothpick.Toothpick;

public class LoginActivity extends DependencyConfigurationAgnosticActivity {
    @Inject UserAuthService uas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);  Application application = getApplication();
        Scope scope = Toothpick.openScopes(application, this);
        Toothpick.inject(this, scope);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        String usernameText = ((EditText) findViewById(R.id.username)).getText().toString();
        String passwordText = ((EditText) findViewById(R.id.password)).getText().toString();

        if (usernameText.isEmpty()) {
            createPopup(getString(R.string.noNameMessage));
            return;
        }

        if (passwordText.isEmpty()) {
            createPopup(getString(R.string.noPassMessage));
            return;
        }

        Optional<UserAuth> ua = uas.getUserAuth(usernameText, passwordText);

        ua.ifPresent(u -> finish());

        createPopup(getString(R.string.incorrectLogin));
    }

    public void continueWithoutLogin(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
