package com.ecommerce.stockapp.model;

import java.time.LocalDateTime;

public class Notification {
    private int id;
    private int userId;
    private String message;
    private String status;
    private LocalDateTime date;

    public Notification(int id, int userId, String message, String status, LocalDateTime date) {
        this.id = id;
        this.userId = userId;
        this.message = message;
        this.status = status;
        this.date = date;
    }

    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public LocalDateTime getDate() { return date; }
}
