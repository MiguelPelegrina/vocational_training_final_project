package com.example.trabajofingrado.model;

import androidx.annotation.NonNull;

/**
 * Child class of base product, responsible of showing an image of a product to the user
 */
public class ShowProduct extends BaseProduct{
    // Fields
    private String image;

    /**
     * Default constructor
     */
    public ShowProduct() {
    }

    /**
     * Parameterized constructor
     * @param image
     * @param name
     * @param unitType Type of units that will be used, eg. grams for rice, units for chicken,
     *                       fillets for fish or meat
     */
    public ShowProduct(String image, String name, String unitType) {
        super(name, unitType);
        this.image = image;
    }

    // Getter
    public String getImage() {
        return image;
    }

    // Setter
    public void setImage(String image) {
        this.image = image;
    }

    @NonNull
    @Override
    public String toString() {
        return "Product{" +
                "image='" + image + '\'' +
                ", name='" + name + '\'' +
                ", unitType='" + unitType + '\'' +
                '}';
    }
}
