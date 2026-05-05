package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.DashboardStats;

import java.math.BigDecimal;
import java.sql.*;

public class DashboardDao {
    private final Database database;

    public DashboardDao(Database database) {
        this.database = database;
    }

    public DashboardStats stats() {
        try (Connection connection = database.getConnection()) {
            int products = count(connection, "SELECT COUNT(*) FROM products");
            int users = count(connection, "SELECT COUNT(*) FROM users");
            int orders = count(connection, "SELECT COUNT(*) FROM orders");
            int lowStock = count(connection, "SELECT COUNT(*) FROM products WHERE quantity <= 5");
            BigDecimal revenue = sum(connection, "SELECT COALESCE(SUM(total_price), 0) FROM orders WHERE status <> 'CANCELLED'");
            return new DashboardStats(products, users, orders, revenue, lowStock);
        } catch (SQLException e) {
            throw new DaoException("Unable to load dashboard stats", e);
        }
    }

    private int count(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private BigDecimal sum(Connection connection, String sql) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(sql); ResultSet rs = statement.executeQuery()) {
            return rs.next() ? rs.getBigDecimal(1) : BigDecimal.ZERO;
        }
    }
}
