package com.example.trabajofingrado.model;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if(o instanceof StorageProduct){
            StorageProduct storageProduct = (StorageProduct) o;
            if(this.getDescription().equals(storageProduct.description)){
                isEqual = true;
            }
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(description, amount);
    }
}
