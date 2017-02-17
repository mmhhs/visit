package com.little.visit.util;

import android.content.Context;
import android.widget.Toast;

public class ToastUtil {

    public static void addToast(Context context,String value){
        try {
            Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}