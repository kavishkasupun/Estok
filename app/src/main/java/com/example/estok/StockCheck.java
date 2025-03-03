package com.example.estok;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Adapter.StockAdapter;
import com.example.estok.Model.StockItem;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StockCheck extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StockAdapter adapter;
    private List<StockItem> stockItemList;
    private FirebaseFirestore db;
    private CustomPrograssDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_check);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.StockCheck), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.accept_recycleview);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        stockItemList = new ArrayList<>();
        adapter = new StockAdapter(this, stockItemList);
        recyclerView.setAdapter(adapter);
        progressDialog = new CustomPrograssDialog(this);

        db = FirebaseFirestore.getInstance();

        // Fetch data from Firestore
        fetchStockItems();
    }

    private void fetchStockItems() {
        progressDialog.show();
        db.collection("Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        stockItemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String name = document.getString("name");
                            String unit = document.getString("unit");
                            String documentId = document.getId();

                            // Handle the "count" field
                            int count = 0; // Default value
                            Object countValue = document.get("count");
                            if (countValue instanceof Number) {
                                // If "count" is a Number, cast it to int
                                count = ((Number) countValue).intValue();
                            } else if (countValue instanceof String) {
                                // If "count" is a String, try to parse it as an integer
                                try {
                                    count = Integer.parseInt((String) countValue);
                                } catch (NumberFormatException e) {
                                    // Handle the case where "count" is not a valid integer
                                    Toast.makeText(StockCheck.this, "Invalid count value for item: " + name, Toast.LENGTH_SHORT).show();
                                    continue; // Skip this document
                                }
                            } else {
                                // Handle the case where "count" is not a Number or String
                                Toast.makeText(StockCheck.this, "Invalid count type for item: " + name, Toast.LENGTH_SHORT).show();
                                continue; // Skip this document
                            }

                            StockItem item = new StockItem(name, count, unit);
                            item.setDocumentId(documentId);
                            stockItemList.add(item);
                        }
                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(StockCheck.this, "Failed to load items: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}