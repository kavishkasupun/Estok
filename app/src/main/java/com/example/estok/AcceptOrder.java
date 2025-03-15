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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.estok.Adapter.AcceptOrderAdapter;
import com.example.estok.Model.AcceptOrderItem;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.apache.poi.ss.usermodel.Cell;
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
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class AcceptOrder extends AppCompatActivity {

    private RecyclerView recyclerView;
    private AcceptOrderAdapter adapter;
    private List<AcceptOrderItem> acceptOrderList;
    private FirebaseFirestore db;
    private CustomPrograssDialog progressDialog;
    private Button btnExportExcel;
    private static final String FILE_NAME = "AcceptOrders.xlsx";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_accept_order);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.acceptOrder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerView = findViewById(R.id.accept_recycleview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        acceptOrderList = new ArrayList<>();
        adapter = new AcceptOrderAdapter(acceptOrderList);
        recyclerView.setAdapter(adapter);
        progressDialog = new CustomPrograssDialog(this);

        db = FirebaseFirestore.getInstance();

        // Initialize Export button
        btnExportExcel = findViewById(R.id.btn_export_excel);
        btnExportExcel.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                } else {
                    generateExcelFile();
                }
            } else {
                generateExcelFile(); // No permission required for Android 10+
            }
        });

        // Fetch data from Firestore
        fetchAcceptOrders();
    }

    private void fetchAcceptOrders() {
        progressDialog.show();
        db.collection("AcceptOrder")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        acceptOrderList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String itemName = document.getString("itemName");
                            String optionName = document.getString("optionName");
                            int quantity = document.getLong("quantity").intValue();
                            String unit = document.getString("unit");
                            String orderId = document.getString("OrderId");
                            String userNumber = document.getString("userNumber");
                            String jobId = document.getString("jobId");
                            Timestamp formattedTime = document.getTimestamp("time");
                            String submitBy = document.getString("submitBy");

                            AcceptOrderItem item = new AcceptOrderItem(itemName, optionName, quantity, unit, orderId, userNumber, jobId, formattedTime, submitBy);
                            acceptOrderList.add(item);
                        }

                        // Sort the list to prioritize today's date
                        sortAcceptOrdersByDate();

                        progressDialog.dismiss();
                        adapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(this, "Failed to load orders: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void sortAcceptOrdersByDate() {
        // Get today's date
        long today = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
        String todayDate = sdf.format(today);

        // Sort the list
        acceptOrderList.sort((item1, item2) -> {
            if (item1.getFormattedTime() == null || item2.getFormattedTime() == null) {
                return 0; // If either timestamp is null, don't change the order
            }

            // Compare dates
            String date1 = sdf.format(item1.getFormattedTime().toDate());
            String date2 = sdf.format(item2.getFormattedTime().toDate());

            if (date1.equals(todayDate) && !date2.equals(todayDate)) {
                return -1; // Today's date comes first
            } else if (!date1.equals(todayDate) && date2.equals(todayDate)) {
                return 1; // Today's date comes first
            } else {
                // If both are today's date or not today's date, sort by time in descending order
                return Long.compare(item2.getFormattedTime().getSeconds(), item1.getFormattedTime().getSeconds());
            }
        });
    }

    private void generateExcelFile() {
        Workbook workbook;
        Sheet sheet;
        boolean isNewFile = false;
        Uri fileUri = null;
        File file = null;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Uri collection = MediaStore.Files.getContentUri("external");

                // Check if the file already exists
                String selection = MediaStore.MediaColumns.DISPLAY_NAME + "=?";
                String[] selectionArgs = new String[]{FILE_NAME};

                try (Cursor cursor = getContentResolver().query(collection, null, selection, selectionArgs, null)) {
                    if (cursor != null && cursor.moveToFirst()) {
                        int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                        if (idColumn != -1) {
                            long fileId = cursor.getLong(idColumn);
                            fileUri = Uri.withAppendedPath(collection, String.valueOf(fileId));
                        }
                    }
                }

                if (fileUri != null) {
                    // Load existing file
                    try (InputStream inputStream = getContentResolver().openInputStream(fileUri)) {
                        workbook = new XSSFWorkbook(inputStream);
                        sheet = workbook.getSheetAt(0);
                    }
                } else {
                    // Create new file
                    isNewFile = true;
                    ContentValues values = new ContentValues();
                    values.put(MediaStore.MediaColumns.DISPLAY_NAME, FILE_NAME);
                    values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                    fileUri = getContentResolver().insert(collection, values);
                    if (fileUri == null) throw new IOException("Failed to create file");

                    workbook = new XSSFWorkbook();
                    sheet = workbook.createSheet("Accept Orders");
                }
            } else {
                // Android 9 and below
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                file = new File(downloadsDir, FILE_NAME);

                if (file.exists()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        workbook = new XSSFWorkbook(fis);
                        sheet = workbook.getSheetAt(0);
                    }
                } else {
                    isNewFile = true;
                    workbook = new XSSFWorkbook();
                    sheet = workbook.createSheet("Accept Orders");
                }
            }

            if (isNewFile) {
                // Create Header Row
                Row headerRow = sheet.createRow(0);
                String[] headers = {"Item Name", "Option Name", "Quantity", "Unit", "Order ID", "User Number", "Job ID", "Accept Time", "Submitted By"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                }
            }

            // Get existing Order IDs and Times to avoid duplicates
            Set<String> existingOrderIds = new HashSet<>();
            Set<String> existingTimes = new HashSet<>();
            for (int i = 1; i <= sheet.getLastRowNum(); i++) { // Start from row 1 (skip header)
                Row row = sheet.getRow(i);
                if (row != null) {
                    Cell orderIdCell = row.getCell(4); // Column index 4 = Order ID
                    Cell timeCell = row.getCell(7); // Column index 7 = Time
                    if (orderIdCell != null && timeCell != null) {
                        existingOrderIds.add(orderIdCell.getStringCellValue().trim());
                        existingTimes.add(timeCell.getStringCellValue().trim());
                    }
                }
            }

            // Filter out duplicates
            List<AcceptOrderItem> newEntries = new ArrayList<>();
            for (AcceptOrderItem item : acceptOrderList) {
                String orderId = item.getOrderId();
                String time = item.getFormattedTime() != null ? item.getFormattedTime().toDate().toString() : "N/A";
                if (!existingOrderIds.contains(orderId) && !existingTimes.contains(time)) {
                    newEntries.add(item);
                }
            }

            if (newEntries.isEmpty()) {
                Toast.makeText(this, "No new data to update.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Append new data
            int rowIndex = sheet.getLastRowNum() + 1;
            for (AcceptOrderItem item : newEntries) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(item.getItemName());
                row.createCell(1).setCellValue(item.getOptionName());
                row.createCell(2).setCellValue(item.getQuantity());
                row.createCell(3).setCellValue(item.getUnit());
                row.createCell(4).setCellValue(item.getOrderId());
                row.createCell(5).setCellValue(item.getUserNumber());
                row.createCell(6).setCellValue(item.getJobId());
                row.createCell(7).setCellValue(item.getFormattedTime() != null ? item.getFormattedTime().toDate().toString() : "N/A");
                row.createCell(8).setCellValue(item.getSubmitBy());
            }

            // Save the file
            try (OutputStream fos = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ?
                    getContentResolver().openOutputStream(fileUri, "wt") : new FileOutputStream(file)) {
                workbook.write(fos);
            }

            workbook.close();
            Toast.makeText(this, "Excel file updated successfully!", Toast.LENGTH_LONG).show();

        } catch (IOException e) {
            Toast.makeText(this, "Error updating file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}