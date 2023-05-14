package com.example.trabajofingrado.model;

import java.util.HashMap;

public class ShoppingList {
    private HashMap<String, StorageProduct> boughtProducts;
    private HashMap<String, StorageProduct> products;
    private HashMap<String, Boolean> users;
    private String id;
    private String lastEdited;
    private String name;
    private String storageId;
    private String storageName;

    /**
     * Default constructor
     */
    public ShoppingList(){}

    /**
     * Constructor with all the obligatory parameters
     *
     * @param users
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, Boolean> users, String name, String lastEdited,
                         String id, String storageId, String storageName) {
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.storageId = storageId;
        this.storageName = storageName;
        this.users = users;
    }

    /**
     * Constructor with all the obligatory parameters
     *
     * @param products
     * @param users
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, StorageProduct> products, HashMap<String, Boolean> users,
                        String name, String lastEdited, String id, String storageId, String storageName) {
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.storageName = storageName;
        this.users = users;
    }

    /**
     * Constructor with all parameters, including optional ones
     *
     * @param boughtProducts
     * @param products
     * @param users
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, StorageProduct> boughtProducts, HashMap<String, StorageProduct> products,
                        HashMap<String, Boolean> users, String name, String lastEdited, String id, String storageId, String storageName) {
        this.boughtProducts = boughtProducts;
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.storageName = storageName;
        this.users = users;
    }

    public HashMap<String, StorageProduct> getBoughtProducts() {
        return boughtProducts;
    }

    public void setBoughtProducts(HashMap<String, StorageProduct> boughtProducts) {
        this.boughtProducts = boughtProducts;
    }

    public HashMap<String, StorageProduct> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, StorageProduct> products) {
        this.products = products;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastEdited() {
        return lastEdited;
    }

    public void setLastEdited(String lastModified) {
        this.lastEdited = lastModified;
    }

    public String getStorageId() {
        return storageId;
    }

    public void setStorageId(String storageId) {
        this.storageId = storageId;
    }

    public String getStorageName() {
        return storageName;
    }

    public void setStorageName(String storageName) {
        this.storageName = storageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashMap<String, Boolean> getUsers() {
        return users;
    }

    public void setUsers(HashMap<String, Boolean> users) {
        this.users = users;
    }


}
