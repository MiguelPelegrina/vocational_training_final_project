package com.example.trabajofingrado.model;

import java.util.HashMap;

public class Storage {
    private String name;
    private String id;
    private HashMap<String, Boolean> users;
    private HashMap<String, String> products;

    public Storage() {
    }

    public Storage(String name, String id, HashMap<String, Boolean> users) {
        this.name = name;
        this.id = id;
        this.users = users;
    }

    public Storage(String name, String id, HashMap<String, Boolean> users, HashMap<String, String> products) {
        this.name = name;
        this.id = id;
        this.users = users;
        this.products = products;
    }

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
}
