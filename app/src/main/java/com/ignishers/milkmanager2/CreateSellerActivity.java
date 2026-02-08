package com.ignishers.milkmanager2;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.ignishers.milkmanager2.data.model.UserAccount;
import com.ignishers.milkmanager2.data.repository.UserRepository;

public class CreateSellerActivity extends AppCompatActivity {

    private TextInputEditText etName, etEmail, etPassword, etMobile, etAddress, etMilkPrice, etEffectiveDate;
    private Button btnSubmit;
    private ProgressBar progressBar;
    private UserRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_seller);

        repository = new UserRepository();

        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etMobile = findViewById(R.id.etMobile);
        etAddress = findViewById(R.id.etAddress);
        etMilkPrice = findViewById(R.id.etMilkPrice);
        etEffectiveDate = findViewById(R.id.etEffectiveDate);
        
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);

        // Default Date: Today
        String today = java.time.LocalDate.now().toString(); // yyyy-MM-dd
        etEffectiveDate.setText(today);

        // Date Picker Handler
        etEffectiveDate.setOnClickListener(v -> {
            java.util.Calendar c = java.util.Calendar.getInstance();
            new android.app.DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                // Ensure format yyyy-MM-dd (Month is 0-indexed)
                String date = String.format(java.util.Locale.US, "%d-%02d-%02d", year, month + 1, dayOfMonth);
                etEffectiveDate.setText(date);
            }, c.get(java.util.Calendar.YEAR), c.get(java.util.Calendar.MONTH), c.get(java.util.Calendar.DAY_OF_MONTH)).show();
        });

        btnSubmit.setOnClickListener(v -> createSeller());
    }

    private void createSeller() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();
        String price = etMilkPrice.getText().toString().trim();
        String date = etEffectiveDate.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || price.isEmpty() || date.isEmpty()) {
            Toast.makeText(this, "Please fill required fields", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSubmit.setEnabled(false);
        
        repository.createSeller(email, password, name, mobile, price, date, new UserRepository.CreateSellerCallback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(CreateSellerActivity.this, "Seller Created Successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                btnSubmit.setEnabled(true);
                Toast.makeText(CreateSellerActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }
}
