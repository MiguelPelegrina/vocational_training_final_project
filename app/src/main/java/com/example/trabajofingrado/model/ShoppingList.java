package com.example.trabajofingrado.model;

import androidx.annotation.NonNull;

import java.util.HashMap;

/**
 *
 */
public class ShoppingList {
    // Fields
    private HashMap<String, StorageProduct> boughtProducts, products;
    private String id, lastEdited, name, storageId, storageName;

    /**
     * Default constructor
     */
    public ShoppingList() {
    }

    /**
     * Constructor with all the obligatory parameters
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(String name, String lastEdited,
                        String id, String storageId, String storageName) {
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.storageId = storageId;
        this.storageName = storageName;
    }

    /**
     * Parameterized constructor with all obligatory parameters
     * @param products
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, StorageProduct> products,
                        String name, String lastEdited, String id, String storageId, String storageName) {
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.storageName = storageName;
    }

    /**
     * Parameterized constructor with all parameters, including optional ones
     *
     * @param boughtProducts
     * @param products
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, StorageProduct> boughtProducts, HashMap<String, StorageProduct> products,
                        String name, String lastEdited, String id, String storageId, String storageName) {
        this.boughtProducts = boughtProducts;
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.storageName = storageName;
    }

    // Getter
    public HashMap<String, StorageProduct> getBoughtProducts() {
        return boughtProducts;
    }

    public String getId() {
        return id;
    }

    public String getLastEdited() {
        return lastEdited;
    }

    public String getName() {
        return name;
    }

    public HashMap<String, StorageProduct> getProducts() {
        return products;
    }

    public String getStorageId() {
        return storageId;
    }

    public String getStorageName() {
        return storageName;
    }

    // Setter
    public void setBoughtProducts(HashMap<String, StorageProduct> boughtProducts) {
        this.boughtProducts = boughtProducts;
    }

    public void setProducts(HashMap<String, StorageProduct> products) {
        this.products = products;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastEdited(String lastModified) {
        this.lastEdited = lastModified;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public void setId(String id) {
        this.id = id;
    }

    @NonNull
    @Override
    public String toString() {
        return "ShoppingList{" +
                "boughtProducts=" + boughtProducts +
                ", products=" + products +
                ", id='" + id + '\'' +
                ", lastEdited='" + lastEdited + '\'' +
                ", name='" + name + '\'' +
                ", storageId='" + storageId + '\'' +
                ", storageName='" + storageName + '\'' +
                '}';
    }
}
