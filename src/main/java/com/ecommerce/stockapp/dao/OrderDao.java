package com.ecommerce.stockapp.dao;

import com.ecommerce.stockapp.model.CartItem;
import com.ecommerce.stockapp.model.Order;
import com.ecommerce.stockapp.model.OrderStatus;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDao {
    private final Database database;

    public OrderDao(Database database) {
        this.database = database;
    }

    public int createOrder(Connection connection, int userId, BigDecimal total, OrderStatus status) throws SQLException {
        String sql = "INSERT INTO orders(user_id, total_price, status, date) VALUES (?, ?, ?, NOW())";
        try (PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setBigDecimal(2, total);
            statement.setString(3, status.name());
            statement.executeUpdate();
            ResultSet keys = statement.getGeneratedKeys();
            return keys.next() ? keys.getInt(1) : 0;
        }
    }

    public void createItem(Connection connection, int orderId, CartItem item) throws SQLException {
        String sql = "INSERT INTO order_items(order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, orderId);
            statement.setInt(2, item.getProduct().getId());
            statement.setInt(3, item.getQuantity());
            statement.setBigDecimal(4, item.getProduct().getPrice());
            statement.executeUpdate();
        }
    }

    public List<Order> findAll() {
        return find("SELECT o.*, u.name AS customer_name FROM orders o JOIN users u ON u.id = o.user_id ORDER BY o.date DESC", 0);
    }

    public List<Order> findByUser(int userId) {
        return find("SELECT o.*, u.name AS customer_name FROM orders o JOIN users u ON u.id = o.user_id WHERE o.user_id = ? ORDER BY o.date DESC", userId);
    }

    public void updateStatus(int orderId, OrderStatus status) {
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            statement.setString(1, status.name());
            statement.setInt(2, orderId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Unable to update order status", e);
        }
    }

    public Database getDatabase() {
        return database;
    }

    private List<Order> find(String sql, int userId) {
        List<Order> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (userId > 0) {
                statement.setInt(1, userId);
            }
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                orders.add(new Order(
                        rs.getInt("id"),
                        rs.getInt("user_id"),
                        rs.getString("customer_name"),
                        rs.getBigDecimal("total_price"),
                        OrderStatus.valueOf(rs.getString("status")),
                        rs.getTimestamp("date").toLocalDateTime()
                ));
            }
            return orders;
        } catch (SQLException e) {
            throw new DaoException("Unable to list orders", e);
        }
    }
}
