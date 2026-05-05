package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class NotificationDao {
    private final Database database;

    public NotificationDao(Database database) {
        this.database = database;
    }

    public void create(int userId, String message) {
        String sql = "INSERT INTO notifications(user_id, message, status, date) VALUES (?, ?, 'UNREAD', NOW())";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, message);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to create notification", e);
        }
    }

    public List<Notification> findByUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM notifications WHERE user_id = ? ORDER BY date DESC")) {
            statement.setInt(1, userId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                notifications.add(new Notification(rs.getInt("id"), rs.getInt("user_id"), rs.getString("message"), rs.getString("status"), rs.getTimestamp("date").toLocalDateTime()));
            }
            return notifications;
        } catch (SQLException e) {
            throw new DaoException("Unable to list notifications", e);
        }
    }
}
