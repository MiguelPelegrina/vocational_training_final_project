package com.example.trabajofingrado.utilities;

import java.util.ArrayList;

public class Utils {
    public static final String PRODUCT_PATH = "products";
    public static final String RECIPE_PATH = "recipes";
    public static final String STORAGE_PATH = "storages";

    public static boolean checkValidStrings(ArrayList<String> strings){
        boolean valid = true;

        for(int i = 0; i < strings.size() && valid; i++){
            if(strings.get(i).trim().length() == 0){
                valid = false;
            }
        }

        return valid;

    }
}
