package ch.epfl.sweng.erpa.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import ch.epfl.sweng.erpa.model.UserAuth;
import ch.epfl.sweng.erpa.R;

public class LoginActivity extends Activity {

    UserAuth userAuth = new UserAuth();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view) {
        String usernameText = ((EditText) findViewById(R.id.username)).getText().toString();
        String passwordText = ((EditText) findViewById(R.id.password)).getText().toString();

        if (usernameText.isEmpty()) {
            createPopup(getString(R.string.noNameMessage));
            return;
        } else if (passwordText.isEmpty()) {
            createPopup(getString(R.string.noPassMessage));
            return;
        }

        if (!userAuth.checkLogin(usernameText, passwordText)) {
            createPopup(getString(R.string.incorrectLogin));
            return;
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
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
