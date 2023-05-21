package com.example.trabajofingrado.model;

import java.util.Objects;

public class StorageProduct {
    private int amount;
    private String name;

    private String unitType;

    public StorageProduct() {
    }

    public StorageProduct(int amount, String name, String unitType) {
        this.amount = amount;
        this.name = name;
        this.unitType = unitType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    @Override
    public String toString() {
        return "StorageProduct{" +
                "amount=" + amount +
                ", name='" + name + '\'' +
                ", unitType='" + unitType + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        boolean isEqual = false;
        if(o instanceof StorageProduct){
            StorageProduct storageProduct = (StorageProduct) o;
            if(this.getName().equals(storageProduct.name)){
                isEqual = true;
            }
        }

        return isEqual;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, amount);
    }
}
