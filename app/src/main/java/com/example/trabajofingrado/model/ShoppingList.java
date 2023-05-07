package com.example.trabajofingrado.model;

import java.util.HashMap;

public class ShoppingList {
    private HashMap<String, String> boughtProducts;
    private HashMap<String, String> products;
    private HashMap<String, Boolean> users;
    private String name;
    private String lastEdited;
    private String storageId;
    private String id;

    /**
     * Default constructor
     */
    public ShoppingList(){}

    /**
     * Constructor with all the obligatory parameters
     *
     * @param products
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, String> products, HashMap<String, Boolean> users, String name, String lastEdited, String storageId, String id) {
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.users = users;
    }

    /**
     * Constructor with all parameters, including optional ones
     *
     * @param boughtProducts
     * @param products
     * @param name
     * @param lastEdited
     * @param storageId
     * @param id
     */
    public ShoppingList(HashMap<String, String> boughtProducts, HashMap<String, String> products,
                        HashMap<String, Boolean> users, String name, String lastEdited, String storageId, String id) {
        this.boughtProducts = boughtProducts;
        this.id = id;
        this.lastEdited = lastEdited;
        this.name = name;
        this.products = products;
        this.storageId = storageId;
        this.users = users;
    }

    public HashMap<String, String> getBoughtProducts() {
        return boughtProducts;
    }

    public void setBoughtProducts(HashMap<String, String> boughtProducts) {
        this.boughtProducts = boughtProducts;
    }

    public HashMap<String, String> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, String> products) {
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
