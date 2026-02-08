package com.ignishers.milkmanager2;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.ignishers.milkmanager2.data.model.UserAccount;

import java.util.List;

public class SellerAdapter extends RecyclerView.Adapter<SellerAdapter.SellerViewHolder> {

    private List<UserAccount> sellers;
    private OnSellerActionListener listener;

    public interface OnSellerActionListener {
        void onToggleStatus(UserAccount seller);
    }

    public SellerAdapter(List<UserAccount> sellers, OnSellerActionListener listener) {
        this.sellers = sellers;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SellerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_seller, parent, false);
        return new SellerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SellerViewHolder holder, int position) {
        UserAccount seller = sellers.get(position);
        holder.bind(seller);
    }

    @Override
    public int getItemCount() {
        return sellers.size();
    }

    class SellerViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvEmail, tvMobile, tvStatus;

        public SellerViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvSellerName);
            tvEmail = itemView.findViewById(R.id.tvSellerEmail);
            tvMobile = itemView.findViewById(R.id.tvSellerMobile);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnLongClickListener(v -> {
                listener.onToggleStatus(sellers.get(getAdapterPosition()));
                return true;
            });
        }

        void bind(UserAccount seller) {
            tvName.setText(seller.getName());
            tvEmail.setText(seller.getUsername());
            tvMobile.setText(seller.getMobile());
            tvStatus.setText(seller.getStatus());

            if ("ACTIVE".equalsIgnoreCase(seller.getStatus())) {
                tvStatus.setBackgroundColor(Color.parseColor("#4CAF50")); // Green
            } else {
                tvStatus.setBackgroundColor(Color.parseColor("#F44336")); // Red
            }
        }
    }
}
