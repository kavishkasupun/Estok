package com.example.estok.Model;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class OrderItem {

    private String itemName;
    private String Option; // This matches the Firestore field
    private int quantity;
    private String unit;
    private String addNumber; // Updated to match Firestore
    private String addJobid;  // Updated to match Firestore
    private Timestamp time;   // This matches the Firestore field
    private String addName;   // Updated to match Firestore
    private boolean isProcessed; // Track if the order has been processed (accepted or rejected)
    private String orderId; // Unique document ID for the order

    // Default constructor required for Firestore
    public OrderItem() {
    }

    public OrderItem(String itemName, String Option, int quantity, String unit, String addNumber, String addJobid, Timestamp time, String addName) {
        this.itemName = itemName;
        this.Option = Option;
        this.quantity = quantity;
        this.unit = unit;
        this.addNumber = addNumber;
        this.addJobid = addJobid;
        this.time = time;
        this.addName = addName;
        this.isProcessed = false; // Default to false
    }

    // Getters and Setters
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public String getOptionName() { return Option; }
    public void setOptionName(String Option) { this.Option = Option; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getUserNumber() { return addNumber; }
    public void setUserNumber(String addNumber) { this.addNumber = addNumber; }

    public String getJobId() { return addJobid; }
    public void setJobId(String addJobid) { this.addJobid = addJobid; }

    public Timestamp getTime() { return time; }
    public void setTime(Timestamp time) { this.time = time; }

    public String getSubmitBy() { return addName; }
    public void setSubmitBy(String addName) { this.addName = addName; }

    public boolean isProcessed() { return isProcessed; }
    public void setProcessed(boolean processed) { isProcessed = processed; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    // Helper method to convert Timestamp to a formatted String
    public String getFormattedTime() {
        if (time != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy 'at' HH:mm:ss", Locale.getDefault());
            return sdf.format(time.toDate());
        }
        return "";
    }
}