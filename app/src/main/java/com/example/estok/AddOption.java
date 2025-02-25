package com.example.estok;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class AddOption extends AppCompatActivity {

    private EditText optionNameField;
    private Button addOptionButton;
    private FirebaseFirestore db;
    private CustomPrograssDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_option);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.AddOption), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = FirebaseFirestore.getInstance();
        optionNameField = findViewById(R.id.option_name);
        addOptionButton = findViewById(R.id.add_option);
        progressDialog = new CustomPrograssDialog(this);

        addOptionButton.setOnClickListener(v -> addOptionToFirestore());
    }

    private void addOptionToFirestore() {
        String optionName = optionNameField.getText().toString().trim();

        if (TextUtils.isEmpty(optionName)) {
            optionNameField.setError("Option name is required");
            return;
        }

        progressDialog.show();
        Map<String, Object> optionData = new HashMap<>();

        db.collection("Estock").document(optionName)
                .set(optionData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Option added successfully!", Toast.LENGTH_SHORT).show();
                    optionNameField.setText("");
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error adding option: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                })
                .addOnCompleteListener(task -> progressDialog.dismiss());
    }
}
