package com.example.trabajofingrado.model;

/**
 * Father class of storage product and show product.
 */
public abstract class BaseProduct {
    // Fields
    protected String name, unitType;

    /**
     * Default constructor
     */
    public BaseProduct() {

    }

    /**
     * Parameterized constructor
     * @param name
     * @param unitType Type of units that will be used, eg. grams for rice, units for chicken,
     *                 fillets for fish or meat
     */
    public BaseProduct(String name, String unitType) {
        this.name = name;
        this.unitType = unitType;
    }

    // Getter
    public String getName() {
        return name;
    }

    public String getUnitType() {
        return unitType;
    }

    // Setter
    public void setName(String name) {
        this.name = name;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }
}
