package com.example.estok;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Adapter.OptionAdapter;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Option extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FirebaseFirestore db;
    private OptionAdapter adapter;
    private List<DocumentSnapshot> optionList = new ArrayList<>();

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

        // Load data from Firestore
        loadOptions();
    }

    private void loadOptions() {
        db.collection("Estock").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    optionList.clear();
                    optionList.addAll(queryDocumentSnapshots.getDocuments());
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }
}