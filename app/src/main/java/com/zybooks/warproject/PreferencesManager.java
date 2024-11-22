package com.zybooks.warproject;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private SharedPreferences preferences;

    public PreferencesManager(Context context) {
        preferences = context.getSharedPreferences("GamePreferences", Context.MODE_PRIVATE);
    }

    public boolean hasSavedGame() {
        return preferences.contains("topDeck") && preferences.contains("bottomDeck");
    }

    public void clearSavedGame() {
        preferences.edit().clear().apply();
    }
}

