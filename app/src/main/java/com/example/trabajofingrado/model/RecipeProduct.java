package com.example.trabajofingrado.model;

public class RecipeProduct {
    // Fields
    private String description;

    private String image;

    private String unit_type;

    public RecipeProduct() {
    }

    public RecipeProduct(String description, String image, String unit_type) {
        this.description = description;
        this.image = image;
        this.unit_type = unit_type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUnit_type() {
        return unit_type;
    }

    public void setUnit_type(String unit_type) {
        this.unit_type = unit_type;
    }
}
