package com.ignishers.milkmanager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AdminDashboardActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        Button btnCreateSeller = findViewById(R.id.btnCreateSeller);
        btnCreateSeller.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, CreateSellerActivity.class);
            startActivity(intent);
        });

        Button btnViewSellers = findViewById(R.id.btnViewSellers);
        btnViewSellers.setOnClickListener(v -> {
            Intent intent = new Intent(AdminDashboardActivity.this, SellerListActivity.class);
            startActivity(intent);
        });
    }
}
