package com.example.trabajofingrado.model;

import java.util.HashMap;

public class Storage {
    private String name, id;
    private HashMap<String, Boolean> shoppingLists, users;
    private HashMap<String, StorageProduct> products;


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
    public Storage(String name, String id, HashMap<String, Boolean> users,
                   HashMap<String, StorageProduct> products, HashMap<String, Boolean> shoppingLists) {
        this.name = name;
        this.id = id;
        this.products = products;
        this.shoppingLists = shoppingLists;
        this.users = users;
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

    public HashMap<String, StorageProduct> getProducts() {
        return products;
    }

    public void setProducts(HashMap<String, StorageProduct> products) {
        this.products = products;
    }

    public HashMap<String, Boolean> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(HashMap<String, Boolean> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

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
