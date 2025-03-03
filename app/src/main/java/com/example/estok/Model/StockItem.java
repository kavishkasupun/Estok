package com.example.estok.Model;

public class StockItem {

    private String name;
    private int count;
    private String unit;
    private String documentId; // Add this field

    // Default constructor required for Firestore
    public StockItem() {
    }

    public StockItem(String name, int count, String unit) {
        this.name = name;
        this.count = count;
        this.unit = unit;
    }

    // Getters and Setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }
}