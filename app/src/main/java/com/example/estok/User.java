package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.estok.Helper.SessionManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FirebaseFirestore;

public class User extends AppCompatActivity {

    private TextView username, email;
    private Button signOutBtn;
    private SessionManager sessionManager;
    private CustomPrograssDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.user), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the progress dialog
        dialog = new CustomPrograssDialog(User.this);
        dialog.show();


        // Initialize BottomNavigationView
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set the currently selected item
        bottomNavigationView.setSelectedItemId(R.id.user_btn);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.user_btn) {
                // Stay on the current activity
                return true;
            } else if (itemId == R.id.add_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), Option.class));
                overridePendingTransition(R.anim.slide_right, R.anim.slide_left); // Apply transition animation
                finish(); // Close the current activity
                return true;
            }

            else if (itemId == R.id.home_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), Home.class));
                overridePendingTransition(R.anim.slide_right, R.anim.slide_left); // Apply transition animation
                finish(); // Close the current activity
                return true;
            }

            return false; // Unhandled cases
        });



        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        signOutBtn = findViewById(R.id.signout);

        // Initialize SessionManager
        sessionManager = new SessionManager(this);

        // Load user details from SessionManager
        loadUserDetails();

        signOutBtn.setOnClickListener(v -> {
            // Show confirmation dialog
            new androidx.appcompat.app.AlertDialog.Builder(User.this)
                    .setTitle("Confirm Logout")
                    .setMessage("Are you sure you want to log out?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with logout
                        sessionManager.logoutUser(); // Clear session
                        Toast.makeText(User.this, "Logged out successfully!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(User.this, MainActivity.class); // Redirect to Login
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish(); // Close User activity
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss dialog if the user cancels
                        dialog.dismiss();
                    })
                    .show();
        });


    }

    private void loadUserDetails() {
        String userId = sessionManager.getUserId(); // Ensure `userId` is stored in SessionManager
        if (userId == null) {
            Toast.makeText(this, "User not logged in. Please log in again.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(userId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String userName = documentSnapshot.getString("name");
                        String userEmail = documentSnapshot.getString("email");

                        username.setText(userName != null ? userName : "Unknown User");
                        email.setText(userEmail != null ? userEmail : "No Email Provided");
                        dialog.dismiss();
                    } else {
                        Toast.makeText(this, "User details not found in Firestore.", Toast.LENGTH_SHORT).show();

                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error retrieving user details: " + e.getMessage(), Toast.LENGTH_SHORT).show();

                });
    }
}