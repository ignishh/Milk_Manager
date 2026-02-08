package com.ignishers.milkmanager2.utils;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import com.ignishers.milkmanager2.R;

public class DialogUtils {

    public interface OnPositiveClickListener {
        void onPositiveClick();
    }

    public static void showWarningDialog(Context context, String title, String message, String positiveAction, OnPositiveClickListener listener) {
        
        // Inflate Custom Layout
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_warning, null);
        
        TextView tvTitle = view.findViewById(R.id.dialogTitle);
        TextView tvMessage = view.findViewById(R.id.dialogMessage);
        android.widget.Button btnPositive = view.findViewById(R.id.btnPositive); // Use Button/TextView
        android.widget.Button btnNegative = view.findViewById(R.id.btnNegative);
        
        tvTitle.setText(title);
        tvMessage.setText(message);
        btnPositive.setText(positiveAction);
        
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(view)
                .setCancelable(true)
                .create();
                
        // Transparent background to let CardView handle corners
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
        
        btnNegative.setOnClickListener(v -> dialog.dismiss());
        btnPositive.setOnClickListener(v -> {
            dialog.dismiss();
            listener.onPositiveClick();
        });
        
        dialog.show();
    }
}
