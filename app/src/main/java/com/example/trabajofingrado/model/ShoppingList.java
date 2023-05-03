package com.example.trabajofingrado.model;

import java.util.HashMap;

public class ShoppingList {
    private HashMap<String, String> boughtProducts;
    private HashMap<String, String> needToBuyProducts;
    private String name;
    private String lastEdited;
    private String storageID;
    private String id;

    /**
     * Default constructor
     */
    public ShoppingList(){}

    /**
     * Constructor with all the obligatory parameters
     *
     * @param needToBuyProducts
     * @param name
     * @param lastEdited
     * @param storageID
     * @param id
     */
    public ShoppingList(HashMap<String, String> needToBuyProducts, String name, String lastEdited, String storageID, String id) {
        this.needToBuyProducts = needToBuyProducts;
        this.name = name;
        this.lastEdited = lastEdited;
        this.storageID = storageID;
        this.id = id;
    }

    /**
     * Constructor with all parameters, including optional ones
     *
     * @param boughtProducts
     * @param needToBuyProducts
     * @param name
     * @param lastEdited
     * @param storageID
     * @param id
     */
    public ShoppingList(HashMap<String, String> boughtProducts, HashMap<String, String> needToBuyProducts, String name, String lastEdited, String storageID, String id) {
        this.boughtProducts = boughtProducts;
        this.needToBuyProducts = needToBuyProducts;
        this.name = name;
        this.lastEdited = lastEdited;
        this.storageID = storageID;
        this.id = id;
    }

    public HashMap<String, String> getBoughtProducts() {
        return boughtProducts;
    }

    public void setBoughtProducts(HashMap<String, String> boughtProducts) {
        this.boughtProducts = boughtProducts;
    }

    public HashMap<String, String> getNeedToBuyProducts() {
        return needToBuyProducts;
    }

    public void setNeedToBuyProducts(HashMap<String, String> needToBuyProducts) {
        this.needToBuyProducts = needToBuyProducts;
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

    public String getStorageID() {
        return storageID;
    }

    public void setStorageID(String storageID) {
        this.storageID = storageID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
