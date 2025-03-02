package com.example.estok;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SplashScreen extends AppCompatActivity {

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_splash_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.splash), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        handler = new Handler();
        handler.postDelayed(() -> {
            // Check login state and admin status
            if (isLoggedIn()) {
                if (isAdmin()) {
                    // If the user is an admin, redirect to AdminMenu
                    startActivity(new Intent(SplashScreen.this, AdminDashbord.class));
                } else {
                    // If the user is not an admin, redirect to HomeActivity
                    startActivity(new Intent(SplashScreen.this, Home.class));
                }
            } else {
                // If user is not logged in, redirect to MainActivity
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            }
            finish();
        }, 2000);
    }

    private boolean isLoggedIn() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        return preferences.getBoolean("isLoggedIn", false);
    }

    private boolean isAdmin() {
        // Get the user's email from SharedPreferences
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String userEmail = preferences.getString("user_email", "");

        // Define the admin email
        String adminEmail = "admin@email.com";

        // Check if the user's email matches the admin email
        return userEmail.equals(adminEmail);
    }
}