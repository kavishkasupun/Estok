package com.example.estok.Model;

import com.google.firebase.Timestamp;

public class AcceptOrderItem {

    private String itemName;
    private String optionName;
    private int quantity;
    private String unit;
    private String orderId;
    private String userNumber;
    private String jobId;
    private Timestamp formattedTime;
    private String submitBy;

    // Default constructor required for calls to DataSnapshot.getValue(AcceptOrderItem.class)
    public AcceptOrderItem() {
    }

    public AcceptOrderItem(String itemName, String optionName, int quantity, String unit, String orderId, String userNumber, String jobId, Timestamp formattedTime, String submitBy) {
        this.itemName = itemName;
        this.optionName = optionName;
        this.quantity = quantity;
        this.unit = unit;
        this.orderId = orderId;
        this.userNumber = userNumber;
        this.jobId = jobId;
        this.formattedTime = formattedTime;
        this.submitBy = submitBy;
    }

    // Getters and Setters
    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getOptionName() {
        return optionName;
    }

    public void setOptionName(String optionName) {
        this.optionName = optionName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public Timestamp getFormattedTime() {
        return formattedTime;
    }

    public void setFormattedTime(Timestamp formattedTime) {
        this.formattedTime = formattedTime;
    }

    public String getSubmitBy() {
        return submitBy;
    }

    public void setSubmitBy(String submitBy) {
        this.submitBy = submitBy;
    }
}