package com.example.trabajofingrado.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.applandeo.materialcalendarview.EventDay;
import com.example.trabajofingrado.R;

import java.util.Calendar;

public class RecipesDay extends EventDay implements Parcelable {
    public static final Creator<RecipesDay> CREATOR = new Creator<RecipesDay>() {
        @Override
        public RecipesDay createFromParcel(Parcel in) {
            return new RecipesDay(in);
        }

        @Override
        public RecipesDay[] newArray(int size) {
            return new RecipesDay[size];
        }
    };

    public RecipesDay(Calendar day) {
        super(day, R.drawable.steaming_pot);
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        parcel.writeSerializable(getCalendar());
    }
    protected RecipesDay(Parcel in) {
        super((Calendar) in.readSerializable(), in.readInt());
    }
}
