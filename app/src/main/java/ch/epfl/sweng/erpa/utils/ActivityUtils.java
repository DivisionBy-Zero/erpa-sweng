package ch.epfl.sweng.erpa.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.widget.TextView;

public class ActivityUtils {

    public static void createPopup(Context context, String text) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        final TextView tv = new TextView(context);
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
