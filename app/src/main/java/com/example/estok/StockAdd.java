package com.example.estok;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.FirebaseFirestore;

public class StockAdd extends AppCompatActivity {


    private TextView itemNameTextView, unitTextView;
    private EditText quantityEditText;
    private FirebaseFirestore db;
    private String documentId;
    private CustomPrograssDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stock_add);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.StockAdd), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        itemNameTextView = findViewById(R.id.ItemName);
        unitTextView = findViewById(R.id.unit);
        quantityEditText = findViewById(R.id.quantity);
        progressDialog = new CustomPrograssDialog(this);

        db = FirebaseFirestore.getInstance();

        // Retrieve data from the intent
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String itemName = extras.getString("itemName");
            int itemCount = extras.getInt("itemCount");
            String itemUnit = extras.getString("itemUnit");
            documentId = extras.getString("documentId"); // Retrieve the document ID

            // Populate the TextViews
            itemNameTextView.setText(itemName);
            unitTextView.setText(itemUnit);
            quantityEditText.setText(String.valueOf(itemCount));
        }

        // Handle the "Add Stock" button click
        findViewById(R.id.add_stock).setOnClickListener(v -> updateStock());

        // Handle the "Delete Stock" button click
        findViewById(R.id.delete_stock).setOnClickListener(v -> deleteStock());
    }

    private void updateStock() {
        String quantityStr = quantityEditText.getText().toString().trim();

        if (quantityStr.isEmpty()) {
            Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
            return;
        }

        int newQuantity = Integer.parseInt(quantityStr);
        progressDialog.show();

        // Update the Firestore document
        db.collection("Items").document(documentId)
                .update("count", newQuantity)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Stock updated successfully!", Toast.LENGTH_SHORT).show();
                    finish(); // Close the activity and return to StockCheck
                    progressDialog.dismiss();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to update stock: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void deleteStock() {
        // Show a confirmation dialog before deletion
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + itemNameTextView.getText() + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        progressDialog.show();
                        // Delete the Firestore document
                        db.collection("Items").document(documentId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(StockAdd.this, "Item deleted successfully!", Toast.LENGTH_SHORT).show();
                                    finish(); // Close the activity and return to StockCheck
                                    progressDialog.dismiss();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(StockAdd.this, "Failed to delete item: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }
}