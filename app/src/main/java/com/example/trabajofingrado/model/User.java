package com.example.trabajofingrado.model;


import java.util.HashMap;

public class User {
    private String email;
    private String name;
    private String password;
    private HashMap<String, Boolean> storages;

    public User() {
    }

    public User(String email, String name, String password) {
        this.email = email;
        this.name = name;
        this.password = password;
        this.storages = null;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public HashMap<String, Boolean> getStorages() {
        return storages;
    }

    public void setStorages(HashMap<String, Boolean> storages) {
        this.storages = storages;
    }
}
