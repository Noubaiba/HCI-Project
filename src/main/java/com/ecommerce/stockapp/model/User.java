package com.ecommerce.stockapp.model;

import java.time.LocalDateTime;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private Role role;
    private UserStatus status;
    private String activationToken;
    private LocalDateTime createdAt;
    private String phone;
    private String deliveryAddress;
    private String profilePicture;

    // Constructeur par défaut (souvent nécessaire pour les outils de mapping)
    public User() {}

    // Constructeur complet (Utilisé par ton UserService et tes DAO)
    public User(int id, String name, String email, String password, Role role, UserStatus status,
                String activationToken, LocalDateTime createdAt, String phone,
                String deliveryAddress, String profilePicture) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = status;
        this.activationToken = activationToken;
        this.createdAt = createdAt;
        this.phone = phone;
        this.deliveryAddress = deliveryAddress;
        this.profilePicture = profilePicture;
    }

    // --- GETTERS ET SETTERS ---

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public String getActivationToken() { return activationToken; }
    public void setActivationToken(String activationToken) { this.activationToken = activationToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getProfilePicture() { return profilePicture; }
    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}