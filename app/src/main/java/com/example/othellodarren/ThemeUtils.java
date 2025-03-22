package com.example.othellodarren;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import androidx.core.content.ContextCompat;

public class ThemeUtils {
    private static final String PREFS_NAME = "theme_prefs";
    private static final String KEY_THEME = "selected_theme";

    public static void applyTheme(Activity activity) {
        SharedPreferences prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String themeName = prefs.getString(KEY_THEME, "Green");

        int themeResId;
        int boardBackgroundResId;

        switch (themeName) {
            case "BlueGray":
                themeResId = R.style.Theme_OthelloDarren_BlueGray;
                boardBackgroundResId = R.color.board_background_blue_gray;
                break;
            case "Dark":
                themeResId = R.style.Theme_OthelloDarren_Dark;
                boardBackgroundResId = R.color.board_background_dark;
                break;
            case "Earth":
                themeResId = R.style.Theme_OthelloDarren_Earth;
                boardBackgroundResId = R.color.board_background_earth;
                break;
            default:
                themeResId = R.style.Theme_OthelloDarren_Green;
                boardBackgroundResId = R.color.board_background_green;
                break;
        }


        activity.setTheme(themeResId);


        View rootView = activity.findViewById(android.R.id.content);
        if (rootView != null) {
            rootView.setBackgroundColor(ContextCompat.getColor(activity, boardBackgroundResId));
        }
    }

    public static void setTheme(Context context, String themeName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_THEME, themeName);
        editor.apply();
    }
}
