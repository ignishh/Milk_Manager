package com.ignishers.milkmanager2;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ignishers.milkmanager2.data.model.UserAccount;
import com.ignishers.milkmanager2.data.repository.UserRepository;

import java.util.List;

public class SellerListActivity extends AppCompatActivity {

    private RecyclerView rvSellers;
    private ProgressBar progressBar;
    private UserRepository repository;
    private SellerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_list);

        repository = new UserRepository();
        rvSellers = findViewById(R.id.rvSellers);
        progressBar = findViewById(R.id.progressBar);
        rvSellers.setLayoutManager(new LinearLayoutManager(this));

        loadSellers();
    }

    private void loadSellers() {
        progressBar.setVisibility(View.VISIBLE);
        repository.getAllSellers(new UserRepository.SellersListCallback() {
            @Override
            public void onSuccess(List<UserAccount> sellers) {
                progressBar.setVisibility(View.GONE);
                adapter = new SellerAdapter(sellers, seller -> showToggleDialog(seller));
                rvSellers.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SellerListActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showToggleDialog(UserAccount seller) {
        boolean isActive = "ACTIVE".equalsIgnoreCase(seller.getStatus());
        String action = isActive ? "DEACTIVATE" : "ACTIVATE";
        
        new AlertDialog.Builder(this)
                .setTitle(action + " Seller?")
                .setMessage("Are you sure you want to " + action.toLowerCase() + " " + seller.getName() + "?\nThey will " + (isActive ? "lose" : "regain") + " login access.")
                .setPositiveButton("Yes", (dialog, which) -> {
                    performToggle(seller, !isActive);
                })
                .setNegativeButton("No", null)
                .show();
    }

    private void performToggle(UserAccount seller, boolean makeActive) {
        progressBar.setVisibility(View.VISIBLE);
        repository.toggleSellerStatus(seller.getUserId(), makeActive, new UserRepository.CreateSellerCallback() {
            @Override
            public void onSuccess() {
                // Refresh List
                loadSellers(); 
                Toast.makeText(SellerListActivity.this, "Status Updated", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(SellerListActivity.this, "Update Failed: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
