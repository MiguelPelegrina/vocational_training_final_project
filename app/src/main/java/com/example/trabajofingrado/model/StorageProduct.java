package com.example.trabajofingrado.model;

import androidx.annotation.NonNull;

import java.util.Objects;

/**
 * Child class of base product, responsible of storing the amount of the product in the database
 */
public class StorageProduct extends BaseProduct{
    // Fields
    private int amount;

    /**
     * Default constructor
     */
    public StorageProduct() {
    }

    /**
     * Parameterized constructor
     * @param amount
     * @param name
     * @param unitType Type of units that will be used, eg. grams for rice, units for chicken,
     *                       fillets for fish or meat
     */
    public StorageProduct(int amount, String name, String unitType) {
        super(name, unitType);
        this.amount = amount;
    }

    // Getter
    public int getAmount() {
        return amount;
    }

    // Setter
    public void setAmount(int amount) {
        this.amount = amount;
    }

    @NonNull
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
