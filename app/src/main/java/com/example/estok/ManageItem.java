package com.example.estok;

import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ManageItem extends AppCompatActivity {


    private Spinner spinnerOption, spinnerItem;
    private Button btnManageItem;
    private FirebaseFirestore db;
    private List<String> optionList = new ArrayList<>();
    private List<String> itemList = new ArrayList<>();
    private CustomPrograssDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_manage_item);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mangeItems), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });




        // Initialize UI components
        spinnerOption = findViewById(R.id.spinner_option);
        spinnerItem = findViewById(R.id.spinner_Item);
        btnManageItem = findViewById(R.id.btn_manageItem);
        db = FirebaseFirestore.getInstance();
        progressDialog = new CustomPrograssDialog(this);

        // Load data into spinners
        loadOptions();
        loadItems();

        // Handle button click
        btnManageItem.setOnClickListener(v -> saveSelectedData());
    }

    private void loadOptions() {
        db.collection("Estock")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        optionList.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            optionList.add(document.getId()); // Retrieves document name
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, optionList);
                        spinnerOption.setAdapter(adapter);
                    } else {
                        Toast.makeText(ManageItem.this, "Failed to load options", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadItems() {
        db.collection("Items")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        itemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            itemList.add(document.getString("name")); // Retrieves Name column
                        }
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, itemList);
                        spinnerItem.setAdapter(adapter);
                    } else {
                        Toast.makeText(ManageItem.this, "Failed to load items", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveSelectedData() {
        String selectedOption = spinnerOption.getSelectedItem() != null ? spinnerOption.getSelectedItem().toString() : "";
        String selectedItem = spinnerItem.getSelectedItem() != null ? spinnerItem.getSelectedItem().toString() : "";

        if (selectedOption.isEmpty() || selectedItem.isEmpty()) {
            Toast.makeText(this, "Please select valid options", Toast.LENGTH_SHORT).show();
            return;
        }
        progressDialog.show();

        // Create a map to store data
        Map<String, Object> data = new HashMap<>();
        data.put("itemName", selectedItem);

        // Save to Firestore under the selected option path
        db.collection("Estock").document(selectedOption).collection("ManagedItems").add(data)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(this, "Add Successful!", Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss())
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to add data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
