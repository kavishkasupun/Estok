package com.example.estok;

import android.app.Dialog;
import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;

import androidx.annotation.NonNull;

public class CustomPrograssDialog extends Dialog {

    public CustomPrograssDialog(@NonNull Context context) {
        super(context);

        WindowManager.LayoutParams parms = getWindow().getAttributes();
        parms.gravity = Gravity.CENTER_HORIZONTAL;
        getWindow().setAttributes(parms);
        setTitle(null);
        setCancelable(false);
        setOnCancelListener(null);
        View view = LayoutInflater.from(context).inflate(R.layout.loading, null);


        setContentView(view);
    }
}