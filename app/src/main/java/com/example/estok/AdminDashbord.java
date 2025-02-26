package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.estok.Helper.SessionManager;

public class AdminDashbord extends AppCompatActivity {


    LinearLayout logout, addUnit, addItems, addOptions, mangeOption;
    private SessionManager sessionManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_dashbord);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.admin), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        // Initialize session manager
        sessionManager = new SessionManager(this);

        logout = findViewById(R.id.logout);
        addUnit = findViewById(R.id.add_unit);
        addItems = findViewById(R.id.add_items);
        addOptions = findViewById(R.id.add_option);
        mangeOption = findViewById(R.id.mangeItems);

        // Set click listener for mangeOption
        mangeOption.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashbord.this, ManageItem.class);
            startActivity(intent);
        });

        // Set click listener for addOptions
        addOptions.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashbord.this, AddOption.class);
            startActivity(intent);
        });

        // Set click listener for addItems
        addItems.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashbord.this, AddItem.class);
            startActivity(intent);
        });

        // Set click listener for addUnit
        addUnit.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashbord.this, AddUnit.class);
            startActivity(intent);
        });

        // Set click listener for logout
        logout.setOnClickListener(v -> {
            // Confirm logout with the user
            new androidx.appcompat.app.AlertDialog.Builder(AdminDashbord.this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Perform logout
                        sessionManager.logoutUser(); // Clear session
                        Intent intent = new Intent(AdminDashbord.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Close AdminDashboard activity
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog
                        dialog.dismiss();
                    })
                    .show();
        });



    }
}