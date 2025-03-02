package com.example.estok;

import android.Manifest;
import android.content.ContentValues;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class ViewOrder extends AppCompatActivity {

    private RecyclerView recyclerView;
    private OrderAdapter adapter;
    private List<OrderItem> orderItemList;
    private FirebaseFirestore db;
    private CustomPrograssDialog progressDialog;
    private static final int STORAGE_PERMISSION_CODE = 100;
    private File excelFile;
    private static final String FILE_NAME = "Orders.xlsx";
    private Button btnExportExcel;
    private Uri fileUri = null;


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

        // Request permission to write to storage
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    STORAGE_PERMISSION_CODE);
        }

        // Define Excel file location
        excelFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);

        // Initialize Export Button
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportExcel.setOnClickListener(v -> exportToExcel());

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
                            String itemName = document.getString("itemName");
                            String option = document.getString("Option");
                            int quantity = document.getLong("quantity").intValue();
                            String unit = document.getString("unit");
                            String addNumber = document.getString("addNumber");
                            String addJobid = document.getString("addJobid");
                            Timestamp time = document.getTimestamp("time");
                            String addName = document.getString("addName");
                            String orderId = document.getId();

                            OrderItem orderItem = new OrderItem(itemName, option, quantity, unit, addNumber, addJobid, time, addName);
                            orderItem.setOrderId(orderId);
                            orderItemList.add(orderItem);
                        }
                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(ViewOrder.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void exportToExcel() {
        db.collection("OrderItem").get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null) {
                List<OrderItem> orderList = new ArrayList<>();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String itemName = document.getString("itemName");
                    String option = document.getString("Option");
                    int quantity = document.getLong("quantity").intValue();
                    String unit = document.getString("unit");
                    String addNumber = document.getString("addNumber");
                    String addJobid = document.getString("addJobid");
                    Timestamp time = document.getTimestamp("time");
                    String addName = document.getString("addName");

                    orderList.add(new OrderItem(itemName, option, quantity, unit, addNumber, addJobid, time, addName));
                }
                updateExcelFile(orderList);
            } else {
                Toast.makeText(ViewOrder.this, "Failed to load orders.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateExcelFile(List<OrderItem> orderList) {
        Workbook workbook;
        Sheet sheet;
        boolean isNewFile = false;

        // Handle file storage based on Android version
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            fileUri = getFileUri();
        } else {
            File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), FILE_NAME);
            fileUri = Uri.fromFile(file);
        }

        try (InputStream fis = (fileUri != null) ? getContentResolver().openInputStream(fileUri) : null) {
            if (fis != null) {
                workbook = new XSSFWorkbook(fis);
                sheet = workbook.getSheet("Orders");
            } else {
                isNewFile = true;
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Orders");
            }
        } catch (IOException e) {
            isNewFile = true;
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Orders");
        }

        // Read existing timestamps to prevent duplicates
        Set<String> existingTimestamps = new HashSet<>();
        if (!isNewFile && sheet != null) {
            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // Skip header
                Cell timeCell = row.getCell(6);
                if (timeCell != null) {
                    existingTimestamps.add(timeCell.getStringCellValue().trim());
                }
            }
        }

        // Create header row if it's a new file
        if (isNewFile) {
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Item Name", "Option", "Quantity", "Unit", "User Number", "Job ID", "Time", "Submitted By"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                CellStyle style = workbook.createCellStyle();
                style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
                style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
                cell.setCellStyle(style);
            }
        }

        // Append new data if not already in the sheet
        int lastRowNum = sheet.getLastRowNum();
        for (OrderItem order : orderList) {
            String orderTime = order.getTime().toDate().toString().trim();
            if (!existingTimestamps.contains(orderTime)) {
                Row row = sheet.createRow(++lastRowNum);
                row.createCell(0).setCellValue(order.getItemName());
                row.createCell(1).setCellValue(order.getOptionName());
                row.createCell(2).setCellValue(order.getQuantity());
                row.createCell(3).setCellValue(order.getUnit());
                row.createCell(4).setCellValue(order.getUserNumber());
                row.createCell(5).setCellValue(order.getJobId());
                row.createCell(6).setCellValue(orderTime);
                row.createCell(7).setCellValue(order.getSubmitBy());
            }
        }

        // Save the updated file
        try (OutputStream fos = (fileUri != null) ? getContentResolver().openOutputStream(fileUri, "wt") : null) {
            if (fos != null) {
                workbook.write(fos);
                workbook.close();
                Toast.makeText(this, "Excel updated successfully!", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Failed to update Excel file.", Toast.LENGTH_SHORT).show();
        }
    }

    private Uri getFileUri() {
        Uri collection = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?";
        String[] selectionArgs = new String[]{FILE_NAME};

        try (Cursor cursor = getContentResolver().query(collection, null, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                if (idColumn != -1) {
                    long fileId = cursor.getLong(idColumn);
                    return Uri.withAppendedPath(collection, String.valueOf(fileId));
                }
            }
        }

        // If file does not exist, create a new one
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        return getContentResolver().insert(collection, values);
    }

    // Handle permission result
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (!(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                Toast.makeText(this, "Storage permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
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