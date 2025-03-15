package com.example.estok;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Adapter.OptionAdapter;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Option extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private OptionAdapter adapter;
    private List<DocumentSnapshot> optionList = new ArrayList<>();
    private CustomPrograssDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_option);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.option), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new OptionAdapter(this, optionList);
        recyclerView.setAdapter(adapter);
        dialog = new CustomPrograssDialog(Option.this);


        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigation);

        // Set the currently selected item
        bottomNavigationView.setSelectedItemId(R.id.add_btn);

        // Set the listener for item selection
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.add_btn) {
                // Stay on the current activity
                return true;
            } else if (itemId == R.id.home_btn) {
                // Navigate to the Module activity
                startActivity(new Intent(getApplicationContext(), Home.class));
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



        // Load data from Firestore
        loadOptions();
    }

    private void loadOptions() {
        dialog.show();
        db.collection("Estock").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    optionList.clear();
                    optionList.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();
                    dialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Option.this, "Error loading options: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    // Handle failure
                });
    }
}