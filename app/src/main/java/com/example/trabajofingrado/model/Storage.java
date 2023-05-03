package com.example.trabajofingrado.model;

import java.util.HashMap;

public class Storage {
    private String name;
    private String id;
    private HashMap<String, Boolean> users;
    private HashMap<String, String> products;
    private HashMap<String, Boolean> shoppingLists;

    public Storage() {
    }

    /**
     * Class constructor with all obligatory parameters
     *
     * @param name
     * @param id
     * @param users
     */
    public Storage(String name, String id, HashMap<String, Boolean> users) {
        this.name = name;
        this.id = id;
        this.users = users;
    }

    /**
     * Constructor with all parameters, including optional ones
     *
     * @param name
     * @param id
     * @param users
     * @param products
     * @param shoppingLists
     */
    public Storage(String name, String id, HashMap<String, Boolean> users, HashMap<String, String> products, HashMap<String, Boolean> shoppingLists) {
        this.name = name;
        this.id = id;
        this.users = users;
        this.products = products;
        this.shoppingLists = shoppingLists;
    }

    // Getter and setter
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public HashMap<String, String> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, String> products) {
        this.products = products;
    }

    public HashMap<String, Boolean> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(HashMap<String, Boolean> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }
}
