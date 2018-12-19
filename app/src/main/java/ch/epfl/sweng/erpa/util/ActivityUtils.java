package ch.epfl.sweng.erpa.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.annimon.stream.Optional;

import ch.epfl.sweng.erpa.R;
import ch.epfl.sweng.erpa.activities.CreateGameActivity;
import ch.epfl.sweng.erpa.activities.DiceActivity;
import ch.epfl.sweng.erpa.activities.GameListActivity;
import ch.epfl.sweng.erpa.activities.MyAccountActivity;
import ch.epfl.sweng.erpa.model.Username;
import ch.epfl.sweng.erpa.operations.OptionalDependencyManager;

public class ActivityUtils {
    public static void createPopup(String text, Context context) {
        createPopup(text, context, () -> {
        });
    }

    public static void createPopup(String text, Context context, Runnable onUserAcknowledge) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);

        final TextView tv = new TextView(context);
        tv.setText(text);
        tv.setTextColor(Color.RED);
        tv.setTextSize(16);

        // set prompts.xml to alertdialog builder
        alertDialogBuilder.setView(tv);

        // set dialog message
        alertDialogBuilder.setCancelable(false).setPositiveButton("OK", (dialog, id) -> onUserAcknowledge.run());

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    //Handle navigationMenu interactions
    public static void addNavigationMenu(Activity activity, DrawerLayout mDrawerLayout,
                                         NavigationView navigationView, OptionalDependencyManager optionalDependencyManager) {
        installDefaultNavigationMenuHandler(navigationView, mDrawerLayout, activity);
        setUsernameInMenu(navigationView, optionalDependencyManager.get(Username.class));
    }

    public static void installDefaultNavigationMenuHandler(NavigationView navigationView,
                                                           DrawerLayout drawerLayout, Context ctx) {
        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers(); // close the drawer where item was tapped
            Intent intent;

            switch (menuItem.getItemId()) {
                case R.id.menu_createGame:
                    intent = new Intent(ctx, CreateGameActivity.class);
                    break;
                case R.id.menu_findGame:
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(GameListActivity.GAME_LIST_VIEWER_ACTIVITY_CLASS_KEY,
                        GameListActivity.GameListType.FIND_GAME);
                    intent = new Intent(ctx, GameListActivity.class);
                    intent.putExtras(bundle);
                    break;
                case R.id.menu_dice:
                    intent = new Intent(ctx, DiceActivity.class);
                    break;
                default:
                    intent = new Intent(ctx, MyAccountActivity.class);
            }
            ctx.startActivity(intent);
            return true;
        });
    }

    public static void setUsernameInMenu(NavigationView navigationView, Optional<Username> username) {
        TextView usernameTV = navigationView.getHeaderView(0).findViewById(R.id.menu_username);
        View loggedUserPanel = navigationView.getHeaderView(0).findViewById(R.id.menu_logged_user_panel);
        username.executeIfPresent(u -> {
            usernameTV.setText(u.getUsername());
            loggedUserPanel.setVisibility(View.VISIBLE);
        }).executeIfAbsent(() -> loggedUserPanel.setVisibility(View.GONE));
    }

    public static void setMenuInToolbar(AppCompatActivity activity, Toolbar toolbar) {
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
