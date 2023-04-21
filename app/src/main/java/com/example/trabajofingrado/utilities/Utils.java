package com.example.trabajofingrado.utilities;

import java.util.ArrayList;

public class Utils {
    public static final String RECIPEPATH = "recipes";
    public static final String USERPATH = "users";
    public static final String STORAGEPATH = "storages";

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
