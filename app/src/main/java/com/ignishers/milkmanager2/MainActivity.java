package com.ignishers.milkmanager2;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.ignishers.milkmanager2.viewmodel.LoginViewModel;

public class MainActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private LoginViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewModel = new androidx.lifecycle.ViewModelProvider(this).get(LoginViewModel.class);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);

        // Observe Login Result (Toast Only)
        viewModel.getLoginResult().observe(this, result -> {
            if (result.startsWith("Error") || result.startsWith("Profile Error")) {
                Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
            }
        });

        // Observe Navigation Event
        viewModel.getNavigateTo().observe(this, role -> {
            if ("ADMIN".equals(role)) {
                Intent intent = new Intent(MainActivity.this, AdminDashboardActivity.class);
                startActivity(intent);
                finish();
            } else if ("SELLER".equals(role)) {
                Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Observe Loading State
        viewModel.getIsLoading().observe(this, isLoading -> {
            btnLogin.setEnabled(!isLoading);
            if (isLoading) {
                btnLogin.setText("Logging in...");
            } else {
                btnLogin.setText("Login");
            }
        });

        btnLogin.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            viewModel.login(username, password);
        });
    }
}