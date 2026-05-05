package com.ecommerce.stockapp.dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ActivityLogDao {
    private final Database database;

    public ActivityLogDao(Database database) {
        this.database = database;
    }

    public void log(Integer userId, String action, String details) {
        String sql = "INSERT INTO system_logs(user_id, action, details, date) VALUES (?, ?, ?, NOW())";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId == null) {
                statement.setNull(1, Types.INTEGER);
            } else {
                statement.setInt(1, userId);
            }
            statement.setString(2, action);
            statement.setString(3, details);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to write system log", e);
        }
    }

    public List<String> recent() {
        List<String> logs = new ArrayList<>();
        String sql = """
                SELECT COALESCE(u.email, 'system') AS actor, sl.action, sl.details, sl.date
                FROM system_logs sl LEFT JOIN users u ON u.id = sl.user_id
                ORDER BY sl.date DESC LIMIT 100
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()) {
            while (rs.next()) {
                logs.add(rs.getTimestamp("date").toLocalDateTime() + " | " + rs.getString("actor") + " | " + rs.getString("action") + " | " + rs.getString("details"));
            }
            return logs;
        } catch (SQLException e) {
            throw new DaoException("Unable to list system logs", e);
        }
    }
}
