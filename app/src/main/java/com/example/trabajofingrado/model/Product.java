package com.example.trabajofingrado.model;

public class Product {
    // Fields
    private String image, name, unitType;

    public Product() {
    }

    public Product(String image, String name, String unitType) {
        this.name = name;
        this.image = image;
        this.unitType = unitType;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    @Override
    public String toString() {
        return "Product{" +
                "image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", unitType='" + unitType + '\'' +
                '}';
    }
}
