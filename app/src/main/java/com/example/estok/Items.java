package com.example.estok;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Items extends AppCompatActivity {

    private Spinner spinnerItem;
    private TextView unitTextView;
    private EditText quantityEditText, addNameEditText, addNumberEditText, addJobidEditText;
    private Button addOrderButton;
    private FirebaseFirestore db;
    private String selectedOption;
    private List<String> itemList = new ArrayList<>();
    private CustomPrograssDialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_items);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.item), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI components
        spinnerItem = findViewById(R.id.spinner_item);
        unitTextView = findViewById(R.id.unit);
        quantityEditText = findViewById(R.id.quantity);
        addNameEditText = findViewById(R.id.addName);
        addNumberEditText = findViewById(R.id.addNumber);
        addJobidEditText = findViewById(R.id.addJobid);
        addOrderButton = findViewById(R.id.add_order);
        dialog = new CustomPrograssDialog(Items.this);
        db = FirebaseFirestore.getInstance();

        // Get the selected option (document name) from the intent
        selectedOption = getIntent().getStringExtra("optionName");

        if (selectedOption != null) {
            loadItems();
        } else {
            Toast.makeText(this, "No option selected", Toast.LENGTH_SHORT).show();
        }

        addOrderButton.setOnClickListener(v -> validateAndSaveOrder());
    }

    private void loadItems() {
        dialog.show();
        db.collection("Estock").document(selectedOption).collection("ManagedItems")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    itemList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String itemName = document.getString("itemName");
                        if (itemName != null) {
                            itemList.add(itemName);
                        }
                    }
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemList);
                    spinnerItem.setAdapter(adapter);

                    if (!itemList.isEmpty()) {
                        loadUnitForItem(itemList.get(0)); // Load unit for the first item
                    }
                })
                .addOnCompleteListener(task -> dialog.dismiss())
                .addOnFailureListener(e -> Toast.makeText(Items.this, "Failed to load items", Toast.LENGTH_SHORT).show());
    }

    private void loadUnitForItem(String itemName) {
        dialog.show();
        db.collection("Items")
                .whereEqualTo("name", itemName)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String unit = document.getString("unit");
                        if (unit != null) {
                            unitTextView.setText(unit);
                            return;
                        }
                    }
                })
                .addOnCompleteListener(task -> dialog.dismiss())
                .addOnFailureListener(e -> Toast.makeText(Items.this, "Failed to load unit", Toast.LENGTH_SHORT).show());
    }

    private void validateAndSaveOrder() {
        String selectedItem = spinnerItem.getSelectedItem() != null ? spinnerItem.getSelectedItem().toString() : "";
        String quantityStr = quantityEditText.getText().toString().trim();
        String addName = addNameEditText.getText().toString().trim();
        String addNumber = addNumberEditText.getText().toString().trim();
        String addJobid = addJobidEditText.getText().toString().trim();
        String unitValue = unitTextView.getText().toString(); // Get the unit value from the TextView

        if (selectedItem.isEmpty() || quantityStr.isEmpty() || addName.isEmpty() || addNumber.isEmpty() || addJobid.isEmpty() || unitValue.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantity = Integer.parseInt(quantityStr);

        db.collection("Items").whereEqualTo("name", selectedItem).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        // Retrieve the count as a Long (or Integer) instead of a String
                        Long availableStockLong = document.getLong("count");
                        int availableStock = availableStockLong != null ? availableStockLong.intValue() : 0;

                        if (quantity > availableStock) {
                            Toast.makeText(Items.this, "Out of stock: " + selectedItem, Toast.LENGTH_SHORT).show();
                        } else {
                            saveOrder(selectedItem, unitValue, quantity, addName, addNumber, addJobid, selectedOption);
                        }
                        return;
                    }
                    Toast.makeText(Items.this, "Item not found", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> Toast.makeText(Items.this, "Failed to check stock", Toast.LENGTH_SHORT).show());
    }

    private void saveOrder(String itemName, String unit, int quantity, String addName, String addNumber, String addJobid, String selectedOption) {
        Map<String, Object> orderData = new HashMap<>();
        orderData.put("itemName", itemName);
        orderData.put("unit", unit); // Save the unit value in Firestore
        orderData.put("quantity", quantity);
        orderData.put("addName", addName);
        orderData.put("addNumber", addNumber);
        orderData.put("addJobid", addJobid);
        orderData.put("Option", selectedOption); // Save selected option (document name)
        orderData.put("time", Timestamp.now());

        // Save order data to Firestore
        dialog.show();
        db.collection("OrderItem").add(orderData)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        dialog.dismiss();
                        Toast.makeText(Items.this, "Order saved successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        dialog.dismiss();
                        Toast.makeText(Items.this, "Failed to save order", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}