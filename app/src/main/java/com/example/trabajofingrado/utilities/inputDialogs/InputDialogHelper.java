package com.example.trabajofingrado.utilities.inputDialogs;

import android.content.Context;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;

public class InputDialogHelper {
    public static AlertDialog.Builder configureAddProductOrIngredientDialog(Context context,
                                                                             AlertDialog.Builder builder,
                                                                             String title){

        builder.setTitle(title);

        LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final EditText inputName = new EditText(context);
        inputName.setHint("Name");
        layout.addView(inputName);

        final EditText inputAmount = new EditText(context);
        inputAmount.setHint("Amount");
        inputAmount.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        inputAmount.setTransformationMethod(null);
        layout.addView(inputAmount);

        final EditText inputUnits = new EditText(context);
        inputUnits.setHint("Units");
        layout.addView(inputUnits);

        builder.setView(layout);

        return builder;
    }
}
