package ch.epfl.sweng.erpa.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Optional;

import javax.inject.Inject;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.CreateGameActivity;
import ch.epfl.sweng.erpa.activities.DiceActivity;
import ch.epfl.sweng.erpa.activities.GameListActivity;
import ch.epfl.sweng.erpa.activities.MyAccountActivity;
import ch.epfl.sweng.erpa.model.UserProfile;
import ch.epfl.sweng.erpa.services.UserProfileService;

import static android.support.v4.app.ActivityCompat.startActivityForResult;
import static ch.epfl.sweng.erpa.activities.GameListActivity.GAME_LIST_ACTIVTIY_CLASS_KEY;

public class ActivityUtils {

    public static void createPopup(String text, Context context) {
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

    //Handle navigationMenu interactions
    public static void addNavigationMenu(Activity activity, DrawerLayout mDrawerLayout, NavigationView navigationView, UserProfile up){
        navigationView.setNavigationItemSelectedListener(
                menuItem -> onNavigationItemMenuSelected(menuItem, mDrawerLayout, activity));
        setUsernameInMenu(navigationView, up);
    }

    public static boolean onNavigationItemMenuSelected(MenuItem menuItem, DrawerLayout mDrawerLayout, Activity activity) {
        // close drawer when item is tapped
        mDrawerLayout.closeDrawers();

        Bundle bundle = new Bundle();

        Intent intent = new Intent(activity, MyAccountActivity.class);
        switch (menuItem.getItemId()) {
            case R.id.menu_createGame:
                intent = new Intent(activity, CreateGameActivity.class);
                break;
            case R.id.menu_findGame:
                bundle.putSerializable(GAME_LIST_ACTIVTIY_CLASS_KEY, GameListActivity.GameList.FIND_GAME);
                intent = new Intent(activity, GameListActivity.class);
                intent.putExtras(bundle);
                break;
            case R.id.menu_dice:
                intent = new Intent(activity, DiceActivity.class);
                break;
            default:
        }

        activity.startActivity(intent);

        return true;
    }

    public static void setUsernameInMenu(NavigationView navigationView, UserProfile up) {

        View headerView = navigationView.getHeaderView(0);
        TextView username = headerView.findViewById(R.id.username_menu_textView);
        if (up != null) {
            username.setText(up.getUsername());
        }
        else {
            username.setText("currently not logged");
        }
    }

    public static void setMenuInToolbar(AppCompatActivity activity, Toolbar toolbar){
        activity.setSupportActionBar(toolbar);
        ActionBar actionbar = activity.getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu);
    }

    public static boolean onOptionItemSelectedUtils(int menuItemId, DrawerLayout mDrawerLayout) {
        switch (menuItemId) {
            case android.R.id.home:
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return false;
    }
}
