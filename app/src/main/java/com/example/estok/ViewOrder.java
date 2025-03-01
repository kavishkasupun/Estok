package com.example.estok;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Adapter.OrderAdapter;
import com.example.estok.Model.OrderItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ViewOrder extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<OrderItem> orderItemList;
    private FirebaseFirestore db;
    private CustomPrograssDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.ViewOrder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.submitrecycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderItemList = new ArrayList<>();
        adapter = new OrderAdapter(orderItemList, this);
        recyclerView.setAdapter(adapter);
        progressDialog = new CustomPrograssDialog(this);

        db = FirebaseFirestore.getInstance();

        loadOrders();
    }

    private void loadOrders() {
        progressDialog.show();
        db.collection("OrderItem")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        orderItemList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            // Manually map Firestore fields to OrderItem
                            String itemName = document.getString("itemName");
                            String option = document.getString("Option");
                            int quantity = document.getLong("quantity").intValue();
                            String unit = document.getString("unit");
                            String addNumber = document.getString("addNumber");
                            String addJobid = document.getString("addJobid");
                            Timestamp time = document.getTimestamp("time");
                            String addName = document.getString("addName");
                            String orderId = document.getId(); // Get the unique document ID

                            // Create an OrderItem object
                            OrderItem orderItem = new OrderItem(itemName, option, quantity, unit, addNumber, addJobid, time, addName);
                            orderItem.setOrderId(orderId); // Set the unique document ID
                            orderItemList.add(orderItem);
                        }
                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewOrder.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void onAcceptButtonClick(OrderItem orderItem, int position) {
        if (orderItem.isProcessed()) {
            Toast.makeText(this, "This order has already been processed.", Toast.LENGTH_SHORT).show();
            return;
        }


        // Check if OrderId already exists in AcceptOrder
        db.collection("AcceptOrder")
                .whereEqualTo("OrderId", orderItem.getOrderId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        if (!task.getResult().isEmpty()) {
                            Toast.makeText(ViewOrder.this, "This order has already been accepted.", Toast.LENGTH_SHORT).show();
                            return;
                        }


                        // Proceed with checking stock and accepting the order
                        db.collection("Items")
                                .whereEqualTo("name", orderItem.getItemName())
                                .get()
                                .addOnCompleteListener(itemsTask -> {
                                    if (itemsTask.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : itemsTask.getResult()) {
                                            // Check if the "count" field is a Number
                                            Object countValue = document.get("count");
                                            int currentCount = 0;

                                            if (countValue instanceof Number) {
                                                currentCount = ((Number) countValue).intValue();
                                            } else if (countValue instanceof String) {
                                                // If "count" is stored as a String, parse it to an integer
                                                try {
                                                    currentCount = Integer.parseInt((String) countValue);
                                                } catch (NumberFormatException e) {
                                                    Toast.makeText(ViewOrder.this, "Invalid count value in Firestore.", Toast.LENGTH_SHORT).show();
                                                    return;
                                                }
                                            } else {
                                                Toast.makeText(ViewOrder.this, "Invalid count value in Firestore.", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            int newCount = currentCount - orderItem.getQuantity();

                                            if (newCount >= 0) {
                                                db.collection("Items").document(document.getId())
                                                        .update("count", newCount)
                                                        .addOnSuccessListener(aVoid -> {
                                                            // Save the order details to the AcceptOrder collection
                                                            saveToAcceptOrder(orderItem);

                                                            // Mark the order as processed
                                                            orderItem.setProcessed(true);
                                                            adapter.notifyItemChanged(position); // Notify adapter to update the view
                                                            Toast.makeText(ViewOrder.this, "Order accepted and stock updated.", Toast.LENGTH_SHORT).show();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(ViewOrder.this, "Failed to update stock.", Toast.LENGTH_SHORT).show());
                                            } else {
                                                Toast.makeText(ViewOrder.this, "Not enough stock.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    } else {
                                        Toast.makeText(ViewOrder.this, "Failed to find item.", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    } else {
                        Toast.makeText(ViewOrder.this, "Failed to check for duplicate OrderId.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Method to save the order details to the AcceptOrder collection
    private void saveToAcceptOrder(OrderItem orderItem) {
        // Get the current date and time
        Timestamp currentTime = Timestamp.now();

        progressDialog.show();

        // Create a new document in the AcceptOrder collection
        db.collection("AcceptOrder")
                .add(new OrderItem(
                        orderItem.getItemName(),
                        orderItem.getOptionName(),
                        orderItem.getQuantity(),
                        orderItem.getUnit(),
                        orderItem.getUserNumber(),
                        orderItem.getJobId(),
                        currentTime, // Use the current time
                        orderItem.getSubmitBy()
                ))
                .addOnSuccessListener(documentReference -> {
                    // Update the OrderId in the AcceptOrder document
                    db.collection("AcceptOrder").document(documentReference.getId())
                            .update("OrderId", orderItem.getOrderId())
                            .addOnSuccessListener(aVoid -> {

                                // Update the formattedTime in the OrderItem table
                                db.collection("OrderItem").document(orderItem.getOrderId())
                                        .update("AcceptOrderTime", currentTime.toDate().toString())
                                        .addOnSuccessListener(aVoid1 -> {
                                            Toast.makeText(ViewOrder.this, "Order saved to AcceptOrder and time updated in OrderItem.", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                        })

                                        .addOnFailureListener(e -> {
                                            Toast.makeText(ViewOrder.this, "Failed to update formattedTime in OrderItem.", Toast.LENGTH_SHORT).show();
                                        });
                            })
                            .addOnFailureListener(e -> {
                                Toast.makeText(ViewOrder.this, "Failed to update OrderId in AcceptOrder.", Toast.LENGTH_SHORT).show();
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(ViewOrder.this, "Failed to save order to AcceptOrder.", Toast.LENGTH_SHORT).show();
                });
    }

    public void onRejectButtonClick(int position) {
        OrderItem orderItem = orderItemList.get(position);
        if (orderItem.isProcessed()) {
            Toast.makeText(this, "This order has already been processed.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Mark the order as processed
        orderItem.setProcessed(true);
        adapter.notifyItemChanged(position); // Notify adapter to update the view
        Toast.makeText(ViewOrder.this, "Order rejected.", Toast.LENGTH_SHORT).show();
    }
}