package com.example.trabajofingrado.model;

import androidx.annotation.NonNull;

import java.util.HashMap;

public class Storage {
    // Fields
    private String name, id;
    private HashMap<String, Boolean> shoppingLists, users;
    private HashMap<String, StorageProduct> products;

    /**
     * Default constructor
     */
    public Storage() {
    }

    /**
     * Parameterized constructor with all obligatory parameters
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
     * Parameterized constructor with all parameters, including optional ones
     *
     * @param name
     * @param id
     * @param users
     * @param products
     * @param shoppingLists
     */
    public Storage(String name, String id, HashMap<String, Boolean> users,
                   HashMap<String, StorageProduct> products, HashMap<String, Boolean> shoppingLists) {
        this.name = name;
        this.id = id;
        this.products = products;
        this.shoppingLists = shoppingLists;
        this.users = users;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public HashMap<String, StorageProduct> getProducts() {
        return products;
    }

    public HashMap<String, Boolean> getShoppingLists() {
        return shoppingLists;
    }

    public HashMap<String, Boolean> getUsers() {
        return users;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProducts(HashMap<String, StorageProduct> products) {
        this.products = products;
    }

    public void setUsers(HashMap<String, Boolean> users) {
        this.users = users;
    }

    public void setShoppingLists(HashMap<String, Boolean> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    @NonNull
    @Override
    public String toString() {
        return "Storage{" +
                "name='" + name + '\'' +
                ", id='" + id + '\'' +
                ", users=" + users +
                ", products=" + products +
                ", shoppingLists=" + shoppingLists +
                '}';
    }
}
