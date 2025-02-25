package com.example.estok;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddItem extends AppCompatActivity {

    private EditText itemNameField, itemCountField;
    private Spinner unitSpinner;
    private Button addItemButton;
    private FirebaseFirestore db;
    private List<String> unitList;
    private CustomPrograssDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.additem), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        itemNameField = findViewById(R.id.item_name);
        itemCountField = findViewById(R.id.item_count);
        unitSpinner = findViewById(R.id.spinner_unit);
        addItemButton = findViewById(R.id.add_items);
        progressDialog = new CustomPrograssDialog(this);

        unitList = new ArrayList<>();
        loadUnits();

        addItemButton.setOnClickListener(v -> saveItemToFirestore());
    }

    private void loadUnits() {
        db.collection("Unit").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String unitName = document.getString("name");
                    if (unitName != null) {
                        unitList.add(unitName);
                    }
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, unitList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                unitSpinner.setAdapter(adapter);
            } else {
                Toast.makeText(this, "Error loading units", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveItemToFirestore() {
        String itemName = itemNameField.getText().toString().trim();
        String itemCount = itemCountField.getText().toString().trim();
        String selectedUnit = unitSpinner.getSelectedItem().toString();

        if (TextUtils.isEmpty(itemName) || TextUtils.isEmpty(itemCount)) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.show();

        Map<String, Object> itemData = new HashMap<>();
        itemData.put("name", itemName);
        itemData.put("count", itemCount);
        itemData.put("unit", selectedUnit);

        db.collection("Items")
                .add(itemData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Item added successfully!", Toast.LENGTH_SHORT).show();
                    itemNameField.setText("");
                    itemCountField.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss());
    }
}
