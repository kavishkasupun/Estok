package com.example.estok;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class AddUnit extends AppCompatActivity {

    private EditText unitNameField;
    private Button addUnitButton;
    private CustomPrograssDialog progressDialog;
    private FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_unit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.addunit), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        unitNameField = findViewById(R.id.unit_name);
        addUnitButton = findViewById(R.id.add_unit);
        progressDialog = new CustomPrograssDialog(this);

        addUnitButton.setOnClickListener(v -> saveUnitToFirestore());
    }

    private void saveUnitToFirestore() {
        String unitName = unitNameField.getText().toString().trim();

        if (TextUtils.isEmpty(unitName)) {
            unitNameField.setError("Unit name is required");
            return;
        }

        progressDialog.show();
        Map<String, Object> unitData = new HashMap<>();
        unitData.put("name", unitName);

        db.collection("Unit")
                .add(unitData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(AddUnit.this, "Unit added successfully", Toast.LENGTH_SHORT).show();
                    unitNameField.setText(""); // Clear input field
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(AddUnit.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss());
    }
}
