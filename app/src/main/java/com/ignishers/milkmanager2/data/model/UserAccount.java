package com.ignishers.milkmanager2.data.model;

import java.util.UUID;

/**
 * Base User Account Model.
 * Represents the fundamental identity of a person in the system (Login/Auth).
 * Maps to 'user_account' table.
 */
public class UserAccount {
    private String userId; // UUID
    private String username; // Email or Phone (Unique)
    private String passwordHash;
    private String role; // "SELLER", "CUSTOMER", "ADMIN"
    private String name;
    private String mobile;
    private String address;
    private String status; // "ACTIVE", "INACTIVE"
    private String createdAt;
    private String lastLoginAt;

    public UserAccount(String username, String passwordHash, String role, String name, String mobile, String address) {
        this.userId = UUID.randomUUID().toString(); // Generate unique ID on creation
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.name = name;
        this.mobile = mobile;
        this.address = address;
        this.status = "ACTIVE";
    }

    // Constructor for DB mapping
    public UserAccount(String userId, String username, String role, String name, String mobile) {
        this.userId = userId;
        this.username = username;
        this.role = role;
        this.name = name;
        this.mobile = mobile;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
