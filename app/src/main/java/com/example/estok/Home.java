package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Home extends AppCompatActivity {

    LinearLayout ItemView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.home), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ItemView = findViewById(R.id.ItemView);

        // Set a click listener for the LinearLayout
        ItemView.setOnClickListener(v -> {
            Intent intent = new Intent(Home.this, Option.class);
            startActivity(intent);
        });

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set the currently selected item
        bottomNavigationView.setSelectedItemId(R.id.home_btn);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.home_btn) {
                // Stay on the current activity
                return true;
            } else if (itemId == R.id.add_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), Option.class));
                overridePendingTransition(R.anim.slide_right, R.anim.slide_left); // Apply transition animation
                finish(); // Close the current activity
                return true;
            }

            else if (itemId == R.id.view_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), View.class));
                overridePendingTransition(R.anim.slide_right, R.anim.slide_left); // Apply transition animation
                finish(); // Close the current activity
                return true;
            }

            else if (itemId == R.id.user_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), User.class));
                overridePendingTransition(R.anim.slide_right, R.anim.slide_left); // Apply transition animation
                finish(); // Close the current activity
                return true;
            }

            return false; // Unhandled cases
        });
    }
}