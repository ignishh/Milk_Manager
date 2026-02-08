package com.ignishers.milkmanager2.data.model;

import java.util.UUID;

/**
 * Transaction Model.
 * Maps to 'milk_transaction' table.
 */
public class TransactionEntity {
    private String transactionId; // UUID
    private String customerId;    // UUID
    private String sellerId;      // UUID
    
    private String date;          // yyyy-MM-dd
    private String session;       // DAY, NIGHT
    private double quantity;
    private double amount;
    private String timestamp;
    private String paymentMode;   // CASH, UPI, etc. (Can be null)
    
    private boolean isSynced;     // Sync Status

    public TransactionEntity(String customerId, String sellerId, String date, String session, double quantity, double amount, String paymentMode) {
        this.transactionId = UUID.randomUUID().toString();
        this.customerId = customerId;
        this.sellerId = sellerId;
        this.date = date;
        this.session = session;
        this.quantity = quantity;
        this.amount = amount;
        this.paymentMode = paymentMode;
        this.timestamp = java.time.LocalDateTime.now().toString();
        this.isSynced = false;
    }

    public String getTransactionId() { return transactionId; }
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }

    public String getCustomerId() { return customerId; }
    public void setCustomerId(String customerId) { this.customerId = customerId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public String getSession() { return session; }
    public void setSession(String session) { this.session = session; }
    
    public String getDate() { return date; }

    public String getPaymentMode() { return paymentMode; }
}
