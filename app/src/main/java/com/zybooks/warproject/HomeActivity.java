package com.zybooks.warproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    private PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        preferencesManager = new PreferencesManager(this);

        // Find buttons
        Button startButton = findViewById(R.id.new_game_button);
        Button continueButton = findViewById(R.id.continue_button);

        // Show the "Continue Game" button if a saved game exists
        if (preferencesManager.hasSavedGame()) {
            continueButton.setVisibility(View.VISIBLE);
        }

        // Start new game
        startButton.setOnClickListener(v -> {
            preferencesManager.clearSavedGame();
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });

        // Continue existing game
        continueButton.setOnClickListener(v -> {
            startActivity(new Intent(HomeActivity.this, MainActivity.class));
            finish();
        });
    }
}
