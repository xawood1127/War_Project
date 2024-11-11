package com.zybooks.warproject;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class HomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Find the TextView by ID
        TextView homeTitle = findViewById(R.id.home_title);
        homeTitle.setText("Welcome to the Home Screen");

        // Find the Button by ID
        Button simpleButton = findViewById(R.id.home_button);

        // Set an OnClickListener to handle the button click
        simpleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to open MainActivity
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);  // Start the MainActivity
                finish();  // Optional: Close HomeActivity so it doesn't stay in the back stack
            }
        });
    }
}