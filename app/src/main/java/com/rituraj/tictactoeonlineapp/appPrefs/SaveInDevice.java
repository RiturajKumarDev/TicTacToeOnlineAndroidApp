package com.rituraj.tictactoeonlineapp.appPrefs;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SaveInDevice {
    public static long getData(Activity activity, String storeName) {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String score = prefs.getString(storeName, null);
        if (score == null)
            return 0;
        return Integer.parseInt(score);
    }

    public static void saveData(Activity activity, String storeName, long data) {
        SharedPreferences prefs = activity.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(storeName, String.valueOf(data));
        editor.apply();
    }
}
