package com.ignishers.milkmanager2.data.model;

import java.util.UUID;

/**
 * Customer Entity.
 * Represents the relationship between a Seller and a Buyer.
 * Maps to 'customer' table.
 */
public class CustomerEntity {
    private String customerId; // UUID
    private String sellerId;   // UUID (Owner of this record)
    private String userId;     // UUID (Link to UserAccount if they registered)
    
    // De-normalized details (Snapshot at time of creation, or sync with UserAccount)
    private String name;
    private String mobile;
    private String address;
    
    private String routeGroupId; // UUID
    private double defaultQuantity;
    private double currentDue;
    private boolean isUpdatedLocally; // For Sync Flag

    public CustomerEntity(String name, String mobile, String routeGroupId, String sellerId) {
        this.customerId = UUID.randomUUID().toString(); // Generate ID offline
        this.sellerId = sellerId;
        this.name = name;
        this.mobile = mobile;
        this.routeGroupId = routeGroupId;
        this.isUpdatedLocally = true;
    }
    
    // setters getters
    public String getCustomerId() { return customerId; }
    public void setCustomerId(String id) { this.customerId = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public double getCurrentDue() { return currentDue; }
    public void setCurrentDue(double currentDue) { this.currentDue = currentDue; }

    public String getSellerId() { return sellerId; }
    public void setSellerId(String sellerId) { this.sellerId = sellerId; }

    public boolean isUpdatedLocally() { return isUpdatedLocally; }
    public void setUpdatedLocally(boolean updatedLocally) { isUpdatedLocally = updatedLocally; }
}
