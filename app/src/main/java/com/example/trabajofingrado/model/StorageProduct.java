package com.example.trabajofingrado.model;

public class StorageProduct {
    private String description;
    private String amount;

    public StorageProduct() {
    }

    public StorageProduct(String name, String amount) {
        this.description = name;
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }
}
